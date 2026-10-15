package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [FlashcardEntity::class, UserStatsEntity::class, ExamResultEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun flashcardDao(): FlashcardDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun examResultDao(): ExamResultDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "parle_french.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate with essential starter flashcards and user stats
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            database.flashcardDao().insertAll(INITIAL_FLASHCARDS)
                            database.userStatsDao().insertOrUpdate(
                                UserStatsEntity(
                                    userId = "local_user",
                                    displayName = "Learner",
                                    targetLevel = "B1",
                                    streakDays = 1,
                                    lastActiveDate = "Today",
                                    cardsMastered = 4,
                                    fluencyScore = 24,
                                    roleplaysCompleted = 0,
                                    quizzesCompleted = 0
                                )
                            )
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        val INITIAL_FLASHCARDS = listOf(
            FlashcardEntity(
                id = "card_1",
                frenchWord = "bonjour",
                gender = "",
                englishMeaning = "hello / good morning",
                pronunciation = "/bɔ̃.ʒuʁ/",
                exampleSentence = "Bonjour madame, comment allez-vous aujourd'hui ?",
                exampleTranslation = "Good morning ma'am, how are you today?",
                category = "Core 500",
                repetitionCount = 2,
                intervalDays = 3,
                easeFactor = 2.5
            ),
            FlashcardEntity(
                id = "card_2",
                frenchWord = "croissant",
                gender = "un",
                englishMeaning = "croissant",
                pronunciation = "/kʁwa.sɑ̃/",
                exampleSentence = "Je voudrais un croissant au beurre et un café, s'il vous plaît.",
                exampleTranslation = "I would like a butter croissant and a coffee, please.",
                category = "Dining",
                repetitionCount = 1,
                intervalDays = 1,
                easeFactor = 2.5
            ),
            FlashcardEntity(
                id = "card_3",
                frenchWord = "gare",
                gender = "la",
                englishMeaning = "train station",
                pronunciation = "/ɡaʁ/",
                exampleSentence = "Où se trouve la gare la plus proche d'ici ?",
                exampleTranslation = "Where is the nearest train station from here?",
                category = "Travel",
                repetitionCount = 0,
                intervalDays = 1,
                easeFactor = 2.5
            ),
            FlashcardEntity(
                id = "card_4",
                frenchWord = "cependant",
                gender = "",
                englishMeaning = "however / nevertheless",
                pronunciation = "/sə.pɑ̃.dɑ̃/",
                exampleSentence = "Le projet est difficile, cependant nous avons confiance en l'équipe.",
                exampleTranslation = "The project is difficult, however we trust the team.",
                category = "DELF Connectors",
                repetitionCount = 0,
                intervalDays = 1,
                easeFactor = 2.5
            ),
            FlashcardEntity(
                id = "card_5",
                frenchWord = "terrasse",
                gender = "la",
                englishMeaning = "patio / outdoor café terrace",
                pronunciation = "/tɛ.ʁas/",
                exampleSentence = "Nous allons nous asseoir en terrasse pour profiter du soleil.",
                exampleTranslation = "We are going to sit on the terrace to enjoy the sun.",
                category = "Dining",
                repetitionCount = 0,
                intervalDays = 1,
                easeFactor = 2.5
            ),
            FlashcardEntity(
                id = "card_6",
                frenchWord = "d'ailleurs",
                gender = "",
                englishMeaning = "besides / by the way / moreover",
                pronunciation = "/da.jœʁ/",
                exampleSentence = "C'est un excellent livre, et d'ailleurs l'auteur est français.",
                exampleTranslation = "It's an excellent book, and by the way the author is French.",
                category = "DELF Connectors",
                repetitionCount = 0,
                intervalDays = 1,
                easeFactor = 2.5
            ),
            FlashcardEntity(
                id = "card_7",
                frenchWord = "embouteillage",
                gender = "un",
                englishMeaning = "traffic jam",
                pronunciation = "/ɑ̃.bu.tɛ.jaʒ/",
                exampleSentence = "Désolé pour le retard, il y avait un gros embouteillage sur le périphérique.",
                exampleTranslation = "Sorry for the delay, there was a big traffic jam on the ring road.",
                category = "Travel",
                repetitionCount = 0,
                intervalDays = 1,
                easeFactor = 2.5
            ),
            FlashcardEntity(
                id = "card_8",
                frenchWord = "compétence",
                gender = "la",
                englishMeaning = "skill / proficiency / expertise",
                pronunciation = "/kɔ̃.pe.tɑ̃s/",
                exampleSentence = "Cette expérience m'a permis de développer mes compétences en négociation.",
                exampleTranslation = "This experience allowed me to develop my negotiation skills.",
                category = "Professional",
                repetitionCount = 0,
                intervalDays = 1,
                easeFactor = 2.5
            ),
            FlashcardEntity(
                id = "card_9",
                frenchWord = "formidable",
                gender = "",
                englishMeaning = "wonderful / terrific / fantastic",
                pronunciation = "/fɔʁ.mi.dabl/",
                exampleSentence = "Quelle formidable nouvelle pour toute la famille !",
                exampleTranslation = "What wonderful news for the whole family!",
                category = "Daily",
                repetitionCount = 1,
                intervalDays = 2,
                easeFactor = 2.5
            ),
            FlashcardEntity(
                id = "card_10",
                frenchWord = "sac à dos",
                gender = "un",
                englishMeaning = "backpack",
                pronunciation = "/sak a do/",
                exampleSentence = "N'oublie pas de mettre ta gourde d'eau dans ton sac à dos.",
                exampleTranslation = "Don't forget to put your water flask in your backpack.",
                category = "Visual",
                repetitionCount = 0,
                intervalDays = 1,
                easeFactor = 2.5
            )
        )
    }
}
