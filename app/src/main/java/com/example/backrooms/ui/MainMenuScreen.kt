package com.example.backrooms.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.backrooms.model.GameDifficulty
import com.example.backrooms.model.LevelDefinition
import com.example.backrooms.model.LevelId

@Composable
fun MainMenuScreen(
    onStartGame: (levelId: LevelId, difficulty: GameDifficulty) -> Unit,
    onOpenTerminal: () -> Unit
) {
    var selectedLevelId by remember { mutableStateOf(LevelId.LEVEL_0) }
    var selectedDifficulty by remember { mutableStateOf(GameDifficulty.SURVIVOR) }
    var showHowToPlay by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0A06))
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Title Header (Found Footage Kane Pixels aesthetic)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFFF2A2A))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "METRAJE ENCONTRADO",
                color = Color(0xFFFF2A2A),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "THE BACKROOMS",
            color = Color(0xFFE5C34D),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            letterSpacing = 3.sp
        )

        Text(
            text = "ARCHIVOS DE INVESTIGACIÓN THE ASYNC FOUNDATION",
            color = Color(0xFFBCAAA4),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Select VHS Cassette Level
        Text(
            text = "SELECCIONAR CINTA / NIVEL PROCEDURAL",
            color = Color(0xFFFFF59D),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(LevelDefinition.ALL_LEVELS) { level ->
                CassetteCard(
                    level = level,
                    isSelected = selectedLevelId == level.id,
                    onClick = { selectedLevelId = level.id }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Level Detail Card
        val currentLevelDef = LevelDefinition.getById(selectedLevelId)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF4E4324), RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF19150E))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = currentLevelDef.name,
                    color = Color(0xFFE5C34D),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = currentLevelDef.subtitle,
                    color = Color(0xFFA1887F),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = currentLevelDef.description,
                    color = Color(0xFFD7CCC8),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ENTIDAD PRINCIPAL: ${currentLevelDef.primaryEntity.displayName}",
                    color = Color(0xFFFF7043),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = "SALIDA: ${currentLevelDef.exitLabel} (${currentLevelDef.exitHint})",
                    color = Color(0xFF81C784),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Difficulty Selector
        Text(
            text = "DIFICULTAD DE EXPEDICIÓN",
            color = Color(0xFFFFF59D),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GameDifficulty.entries.forEach { diff ->
                val isSelected = selectedDifficulty == diff
                OutlinedButton(
                    onClick = { selectedDifficulty = diff },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) Color(0xFFE5C34D) else Color.Transparent
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (isSelected) Color(0xFFE5C34D) else Color(0xFF5D4037)
                        )
                    )
                ) {
                    Text(
                        text = diff.displayName.split(" ").first(),
                        color = if (isSelected) Color.Black else Color.LightGray,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // Primary Action: Start Game
        Button(
            onClick = { onStartGame(selectedLevelId, selectedDifficulty) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("start_game_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5C34D)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "REPRODUCIR CINTA // INICIAR JUEGO",
                color = Color.Black,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Secondary: Terminal Database
        OutlinedButton(
            onClick = onOpenTerminal,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("open_terminal_button"),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Computer, contentDescription = null, tint = Color(0xFF39FF14))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "BASE DE DATOS A-SYNC (LORE Y ENTIDADES)",
                color = Color(0xFF39FF14),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tertiary: How to play
        TextButton(
            onClick = { showHowToPlay = true },
            modifier = Modifier.testTag("how_to_play_button")
        ) {
            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Color(0xFFB0BEC5))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "¿Cómo sobrevivir? Guía de Controles",
                color = Color(0xFFB0BEC5),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (showHowToPlay) {
        Dialog(onDismissRequest = { showHowToPlay = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(0.95f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1A14))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "MANUAL DE SUPERVIVENCIA LIMINAL",
                        color = Color(0xFFE5C34D),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GuideRow(icon = "🕹️", title = "Joystick Izquierdo", desc = "Muévete adelante, atrás y hacia los lados.")
                    GuideRow(icon = "👆", title = "Deslizar Derecha", desc = "Toca y arrastra el lado derecho de la pantalla para girar la vista.")
                    GuideRow(icon = "🏃", title = "Botón Correr (Sprint)", desc = "Aumenta la velocidad para huir pero consume tu barra verde de Estamina.")
                    GuideRow(icon = "🔦", title = "Linterna", desc = "Enciende/apaga la luz. Te permite ver y previene perder la cordura, pero gasta batería.")
                    GuideRow(icon = "💧", title = "Agua de Almendras", desc = "Bébela para recuperar la cordura (barra azul) antes de volverte loco.")
                    GuideRow(icon = "✏️", title = "Tiza", desc = "Marca flechas en las paredes para no dar vueltas en círculos en el laberinto.")
                    GuideRow(icon = "🚪", title = "Encontrar la Salida", desc = "En el Nivel 0 busca una pared con glitch violeta ('No-Clip') para descender.")

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showHowToPlay = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4B53D))
                    ) {
                        Text("ENTENDIDO", color = Color.Black, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
private fun CassetteCard(
    level: LevelDefinition,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFFE5C34D) else Color(0xFF3E3628),
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF262014) else Color(0xFF15130D)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // VHS tape icon look
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .background(Color(0xFF0A0906), RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFF2B2519), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VHS-TAPE",
                        color = Color(0xFFFF5252),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "● REC",
                        color = Color(0xFFFF5252),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = level.name,
                color = if (isSelected) Color(0xFFE5C34D) else Color.White,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1
            )
            Text(
                text = level.subtitle,
                color = Color.Gray,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun GuideRow(icon: String, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                color = Color(0xFFFFF59D),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Text(
                text = desc,
                color = Color(0xFFE0E0E0),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
        }
    }
}
