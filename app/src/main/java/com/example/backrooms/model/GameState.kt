package com.example.backrooms.model

enum class GameStatus {
    PLAYING,
    PAUSED,
    LEVEL_CLEARED,
    GAME_OVER_CAUGHT,
    VICTORY_ESCAPED
}

enum class GameDifficulty(val displayName: String, val description: String, val entitySpeedMul: Float, val sanityDrainMul: Float) {
    EXPLORER(
        displayName = "Explorador Liminal",
        description = "Entidades más lentas, la cordura baja despacio. Para quienes quieren explorar y ambientarse.",
        entitySpeedMul = 0.75f,
        sanityDrainMul = 0.6f
    ),
    SURVIVOR(
        displayName = "Superviviente VHS",
        description = "Experiencia clásica de los vídeos de metraje encontrado. Tensión equilibrada.",
        entitySpeedMul = 1.0f,
        sanityDrainMul = 1.0f
    ),
    NIGHTMARE(
        displayName = "Pesadilla Async",
        description = "Las entidades son agresivas e implacables. La oscuridad consume tu mente rápidamente.",
        entitySpeedMul = 1.35f,
        sanityDrainMul = 1.6f
    )
}

data class GameUiState(
    val levelDef: LevelDefinition,
    val map: GeneratedMap,
    val playerX: Float,
    val playerY: Float,
    val dirX: Float,
    val dirY: Float,
    val planeX: Float,
    val planeY: Float,
    val health: Float = 1.0f,
    val sanity: Float = 1.0f,
    val stamina: Float = 1.0f,
    val isSprinting: Boolean = false,
    val flashlightOn: Boolean = true,
    val flashlightBattery: Float = 0.95f,
    val inventoryAlmondWater: Int = 1,
    val inventoryBatteries: Int = 1,
    val inventoryChalk: Int = 5,
    val wallMarkers: List<WallMarker> = emptyList(),
    val collectedTapes: List<ItemInstance> = emptyList(),
    val tapeTimeSeconds: Int = 0,
    val gameStatus: GameStatus = GameStatus.PLAYING,
    val activeSubtitle: String? = null,
    val audioGlitchIntensity: Float = 0f,
    val flickerIntensity: Float = 1.0f,
    val nearbyItem: ItemInstance? = null,
    val isNearExit: Boolean = false,
    val activeTapeNote: ItemInstance? = null,
    val difficulty: GameDifficulty = GameDifficulty.SURVIVOR
)
