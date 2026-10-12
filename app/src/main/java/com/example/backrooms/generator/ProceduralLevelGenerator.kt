package com.example.backrooms.generator

import com.example.backrooms.model.*
import java.util.LinkedList
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object ProceduralLevelGenerator {

    private val LORE_NOTES_LEVEL_0 = listOf(
        Pair("Bitácora Async #01", "Si escuchas algo deambulando cerca, seguro que ya te ha escuchado a ti. Quédate inmóvil o corre doblando esquinas."),
        Pair("Manuscrito en la pared", "El zumbido no cesa. 60 hercios constantes. Dicen que si no-clipeas en una pared sombría puedes caer a otro lugar."),
        Pair("Nota de Explorador", "El agua de almendras salvó mi mente. Sin ella, el papel tapiz empieza a susurrarte.")
    )

    private val LORE_NOTES_LEVEL_1 = listOf(
        Pair("Informe de Reconocimiento B", "El Nivel 1 parece un almacén infinito. Las luces se apagan periódicamente. Si ves una sonrisa en la sombra, ¡apaga tu linterna!"),
        Pair("Cinta Dañada Async #14", "Descubrimos un hueco de ascensor hacia el este. Necesitamos encontrar las baterías para reactivar el panel.")
    )

    private val LORE_NOTES_LEVEL_2 = listOf(
        Pair("Diario Calcinado", "El calor es insoportable. Las tuberías crujen y escupen vapor a presión. Los sabuesos cazan en las bifurcaciones."),
        Pair("Mensaje de Emergencia", "Sigue las señales rojas de salida. Conducen al Nivel !... Dios nos ayude si llegamos allí.")
    )

    private val LORE_NOTES_LEVEL_RUN = listOf(
        Pair("Última Transmisión", "¡NO TE DETENGAS! ¡NO MIRES ATRÁS! ¡CORRE HACIA LAS PUERTAS AZULES O ROJAS AL FINAL!")
    )

    fun generate(levelDef: LevelDefinition, seed: Long = System.currentTimeMillis()): GeneratedMap {
        val random = Random(seed)
        val w = levelDef.mapWidth
        val h = levelDef.mapHeight
        val grid = Array(h) { IntArray(w) { 1 } } // 1 = WALL

        if (levelDef.isSprintRun) {
            generateRunCorridor(grid, w, h, random)
        } else {
            generateLiminalMaze(grid, w, h, levelDef, random)
        }

        // Ensure logical connectivity using BFS Flood-Fill
        val reachable = ensureLogicalConnectivity(grid, w, h, random)

        // Find candidate spawn (far from edges, inside reachable floor)
        val (spawnX, spawnY) = if (levelDef.isSprintRun) {
            Pair(w / 2f + 0.5f, 2.5f)
        } else {
            val candidate = reachable.firstOrNull { it.first in 2..5 && it.second in 2..5 }
                ?: reachable.random(random)
            Pair(candidate.first + 0.5f, candidate.second + 0.5f)
        }

        // Choose exit point at the furthest connected floor tile from spawn
        val (exitX, exitY) = if (levelDef.isSprintRun) {
            Pair(w / 2, h - 2)
        } else {
            var furthest = reachable.first()
            var maxDist = 0
            for (p in reachable) {
                val dist = abs(p.first - spawnX.toInt()) + abs(p.second - spawnY.toInt())
                if (dist > maxDist) {
                    maxDist = dist
                    furthest = p
                }
            }
            furthest
        }
        grid[exitY][exitX] = 2 // 2 = EXIT

        // Spawn items across reachable rooms
        val items = mutableListOf<ItemInstance>()
        val freeTiles = reachable.filter { 
            (it.first != spawnX.toInt() || it.second != spawnY.toInt()) &&
            (it.first != exitX || it.second != exitY)
        }.toMutableList()
        freeTiles.shuffle(random)

        var itemId = 1

        // Spawn Almond Water
        val almondCount = min(levelDef.almondWaterCount, freeTiles.size)
        repeat(almondCount) {
            if (freeTiles.isNotEmpty()) {
                val tile = freeTiles.removeAt(0)
                items.add(ItemInstance(itemId++, tile.first + 0.5f, tile.second + 0.5f, ItemType.ALMOND_WATER))
            }
        }

        // Spawn Batteries
        val batCount = min(levelDef.batteryCount, freeTiles.size)
        repeat(batCount) {
            if (freeTiles.isNotEmpty()) {
                val tile = freeTiles.removeAt(0)
                items.add(ItemInstance(itemId++, tile.first + 0.5f, tile.second + 0.5f, ItemType.BATTERY))
            }
        }

        // Spawn Chalk markers
        repeat(2) {
            if (freeTiles.isNotEmpty()) {
                val tile = freeTiles.removeAt(0)
                items.add(ItemInstance(itemId++, tile.first + 0.5f, tile.second + 0.5f, ItemType.CHALK))
            }
        }

        // Spawn Survivor Tape Lore Notes
        val lorePool = when (levelDef.id) {
            LevelId.LEVEL_0 -> LORE_NOTES_LEVEL_0
            LevelId.LEVEL_1 -> LORE_NOTES_LEVEL_1
            LevelId.LEVEL_2 -> LORE_NOTES_LEVEL_2
            LevelId.LEVEL_RUN -> LORE_NOTES_LEVEL_RUN
        }
        val tapeCount = min(levelDef.tapeCount, lorePool.size)
        repeat(tapeCount) { idx ->
            if (freeTiles.isNotEmpty()) {
                val tile = freeTiles.removeAt(0)
                val note = lorePool[idx % lorePool.size]
                items.add(
                    ItemInstance(
                        id = itemId++,
                        x = tile.first + 0.5f,
                        y = tile.second + 0.5f,
                        type = ItemType.SURVIVOR_TAPE,
                        loreTitle = note.first,
                        loreContent = note.second
                    )
                )
            }
        }

        // Spawn Hazards
        val hazards = mutableListOf<HazardInstance>()
        if (levelDef.hasSteamHazards) {
            repeat(4) {
                if (freeTiles.isNotEmpty()) {
                    val tile = freeTiles.removeAt(0)
                    grid[tile.second][tile.first] = 3 // STEAM
                    hazards.add(HazardInstance(tile.first + 0.5f, tile.second + 0.5f, 3))
                }
            }
        }

        if (levelDef.hasBlackoutHazards) {
            repeat(3) {
                if (freeTiles.isNotEmpty()) {
                    val tile = freeTiles.removeAt(0)
                    grid[tile.second][tile.first] = 4 // BLACKOUT
                    hazards.add(HazardInstance(tile.first + 0.5f, tile.second + 0.5f, 4))
                }
            }
        }

        // Spawn Entities far enough from player spawn
        val entities = mutableListOf<EntityInstance>()
        val farTiles = freeTiles.filter {
            val dist = abs(it.first - spawnX) + abs(it.second - spawnY)
            dist > if (levelDef.isSprintRun) 1.5f else 7.0f
        }.toMutableList()

        if (levelDef.isSprintRun) {
            // In Level !, spawn the horde immediately behind spawn (chasing player)
            entities.add(
                EntityInstance(
                    id = 1,
                    x = spawnX,
                    y = max(1.0f, spawnY - 1.8f),
                    type = EntityType.HORDE,
                    state = EntityState.CHASING,
                    dirX = 0f,
                    dirY = 1f
                )
            )
        } else {
            repeat(levelDef.entityCount) { idx ->
                if (farTiles.isNotEmpty()) {
                    val tile = farTiles.removeAt(0)
                    entities.add(
                        EntityInstance(
                            id = idx + 1,
                            x = tile.first + 0.5f,
                            y = tile.second + 0.5f,
                            type = levelDef.primaryEntity,
                            state = EntityState.PATROLLING
                        )
                    )
                }
            }
        }

        return GeneratedMap(
            levelDef = levelDef,
            width = w,
            height = h,
            grid = grid,
            spawnX = spawnX,
            spawnY = spawnY,
            exitX = exitX,
            exitY = exitY,
            items = items,
            entities = entities,
            hazards = hazards,
            seed = seed
        )
    }

    private fun generateLiminalMaze(grid: Array<IntArray>, w: Int, h: Int, levelDef: LevelDefinition, random: Random) {
        // Room placement: carve 4 to 8 distinctive liminal rooms
        val roomCount = if (levelDef.id == LevelId.LEVEL_0) 7 else 5
        val rooms = mutableListOf<Room>()

        repeat(roomCount) {
            val rw = random.nextInt(3, 6)
            val rh = random.nextInt(3, 6)
            val rx = random.nextInt(1, max(2, w - rw - 1))
            val ry = random.nextInt(1, max(2, h - rh - 1))
            val newRoom = Room(rx, ry, rw, rh)

            var overlaps = false
            for (r in rooms) {
                if (newRoom.intersects(r)) {
                    overlaps = true
                    break
                }
            }

            if (!overlaps) {
                rooms.add(newRoom)
                for (y in ry until ry + rh) {
                    for (x in rx until rx + rw) {
                        grid[y][x] = 0 // Floor
                    }
                }
                // Optional central pillar in larger rooms (Kane Pixels aesthetic)
                if (rw >= 4 && rh >= 4 && random.nextBoolean()) {
                    val px = rx + rw / 2
                    val py = ry + rh / 2
                    grid[py][px] = 6 // Pillar
                }
            }
        }

        // Connect rooms with corridors
        for (i in 0 until rooms.size - 1) {
            val r1 = rooms[i]
            val r2 = rooms[i + 1]
            carveCorridor(grid, r1.centerX, r1.centerY, r2.centerX, r2.centerY, random)
        }

        // Extra Backrooms hallway branches and loops to create liminal labyrinth feel
        val extraBranches = if (levelDef.id == LevelId.LEVEL_2) 16 else 10
        repeat(extraBranches) {
            val x1 = random.nextInt(2, w - 2)
            val y1 = random.nextInt(2, h - 2)
            val len = random.nextInt(3, 7)
            val horizontal = random.nextBoolean()
            if (horizontal) {
                val endX = min(w - 2, x1 + len)
                for (x in x1..endX) grid[y1][x] = 0
            } else {
                val endY = min(h - 2, y1 + len)
                for (y in y1..endY) grid[y][x1] = 0
            }
        }
    }

    private fun generateRunCorridor(grid: Array<IntArray>, w: Int, h: Int, random: Random) {
        val midX = w / 2
        // Carve wide straight hospital corridor from top to bottom
        for (y in 1 until h - 1) {
            for (x in midX - 2..midX + 2) {
                grid[y][x] = 0
            }
        }

        // Place random obstacle debris (stretchers, overturned boxes)
        for (y in 4 until h - 4 step 3) {
            val obsX = random.nextInt(midX - 1, midX + 2)
            grid[y][obsX] = 1 // Obstacle wall
        }
    }

    private fun carveCorridor(grid: Array<IntArray>, x1: Int, y1: Int, x2: Int, y2: Int, random: Random) {
        var cx = x1
        var cy = y1
        if (random.nextBoolean()) {
            // Horizontal then vertical
            while (cx != x2) {
                grid[cy][cx] = 0
                cx += if (x2 > cx) 1 else -1
            }
            while (cy != y2) {
                grid[cy][cx] = 0
                cy += if (y2 > cy) 1 else -1
            }
        } else {
            // Vertical then horizontal
            while (cy != y2) {
                grid[cy][cx] = 0
                cy += if (y2 > cy) 1 else -1
            }
            while (cx != x2) {
                grid[cy][cx] = 0
                cx += if (x2 > cx) 1 else -1
            }
        }
    }

    private fun ensureLogicalConnectivity(grid: Array<IntArray>, w: Int, h: Int, random: Random): List<Pair<Int, Int>> {
        // Collect all floor tiles (grid[y][x] == 0)
        val allFloorTiles = mutableListOf<Pair<Int, Int>>()
        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                if (grid[y][x] == 0) {
                    allFloorTiles.add(Pair(x, y))
                }
            }
        }

        if (allFloorTiles.isEmpty()) {
            // Emergency fallback: carve small central room
            for (y in h / 2 - 2..h / 2 + 2) {
                for (x in w / 2 - 2..w / 2 + 2) {
                    grid[y][x] = 0
                    allFloorTiles.add(Pair(x, y))
                }
            }
        }

        // BFS to find connected components
        val visited = Array(h) { BooleanArray(w) }
        val components = mutableListOf<List<Pair<Int, Int>>>()

        for (tile in allFloorTiles) {
            if (!visited[tile.second][tile.first]) {
                val comp = mutableListOf<Pair<Int, Int>>()
                val queue = LinkedList<Pair<Int, Int>>()
                queue.add(tile)
                visited[tile.second][tile.first] = true

                while (queue.isNotEmpty()) {
                    val curr = queue.poll()
                    comp.add(curr)
                    val neighbors = listOf(
                        Pair(curr.first + 1, curr.second),
                        Pair(curr.first - 1, curr.second),
                        Pair(curr.first, curr.second + 1),
                        Pair(curr.first, curr.second - 1)
                    )
                    for (n in neighbors) {
                        if (n.first in 1 until w - 1 && n.second in 1 until h - 1) {
                            if (grid[n.second][n.first] == 0 && !visited[n.second][n.first]) {
                                visited[n.second][n.first] = true
                                queue.add(n)
                            }
                        }
                    }
                }
                components.add(comp)
            }
        }

        // If multiple components, connect them all into one unified graph
        if (components.size > 1) {
            val mainComp = components.maxByOrNull { it.size } ?: components.first()
            for (comp in components) {
                if (comp != mainComp) {
                    val p1 = comp.random(random)
                    val p2 = mainComp.random(random)
                    carveCorridor(grid, p1.first, p1.second, p2.first, p2.second, random)
                }
            }
        }

        // Final list of guaranteed connected floor tiles
        val finalConnected = mutableListOf<Pair<Int, Int>>()
        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                if (grid[y][x] == 0) {
                    finalConnected.add(Pair(x, y))
                }
            }
        }
        return finalConnected
    }

    private data class Room(val x: Int, val y: Int, val w: Int, val h: Int) {
        val centerX: Int get() = x + w / 2
        val centerY: Int get() = y + h / 2

        fun intersects(other: Room): Boolean {
            return x <= other.x + other.w && x + w >= other.x &&
                    y <= other.y + other.h && y + h >= other.y
        }
    }
}
