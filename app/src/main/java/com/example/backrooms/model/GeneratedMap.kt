package com.example.backrooms.model

data class HazardInstance(
    val x: Float,
    val y: Float,
    val type: Int, // 3 = STEAM, 4 = BLACKOUT, 5 = OBSTACLE
    var activeTimer: Float = 0f
)

data class GeneratedMap(
    val levelDef: LevelDefinition,
    val width: Int,
    val height: Int,
    val grid: Array<IntArray>,
    val spawnX: Float,
    val spawnY: Float,
    val exitX: Int,
    val exitY: Int,
    val items: MutableList<ItemInstance>,
    val entities: MutableList<EntityInstance>,
    val hazards: MutableList<HazardInstance>,
    val seed: Long
) {
    fun isWall(x: Int, y: Int): Boolean {
        if (x !in 0 until width || y !in 0 until height) return true
        val cell = grid[y][x]
        return cell == 1 || cell == 2 || cell == 6
    }

    fun isExit(x: Int, y: Int): Boolean {
        if (x !in 0 until width || y !in 0 until height) return false
        return grid[y][x] == 2
    }

    fun getCell(x: Int, y: Int): Int {
        if (x !in 0 until width || y !in 0 until height) return 1
        return grid[y][x]
    }
}
