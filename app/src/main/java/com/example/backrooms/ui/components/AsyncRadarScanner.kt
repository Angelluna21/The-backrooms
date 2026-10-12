package com.example.backrooms.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backrooms.model.GameUiState

@Composable
fun AsyncRadarScanner(
    uiState: GameUiState,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .fillMaxHeight(0.85f)
            .testTag("radar_scanner_dialog"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1A12))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ASYNC M.I.R.A. RADAR V2.4",
                        color = Color(0xFF39FF14),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "FRECUENCIA 60Hz // MAPEO LIMINAL",
                        color = Color(0xFF1E824C),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar Escáner",
                        tint = Color(0xFF39FF14)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Canvas Grid
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFF1E824C), RoundedCornerShape(8.dp))
                    .background(Color(0xFF071209))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val map = uiState.map
                    val mw = map.width
                    val mh = map.height
                    val cellW = this.size.width / mw
                    val cellH = this.size.height / mh

                    // Draw floor/walls
                    for (y in 0 until mh) {
                        for (x in 0 until mw) {
                            val cell = map.grid[y][x]
                            val topLeft = Offset(x * cellW, y * cellH)
                            val cellSize = Size(cellW, cellH)

                            when (cell) {
                                1, 6 -> { // Wall or pillar
                                    drawRect(Color(0xFF1B4323), topLeft, cellSize)
                                }
                                2 -> { // Exit
                                    drawRect(Color(0xFFE040FB), topLeft, cellSize)
                                }
                                else -> { // Floor
                                    drawRect(Color(0xFF081C0E), topLeft, cellSize)
                                }
                            }
                        }
                    }

                    // Draw items as small yellow/blue blips
                    for (item in map.items) {
                        if (!item.isCollected) {
                            val ix = item.x * cellW
                            val iy = item.y * cellH
                            drawCircle(Color(0xFFFFEB3B), radius = 2.5.dp.toPx(), center = Offset(ix, iy))
                        }
                    }

                    // Draw player chalk markers as white crosses
                    for (marker in uiState.wallMarkers) {
                        val mx = marker.x * cellW
                        val my = marker.y * cellH
                        drawCircle(Color.White, radius = 2.dp.toPx(), center = Offset(mx, my))
                    }

                    // Draw Player as pulsing green arrow
                    val px = uiState.playerX * cellW
                    val py = uiState.playerY * cellH
                    drawCircle(Color(0xFF39FF14), radius = 5.dp.toPx(), center = Offset(px, py))
                    drawLine(
                        color = Color(0xFF39FF14),
                        start = Offset(px, py),
                        end = Offset(px + uiState.dirX * cellW * 2f, py + uiState.dirY * cellH * 2f),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                LegendItem(color = Color(0xFF39FF14), label = "Tú")
                LegendItem(color = Color(0xFFE040FB), label = "Salida / No-Clip")
                LegendItem(color = Color(0xFFFFEB3B), label = "Suministros")
                LegendItem(color = Color.White, label = "Tiza")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = Color(0xFF9CCC65),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
