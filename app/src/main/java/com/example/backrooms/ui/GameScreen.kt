package com.example.backrooms.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.backrooms.engine.RaycastRenderer
import com.example.backrooms.model.GameStatus
import com.example.backrooms.model.GameUiState
import com.example.backrooms.ui.components.*
import com.example.backrooms.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateMainMenu: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRadarScanner by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. Raycasted 3D Viewport
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            RaycastRenderer.renderScene(
                drawScope = this,
                map = uiState.map,
                posX = uiState.playerX,
                posY = uiState.playerY,
                dirX = uiState.dirX,
                dirY = uiState.dirY,
                planeX = uiState.planeX,
                planeY = uiState.planeY,
                flashlightOn = uiState.flashlightOn,
                flashlightBattery = uiState.flashlightBattery,
                flickerIntensity = uiState.flickerIntensity,
                vhsDistortion = uiState.audioGlitchIntensity,
                wallMarkers = uiState.wallMarkers
            )
        }

        // 2. Camera Touch Pan Surface (Right half of the screen)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.55f)
                .align(Alignment.CenterEnd)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // Sensitivity: horizontal drag rotates camera
                        val sensitivity = 0.0055f
                        viewModel.rotateCamera(dragAmount.x * sensitivity)
                    }
                }
        )

        // 3. VHS Camcorder HUD Header
        VhsHeader(
            uiState = uiState,
            onToggleRadar = { showRadarScanner = !showRadarScanner },
            onPause = { viewModel.pauseGame() }
        )

        // 4. Sanity & Stamina Gauges (Top Left)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 65.dp)
        ) {
            // Sanity Bar
            GaugeBar(
                label = "CORDURA",
                value = uiState.sanity,
                color = Color(0xFF4DD0E1),
                warning = uiState.sanity < 0.35f
            )
            Spacer(modifier = Modifier.height(6.dp))
            // Stamina Bar
            GaugeBar(
                label = "ESTAMINA",
                value = uiState.stamina,
                color = Color(0xFF39FF14),
                warning = uiState.stamina < 0.25f
            )
        }

        // 5. Context Interactive Action Prompt (Center Screen)
        if (uiState.nearbyItem != null) {
            Button(
                onClick = { viewModel.pickupNearbyItem() },
                modifier = Modifier
                    .align(Alignment.Center)
                    .testTag("pickup_item_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5C34D))
            ) {
                Icon(Icons.Default.TouchApp, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "RECOGER: ${uiState.nearbyItem?.type?.displayName?.uppercase()}",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else if (uiState.isNearExit) {
            Button(
                onClick = { viewModel.interactExit() },
                modifier = Modifier
                    .align(Alignment.Center)
                    .testTag("exit_level_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "¡CRUZAR: ${uiState.levelDef.exitLabel.uppercase()}!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // 6. Subtitles & Sound Transcriptions (Bottom Center)
        AnimatedVisibility(
            visible = uiState.activeSubtitle != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xCC000000), RoundedCornerShape(4.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = uiState.activeSubtitle.orEmpty(),
                    color = Color(0xFFFFF59D),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // 7. Virtual Analog Joystick (Bottom Left)
        VirtualJoystick(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 20.dp),
            onMove = { x, y -> viewModel.updateMovementInput(x, y) }
        )

        // 8. Quick Survival Action Buttons (Bottom Right)
        QuickActionButtons(
            uiState = uiState,
            viewModel = viewModel,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 18.dp)
        )

        // 9. Modals / Dialogs
        if (showRadarScanner) {
            Dialog(onDismissRequest = { showRadarScanner = false }) {
                AsyncRadarScanner(
                    uiState = uiState,
                    onDismiss = { showRadarScanner = false }
                )
            }
        }

        uiState.activeTapeNote?.let { note ->
            FoundNoteDialog(note = note, onDismiss = { viewModel.closeTapeNote() })
        }

        when (uiState.gameStatus) {
            GameStatus.GAME_OVER_CAUGHT -> {
                GameOverDialog(
                    uiState = uiState,
                    onRestart = { viewModel.restartCurrentLevel() },
                    onMainMenu = onNavigateMainMenu
                )
            }
            GameStatus.LEVEL_CLEARED -> {
                LevelClearedDialog(
                    uiState = uiState,
                    onNextLevel = { viewModel.nextLevel() },
                    onMainMenu = onNavigateMainMenu
                )
            }
            GameStatus.PAUSED -> {
                PauseMenuDialog(
                    onResume = { viewModel.resumeGame() },
                    onRestart = { viewModel.restartCurrentLevel() },
                    onMainMenu = onNavigateMainMenu
                )
            }
            GameStatus.VICTORY_ESCAPED -> {
                VictoryEscapedDialog(onMainMenu = onNavigateMainMenu)
            }
            GameStatus.PLAYING -> {}
        }
    }
}

@Composable
private fun VhsHeader(
    uiState: GameUiState,
    onToggleRadar: () -> Unit,
    onPause: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: REC indicator & timestamp
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF2A2A))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "REC ● SP-VHS",
                    color = Color(0xFFFF2A2A),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            val m = uiState.tapeTimeSeconds / 60
            val s = uiState.tapeTimeSeconds % 60
            Text(
                text = "OCT.12 1996  %02d:%02d:00".format(m, s),
                color = Color(0xFFFFF59D),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
        }

        // Center: Level Name
        Text(
            text = uiState.levelDef.name,
            color = Color(0xFFE5C34D),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        // Right: Radar Button, Flashlight status & Pause
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onToggleRadar,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0x661E824C), RoundedCornerShape(4.dp))
                    .testTag("toggle_radar_button")
            ) {
                Icon(
                    Icons.Default.Radar,
                    contentDescription = "Radar Async",
                    tint = Color(0xFF39FF14),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onPause,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0x66333333), RoundedCornerShape(4.dp))
                    .testTag("pause_button")
            ) {
                Icon(
                    Icons.Default.Pause,
                    contentDescription = "Pausa",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun GaugeBar(
    label: String,
    value: Float,
    color: Color,
    warning: Boolean
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                color = if (warning) Color(0xFFFF5252) else color,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${(value * 100).toInt()}%",
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .width(90.dp)
                .height(6.dp)
                .background(Color(0x66000000))
                .border(1.dp, Color(0x66FFFFFF))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(value.coerceIn(0f, 1f))
                    .background(if (warning) Color(0xFFFF5252) else color)
            )
        }
    }
}

