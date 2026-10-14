package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FlashcardEntity
import com.example.domain.FrenchTtsManager
import com.example.domain.ReviewRating
import com.example.ui.components.FrenchAudioButton
import com.example.ui.components.GenderBadge
import com.example.ui.flashcards.FlashcardViewModel
import com.example.ui.theme.BleuRoy
import com.example.ui.theme.OrAccent
import com.example.ui.theme.OrClair
import com.example.ui.theme.OrParis
import com.example.ui.theme.RougeFrance
import com.example.ui.theme.TextSecondaire
import com.example.ui.theme.VertSucces

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

    val categories = listOf("All", "Core 500", "Dining", "Travel", "DELF Connectors", "Visual Context", "Custom")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Répétition Espacée (SRS)", fontWeight = FontWeight.Bold) },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = OrClair,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "${activeCards.size} cartes",
                            color = OrAccent,
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
                containerColor = BleuRoy,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_flashcard_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter un mot")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Category Selector Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) BleuRoy else Color(0xFFF1F5F9),
                        modifier = Modifier
                            .clickable { viewModel.setCategory(cat) }
                            .testTag("category_chip_${cat.replace(" ", "_")}")
                    ) {
                        Text(
                            text = if (cat == "All") "Toutes les cartes" else cat,
                            color = if (isSelected) Color.White else TextSecondaire,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (isCompleted || activeCards.isEmpty()) {
                // Completed State
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
                            tint = VertSucces,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Session Terminée !",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Vous avez révisé tous les mots de cette session. Votre mémoire à long terme se renforce selon l'algorithme SM-2.",
                            textAlign = TextAlign.Center,
                            color = TextSecondaire,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.restartReview() },
                            colors = ButtonDefaults.buttonColors(containerColor = BleuRoy),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("restart_review_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Revoir la sélection")
                        }
                    }
                }
            } else {
                val currentCard = activeCards[currentIndex.coerceIn(activeCards.indices)]

                // Progress Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Carte ${currentIndex + 1} sur ${activeCards.size}",
                            fontSize = 12.sp,
                            color = TextSecondaire,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Intervalle : ${currentCard.intervalDays}j",
                            fontSize = 12.sp,
                            color = BleuRoy,
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
                        color = BleuRoy,
                        trackColor = Color(0xFFDBEAFE)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive 3D Flip Flashcard
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clickable { viewModel.flipCard() }
                        .testTag("active_flashcard"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Top info row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    GenderBadge(gender = currentCard.gender)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFF1F5F9)
                                    ) {
                                        Text(
                                            text = currentCard.category,
                                            fontSize = 11.sp,
                                            color = TextSecondaire,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                FrenchAudioButton(
                                    textToSpeak = currentCard.frenchWord,
                                    ttsManager = ttsManager,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            // Center Content
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 16.dp)
                            ) {
                                Text(
                                    text = currentCard.frenchWord,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BleuRoy,
                                    textAlign = TextAlign.Center
                                )

                                if (currentCard.pronunciation.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currentCard.pronunciation,
                                        fontSize = 14.sp,
                                        color = TextSecondaire
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Flipped Details
                                if (isFlipped) {
                                    Text(
                                        text = currentCard.englishMeaning,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )

                                    if (currentCard.exampleSentence.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFFF8FAFC),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "Exemple en contexte :",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextSecondaire
                                                    )
                                                    FrenchAudioButton(
                                                        textToSpeak = currentCard.exampleSentence,
                                                        ttsManager = ttsManager,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = currentCard.exampleSentence,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (currentCard.exampleTranslation.isNotBlank()) {
                                                    Text(
                                                        text = currentCard.exampleTranslation,
                                                        fontSize = 12.sp,
                                                        color = TextSecondaire,
                                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TouchApp,
                                            contentDescription = null,
                                            tint = TextSecondaire,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Touchez la carte pour révéler la signification",
                                            fontSize = 12.sp,
                                            color = TextSecondaire
                                        )
                                    }
                                }
                            }

                            // Bottom Hint
                            Text(
                                text = if (isFlipped) "Évaluez votre rappel ci-dessous" else "Rappel actif : devinez avant de retourner !",
                                fontSize = 11.sp,
                                color = TextSecondaire
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SM-2 Rating Buttons (Active Recall scoring)
                if (isFlipped) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Sm2RatingButton(
                            label = "À revoir",
                            subLabel = "1j",
                            color = RougeFrance,
                            onClick = { viewModel.reviewCurrentCard(ReviewRating.AGAIN) },
                            modifier = Modifier.weight(1f)
                        )
                        Sm2RatingButton(
                            label = "Difficile",
                            subLabel = "${currentCard.intervalDays}j",
                            color = OrParis,
                            onClick = { viewModel.reviewCurrentCard(ReviewRating.HARD) },
                            modifier = Modifier.weight(1f)
                        )
                        Sm2RatingButton(
                            label = "Bien",
                            subLabel = "${currentCard.intervalDays * 2}j",
                            color = BleuRoy,
                            onClick = { viewModel.reviewCurrentCard(ReviewRating.GOOD) },
                            modifier = Modifier.weight(1f)
                        )
                        Sm2RatingButton(
                            label = "Facile",
                            subLabel = "${currentCard.intervalDays * 3}j",
                            color = VertSucces,
                            onClick = { viewModel.reviewCurrentCard(ReviewRating.EASY) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Button(
                        onClick = { viewModel.flipCard() },
                        colors = ButtonDefaults.buttonColors(containerColor = BleuRoy),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(bottom = 8.dp)
                            .testTag("reveal_card_btn")
                    ) {
                        Text("Révéler la réponse", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Add Custom Flashcard Dialog
    if (showAddDialog) {
        var frenchWord by remember { mutableStateOf("") }
        var gender by remember { mutableStateOf("un") }
        var englishMeaning by remember { mutableStateOf("") }
        var pronunciation by remember { mutableStateOf("") }
        var exampleSentence by remember { mutableStateOf("") }
        var exampleTranslation by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Custom") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Ajouter un mot de vocabulaire") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = frenchWord,
                        onValueChange = { frenchWord = it },
                        label = { Text("Mot en français *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("le", "la", "un", "une", "").forEach { g ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (gender == g) BleuRoy else Color(0xFFF1F5F9),
                                modifier = Modifier.clickable { gender = g }
                            ) {
                                Text(
                                    text = if (g.isEmpty()) "aucun" else g,
                                    color = if (gender == g) Color.White else TextSecondaire,
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
                        label = { Text("Signification en anglais *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = exampleSentence,
                        onValueChange = { exampleSentence = it },
                        label = { Text("Exemple de phrase en français") },
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
                                category = category
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BleuRoy)
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Annuler")
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
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
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
