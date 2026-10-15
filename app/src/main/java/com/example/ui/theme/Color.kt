package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// "Parle French" brand palette — French tricolour inspired, tuned for
// comfortable contrast in both the light and the dark theme.

val NightBlue = Color(0xFF0F172A)
val RoyalBlue = Color(0xFF1D4ED8)
val SkyBlue = Color(0xFF38BDF8)
val SlateSurface = Color(0xFF1E293B)

val Gold = Color(0xFFF59E0B)
val GoldLight = Color(0xFFFEF3C7)
val GoldDark = Color(0xFFD97706)

val FrenchRed = Color(0xFFDC2626)
val RedSoft = Color(0xFFFEE2E2)

val SuccessGreen = Color(0xFF10B981)
val GreenSoft = Color(0xFFD1FAE5)

val AppBackground = Color(0xFFF8FAFC)
val CardSurface = Color(0xFFFFFFFF)
val Charcoal = Color(0xFF0F172A)
val HairlineBorder = Color(0xFFE2E8F0)

private val SecondaryTextLight = Color(0xFF475569)
private val SecondaryTextDark = Color(0xFF94A3B8)

/**
 * Secondary / body text tone that adapts to the active theme so it stays
 * readable on the dark navy surfaces instead of always using the light
 * slate tone. Evaluated eagerly at each usage site.
 */
val TextSecondary: Color
    @Composable get() = if (MaterialTheme.colorScheme.background.perceivedLuminance() < 0.5f) {
        SecondaryTextDark
    } else {
        SecondaryTextLight
    }

/** Perceptual luminance helper used to pick the right secondary text tone. */
private fun Color.perceivedLuminance(): Float = red * 0.299f + green * 0.587f + blue * 0.114f
