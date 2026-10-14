package com.example.ui.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.ai.FrenchAiService
import com.example.data.ai.VisualVocabularyItem
import com.example.data.local.FlashcardEntity
import com.example.data.repository.FrenchLearningRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ScenePreset(
    val id: String,
    val title: String,
    val frenchTitle: String,
    val drawableResId: Int,
    val description: String
)

class VisionVocabularyViewModel(
    private val repository: FrenchLearningRepository,
    private val aiService: FrenchAiService = FrenchAiService()
) : ViewModel() {

    val presets = listOf(
        ScenePreset(
            id = "preset_cafe",
            title = "Parisian Café Terrace",
            frenchTitle = "Terrasse de Café Parisien",
            drawableResId = R.drawable.french_cafe_banner_1792000877892,
            description = "Explore vocabulary for coffee, croissants, wicker chairs, cobblestones, and bistro facades."
        ),
        ScenePreset(
            id = "preset_market",
            title = "French Open-Air Market",
            frenchTitle = "Marché Provençal et Boulangerie",
            drawableResId = R.drawable.french_market_scene_1792000895580,
            description = "Discover vocabulary for fresh baguettes, cheeses, sun-ripened tomatoes, and market stalls."
        )
    )

    private val _selectedBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedBitmap: StateFlow<Bitmap?> = _selectedBitmap.asStateFlow()

    private val _selectedPreset = MutableStateFlow<ScenePreset?>(presets.first())
    val selectedPreset: StateFlow<ScenePreset?> = _selectedPreset.asStateFlow()

    private val _detectedItems = MutableStateFlow<List<VisualVocabularyItem>>(emptyList())
    val detectedItems: StateFlow<List<VisualVocabularyItem>> = _detectedItems.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _savedCardIds = MutableStateFlow<Set<String>>(emptySet())
    val savedCardIds: StateFlow<Set<String>> = _savedCardIds.asStateFlow()

    fun selectPreset(context: Context, preset: ScenePreset) {
        _selectedPreset.value = preset
        try {
            val bitmap = BitmapFactory.decodeResource(context.resources, preset.drawableResId)
            _selectedBitmap.value = bitmap
            analyzeBitmap(bitmap)
        } catch (e: Exception) {
            _statusMessage.value = "Impossible de charger l'image prédéfinie."
        }
    }

    fun onImageSelected(context: Context, uri: Uri) {
        _selectedPreset.value = null
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    _selectedBitmap.value = bitmap
                    analyzeBitmap(bitmap)
                }
            } catch (e: Exception) {
                _statusMessage.value = "Erreur lors du chargement de l'image sélectionnée."
            }
        }
    }

    fun onBitmapCaptured(bitmap: Bitmap) {
        _selectedPreset.value = null
        _selectedBitmap.value = bitmap
        analyzeBitmap(bitmap)
    }

    private fun analyzeBitmap(bitmap: Bitmap) {
        _isAnalyzing.value = true
        _statusMessage.value = null
        viewModelScope.launch {
            try {
                val results = aiService.analyzeImageForVocabulary(bitmap)
                _detectedItems.value = results
                _statusMessage.value = "${results.size} objets identifiés en français !"
            } catch (e: Exception) {
                _statusMessage.value = "Analyse visuelle terminée avec les mots clés de la scène."
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun saveItemToFlashcards(item: VisualVocabularyItem) {
        val newCard = FlashcardEntity(
            id = "vis_${UUID.randomUUID().toString().take(8)}",
            frenchWord = item.frenchWord,
            gender = item.gender,
            englishMeaning = item.englishMeaning,
            pronunciation = item.pronunciationIpa,
            exampleSentence = item.exampleSentenceFrench,
            exampleTranslation = item.exampleSentenceEnglish,
            category = "Visual Context",
            repetitionCount = 0,
            intervalDays = 1,
            easeFactor = 2.5
        )
        viewModelScope.launch {
            repository.addFlashcard(newCard)
            _savedCardIds.value = _savedCardIds.value + item.frenchWord
        }
    }

    fun saveAllDetectedItems() {
        val items = _detectedItems.value
        viewModelScope.launch {
            for (item in items) {
                if (!savedCardIds.value.contains(item.frenchWord)) {
                    saveItemToFlashcards(item)
                }
            }
            _statusMessage.value = "Tous les objets ont été ajoutés à vos cartes mémoire !"
        }
    }
}
