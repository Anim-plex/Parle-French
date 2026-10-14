package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards ORDER BY nextReviewEpochMs ASC")
    fun getAllCards(): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE nextReviewEpochMs <= :currentEpochMs ORDER BY nextReviewEpochMs ASC")
    fun getDueCards(currentEpochMs: Long): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE category = :category ORDER BY frenchWord ASC")
    fun getCardsByCategory(category: String): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE id = :id LIMIT 1")
    suspend fun getCardById(id: String): FlashcardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<FlashcardEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: FlashcardEntity)

    @Update
    suspend fun update(card: FlashcardEntity)

    @Delete
    suspend fun delete(card: FlashcardEntity)

    @Query("SELECT COUNT(*) FROM flashcards")
    fun getCardCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM flashcards WHERE repetitionCount >= 4")
    fun getMasteredCardCount(): Flow<Int>
}

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE userId = :userId LIMIT 1")
    fun getStats(userId: String): Flow<UserStatsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: UserStatsEntity)
}

@Dao
interface ExamResultDao {
    @Query("SELECT * FROM exam_results ORDER BY timestamp DESC")
    fun getAllResults(): Flow<List<ExamResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: ExamResultEntity)
}
