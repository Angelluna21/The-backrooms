package com.example.backrooms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.backrooms.generator.ProceduralLevelGenerator
import com.example.backrooms.model.*
import com.example.backrooms.util.HapticHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

class GameViewModel : ViewModel() {

    private var hapticHelper: HapticHelper? = null

    private val _uiState = MutableStateFlow(createInitialState(LevelId.LEVEL_0, GameDifficulty.SURVIVOR))
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var gameLoopJob: Job? = null

    // Input vectors from on-screen controls
    private var inputMoveX = 0f
    private var inputMoveY = 0f

    // Internal timing
    private var lastTickTime = System.currentTimeMillis()
    private var elapsedSecondsCounter = 0f
    private var subtitleTimer = 0f
    private var heartbeatTimer = 0f

    init {
        startGameLoop()
    }

    fun setHapticHelper(helper: HapticHelper) {
        this.hapticHelper = helper
    }

    fun startLevel(levelId: LevelId, difficulty: GameDifficulty = _uiState.value.difficulty) {
        val levelDef = LevelDefinition.getById(levelId)
        val generatedMap = ProceduralLevelGenerator.generate(levelDef)

        // Starting orientation facing south (0, 1) or north (0, -1)
        val startDirX = 0f
        val startDirY = 1f
        val startPlaneX = -0.66f
        val startPlaneY = 0f

        _uiState.update {
            it.copy(
                levelDef = levelDef,
                map = generatedMap,
                playerX = generatedMap.spawnX,
                playerY = generatedMap.spawnY,
                dirX = startDirX,
                dirY = startDirY,
                planeX = startPlaneX,
                planeY = startPlaneY,
                health = 1.0f,
                sanity = 1.0f,
                stamina = 1.0f,
                isSprinting = false,
                flashlightOn = true,
                flashlightBattery = 0.95f,
                inventoryAlmondWater = 1,
                inventoryBatteries = 1,
                inventoryChalk = 5,
                wallMarkers = emptyList(),
                collectedTapes = emptyList(),
                tapeTimeSeconds = 0,
                gameStatus = GameStatus.PLAYING,
                activeSubtitle = "*Encendiendo grabadora VHS... ${levelDef.name}*",
                audioGlitchIntensity = 0f,
                flickerIntensity = 1.0f,
                nearbyItem = null,
                isNearExit = false,
                activeTapeNote = null,
                difficulty = difficulty
            )
        }
        subtitleTimer = 3.5f
        startGameLoop()
    }

    fun restartCurrentLevel() {
        startLevel(_uiState.value.levelDef.id, _uiState.value.difficulty)
    }

    fun nextLevel() {
        val nextDef = LevelDefinition.getNext(_uiState.value.levelDef.id)
        if (nextDef != null) {
            startLevel(nextDef.id, _uiState.value.difficulty)
        } else {
            // Completed all levels
            _uiState.update { it.copy(gameStatus = GameStatus.VICTORY_ESCAPED) }
        }
    }

    fun pauseGame() {
        _uiState.update { it.copy(gameStatus = GameStatus.PAUSED) }
    }

    fun resumeGame() {
        _uiState.update { it.copy(gameStatus = GameStatus.PLAYING) }
    }

    fun updateMovementInput(x: Float, y: Float) {
        inputMoveX = x.coerceIn(-1f, 1f)
        inputMoveY = y.coerceIn(-1f, 1f)
    }

    fun rotateCamera(deltaAngleRadians: Float) {
        val s = _uiState.value
        if (s.gameStatus != GameStatus.PLAYING) return

        val oldDirX = s.dirX
        val newDirX = s.dirX * cos(deltaAngleRadians) - s.dirY * sin(deltaAngleRadians)
        val newDirY = oldDirX * sin(deltaAngleRadians) + s.dirY * cos(deltaAngleRadians)

        val oldPlaneX = s.planeX
        val newPlaneX = s.planeX * cos(deltaAngleRadians) - s.planeY * sin(deltaAngleRadians)
        val newPlaneY = oldPlaneX * sin(deltaAngleRadians) + s.planeY * cos(deltaAngleRadians)

        _uiState.update {
            it.copy(
                dirX = newDirX,
                dirY = newDirY,
                planeX = newPlaneX,
                planeY = newPlaneY
            )
        }
    }

