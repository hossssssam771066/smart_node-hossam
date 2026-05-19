package com.smartnode.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1F3A5F),
    onPrimary = Color.White,
    secondary = Color(0xFF2B4F7F),
    background = Color(0xFFF7F8FA),
    surface = Color.White,
    onSurface = Color(0xFF1B1B1B),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF0E1F33),
    secondary = Color(0xFFA8C0E3),
    background = Color(0xFF101418),
    surface = Color(0xFF1B1F23),
    onSurface = Color(0xFFE7EAEE),
)

@Composable
fun SmartNodeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
