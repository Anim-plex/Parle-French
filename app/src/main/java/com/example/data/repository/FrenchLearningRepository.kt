package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.ExamResultEntity
import com.example.data.local.FlashcardEntity
import com.example.data.local.UserStatsEntity
import com.example.domain.ReviewRating
import com.example.domain.SpacedRepetitionCalculator
import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FrenchLearningRepository(
    private val context: Context,
    private val db: AppDatabase = AppDatabase.getInstance(context),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(
        context.getString(R.string.firestore_database_id)
    )
) {
    private val TAG = "FrenchRepo"
    private val flashcardDao = db.flashcardDao()
    private val userStatsDao = db.userStatsDao()
    private val examResultDao = db.examResultDao()
    private val auth = Firebase.auth

    // Local observation flows (Offline-First)
    fun observeAllFlashcards(): Flow<List<FlashcardEntity>> = flashcardDao.getAllCards()

    fun observeDueFlashcards(): Flow<List<FlashcardEntity>> =
        flashcardDao.getDueCards(System.currentTimeMillis())

    fun observeUserStats(): Flow<UserStatsEntity?> =
        userStatsDao.getStats(auth.currentUser?.uid ?: "local_user")

    fun observeExamResults(): Flow<List<ExamResultEntity>> =
        examResultDao.getAllResults()

    suspend fun addFlashcard(card: FlashcardEntity) = withContext(Dispatchers.IO) {
        flashcardDao.insert(card)
        syncCardToCloud(card)
    }

    suspend fun reviewCard(card: FlashcardEntity, rating: ReviewRating) = withContext(Dispatchers.IO) {
        val updatedCard = SpacedRepetitionCalculator.calculateNextReview(card, rating)
        flashcardDao.update(updatedCard)
        updateStatsAfterReview(rating)
        syncCardToCloud(updatedCard)
    }

    suspend fun recordExamResult(result: ExamResultEntity) = withContext(Dispatchers.IO) {
        examResultDao.insert(result)
        syncExamToCloud(result)
        updateStatsAfterExam(result.score, result.maxScore)
    }

    suspend fun recordRoleplayCompleted(scenarioTitle: String, score: Int) = withContext(Dispatchers.IO) {
        val currentUserId = auth.currentUser?.uid ?: "local_user"
        val stats = userStatsDao.getStats(currentUserId).firstOrNull() ?: UserStatsEntity(userId = currentUserId)
        val newStats = stats.copy(
            roleplaysCompleted = stats.roleplaysCompleted + 1,
            fluencyScore = (stats.fluencyScore + 4).coerceAtMost(100),
            streakDays = stats.streakDays.coerceAtLeast(1)
        )
        userStatsDao.insertOrUpdate(newStats)
        syncStatsToCloud(newStats)
    }

    private suspend fun updateStatsAfterReview(rating: ReviewRating) {
        val currentUserId = auth.currentUser?.uid ?: "local_user"
        val stats = userStatsDao.getStats(currentUserId).firstOrNull() ?: UserStatsEntity(userId = currentUserId)
        val bonus = if (rating == ReviewRating.EASY || rating == ReviewRating.GOOD) 1 else 0
        val newStats = stats.copy(
            cardsMastered = stats.cardsMastered + bonus,
            fluencyScore = (stats.fluencyScore + (if (bonus > 0) 1 else 0)).coerceAtMost(100)
        )
        userStatsDao.insertOrUpdate(newStats)
        syncStatsToCloud(newStats)
    }

    private suspend fun updateStatsAfterExam(score: Int, maxScore: Int) {
        val currentUserId = auth.currentUser?.uid ?: "local_user"
        val stats = userStatsDao.getStats(currentUserId).firstOrNull() ?: UserStatsEntity(userId = currentUserId)
        val percent = (score.toDouble() / maxScore.coerceAtLeast(1) * 100).toInt()
        val newStats = stats.copy(
            quizzesCompleted = stats.quizzesCompleted + 1,
            fluencyScore = ((stats.fluencyScore * 4 + percent) / 5).coerceIn(0, 100)
        )
        userStatsDao.insertOrUpdate(newStats)
        syncStatsToCloud(newStats)
    }

    // Cloud Synchronization to Firestore
    private suspend fun syncCardToCloud(card: FlashcardEntity) {
        val user = auth.currentUser ?: return
        val path = "users/${user.uid}/flashcards/${card.id}"
        try {
            val payload = mapOf(
                "cardId" to card.id,
                "userId" to user.uid,
                "frenchWord" to card.frenchWord,
                "gender" to card.gender,
                "englishMeaning" to card.englishMeaning,
                "pronunciation" to card.pronunciation,
                "exampleSentence" to card.exampleSentence,
                "exampleTranslation" to card.exampleTranslation,
                "category" to card.category,
                "repetitionCount" to card.repetitionCount,
                "intervalDays" to card.intervalDays,
                "easeFactor" to card.easeFactor,
                "nextReviewTimestamp" to card.nextReviewEpochMs,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("users").document(user.uid)
                .collection("flashcards").document(card.id)
                .set(payload)
                .await()
            flashcardDao.update(card.copy(isSynced = true))
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Log.w(TAG, "Offline: Flashcard sync deferred: ${e.message}")
        }
    }

    private suspend fun syncStatsToCloud(stats: UserStatsEntity) {
        val user = auth.currentUser ?: return
        val path = "users/${user.uid}"
        try {
            val payload = mapOf(
                "userId" to user.uid,
                "displayName" to (user.displayName ?: stats.displayName),
                "email" to (user.email ?: ""),
                "targetLevel" to stats.targetLevel,
                "streakDays" to stats.streakDays,
                "cardsMastered" to stats.cardsMastered,
                "totalRoleplaySessions" to stats.roleplaysCompleted,
                "totalQuizzesTaken" to stats.quizzesCompleted,
                "overallFluencyScore" to stats.fluencyScore,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("users").document(user.uid)
                .set(payload)
                .await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Log.w(TAG, "Offline: Stats sync deferred: ${e.message}")
        }
    }

    private suspend fun syncExamToCloud(result: ExamResultEntity) {
        val user = auth.currentUser ?: return
        val path = "users/${user.uid}/exams/${result.attemptId}"
        try {
            val payload = mapOf(
                "attemptId" to result.attemptId,
                "userId" to user.uid,
                "examType" to result.examType,
                "title" to result.title,
                "score" to result.score,
                "maxScore" to result.maxScore,
                "feedback" to result.feedback,
                "essayText" to result.essayText,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("users").document(user.uid)
                .collection("exams").document(result.attemptId)
                .set(payload)
                .await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Log.w(TAG, "Offline: Exam sync deferred: ${e.message}")
        }
    }

    /**
     * Trigger two-way sync on user sign-in: downloads cloud flashcards and uploads unsynced local cards
     */
    suspend fun performFullCloudSync() = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: return@withContext
        try {
            // Upload local unsynced flashcards
            val localCards = flashcardDao.getAllCards().firstOrNull() ?: emptyList()
            for (card in localCards) {
                if (!card.isSynced) {
                    syncCardToCloud(card)
                }
            }

            // Sync stats
            val localStats = userStatsDao.getStats(user.uid).firstOrNull()
                ?: userStatsDao.getStats("local_user").firstOrNull()
                ?: UserStatsEntity(userId = user.uid)
            val updatedStats = localStats.copy(
                userId = user.uid,
                displayName = user.displayName ?: localStats.displayName
            )
            userStatsDao.insertOrUpdate(updatedStats)
            syncStatsToCloud(updatedStats)

            // Download cloud flashcards into Room
            val snapshot = firestore.collection("users").document(user.uid)
                .collection("flashcards")
                .get()
                .await()

            val remoteCards = snapshot.documents.mapNotNull { doc ->
                val cardId = doc.getString("cardId") ?: doc.id
                val frenchWord = doc.getString("frenchWord") ?: return@mapNotNull null
                val englishMeaning = doc.getString("englishMeaning") ?: ""
                FlashcardEntity(
                    id = cardId,
                    frenchWord = frenchWord,
                    gender = doc.getString("gender") ?: "",
                    englishMeaning = englishMeaning,
                    pronunciation = doc.getString("pronunciation") ?: "",
                    exampleSentence = doc.getString("exampleSentence") ?: "",
                    exampleTranslation = doc.getString("exampleTranslation") ?: "",
                    category = doc.getString("category") ?: "Cloud",
                    repetitionCount = (doc.getLong("repetitionCount") ?: 0L).toInt(),
                    intervalDays = (doc.getLong("intervalDays") ?: 1L).toInt(),
                    easeFactor = doc.getDouble("easeFactor") ?: 2.5,
                    nextReviewEpochMs = doc.getLong("nextReviewTimestamp") ?: System.currentTimeMillis(),
                    isSynced = true
                )
            }
            if (remoteCards.isNotEmpty()) {
                flashcardDao.insertAll(remoteCards)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Full cloud sync notice: ${e.message}")
        }
    }
}
