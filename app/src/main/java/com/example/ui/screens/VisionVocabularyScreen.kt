package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.VisualVocabularyItem
import com.example.domain.FrenchTtsManager
import com.example.ui.components.CefrLevelBadge
import com.example.ui.components.FrenchAudioButton
import com.example.ui.components.GenderBadge
import com.example.ui.theme.BleuRoy
import com.example.ui.theme.OrAccent
import com.example.ui.theme.OrClair
import com.example.ui.theme.TextSecondaire
import com.example.ui.theme.VertSucces
import com.example.ui.vision.ScenePreset
import com.example.ui.vision.VisionVocabularyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisionVocabularyScreen(
    viewModel: VisionVocabularyViewModel,
    ttsManager: FrenchTtsManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedBitmap by viewModel.selectedBitmap.collectAsState()
    val selectedPreset by viewModel.selectedPreset.collectAsState()
    val detectedItems by viewModel.detectedItems.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val savedCards by viewModel.savedCardIds.collectAsState()

    // Initialize with first preset if nothing loaded yet
    LaunchedEffect(Unit) {
        if (selectedBitmap == null && viewModel.presets.isNotEmpty()) {
            viewModel.selectPreset(context, viewModel.presets.first())
        }
    }

    // Android Photo Picker launcher (Zero-permission mandated by Play policy)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onImageSelected(context, uri)
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.onBitmapCaptured(bitmap)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vision & Objets Réels", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Introductory Header
            item {
                Text(
                    text = "Apprenez le vocabulaire français en observant le monde réel. L'IA identifie les objets, leur genre (le/la) et génère des phrases authentiques.",
                    fontSize = 13.sp,
                    color = TextSecondaire,
                    lineHeight = 18.sp
                )
            }

            // Image Source Controls (Presets, Photo Picker, Camera)
            item {
                Column {
                    Text(
                        text = "Scènes Prédéfinies ou Vos Photos :",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(viewModel.presets) { preset ->
                            val isSelected = selectedPreset?.id == preset.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) BleuRoy else Color(0xFFF1F5F9),
                                modifier = Modifier
                                    .clickable { viewModel.selectPreset(context, preset) }
                                    .testTag("preset_btn_${preset.id}")
                            ) {
                                Text(
                                    text = preset.frenchTitle,
                                    color = if (isSelected) Color.White else TextSecondaire,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }

                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFEFF6FF),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                modifier = Modifier
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            androidx.activity.result.PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    }
                                    .testTag("pick_gallery_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = BleuRoy, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Galerie", color = BleuRoy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFEFF6FF),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                modifier = Modifier
                                    .clickable { cameraLauncher.launch(null) }
                                    .testTag("take_photo_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = BleuRoy, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Prendre Photo", color = BleuRoy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Image Preview Canvas
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .testTag("vision_image_preview"),
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (selectedBitmap != null) {
                            Image(
                                bitmap = selectedBitmap!!.asImageBitmap(),
                                contentDescription = "Scène analysée",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        if (isAnalyzing) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0x99000000)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Analyse des objets par l'IA...",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Results Header with "Tout Ajouter"
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Objets & Vocabulaire Identifiés",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (!statusMessage.isNullOrBlank()) {
                            Text(
                                text = statusMessage!!,
                                fontSize = 11.sp,
                                color = VertSucces,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (detectedItems.isNotEmpty()) {
                        Button(
                            onClick = { viewModel.saveAllDetectedItems() },
                            colors = ButtonDefaults.buttonColors(containerColor = VertSucces),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("save_all_items_btn")
                        ) {
                            Icon(Icons.Default.PlaylistAddCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tout ajouter", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Detected Objects List
            items(detectedItems) { item ->
                val isSaved = savedCards.contains(item.frenchWord)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("detected_item_${item.frenchWord}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                GenderBadge(gender = item.gender)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.frenchWord,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BleuRoy
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.pronunciationIpa,
                                    fontSize = 12.sp,
                                    color = TextSecondaire
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CefrLevelBadge(level = item.cefrLevel)
                                Spacer(modifier = Modifier.width(4.dp))
                                FrenchAudioButton(
                                    textToSpeak = "${item.gender} ${item.frenchWord}",
                                    ttsManager = ttsManager,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.englishMeaning,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Context sentence
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF8FAFC),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.exampleSentenceFrench,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    FrenchAudioButton(
                                        textToSpeak = item.exampleSentenceFrench,
                                        ttsManager = ttsManager,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Text(
                                    text = item.exampleSentenceEnglish,
                                    fontSize = 11.sp,
                                    color = TextSecondaire,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Save to Flashcards button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (isSaved) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = VertSucces, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Dans vos cartes", fontSize = 12.sp, color = VertSucces, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { viewModel.saveItemToFlashcards(item) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("save_card_btn_${item.frenchWord}")
                                ) {
                                    Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ajouter aux cartes SRS", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
