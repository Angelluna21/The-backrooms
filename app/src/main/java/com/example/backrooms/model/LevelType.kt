package com.example.backrooms.model

import androidx.compose.ui.graphics.Color

enum class LevelId {
    LEVEL_0,
    LEVEL_1,
    LEVEL_2,
    LEVEL_RUN
}

data class LevelDefinition(
    val id: LevelId,
    val name: String,
    val subtitle: String,
    val description: String,
    val wallColorPrimary: Color,
    val wallColorDark: Color,
    val floorColor: Color,
    val ceilingColor: Color,
    val ambientLight: Float,
    val flickerFrequency: Float,
    val primaryEntity: EntityType,
    val entityCount: Int,
    val almondWaterCount: Int,
    val batteryCount: Int,
    val tapeCount: Int,
    val exitLabel: String,
    val exitHint: String,
    val hasSteamHazards: Boolean = false,
    val hasBlackoutHazards: Boolean = false,
    val isSprintRun: Boolean = false,
    val mapWidth: Int = 25,
    val mapHeight: Int = 25
) {
    companion object {
        val ALL_LEVELS = listOf(
            LevelDefinition(
                id = LevelId.LEVEL_0,
                name = "Nivel 0: El Vestíbulo",
                subtitle = "The Lobby - Espacio Liminal",
                description = "Habitaciones infinitas con papel tapiz amarillo húmedo, alfombra mojada y zumbido constante de luces fluorescentes. Encuentra el fallo dimensional (No-Clip) en la pared para descender.",
                wallColorPrimary = Color(0xFFDEC355),
                wallColorDark = Color(0xFFB59A30),
                floorColor = Color(0xFF635327),
                ceilingColor = Color(0xFFE4DCBA),
                ambientLight = 0.85f,
                flickerFrequency = 0.08f,
                primaryEntity = EntityType.BACTERIA,
                entityCount = 1,
                almondWaterCount = 4,
                batteryCount = 4,
                tapeCount = 2,
                exitLabel = "Pared No-Clip",
                exitHint = "Busca una pared con distorsión dimensional que parpadea y vibra.",
                mapWidth = 23,
                mapHeight = 23
            ),
            LevelDefinition(
                id = LevelId.LEVEL_1,
                name = "Nivel 1: Zona Habitable",
                subtitle = "Habitable Zone - Estructura Industrial",
                description = "Un almacén lúgubre de hormigón con charcos oscuros y cajas de suministros. Cuidado con las zonas de apagón y El Sonriente que se oculta en la negrura.",
                wallColorPrimary = Color(0xFF5E626B),
                wallColorDark = Color(0xFF42454B),
                floorColor = Color(0xFF282A2E),
                ceilingColor = Color(0xFF3B3E44),
                ambientLight = 0.55f,
                flickerFrequency = 0.18f,
                primaryEntity = EntityType.SMILER,
                entityCount = 2,
                almondWaterCount = 5,
                batteryCount = 5,
                tapeCount = 2,
                exitLabel = "Ascensor de Emergencia",
                exitHint = "Encuentra las puertas de ascensor metálico en el extremo del complejo.",
                hasBlackoutHazards = true,
                mapWidth = 27,
                mapHeight = 27
            ),
            LevelDefinition(
                id = LevelId.LEVEL_2,
                name = "Nivel 2: Sueños de Tuberías",
                subtitle = "Pipe Dreams - Túneles Claustrofóbicos",
                description = "Túneles de mantenimiento asfixiantes con tuberías calientes de vapor. Los Sabuesos rondan por los cruces estrechos. Evita las fugas de vapor ardiente.",
                wallColorPrimary = Color(0xFF7E4A35),
                wallColorDark = Color(0xFF5A3121),
                floorColor = Color(0xFF33231C),
                ceilingColor = Color(0xFF2B1D17),
                ambientLight = 0.45f,
                flickerFrequency = 0.12f,
                primaryEntity = EntityType.HOUND,
                entityCount = 2,
                almondWaterCount = 4,
                batteryCount = 4,
                tapeCount = 2,
                exitLabel = "Puerta Salida de Emergencia",
                exitHint = "Una compuerta de acero pesada con señal fluorescente de EXIT.",
                hasSteamHazards = true,
                mapWidth = 27,
                mapHeight = 27
            ),
            LevelDefinition(
                id = LevelId.LEVEL_RUN,
                name = "Nivel !: ¡Corre Por Tu Vida!",
                subtitle = "Run For Your Life! - Pasillo Hospitalario Rojo",
                description = "Un pasillo interminable iluminado por luces estroboscópicas rojas y sirenas ensordecedoras. ¡No mires atrás! Corre a toda velocidad esquivando escombros hasta llegar a las puertas de salida.",
                wallColorPrimary = Color(0xFFC62828),
                wallColorDark = Color(0xFF8E0000),
                floorColor = Color(0xFF380808),
                ceilingColor = Color(0xFF500B0B),
                ambientLight = 0.70f,
                flickerFrequency = 0.45f,
                primaryEntity = EntityType.HORDE,
                entityCount = 3,
                almondWaterCount = 6,
                batteryCount = 3,
                tapeCount = 1,
                exitLabel = "Puertas Dobles de Escape",
                exitHint = "¡Las puertas dobles de salida están al final del pasillo! ¡NO TE DETENGAS!",
                isSprintRun = true,
                mapWidth = 13,
                mapHeight = 35
            )
        )

        fun getById(id: LevelId): LevelDefinition {
            return ALL_LEVELS.find { it.id == id } ?: ALL_LEVELS.first()
        }

        fun getNext(currentId: LevelId): LevelDefinition? {
            val idx = ALL_LEVELS.indexOfFirst { it.id == currentId }
            return if (idx in 0 until ALL_LEVELS.lastIndex) ALL_LEVELS[idx + 1] else null
        }
    }
}
