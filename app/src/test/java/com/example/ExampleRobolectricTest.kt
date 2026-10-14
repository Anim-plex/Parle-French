package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.FlashcardEntity
import com.example.domain.ReviewRating
import com.example.domain.SpacedRepetitionCalculator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Parle French", appName)
    }

    @Test
    fun `test spaced repetition SM-2 calculation advances interval on GOOD rating`() {
        val card = FlashcardEntity(
            id = "test_card",
            frenchWord = "bonjour",
            gender = "",
            englishMeaning = "hello",
            pronunciation = "/bɔ̃.ʒuʁ/",
            exampleSentence = "Bonjour le monde",
            exampleTranslation = "Hello world",
            category = "Core 500",
            repetitionCount = 1,
            intervalDays = 1,
            easeFactor = 2.5
        )

        val updated = SpacedRepetitionCalculator.calculateNextReview(card, ReviewRating.GOOD)
        assertEquals(2, updated.repetitionCount)
        assertTrue(updated.intervalDays > 1)
        assertTrue(updated.nextReviewEpochMs > System.currentTimeMillis())
    }

    @Test
    fun `test spaced repetition SM-2 calculation resets on AGAIN rating`() {
        val card = FlashcardEntity(
            id = "test_card",
            frenchWord = "gare",
            gender = "la",
            englishMeaning = "station",
            pronunciation = "/ɡaʁ/",
            exampleSentence = "À la gare",
            exampleTranslation = "At the station",
            category = "Travel",
            repetitionCount = 3,
            intervalDays = 6,
            easeFactor = 2.5
        )

        val updated = SpacedRepetitionCalculator.calculateNextReview(card, ReviewRating.AGAIN)
        assertEquals(0, updated.repetitionCount)
        assertEquals(1, updated.intervalDays)
    }

    @Test
    fun `test initial starter flashcards list has essential French items`() {
        val starters = AppDatabase.INITIAL_FLASHCARDS
        assertTrue(starters.isNotEmpty())
        assertTrue(starters.any { it.frenchWord == "bonjour" })
        assertTrue(starters.any { it.frenchWord == "croissant" })
        assertTrue(starters.any { it.frenchWord == "cependant" })
    }
}