@Composable
private fun QuickActionButtons(
    uiState: GameUiState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // 1. Chalk Marker
        ActionButtonWithBadge(
            icon = Icons.Default.BorderColor,
            label = "Tiza",
            count = uiState.inventoryChalk,
            active = false,
            color = Color(0xFFB0BEC5),
            testTag = "btn_chalk",
            onClick = { viewModel.placeChalkMarker() }
        )

        // 2. Battery
        ActionButtonWithBadge(
            icon = Icons.Default.BatteryChargingFull,
            label = "Pila",
            count = uiState.inventoryBatteries,
            active = false,
            color = Color(0xFFFFD54F),
            testTag = "btn_battery",
            onClick = { viewModel.useBattery() }
        )

        // 3. Almond Water
        ActionButtonWithBadge(
            icon = Icons.Default.LocalDrink,
            label = "Almendra",
            count = uiState.inventoryAlmondWater,
            active = false,
            color = Color(0xFF4DD0E1),
            testTag = "btn_almond_water",
            onClick = { viewModel.drinkAlmondWater() }
        )

        // 4. Sprint (Hold or Toggle)
        IconButton(
            onClick = { viewModel.setSprinting(!uiState.isSprinting) },
            modifier = Modifier
                .size(52.dp)
                .background(
                    if (uiState.isSprinting) Color(0xFF43A047) else Color(0x66000000),
                    CircleShape
                )
                .border(2.dp, if (uiState.isSprinting) Color(0xFF39FF14) else Color(0x66FFFFFF), CircleShape)
                .testTag("btn_sprint")
        ) {
            Icon(
                Icons.Default.DirectionsRun,
                contentDescription = "Correr",
                tint = if (uiState.isSprinting) Color.White else Color(0xFFB0BEC5),
                modifier = Modifier.size(26.dp)
            )
        }

        // 5. Flashlight Toggle
        IconButton(
            onClick = { viewModel.toggleFlashlight() },
            modifier = Modifier
                .size(56.dp)
                .background(
                    if (uiState.flashlightOn) Color(0xFFE5C34D) else Color(0x66000000),
                    CircleShape
                )
                .border(2.dp, if (uiState.flashlightOn) Color(0xFFFFF59D) else Color(0x66FFFFFF), CircleShape)
                .testTag("btn_flashlight")
        ) {
            Icon(
                Icons.Default.FlashlightOn,
                contentDescription = "Linterna",
                tint = if (uiState.flashlightOn) Color.Black else Color.LightGray,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun ActionButtonWithBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    count: Int,
    active: Boolean,
    color: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Box(contentAlignment = Alignment.TopEnd) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .background(if (active) color else Color(0x66000000), CircleShape)
                .border(1.dp, color.copy(alpha = 0.7f), CircleShape)
                .testTag(testTag)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) Color.Black else color,
                modifier = Modifier.size(22.dp)
            )
        }
        if (count > 0) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(Color(0xFFE53935), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$count",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PauseMenuDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1A14))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GRABACIÓN PAUSADA",
                    color = Color(0xFFE5C34D),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4B53D))
                ) {
                    Text("CONTINUAR EXPEDICIÓN", color = Color.Black, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
                    Text("REINICIAR ESTE NIVEL", color = Color.LightGray, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onMainMenu, modifier = Modifier.fillMaxWidth()) {
                    Text("SALIR AL MENÚ PRINCIPAL", color = Color.LightGray, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun VictoryEscapedDialog(onMainMenu: () -> Unit) {
    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1A12))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¡HAS ESCAPADO DE LAS TRASTIENDAS!",
                    color = Color(0xFF39FF14),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Lograste sobrevivir a todos los niveles liminales. El metraje de tu cámara ahora es prueba irrefutable de la anomalía.",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onMainMenu,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("VOLVER AL ARCHIVO", color = Color.White, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
