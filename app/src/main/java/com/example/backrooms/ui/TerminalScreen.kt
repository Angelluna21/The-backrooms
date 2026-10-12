package com.example.backrooms.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backrooms.model.EntityType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ASYNC CORP. BASE DE DATOS",
                            color = Color(0xFF39FF14),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "PROYECTO KV-31 // ACCESO CLASIFICADO",
                            color = Color(0xFF1E824C),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("terminal_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color(0xFF39FF14))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF071209))
            )
        },
        containerColor = Color(0xFF040A05)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Tab row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF091F0E), RoundedCornerShape(4.dp))
                    .padding(4.dp)
            ) {
                TabButton(
                    title = "ENTIDADES",
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    title = "RECURSOS Y LORE",
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(EntityType.entries) { entity ->
                        EntityDossierCard(entity = entity)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        ProtocolCard(
                            title = "ANOMALÍA NO-CLIP",
                            subtitle = "Fallo de Colisión Espacio-Temporal",
                            content = "En ciertas paredes del Vestíbulo la textura vibra en frecuencias anómalas. Tocar o empujar estas secciones permite caer ('no-clipear') a través de la realidad ordinaria hacia niveles más profundos."
                        )
                    }
                    item {
                        ProtocolCard(
                            title = "AGUA DE ALMENDRAS",
                            subtitle = "Estabilizador Psicológico",
                            content = "Sustancia líquida dulce y transparente recolectada en botellas por las trastiendas. Su consumo restaura la cordura deteriorada y combate las alucinaciones provocadas por el aislamiento."
                        )
                    }
                    item {
                        ProtocolCard(
                            title = "TIZA Y NAVEGACIÓN",
                            subtitle = "Protocolo para evitar bucles infinitos",
                            content = "El trazado de las salas liminales cambia sutilmente y desorienta la brújula. Dibujar marcas de tiza en los marcos y esquinas es el único método fiable para reconocer pasillos ya explorados."
                        )
                    }
                    item {
                        ProtocolCard(
                            title = "COMPORTAMIENTO DE LUZ",
                            subtitle = "60Hz y Fotofobia",
                            content = "Las lámparas fluorescentes parpadean a intervalos caóticos. Criaturas como 'El Sonriente' acechan en la negrura absoluta pero son atraídas por la luz directa de linternas potentes."
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabButton(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(2.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF1E824C) else Color.Transparent
        )
    ) {
        Text(
            text = title,
            color = if (selected) Color.White else Color(0xFF39FF14),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun EntityDossierCard(entity: EntityType) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E824C), RoundedCornerShape(6.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF081C0E))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = entity.loreTitle.uppercase(),
                color = Color(0xFFFF5252),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = entity.displayName,
                color = Color(0xFF39FF14),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = entity.description,
                color = Color(0xFFD7CCC8),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ALERTA ACÚSTICA: ${entity.alertSoundText}",
                color = Color(0xFFFFD54F),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ProtocolCard(title: String, subtitle: String, content: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E824C), RoundedCornerShape(6.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF081C0E))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                color = Color(0xFF39FF14),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = subtitle,
                color = Color(0xFF81C784),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                color = Color(0xFFEEEEEE),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}
