package com.example.ui.flashcards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.FlashcardEntity
import com.example.data.repository.FrenchLearningRepository
import com.example.domain.ReviewRating
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class FlashcardViewModel(
    private val repository: FrenchLearningRepository
) : ViewModel() {

    val allFlashcards: StateFlow<List<FlashcardEntity>> = repository.observeAllFlashcards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dueFlashcards: StateFlow<List<FlashcardEntity>> = repository.observeDueFlashcards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentReviewIndex = MutableStateFlow(0)
    val currentReviewIndex: StateFlow<Int> = _currentReviewIndex.asStateFlow()

    private val _isFlipped = MutableStateFlow(false)
    val isFlipped: StateFlow<Boolean> = _isFlipped.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _isReviewCompleted = MutableStateFlow(false)
    val isReviewCompleted: StateFlow<Boolean> = _isReviewCompleted.asStateFlow()

    /**
     * Snapshot of the cards in the current review session. The live
     * due/flashcard lists shrink as cards are reviewed (Room re-emits),
     * so pinning the session keeps the progress counter and the rating
     * buttons pointing at the right card until the session is reset.
     */
    private val _sessionCards = MutableStateFlow<List<FlashcardEntity>>(emptyList())
    val sessionCards: StateFlow<List<FlashcardEntity>> = _sessionCards.asStateFlow()

    fun flipCard() {
        _isFlipped.value = !_isFlipped.value
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
        _currentReviewIndex.value = 0
        _isFlipped.value = false
        _isReviewCompleted.value = false
        _sessionCards.value = emptyList() // re-snapshot on next read
    }

    fun reviewCurrentCard(rating: ReviewRating) {
        val cards = getActiveReviewCards()
        val index = _currentReviewIndex.value
        if (index in cards.indices) {
            val card = cards[index]
            viewModelScope.launch {
                repository.reviewCard(card, rating)
            }
            if (index + 1 < cards.size) {
                _currentReviewIndex.value = index + 1
                _isFlipped.value = false
            } else {
                _isReviewCompleted.value = true
            }
        }
    }

    fun restartReview() {
        _currentReviewIndex.value = 0
        _isFlipped.value = false
        _isReviewCompleted.value = false
        _sessionCards.value = emptyList() // re-snapshot on next read
    }

    /**
     * Cards for the active session. The list is pinned (see [sessionCards])
     * once a non-empty candidate set exists, so reviewed cards stay in
     * place for the rest of the session instead of vanishing mid-deck.
     */
    fun getActiveReviewCards(): List<FlashcardEntity> {
        val pinned = _sessionCards.value
        if (pinned.isNotEmpty()) return pinned
        val source = if (dueFlashcards.value.isNotEmpty()) dueFlashcards.value else allFlashcards.value
        if (source.isEmpty()) return emptyList() // still loading from Room
        val category = _selectedCategory.value
        return if (category == "All") source else source.filter { it.category == category }
    }

    /** Pins the current session list (safe to call repeatedly / from effects). */
    fun ensureSessionSnapshot() {
        if (_sessionCards.value.isNotEmpty()) return
        val source = if (dueFlashcards.value.isNotEmpty()) dueFlashcards.value else allFlashcards.value
        if (source.isEmpty()) return
        val category = _selectedCategory.value
        _sessionCards.value = if (category == "All") source else source.filter { it.category == category }
    }

    fun addCustomFlashcard(
        french: String,
        gender: String,
        english: String,
        pronunciation: String,
        exampleSentence: String,
        exampleTranslation: String,
        category: String
    ) {
        val newCard = FlashcardEntity(
            id = "custom_${UUID.randomUUID().toString().take(8)}",
            frenchWord = french.trim(),
            gender = gender.trim(),
            englishMeaning = english.trim(),
            pronunciation = pronunciation.trim(),
            exampleSentence = exampleSentence.trim(),
            exampleTranslation = exampleTranslation.trim(),
            category = category.ifBlank { "Custom" },
            repetitionCount = 0,
            intervalDays = 1,
            easeFactor = 2.5
        )
        viewModelScope.launch {
            repository.addFlashcard(newCard)
        }
    }
}
