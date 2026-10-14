package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.FrenchTtsManager
import com.example.ui.theme.BleuRoy
import com.example.ui.theme.OrAccent
import com.example.ui.theme.OrClair
import com.example.ui.theme.RougeFrance
import com.example.ui.theme.VertSucces

@Composable
fun FrenchAudioButton(
    textToSpeak: String,
    ttsManager: FrenchTtsManager,
    modifier: Modifier = Modifier,
    slow: Boolean = false,
    tint: Color = BleuRoy
) {
    IconButton(
        onClick = { ttsManager.speak(textToSpeak, slow) },
        modifier = modifier
            .testTag("audio_button_${textToSpeak.take(10).replace(" ", "_")}")
            .size(40.dp)
    ) {
        Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = "Écouter la prononciation française",
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun CefrLevelBadge(
    level: String,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (level.uppercase()) {
        "A1" -> Color(0xFFE0F2FE) to Color(0xFF0369A1)
        "A2" -> Color(0xFFDCFCE7) to Color(0xFF15803D)
        "B1" -> Color(0xFFFEF3C7) to Color(0xFFB45309)
        "B2" -> Color(0xFFFFEDD5) to Color(0xFFC2410C)
        "C1", "C2" -> Color(0xFFF3E8FF) to Color(0xFF7E22CE)
        else -> Color(0xFFF1F5F9) to Color(0xFF475569)
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = level,
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun GenderBadge(
    gender: String,
    modifier: Modifier = Modifier
) {
    if (gender.isBlank()) return

    val (bg, fg) = when (gender.lowercase().trim()) {
        "le", "un", "masculin" -> Color(0xFFDBEAFE) to BleuRoy
        "la", "une", "féminin" -> Color(0xFFFCE7F3) to Color(0xFFBE185D)
        else -> Color(0xFFF1F5F9) to Color(0xFF64748B)
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = gender,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun FrenchTricolorStripe(modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        Box(
            modifier = Modifier
                .weight(1f)
                .size(height = 3.dp, width = 10.dp)
                .background(BleuRoy)
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
                .background(RougeFrance)
        )
    }
}
