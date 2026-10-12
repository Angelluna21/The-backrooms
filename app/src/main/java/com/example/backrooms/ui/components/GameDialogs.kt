package com.example.backrooms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.backrooms.model.GameUiState
import com.example.backrooms.model.ItemInstance

@Composable
fun FoundNoteDialog(
    note: ItemInstance,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("found_note_dialog"),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1A14))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "● TRANSCRIPCIÓN RECUPERADA",
                        color = Color(0xFFFF5252),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = note.loreTitle,
                    color = Color(0xFFFFD54F),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF12100C))
                        .border(1.dp, Color(0xFF3E3628), RoundedCornerShape(4.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = note.loreContent,
                        color = Color(0xFFEFEBE9),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("close_note_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4B53D))
                ) {
                    Text(
                        text = "GUARDAR EN DIARIO Y CONTINUAR",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun GameOverDialog(
    uiState: GameUiState,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("game_over_dialog"),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A0A0A))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SEÑAL PERDIDA",
                    color = Color(0xFFFF1744),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    letterSpacing = 3.sp
                )

                Text(
                    text = "METRAJE TERMINADO // CASSETTE DAÑADO",
                    color = Color(0xFFE57373),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (uiState.sanity <= 0.05f) {
                        "Tu cordura se extinguió en la penumbra. Te convertiste en otra sombra atrapada en las trastiendas."
                    } else {
                        "La entidad te alcanzó en los pasillos liminales. Nadie encontró tu rastro."
                    },
                    color = Color(0xFFEEEEEE),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Stats Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF280E0E))
                        .padding(12.dp)
                ) {
                    val m = uiState.tapeTimeSeconds / 60
                    val s = uiState.tapeTimeSeconds % 60
                    Text(
                        text = "TIEMPO SOBREVIVIDO: %02d:%02d".format(m, s),
                        color = Color(0xFFFFCDD2),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "NIVEL: ${uiState.levelDef.name}",
                        color = Color(0xFFFFCDD2),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "CINTAS RECUPERADAS: ${uiState.collectedTapes.size}",
                        color = Color(0xFFFFCDD2),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("restart_level_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "REBOBINAR Y REINTENTAR",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onMainMenu,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("main_menu_button")
                ) {
                    Text(
                        text = "VOLVER AL MENÚ PRINCIPAL",
                        color = Color.LightGray,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun LevelClearedDialog(
    uiState: GameUiState,
    onNextLevel: () -> Unit,
    onMainMenu: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("level_cleared_dialog"),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1A12))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NO-CLIP EXITOSO",
                    color = Color(0xFF39FF14),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "BRECHA DIMENSIONAL ATRAVESADA",
                    color = Color(0xFF81C784),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Has escapado con éxito de ${uiState.levelDef.name}. Tu cámara sigue grabando mientras caes a un estrato más profundo.",
                    color = Color(0xFFE8F5E9),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF07140B))
                        .padding(12.dp)
                ) {
                    val m = uiState.tapeTimeSeconds / 60
                    val s = uiState.tapeTimeSeconds % 60
                    Text(
                        text = "TIEMPO REGISTRADO: %02d:%02d".format(m, s),
                        color = Color(0xFFA5D6A7),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "CORDURA RESIDUAL: ${(uiState.sanity * 100).toInt()}%",
                        color = Color(0xFFA5D6A7),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "MARCAS DE TIZA EMPLEADAS: ${uiState.wallMarkers.size}",
                        color = Color(0xFFA5D6A7),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onNextLevel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("next_level_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text(
                        text = "DESCENDER AL SIGUIENTE NIVEL",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onMainMenu,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "MENÚ PRINCIPAL",
                        color = Color.LightGray,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
