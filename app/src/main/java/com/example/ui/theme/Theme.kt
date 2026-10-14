package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BleuCiel,
    onPrimary = BleuNuit,
    primaryContainer = BleuRoy,
    onPrimaryContainer = Color.White,
    secondary = OrParis,
    onSecondary = BleuNuit,
    secondaryContainer = OrAccent,
    tertiary = RougeFrance,
    background = BleuNuit,
    surface = BleuSurface,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = BleuRoy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = BleuRoy,
    secondary = OrParis,
    onSecondary = Color.White,
    secondaryContainer = OrClair,
    onSecondaryContainer = OrAccent,
    tertiary = RougeFrance,
    background = CremeFond,
    surface = SurfaceCard,
    onBackground = TextPrimaire,
    onSurface = TextPrimaire
)

@Composable
fun ParleFrenchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep distinctive French palette
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
