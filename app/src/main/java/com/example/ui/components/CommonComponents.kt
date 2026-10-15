package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.FrenchTtsManager
import com.example.ui.theme.FrenchRed
import com.example.ui.theme.RoyalBlue

/**
 * On dark surfaces the pastel badge backgrounds are swapped for translucent
 * tints with lightened foregrounds, so badges keep their meaning (and their
 * contrast) in both themes.
 */
@Composable
private fun badgeColors(background: Color, foreground: Color): Pair<Color, Color> {
    if (!isSystemInDarkTheme()) return background to foreground
    return foreground.copy(alpha = 0.22f) to lerp(foreground, Color.White, 0.45f)
}

/** Plays the French pronunciation of [textToSpeak] through the device TTS engine. */
@Composable
fun PronounceButton(
    textToSpeak: String,
    ttsManager: FrenchTtsManager,
    modifier: Modifier = Modifier,
    slow: Boolean = false,
    tint: Color = RoyalBlue
) {
    IconButton(
        onClick = { ttsManager.speak(textToSpeak, slow) },
        modifier = modifier
            .testTag("audio_button_${textToSpeak.take(10).replace(" ", "_")}")
            .size(40.dp)
    ) {
        Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = "Play French pronunciation",
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}

/** Compact CEFR level chip (A1–C2) with a level-appropriate colour. */
@Composable
fun CefrBadge(
    level: String,
    modifier: Modifier = Modifier
) {
    val base = when (level.uppercase()) {
        "A1" -> Color(0xFFE0F2FE) to Color(0xFF0369A1)
        "A2" -> Color(0xFFDCFCE7) to Color(0xFF15803D)
        "B1" -> Color(0xFFFEF3C7) to Color(0xFFB45309)
        "B2" -> Color(0xFFFFEDD5) to Color(0xFFC2410C)
        "C1", "C2" -> Color(0xFFF3E8FF) to Color(0xFF7E22CE)
        else -> Color(0xFFF1F5F9) to Color(0xFF475569)
    }
    val (bg, fg) = badgeColors(base.first, base.second)

    Surface(color = bg, shape = RoundedCornerShape(8.dp), modifier = modifier) {
        Text(
            text = level,
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/** French grammatical gender chip ("le", "la", "un", "une"). */
@Composable
fun GenderBadge(
    gender: String,
    modifier: Modifier = Modifier
) {
    if (gender.isBlank()) return

    val base = when (gender.lowercase().trim()) {
        "le", "un", "masculine" -> Color(0xFFDBEAFE) to RoyalBlue
        "la", "une", "feminine" -> Color(0xFFFCE7F3) to Color(0xFFBE185D)
        else -> Color(0xFFF1F5F9) to Color(0xFF64748B)
    }
    val (bg, fg) = badgeColors(base.first, base.second)

    Surface(color = bg, shape = RoundedCornerShape(6.dp), modifier = modifier) {
        Text(
            text = gender,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/** Thin blue / white / red accent bar used under section headers. */
@Composable
fun TricolorStripe(modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        Box(
            modifier = Modifier
                .weight(1f)
                .size(height = 3.dp, width = 10.dp)
                .background(RoyalBlue)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .size(height = 3.dp, width = 10.dp)
                .background(Color.White)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .size(height = 3.dp, width = 10.dp)
                .background(FrenchRed)
        )
    }
}
