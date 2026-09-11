package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DevPrimary,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF0369A1),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = DevSecondary,
    onSecondary = Color(0xFF022C22),
    secondaryContainer = Color(0xFF065F46),
    onSecondaryContainer = Color(0xFFD1FAE5),
    tertiary = DevTertiary,
    onTertiary = Color(0xFF1E1B4B),
    background = DevBackground,
    onBackground = DevTextPrimary,
    surface = DevSurface,
    onSurface = DevTextPrimary,
    surfaceVariant = DevSurfaceVariant,
    onSurfaceVariant = DevTextSecondary,
    outline = DevCardBorder
)

private val LightColorScheme = darkColorScheme(
    primary = DevPrimary,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF0369A1),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = DevSecondary,
    onSecondary = Color(0xFF022C22),
    secondaryContainer = Color(0xFF065F46),
    onSecondaryContainer = Color(0xFFD1FAE5),
    tertiary = DevTertiary,
    onTertiary = Color(0xFF1E1B4B),
    background = DevBackground,
    onBackground = DevTextPrimary,
    surface = DevSurface,
    onSurface = DevTextPrimary,
    surfaceVariant = DevSurfaceVariant,
    onSurfaceVariant = DevTextSecondary,
    outline = DevCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
