package com.example.domain

import com.example.data.local.FlashcardEntity
import java.util.concurrent.TimeUnit
import kotlin.math.max

enum class ReviewRating(val quality: Int) {
    AGAIN(1), // Complete blackout, restart
    HARD(2),  // Difficult recall
    GOOD(3),  // Successful recall with effort
    EASY(4)   // Immediate and effortless recall
}

object SpacedRepetitionCalculator {
    /**
     * Standard SM-2 Spaced Repetition calculation
     */
    fun calculateNextReview(
        card: FlashcardEntity,
        rating: ReviewRating
    ): FlashcardEntity {
        val q = rating.quality
        val currentRep = card.repetitionCount
        val currentEase = card.easeFactor
        val currentInterval = card.intervalDays

        val newRep: Int
        val newInterval: Int
        var newEase = currentEase + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        if (newEase < 1.3) newEase = 1.3

        if (q < 3) {
            // Failed recall
            newRep = 0
            newInterval = 1
        } else {
            // Successful recall
            newRep = currentRep + 1
            newInterval = when (currentRep) {
                0 -> 1
                1 -> 3
                2 -> 6
                else -> (currentInterval * newEase).toInt().coerceAtLeast(currentInterval + 1)
            }
        }

        val nextEpochMs = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(newInterval.toLong())

        return card.copy(
            repetitionCount = newRep,
            intervalDays = newInterval,
            easeFactor = (newEase * 100).toInt() / 100.0,
            nextReviewEpochMs = nextEpochMs,
            isSynced = false
        )
    }
}
