package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey val id: String,
    val frenchWord: String,
    val gender: String, // e.g., "le", "la", "un", "une", or ""
    val englishMeaning: String,
    val pronunciation: String,
    val exampleSentence: String,
    val exampleTranslation: String,
    val category: String, // e.g. "Core 500", "Travel", "Dining", "DELF Connectors", "Visual"
    val repetitionCount: Int = 0,
    val intervalDays: Int = 1,
    val easeFactor: Double = 2.5,
    val nextReviewEpochMs: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val userId: String = "local_user",
    val displayName: String = "French Learner",
    val targetLevel: String = "B1",
    val streakDays: Int = 1,
    val lastActiveDate: String = "",
    val cardsMastered: Int = 0,
    val fluencyScore: Int = 15,
    val roleplaysCompleted: Int = 0,
    val quizzesCompleted: Int = 0
)

@Entity(tableName = "exam_results")
data class ExamResultEntity(
    @PrimaryKey val attemptId: String,
    val examType: String,
    val title: String,
    val score: Int,
    val maxScore: Int,
    val feedback: String,
    val essayText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