    fun toggleFlashlight() {
        _uiState.update {
            val next = !it.flashlightOn
            if (next) hapticHelper?.pulseLight()
            it.copy(flashlightOn = next)
        }
    }

    fun setSprinting(sprinting: Boolean) {
        _uiState.update {
            it.copy(isSprinting = sprinting && it.stamina > 0.15f)
        }
    }

    fun drinkAlmondWater() {
        val s = _uiState.value
        if (s.inventoryAlmondWater > 0 && s.sanity < 0.98f) {
            hapticHelper?.pulseLight()
            _uiState.update {
                it.copy(
                    inventoryAlmondWater = it.inventoryAlmondWater - 1,
                    sanity = (it.sanity + 0.35f).coerceAtMost(1.0f),
                    stamina = (it.stamina + 0.50f).coerceAtMost(1.0f),
                    activeSubtitle = "*Bebiendo Agua de Almendras... La mente se aclara.*"
                )
            }
            subtitleTimer = 3.0f
        }
    }

    fun useBattery() {
        val s = _uiState.value
        if (s.inventoryBatteries > 0 && s.flashlightBattery < 0.95f) {
            hapticHelper?.pulseLight()
            _uiState.update {
                it.copy(
                    inventoryBatteries = it.inventoryBatteries - 1,
                    flashlightBattery = (it.flashlightBattery + 0.45f).coerceAtMost(1.0f),
                    activeSubtitle = "*Batería reemplazada (+45% carga)*"
                )
            }
            subtitleTimer = 2.5f
        }
    }

    fun placeChalkMarker() {
        val s = _uiState.value
        if (s.inventoryChalk > 0) {
            hapticHelper?.pulseLight()
            val markerX = s.playerX + s.dirX * 0.8f
            val markerY = s.playerY + s.dirY * 0.8f
            val newMarker = WallMarker(markerX, markerY, "→")
            _uiState.update {
                it.copy(
                    inventoryChalk = it.inventoryChalk - 1,
                    wallMarkers = it.wallMarkers + newMarker,
                    activeSubtitle = "*Marca de tiza colocada en la pared*"
                )
            }
            subtitleTimer = 2.0f
        }
    }

    fun pickupNearbyItem() {
        val s = _uiState.value
        val item = s.nearbyItem ?: return

        hapticHelper?.pulseLight()
        item.isCollected = true

        var newAlmond = s.inventoryAlmondWater
        var newBat = s.inventoryBatteries
        var newChalk = s.inventoryChalk
        var newTapes = s.collectedTapes
        var noteToShow: ItemInstance? = null
        var subText = ""

        when (item.type) {
            ItemType.ALMOND_WATER -> {
                newAlmond++
                subText = "*Agua de Almendras recogida (+1)*"
            }
            ItemType.BATTERY -> {
                newBat++
                subText = "*Batería de repuesto recogida (+1)*"
            }
            ItemType.CHALK -> {
                newChalk += 3
                subText = "*Tiza para marcar recogida (+3 usos)*"
            }
            ItemType.SURVIVOR_TAPE -> {
                newTapes = newTapes + item
                noteToShow = item
                subText = "*Cinta encontrada: ${item.loreTitle}*"
            }
        }

        _uiState.update {
            it.copy(
                inventoryAlmondWater = newAlmond,
                inventoryBatteries = newBat,
                inventoryChalk = newChalk,
                collectedTapes = newTapes,
                nearbyItem = null,
                activeTapeNote = noteToShow,
                activeSubtitle = subText
            )
        }
        subtitleTimer = 3.0f
    }

    fun closeTapeNote() {
        _uiState.update { it.copy(activeTapeNote = null) }
    }

