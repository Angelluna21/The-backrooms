package com.example.backrooms.model

import androidx.compose.ui.graphics.Color

enum class ItemType(
    val displayName: String,
    val description: String,
    val iconName: String,
    val primaryColor: Color
) {
    ALMOND_WATER(
        displayName = "Agua de Almendras",
        description = "Líquido dulce vital que previene la locura liminal. Restaura un +35% de Cordura y recupera energía.",
        iconName = "water_drop",
        primaryColor = Color(0xFF4DD0E1)
    ),
    BATTERY(
        displayName = "Batería de Litio",
        description = "Pilas de repuesto para la linterna y la videocámara VHS (+40% carga).",
        iconName = "battery_charging_full",
        primaryColor = Color(0xFFFFD54F)
    ),
    CHALK(
        displayName = "Tiza de Marcado",
        description = "Tiza blanca para dibujar flechas y señales en las paredes infinitas (+3 marcas).",
        iconName = "edit",
        primaryColor = Color(0xFFEEEEEE)
    ),
    SURVIVOR_TAPE(
        displayName = "Cinta de A-Sync / Diario",
        description = "Grabación en casete dejada por investigadores de The Async Foundation.",
        iconName = "videocam",
        primaryColor = Color(0xFFFF7043)
    )
}

data class ItemInstance(
    val id: Int,
    val x: Float,
    val y: Float,
    val type: ItemType,
    var isCollected: Boolean = false,
    val loreTitle: String = "",
    val loreContent: String = ""
)

data class WallMarker(
    val x: Float,
    val y: Float,
    val text: String = "→",
    val color: Color = Color(0xFFEEEEEE)
)
