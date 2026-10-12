package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BackroomsColorScheme = darkColorScheme(
    primary = LiminalYellow,
    onPrimary = VoidBlack,
    secondary = CamcorderGreen,
    onSecondary = VoidBlack,
    tertiary = AlmondWaterBlue,
    onTertiary = VoidBlack,
    background = VoidBlack,
    onBackground = FluorescentWhite,
    surface = SurfaceDark,
    onSurface = FluorescentWhite,
    error = VhsRecRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = BackroomsColorScheme,
        typography = Typography,
        content = content
    )
}
