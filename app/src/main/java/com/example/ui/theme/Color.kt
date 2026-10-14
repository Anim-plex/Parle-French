package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// French Heritage & Parisian Chic Color Palette
val BleuNuit = Color(0xFF0F172A)
val BleuRoy = Color(0xFF1D4ED8)
val BleuCiel = Color(0xFF38BDF8)
val BleuSurface = Color(0xFF1E293B)

val OrParis = Color(0xFFF59E0B)
val OrClair = Color(0xFFFEF3C7)
val OrAccent = Color(0xFFD97706)

val RougeFrance = Color(0xFFDC2626)
val RougeDoux = Color(0xFFFEE2E2)

val VertSucces = Color(0xFF10B981)
val VertClair = Color(0xFFD1FAE5)

val CremeFond = Color(0xFFF8FAFC)
val SurfaceCard = Color(0xFFFFFFFF)
val TextPrimaire = Color(0xFF0F172A)
val TextPrimaireSombre = Color(0xFFE2E8F0)
val TextSecondaireClair = Color(0xFF475569)
val TextSecondaireSombre = Color(0xFF94A3B8)
val BordureSubtile = Color(0xFFE2E8F0)

/**
 * Secondary body text. Adapts to the active theme so it stays readable
 * on the dark navy surfaces instead of using the light-mode slate tone.
 * Evaluated eagerly (it is a Composable getter) at each usage site.
 */
val TextSecondaire: Color
    @Composable get() = if (MaterialTheme.colorScheme.background.luminanceSafe() < 0.5f) {
        TextSecondaireSombre
    } else {
        TextSecondaireClair
    }

/** Perceptual luminance helper used to pick the right secondary text tone. */
private fun Color.luminanceSafe(): Float = (red * 0.299f + green * 0.587f + blue * 0.114f)
