package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val MadrassaPrimary = Color(0xFF1D4ED8)
val MadrassaPrimaryVariant = Color(0xFF1E40AF)
val MadrassaSecondary = Color(0xFF0284C7)
val MadrassaBackground = Color(0xFFF8FAFC)
val MadrassaSurface = Color(0xFFFFFFFF)

private val LightColorScheme =
    lightColorScheme(
        primary = MadrassaPrimary,
        secondary = MadrassaSecondary,
        background = MadrassaBackground,
        surface = MadrassaSurface,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = Color(0xFF0F172A),
        onSurface = Color(0xFF0F172A),
    )

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}

