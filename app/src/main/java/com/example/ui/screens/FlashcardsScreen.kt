package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FlashcardEntity
import com.example.domain.FrenchTtsManager
import com.example.domain.ReviewRating
import com.example.ui.components.GenderBadge
import com.example.ui.components.PronounceButton
import com.example.ui.flashcards.FlashcardViewModel
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextSecondary

private val REVIEW_CATEGORIES = listOf(
    "All", "Core 500", "Dining", "Travel",
    "DELF Connectors", "Professional", "Daily", "Visual", "Custom"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    viewModel: FlashcardViewModel,
    ttsManager: FrenchTtsManager,
    modifier: Modifier = Modifier
) {
    val allCards by viewModel.allFlashcards.collectAsState()
    val dueCards by viewModel.dueFlashcards.collectAsState()
    val currentIndex by viewModel.currentReviewIndex.collectAsState()
    val isFlipped by viewModel.isFlipped.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isCompleted by viewModel.isReviewCompleted.collectAsState()

    val activeCards = viewModel.getActiveReviewCards()
    var showAddDialog by remember { mutableStateOf(false) }

    // Pin the review session as soon as the card lists are available so the
    // progress counter and rating buttons keep pointing at the same cards
    // while the live due-list shrinks with each answer.
    LaunchedEffect(allCards.size, dueCards.size, selectedCategory) {
        viewModel.ensureSessionSnapshot()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spaced Repetition", fontWeight = FontWeight.Bold) },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GoldLight,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "${activeCards.size} cards",
                            color = GoldDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = RoyalBlue,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_flashcard_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add a word")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items(REVIEW_CATEGORIES) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) RoyalBlue else Color(0xFFF1F5F9),
                        modifier = Modifier
                            .clickable { viewModel.setCategory(cat) }
                            .testTag("category_chip_${cat.replace(" ", "_")}")
                    ) {
                        Text(
                            text = if (cat == "All") "All cards" else cat,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (isCompleted || activeCards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Session complete!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "You reviewed every word in this session. Your long-term memory is strengthening thanks to the SM-2 algorithm.",
                            textAlign = TextAlign.Center,
                            color = TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.restartReview() },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("restart_review_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Review again")
                        }
                    }
                }
            } else {
                val currentCard = activeCards[currentIndex.coerceIn(activeCards.indices)]

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Card ${currentIndex + 1} of ${activeCards.size}",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Interval: ${currentCard.intervalDays}d",
                            fontSize = 12.sp,
                            color = RoyalBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (currentIndex + 1).toFloat() / activeCards.size.coerceAtLeast(1) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = RoyalBlue,
                        trackColor = Color(0xFFDBEAFE)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // The rotation drives both the animation and which face is
                // visible; the back face is counter-rotated so it reads
                // correctly once the card passes 90°.
                val flipRotation by animateFloatAsState(
                    targetValue = if (isFlipped) 180f else 0f,
                    animationSpec = tween(durationMillis = 550),
                    label = "flashcardFlip"
                )
                val flipDensity = LocalDensity.current

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clickable { viewModel.flipCard() }
                        .graphicsLayer {
                            rotationY = flipRotation
                            cameraDistance = 14f * flipDensity.density
                        }
                        .testTag("active_flashcard"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    if (flipRotation <= 90f) {
                        FlashcardFrontFace(
                            card = currentCard,
                            ttsManager = ttsManager,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        FlashcardBackFace(
                            card = currentCard,
                            ttsManager = ttsManager,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { rotationY = 180f }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isFlipped) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Sm2RatingButton(
                            label = "Again",
                            subLabel = "1d",
                            color = com.example.ui.theme.FrenchRed,
                            onClick = { viewModel.reviewCurrentCard(ReviewRating.AGAIN) },
                            modifier = Modifier.weight(1f)
                        )
                        Sm2RatingButton(
                            label = "Hard",
                            subLabel = "${currentCard.intervalDays}d",
                            color = GoldDark,
                            onClick = { viewModel.reviewCurrentCard(ReviewRating.HARD) },
                            modifier = Modifier.weight(1f)
                        )
                        Sm2RatingButton(
                            label = "Good",
                            subLabel = "${currentCard.intervalDays * 2}d",
                            color = RoyalBlue,
                            onClick = { viewModel.reviewCurrentCard(ReviewRating.GOOD) },
                            modifier = Modifier.weight(1f)
                        )
                        Sm2RatingButton(
                            label = "Easy",
                            subLabel = "${currentCard.intervalDays * 3}d",
                            color = SuccessGreen,
                            onClick = { viewModel.reviewCurrentCard(ReviewRating.EASY) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Button(
                        onClick = { viewModel.flipCard() },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(bottom = 8.dp)
                            .testTag("reveal_card_btn")
                    ) {
                        Text("Reveal answer", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var frenchWord by remember { mutableStateOf("") }
        var gender by remember { mutableStateOf("un") }
        var englishMeaning by remember { mutableStateOf("") }
        var pronunciation by remember { mutableStateOf("") }
        var exampleSentence by remember { mutableStateOf("") }
        var exampleTranslation by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add a vocabulary word") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = frenchWord,
                        onValueChange = { frenchWord = it },
                        label = { Text("French word *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("le", "la", "un", "une", "").forEach { g ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (gender == g) RoyalBlue else Color(0xFFF1F5F9),
                                modifier = Modifier.clickable { gender = g }
                            ) {
                                Text(
                                    text = if (g.isEmpty()) "none" else g,
                                    color = if (gender == g) Color.White else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = englishMeaning,
                        onValueChange = { englishMeaning = it },
                        label = { Text("English meaning *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = exampleSentence,
                        onValueChange = { exampleSentence = it },
                        label = { Text("Example sentence (French)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = exampleTranslation,
                        onValueChange = { exampleTranslation = it },
                        label = { Text("Example translation (English)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pronunciation,
                        onValueChange = { pronunciation = it },
                        label = { Text("Pronunciation (IPA)") },
                        placeholder = { Text("e.g. bɔ̃ʒuʁ") },
                        supportingText = { Text("Optional — shown under the word on the card") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (frenchWord.isNotBlank() && englishMeaning.isNotBlank()) {
                            viewModel.addCustomFlashcard(
                                french = frenchWord,
                                gender = gender,
                                english = englishMeaning,
                                pronunciation = pronunciation,
                                exampleSentence = exampleSentence,
                                exampleTranslation = exampleTranslation,
                                category = "Custom"
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun Sm2RatingButton(
    label: String,
    subLabel: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = modifier
            .height(52.dp)
            .clickable { onClick() }
            .testTag("sm2_btn_${label}")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = subLabel, fontSize = 10.sp, color = color.copy(alpha = 0.8f))
        }
    }
}

/** Gender, category and audio controls shown at the top of both faces. */
@Composable
private fun FlashcardTopRow(
    card: FlashcardEntity,
    ttsManager: FrenchTtsManager
) {
    val isDark = isSystemInDarkTheme()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GenderBadge(gender = card.gender)
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFF1F5F9)
            ) {
                Text(
                    text = card.category,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        PronounceButton(
            textToSpeak = card.frenchWord,
            ttsManager = ttsManager,
            modifier = Modifier.size(36.dp)
        )
    }
}

/** Question side of the card: word + pronunciation + tap hint. */
@Composable
private fun FlashcardFrontFace(
    card: FlashcardEntity,
    ttsManager: FrenchTtsManager,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.padding(24.dp)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FlashcardTopRow(card = card, ttsManager = ttsManager)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Text(
                    text = card.frenchWord,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = RoyalBlue,
                    textAlign = TextAlign.Center
                )

                if (card.pronunciation.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = card.pronunciation,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tap the card to reveal the meaning",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Text(
                text = "Active recall: guess before you flip!",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

/** Answer side of the card: meaning + contextual example (scrollable). */
@Composable
private fun FlashcardBackFace(
    card: FlashcardEntity,
    ttsManager: FrenchTtsManager,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    Box(modifier = modifier.padding(24.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FlashcardTopRow(card = card, ttsManager = ttsManager)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = card.frenchWord,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = RoyalBlue,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = card.englishMeaning,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            if (card.exampleSentence.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Example in context:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            PronounceButton(
                                textToSpeak = card.exampleSentence,
                                ttsManager = ttsManager,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = card.exampleSentence,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (card.exampleTranslation.isNotBlank()) {
                            Text(
                                text = card.exampleTranslation,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Rate your recall below",
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}
