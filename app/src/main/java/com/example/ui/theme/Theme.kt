package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = SkyBlue,
    onPrimary = NightBlue,
    primaryContainer = RoyalBlue,
    onPrimaryContainer = Color.White,
    secondary = Gold,
    onSecondary = NightBlue,
    secondaryContainer = GoldDark,
    onSecondaryContainer = Color.White,
    tertiary = FrenchRed,
    onTertiary = Color.White,
    background = NightBlue,
    onBackground = Color.White,
    surface = SlateSurface,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFE2E8F0),
    outline = Color(0xFF475569)
)

private val LightColors = lightColorScheme(
    primary = RoyalBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = RoyalBlue,
    secondary = Gold,
    onSecondary = Color.White,
    secondaryContainer = GoldLight,
    onSecondaryContainer = GoldDark,
    tertiary = FrenchRed,
    onTertiary = Color.White,
    background = AppBackground,
    onBackground = Charcoal,
    surface = CardSurface,
    onSurface = Charcoal,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = HairlineBorder
)

@Composable
fun ParleFrenchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
