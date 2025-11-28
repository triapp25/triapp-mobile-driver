package com.triapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta de cores do app
object TriColors {
    val Black = Color(0xFF000000)
    val Background = Color(0xFF000000)
    val SurfaceDark = Color(0xFF1C1C1E)
    val SurfaceLight = Color(0xFF2C2C2E)
    val White = Color(0xFFFFFFFF)
    val TextGray = Color(0xFF8E8E93)
    val Divider = Color(0xFF2C2C2E)
    val Red = Color(0xFFFF453A)

    val AccentGreen = Color(0xFF34C759)
    val ButtonGray = Color(0xFF3A3A3C)
    val DarkText = Color(0xFF48484A)
}

private val DarkColorScheme = darkColorScheme(
    primary = TriColors.White,
    onPrimary = TriColors.Black,
    background = TriColors.Background,
    onBackground = TriColors.White,
    surface = TriColors.SurfaceDark,
    onSurface = TriColors.White,
    surfaceVariant = TriColors.SurfaceLight,
    secondary = TriColors.TextGray,
    onSecondary = TriColors.Black,
    error = TriColors.Red,
    onError = TriColors.White,
    outline = TriColors.Divider
)

@Composable
fun TriAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}
