package com.example.backrooms.model

import androidx.compose.ui.graphics.Color

enum class EntityType(
    val displayName: String,
    val loreTitle: String,
    val description: String,
    val baseSpeed: Float,
    val chaseSpeed: Float,
    val detectionRadius: Float,
    val primaryColor: Color,
    val eyeColor: Color,
    val alertSoundText: String
) {
    BACTERIA(
        displayName = "La Bacteria",
        loreTitle = "Entidad 0 - Hilachas Vivientes",
        description = "Masa distorsionada de alambres y filamentos biológicos que imita siluetas humanas. Emite sonidos guturales y gemidos huecos al acechar.",
        baseSpeed = 0.022f,
        chaseSpeed = 0.048f,
        detectionRadius = 5.5f,
        primaryColor = Color(0xFF1E1C18),
        eyeColor = Color(0xFFFF5252),
        alertSoundText = "*Crujidos metálicos y respiración hueca...*"
    ),
    SMILER(
        displayName = "El Sonriente",
        loreTitle = "Entidad 3 - Rostro en la Oscuridad",
        description = "Una enorme sonrisa incandescente y ojos desorbitados que flotan en la oscuridad absoluta. Se siente atraído por la luz intensa de la linterna.",
        baseSpeed = 0.018f,
        chaseSpeed = 0.052f,
        detectionRadius = 6.5f,
        primaryColor = Color(0xFF0A0A0A),
        eyeColor = Color(0xFFFFFFFF),
        alertSoundText = "*Un silbido agudo y una risa distante...*"
    ),
    HOUND(
        displayName = "El Sabueso",
        loreTitle = "Entidad 8 - Fiera Humanoide",
        description = "Criatura cuadrúpeda violenta con cabello desgreñado. Rápida en pasillos rectos pero se desorienta si mantienes el contacto visual sin retroceder abruptamente.",
        baseSpeed = 0.026f,
        chaseSpeed = 0.058f,
        detectionRadius = 5.0f,
        primaryColor = Color(0xFF3E2723),
        eyeColor = Color(0xFFFFAB00),
        alertSoundText = "*Gruñido sordo y pisadas frenéticas sobre metal...*"
    ),
    HORDE(
        displayName = "La Horda Roja",
        loreTitle = "Entidad ! - Enjambre de Pesadilla",
        description = "Enjambre de sombras y entidades que persiguen a cualquiera que ingrese al Nivel !. Imposible de combatir; la única opción es correr.",
        baseSpeed = 0.045f,
        chaseSpeed = 0.065f,
        detectionRadius = 15.0f,
        primaryColor = Color(0xFFB71C1C),
        eyeColor = Color(0xFFFF1744),
        alertSoundText = "*¡SIRENAS DE ALARMA Y PASOS MASIVOS ACERCÁNDOSE!*"
    )
}

enum class EntityState {
    PATROLLING,
    SEARCHING,
    ALERTED,
    CHASING
}

data class EntityInstance(
    val id: Int,
    var x: Float,
    var y: Float,
    val type: EntityType,
    var state: EntityState = EntityState.PATROLLING,
    var dirX: Float = 1f,
    var dirY: Float = 0f,
    var currentSpeed: Float = type.baseSpeed,
    var alertTimer: Float = 0f,
    var changeDirCooldown: Float = 0f
)
