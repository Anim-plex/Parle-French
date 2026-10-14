package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.UserStatsEntity
import com.example.ui.components.CefrLevelBadge
import com.example.ui.components.FrenchTricolorStripe
import com.example.ui.theme.BleuRoy
import com.example.ui.theme.OrAccent
import com.example.ui.theme.OrParis
import com.example.ui.theme.RougeFrance
import com.example.ui.theme.TextSecondaire
import com.example.ui.theme.VertSucces

@Composable
fun HomeScreen(
    userStats: UserStatsEntity?,
    dueCardsCount: Int,
    isSignedIn: Boolean,
    onNavigateToRoleplay: () -> Unit,
    onNavigateToFlashcards: () -> Unit,
    onNavigateToVision: () -> Unit,
    onNavigateToExam: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stats = userStats ?: UserStatsEntity()
    val isDarkTheme = isSystemInDarkTheme()

    // Sync-status chip colors: light pastels in light mode, translucent
    // tints in dark mode so the chip never reads as a white slab.
    val syncChipBg = when {
        isDarkTheme && isSignedIn -> VertSucces.copy(alpha = 0.18f)
        isDarkTheme -> Color.White.copy(alpha = 0.08f)
        isSignedIn -> Color(0xFFDCFCE7)
        else -> Color(0xFFF1F5F9)
    }
    val syncChipFg = when {
        isDarkTheme && isSignedIn -> lerp(VertSucces, Color.White, 0.35f)
        isDarkTheme -> TextSecondaire
        isSignedIn -> Color(0xFF15803D)
        else -> TextSecondaire
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Bar & Brand Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = BleuRoy,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("PF", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Parle French",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Immersion & Aisance Conversationnelle",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaire
                            )
                        }
                    }

                    // Cloud Sync Indicator Chip
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = syncChipBg,
                        modifier = Modifier
                            .clickable { onNavigateToProfile() }
                            .testTag("sync_chip")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isSignedIn) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = "État de synchronisation",
                                tint = syncChipFg,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSignedIn) "Synchronisé" else "Hors-ligne",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = syncChipFg
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                FrenchTricolorStripe(modifier = Modifier.fillMaxWidth())
            }
        }

        // Hero Banner with Parisian Café Illustration
        item {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("hero_banner")
            ) {
                Image(
                    painter = painterResource(id = R.drawable.french_cafe_banner_1792000877892),
                    contentDescription = "Terrasse de café parisien",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Gradient Scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xCC0F172A))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CefrLevelBadge(level = stats.targetLevel)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Niveau Cible : ${stats.targetLevel}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bonjour ! Prêt pour votre immersion ?",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Entraînez-vous à parler avec des situations réalistes et de l'IA en direct.",
                        color = Color(0xFFE2E8F0),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Personalized Performance & Fluency Tracking Card
        item {
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .testTag("performance_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Votre Progression",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Série active",
                                tint = RougeFrance,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${stats.streakDays} jours de suite",
                                fontWeight = FontWeight.Bold,
                                color = RougeFrance,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Fluency score bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Aisance Conversationnelle", fontSize = 13.sp, color = TextSecondaire)
                        Text("${stats.fluencyScore}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BleuRoy)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { stats.fluencyScore / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = BleuRoy,
                        trackColor = Color(0xFFDBEAFE)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stat Counters Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$dueCardsCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OrAccent)
                            Text(text = "À réviser", fontSize = 12.sp, color = TextSecondaire)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${stats.cardsMastered}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = VertSucces)
                            Text(text = "Cartes maîtrisées", fontSize = 12.sp, color = TextSecondaire)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${stats.roleplaysCompleted}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BleuRoy)
                            Text(text = "Jeux de rôle", fontSize = 12.sp, color = TextSecondaire)
                        }
                    }
                }
            }
        }

        // Feature Navigation Grid
        item {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Modules d'Apprentissage",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureHubCard(
                        title = "Jeux de Rôle IA",
                        subtitle = "Dialogues & retour temps réel",
                        icon = Icons.Default.Chat,
                        iconTint = BleuRoy,
                        badgeText = "Fluence",
                        onClick = onNavigateToRoleplay,
                        modifier = Modifier.weight(1f)
                    )
                    FeatureHubCard(
                        title = "Répétition SRS",
                        subtitle = "Cartes mémoire SM-2",
                        icon = Icons.Default.Style,
                        iconTint = OrParis,
                        badgeText = "$dueCardsCount dues",
                        onClick = onNavigateToFlashcards,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureHubCard(
                        title = "Vision & Objets",
                        subtitle = "Reconnaissance visuelle",
                        icon = Icons.Default.CameraAlt,
                        iconTint = VertSucces,
                        badgeText = "Photos & IA",
                        onClick = onNavigateToVision,
                        modifier = Modifier.weight(1f)
                    )
                    FeatureHubCard(
                        title = "Examens & DELF",
                        subtitle = "Essais & syntaxe notés",
                        icon = Icons.Default.Description,
                        iconTint = RougeFrance,
                        badgeText = "A1 - B2",
                        onClick = onNavigateToExam,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Today's Spotlight Roleplay
        item {
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
                    .fillMaxWidth()
                    .testTag("spotlight_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) BleuRoy.copy(alpha = 0.16f) else Color(0xFFEFF6FF)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = BleuRoy,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Scénario Recommandé",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BleuRoy
                        )
                        Text(
                            text = "Commander au Café Parisien",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Pratiquez la commande, l'addition et la politesse.",
                            fontSize = 12.sp,
                            color = TextSecondaire
                        )
                    }
                    Button(
                        onClick = onNavigateToRoleplay,
                        colors = ButtonDefaults.buttonColors(containerColor = BleuRoy),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("start_spotlight_btn")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Démarrer", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureHubCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    badgeText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag("feature_card_${title.replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = iconTint.copy(alpha = 0.12f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                    }
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondaire,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = subtitle, fontSize = 11.sp, color = TextSecondaire, maxLines = 1)
        }
    }
}