    fun interactExit() {
        val s = _uiState.value
        if (s.isNearExit) {
            hapticHelper?.pulseWarning()
            _uiState.update {
                it.copy(
                    gameStatus = GameStatus.LEVEL_CLEARED,
                    activeSubtitle = "*¡HAS ESCAPADO DEL ${it.levelDef.name.uppercase()}!*"
                )
            }
        }
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        lastTickTime = System.currentTimeMillis()
        gameLoopJob = viewModelScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                val delta = ((now - lastTickTime) / 1000f).coerceIn(0.001f, 0.1f)
                lastTickTime = now

                if (_uiState.value.gameStatus == GameStatus.PLAYING) {
                    gameTick(delta)
                }

                delay(16) // ~60 FPS
            }
        }
    }

    private fun gameTick(dt: Float) {
        val s = _uiState.value
        val map = s.map

        // 1. Timecode & Subtitles
        elapsedSecondsCounter += dt
        val newTapeTime = if (elapsedSecondsCounter >= 1f) {
            elapsedSecondsCounter -= 1f
            s.tapeTimeSeconds + 1
        } else {
            s.tapeTimeSeconds
        }

        if (subtitleTimer > 0f) {
            subtitleTimer -= dt
        }
        val currentSubtitle = if (subtitleTimer > 0f) s.activeSubtitle else null

        // 2. Fluorescent Flicker
        val rand = Random.Default.nextFloat()
        val flicker = if (rand < s.levelDef.flickerFrequency) {
            (0.3f + Random.Default.nextFloat() * 0.4f)
        } else {
            1.0f
        }

        // 3. Movement with Sliding Wall Collisions
        val speedMul = if (s.isSprinting && s.stamina > 0.1f) 1.8f else 1.0f
        val baseSpeed = 2.4f * dt * speedMul
        var newX = s.playerX
        var newY = s.playerY

        val moveForward = inputMoveY
        val moveStrafe = inputMoveX

        val deltaX = (s.dirX * moveForward + s.planeX * moveStrafe) * baseSpeed
        val deltaY = (s.dirY * moveForward + s.planeY * moveStrafe) * baseSpeed

        val radius = 0.25f
        // Check X movement
        val targetX = newX + deltaX
        if (!map.isWall((targetX + if (deltaX > 0) radius else -radius).toInt(), newY.toInt())) {
            newX = targetX
        }
        // Check Y movement
        val targetY = newY + deltaY
        if (!map.isWall(newX.toInt(), (targetY + if (deltaY > 0) radius else -radius).toInt())) {
            newY = targetY
        }

        // 4. Stamina & Flashlight Consumption
        var newStamina = s.stamina
        if (s.isSprinting && (abs(moveForward) > 0.1f || abs(moveStrafe) > 0.1f)) {
            newStamina = (newStamina - 0.25f * dt).coerceAtLeast(0.0f)
        } else {
            newStamina = (newStamina + 0.18f * dt).coerceAtMost(1.0f)
        }

        var newBattery = s.flashlightBattery
        if (s.flashlightOn) {
            newBattery = (newBattery - 0.012f * dt).coerceAtLeast(0.0f)
        }

        // 5. Sanity Decay (drains in darkness, or near entities)
        var newSanity = s.sanity
        val darkPenalty = if (!s.flashlightOn || s.flashlightBattery <= 0.05f) 0.045f else 0.010f
        newSanity = (newSanity - darkPenalty * dt * s.difficulty.sanityDrainMul).coerceIn(0f, 1f)

        // 6. Hazard checks (Steam vents, blackout zones)
        for (hazard in map.hazards) {
            val dist = hypot(newX - hazard.x, newY - hazard.y)
            if (dist < 0.6f && hazard.type == 3) {
                // Steam burn
                newSanity = (newSanity - 0.08f * dt).coerceAtLeast(0f)
            }
        }

        // 7. Check Proximity to Items
        var nearbyItem: ItemInstance? = null
        for (item in map.items) {
            if (!item.isCollected) {
                val dist = hypot(newX - item.x, newY - item.y)
                if (dist < 1.1f) {
                    nearbyItem = item
                    break
                }
            }
        }

        // 8. Check Proximity to Exit
        val distToExit = hypot(newX - (map.exitX + 0.5f), newY - (map.exitY + 0.5f))
        val nearExit = distToExit < 1.35f

        // 9. Entity AI & Tracking
        var maxGlitch = 0f
        var caughtByEntity = false
        val entitySpeedMul = s.difficulty.entitySpeedMul

        for (entity in map.entities) {
            val dist = hypot(newX - entity.x, newY - entity.y)

            // Line of sight check
            val hasLineOfSight = checkLineOfSight(map, entity.x, entity.y, newX, newY)

            // State machine
            if (hasLineOfSight && dist < entity.type.detectionRadius) {
                entity.state = EntityState.CHASING
                entity.alertTimer = 6.0f
            } else if (entity.alertTimer > 0f) {
                entity.alertTimer -= dt
                if (entity.alertTimer <= 0f) {
                    entity.state = EntityState.PATROLLING
                }
            }

            // Move Entity
            val speed = (if (entity.state == EntityState.CHASING) entity.type.chaseSpeed else entity.type.baseSpeed) * entitySpeedMul

            if (entity.state == EntityState.CHASING) {
                val dx = (newX - entity.x) / dist
                val dy = (newY - entity.y) / dist
                val nextEx = entity.x + dx * speed
                val nextEy = entity.y + dy * speed
                if (!map.isWall(nextEx.toInt(), entity.y.toInt())) entity.x = nextEx
                if (!map.isWall(entity.x.toInt(), nextEy.toInt())) entity.y = nextEy
            } else {
                // Patrol roaming
                entity.changeDirCooldown -= dt
                if (entity.changeDirCooldown <= 0f) {
                    entity.changeDirCooldown = (2f..5f).random()
                    val angle = (0f..(2 * PI).toFloat()).random()
                    entity.dirX = cos(angle)
                    entity.dirY = sin(angle)
                }
                val nextEx = entity.x + entity.dirX * speed
                val nextEy = entity.y + entity.dirY * speed
                if (!map.isWall(nextEx.toInt(), entity.y.toInt())) {
                    entity.x = nextEx
                } else {
                    entity.dirX = -entity.dirX
                }
                if (!map.isWall(entity.x.toInt(), nextEy.toInt())) {
                    entity.y = nextEy
                } else {
                    entity.dirY = -entity.dirY
                }
            }

            // VHS static distortion and sound alert based on entity distance
            if (dist < 8.0f) {
                val glitch = (1.0f - dist / 8.0f).coerceIn(0f, 1f)
                if (glitch > maxGlitch) maxGlitch = glitch
            }

            // Entity caught player
            if (dist < 0.65f) {
                caughtByEntity = true
            }
        }

        // Heartbeat haptic when sanity is low or entity is near
        heartbeatTimer += dt
        if (maxGlitch > 0.4f || newSanity < 0.35f) {
            val freq = if (maxGlitch > 0.7f) 0.5f else 1.0f
            if (heartbeatTimer >= freq) {
                heartbeatTimer = 0f
                hapticHelper?.pulseHeartbeat()
            }
        }

        // Check if player died or was caught
        if (caughtByEntity) {
            hapticHelper?.pulseJumpscare()
            _uiState.update {
                it.copy(
                    gameStatus = GameStatus.GAME_OVER_CAUGHT,
                    activeSubtitle = "*¡SEÑAL PERDIDA - CONTACTO FATAL CON LA ENTIDAD!*"
                )
            }
            return
        }

        if (newSanity <= 0.01f) {
            hapticHelper?.pulseJumpscare()
            _uiState.update {
                it.copy(
                    gameStatus = GameStatus.GAME_OVER_CAUGHT,
                    activeSubtitle = "*PERDISTE EL JUICIO EN LAS TRAS TIENDAS...*"
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                playerX = newX,
                playerY = newY,
                health = 1.0f,
                sanity = newSanity,
                stamina = newStamina,
                flashlightBattery = newBattery,
                tapeTimeSeconds = newTapeTime,
                activeSubtitle = currentSubtitle,
                audioGlitchIntensity = maxGlitch,
                flickerIntensity = flicker,
                nearbyItem = nearbyItem,
                isNearExit = nearExit
            )
        }
    }

    private fun checkLineOfSight(map: GeneratedMap, x1: Float, y1: Float, x2: Float, y2: Float): Boolean {
        val dist = hypot(x2 - x1, y2 - y1)
        val steps = (dist * 3).toInt().coerceAtLeast(1)
        val dx = (x2 - x1) / steps
        val dy = (y2 - y1) / steps

        var cx = x1
        var cy = y1
        repeat(steps) {
            cx += dx
            cy += dy
            if (map.isWall(cx.toInt(), cy.toInt())) return false
        }
        return true
    }

    private fun createInitialState(levelId: LevelId, difficulty: GameDifficulty): GameUiState {
        val levelDef = LevelDefinition.getById(levelId)
        val map = ProceduralLevelGenerator.generate(levelDef)
        return GameUiState(
            levelDef = levelDef,
            map = map,
            playerX = map.spawnX,
            playerY = map.spawnY,
            dirX = 0f,
            dirY = 1f,
            planeX = -0.66f,
            planeY = 0f,
            difficulty = difficulty
        )
    }

    private fun ClosedFloatingPointRange<Float>.random(): Float {
        return start + Random.Default.nextFloat() * (endInclusive - start)
    }
}
