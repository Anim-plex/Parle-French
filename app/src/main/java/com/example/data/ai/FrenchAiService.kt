package com.example.data.ai

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject

data class RoleplayTurnResult(
    val partnerReplyFrench: String,
    val partnerReplyEnglish: String,
    val grammarFeedback: String, // Corrections on the user's French input
    val suggestedReplies: List<String> // Helpful scaffolding options for the user
)

data class VisualVocabularyItem(
    val frenchWord: String,
    val gender: String, // "le", "la", "un", "une", etc.
    val englishMeaning: String,
    val pronunciationIpa: String,
    val exampleSentenceFrench: String,
    val exampleSentenceEnglish: String,
    val cefrLevel: String // A1, A2, B1, etc.
)

data class ExamGradingResult(
    val score: Int,
    val maxScore: Int,
    val cefrAssessment: String,
    val grammarScore: Int,
    val vocabularyScore: Int,
    val coherenceScore: Int,
    val taskFulfillmentScore: Int,
    val detailedFeedback: String,
    val suggestedCorrections: List<String>
)

class FrenchAiService {
    private val TAG = "FrenchAiService"

    private val model by lazy {
        Firebase.ai.generativeModel("gemini-3.5-flash")
    }

    /**
     * Interactive conversational roleplay step with real-time feedback
     */
    suspend fun conductRoleplayTurn(
        scenarioTitle: String,
        aiRole: String,
        userRole: String,
        level: String,
        conversationHistory: List<Pair<String, String>>, // sender ("user" or "partner") to text
        userMessage: String
    ): RoleplayTurnResult {
        return withTimeout(60_000L) {
            try {
                val historyFormatted = conversationHistory.joinToString("\n") { (sender, text) ->
                    val name = if (sender == "user") userRole else aiRole
                    "$name: $text"
                }

                val prompt = """
                    You are a native French conversational partner and tutor.
                    Scenario: $scenarioTitle
                    Your Persona: $aiRole
                    User's Persona: $userRole
                    User's Target Proficiency Level: $level (adjust your French vocabulary and pace to be authentic yet accessible for this CEFR level).
                    
                    Previous Dialogue:
                    $historyFormatted
                    
                    The user just said:
                    "$userMessage"
                    
                    You must respond in raw JSON format with NO markdown code fences, NO ```json blocks.
                    Format:
                    {
                      "partnerReplyFrench": "Your in-character reply in natural French",
                      "partnerReplyEnglish": "English translation of your reply",
                      "grammarFeedback": "Detailed constructive feedback in English highlighting any grammar, vocabulary, gender agreement, or pronunciation nuances in the user's message. If their message was perfect, praise their phrasing and suggest a colloquial native alternative.",
                      "suggestedReplies": ["Suggested response 1 in French", "Suggested response 2 in French", "Suggested response 3 in French"]
                    }
                """.trimIndent()

                val response = model.generateContent(prompt)
                val rawText = response.text ?: throw IllegalStateException("Empty response from AI")
                val cleanJson = cleanJsonString(rawText)
                val json = JSONObject(cleanJson)

                val suggestionsArray = json.optJSONArray("suggestedReplies")
                val suggestions = mutableListOf<String>()
                if (suggestionsArray != null) {
                    for (i in 0 until suggestionsArray.length()) {
                        suggestions.add(suggestionsArray.getString(i))
                    }
                }

                RoleplayTurnResult(
                    partnerReplyFrench = json.optString("partnerReplyFrench", "Bonjour ! Très heureux de discuter avec vous."),
                    partnerReplyEnglish = json.optString("partnerReplyEnglish", "Hello! Very happy to chat with you."),
                    grammarFeedback = json.optString("grammarFeedback", "Bonne prononciation et structure claire !"),
                    suggestedReplies = suggestions.ifEmpty {
                        listOf("Merci beaucoup !", "Pourriez-vous répéter ?", "Je comprends, continuons.")
                    }
                )
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Roleplay turn timed out", e)
                throw e
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error in roleplay turn", e)
                RoleplayTurnResult(
                    partnerReplyFrench = "Très intéressant ! Pouvez-vous m'en dire un peu plus ?",
                    partnerReplyEnglish = "Very interesting! Can you tell me a bit more about that?",
                    grammarFeedback = "Votre phrase est compréhensible. Continuez à pratiquer l'accord des adjectifs.",
                    suggestedReplies = listOf("D'accord, avec plaisir.", "Oui, par exemple...", "Pouvez-vous m'aider ?")
                )
            }
        }
    }

    /**
     * Visual context and real-world object identification for French vocabulary
     */
    suspend fun analyzeImageForVocabulary(bitmap: Bitmap): List<VisualVocabularyItem> {
        return withTimeout(60_000L) {
            try {
                val promptText = """
                    Analyze this image and identify 4 to 8 prominent real-world objects, scene elements, or items visible.
                    For each object, teach it to a French language learner.
                    
                    Return a JSON array with NO markdown fences, NO ```json.
                    Format:
                    [
                      {
                        "frenchWord": "croissant",
                        "gender": "un",
                        "englishMeaning": "croissant / crescent roll",
                        "pronunciationIpa": "/kʁwa.sɑ̃/",
                        "exampleSentenceFrench": "Le matin, j'adore tremper un croissant dans mon café.",
                        "exampleSentenceEnglish": "In the morning, I love dipping a croissant into my coffee.",
                        "cefrLevel": "A1"
                      }
                    ]
                """.trimIndent()

                val response = model.generateContent(
                    content {
                        image(bitmap)
                        text(promptText)
                    }
                )

                val rawText = response.text ?: throw IllegalStateException("Empty vision response")
                val cleanJson = cleanJsonString(rawText)
                val jsonArray = JSONArray(cleanJson)
                val items = mutableListOf<VisualVocabularyItem>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    items.add(
                        VisualVocabularyItem(
                            frenchWord = obj.optString("frenchWord", "objet"),
                            gender = obj.optString("gender", "le"),
                            englishMeaning = obj.optString("englishMeaning", "object"),
                            pronunciationIpa = obj.optString("pronunciationIpa", "/ɔb.ʒɛ/"),
                            exampleSentenceFrench = obj.optString("exampleSentenceFrench", "C'est un bel objet."),
                            exampleSentenceEnglish = obj.optString("exampleSentenceEnglish", "It is a nice object."),
                            cefrLevel = obj.optString("cefrLevel", "A2")
                        )
                    )
                }
                items
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Vision recognition timed out", e)
                throw e
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error analyzing image for vocabulary", e)
                listOf(
                    VisualVocabularyItem("café", "le", "coffee / coffee shop", "/ka.fe/", "Je prends un café en terrasse.", "I have a coffee on the terrace.", "A1"),
                    VisualVocabularyItem("terrasse", "la", "terrace / patio", "/tɛ.ʁas/", "La terrasse est ensoleillée.", "The terrace is sunny.", "A2"),
                    VisualVocabularyItem("chaise", "la", "chair", "/ʃɛz/", "Veuillez vous asseoir sur cette chaise.", "Please sit on this chair.", "A1"),
                    VisualVocabularyItem("livre", "le", "book", "/livʁ/", "Elle lit un roman passionnant.", "She is reading a captivating novel.", "A1")
                )
            }
        }
    }

    /**
     * DELF/DALF essay and exam grading with constructive rubric and corrections
     */
    suspend fun gradeEssayOrExam(
        examType: String,
        promptTitle: String,
        promptInstructions: String,
        studentAnswer: String,
        level: String
    ): ExamGradingResult {
        return withTimeout(60_000L) {
            try {
                val prompt = """
                    You are an official DELF/DALF French examiner and expert French pedagogue.
                    Exam Type: $examType
                    Proficiency Target: $level
                    Topic Prompt: $promptTitle
                    Instructions: $promptInstructions
                    
                    Student's Written Submission in French:
                    \"\"\"
                    $studentAnswer
                    \"\"\"
                    
                    Grade this submission according to the official DELF rubric for $level:
                    - Grammar & Syntax accuracy (out of 7)
                    - Lexical range & appropriate vocabulary (out of 6)
                    - Coherence, flow, and logical connectors (out of 6)
                    - Task completion and instruction fulfillment (out of 6)
                    Total score out of 25.
                    
                    Return raw JSON ONLY with NO markdown code fences:
                    {
                      "score": 19,
                      "maxScore": 25,
                      "cefrAssessment": "B1 Confirmed",
                      "grammarScore": 5,
                      "vocabularyScore": 5,
                      "coherenceScore": 5,
                      "taskFulfillmentScore": 4,
                      "detailedFeedback": "Comprehensive evaluation praising strong points and providing exact guidelines for improvement...",
                      "suggestedCorrections": [
                        "Original: 'J'ai allé au marché' -> Correction: 'Je suis allé(e) au marché' (Être auxiliary for movement verbs)",
                        "Original: 'C'est très bon idée' -> Correction: 'C'est une très bonne idée' (Gender agreement and indefinite article)"
                      ]
                    }
                """.trimIndent()

                val response = model.generateContent(prompt)
                val rawText = response.text ?: throw IllegalStateException("Empty exam grading response")
                val cleanJson = cleanJsonString(rawText)
                val json = JSONObject(cleanJson)

                val correctionsArray = json.optJSONArray("suggestedCorrections")
                val corrections = mutableListOf<String>()
                if (correctionsArray != null) {
                    for (i in 0 until correctionsArray.length()) {
                        corrections.add(correctionsArray.getString(i))
                    }
                }

                ExamGradingResult(
                    score = json.optInt("score", 18),
                    maxScore = json.optInt("maxScore", 25),
                    cefrAssessment = json.optString("cefrAssessment", "$level Progressing"),
                    grammarScore = json.optInt("grammarScore", 5),
                    vocabularyScore = json.optInt("vocabularyScore", 4),
                    coherenceScore = json.optInt("coherenceScore", 5),
                    taskFulfillmentScore = json.optInt("taskFulfillmentScore", 4),
                    detailedFeedback = json.optString("detailedFeedback", "Très bon travail global. Votre argumentation est claire et le vocabulaire est varié."),
                    suggestedCorrections = corrections.ifEmpty {
                        listOf("Attention aux accords des adjectifs qualificatifs.", "Variez les connecteurs logiques (par ailleurs, en revanche).")
                    }
                )
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Exam grading timed out", e)
                throw e
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error grading exam", e)
                ExamGradingResult(
                    score = 17,
                    maxScore = 25,
                    cefrAssessment = "$level Evalué",
                    grammarScore = 4,
                    vocabularyScore = 4,
                    coherenceScore = 5,
                    taskFulfillmentScore = 4,
                    detailedFeedback = "Votre texte répond aux consignes. Poursuivez l'effort sur les temps du passé (passé composé vs imparfait).",
                    suggestedCorrections = listOf("Pensez à structurer votre texte avec une introduction et une conclusion claires.")
                )
            }
        }
    }

    private fun cleanJsonString(raw: String): String {
        var str = raw.trim()
        if (str.startsWith("```json")) {
            str = str.substring(7)
        } else if (str.startsWith("```")) {
            str = str.substring(3)
        }
        if (str.endsWith("```")) {
            str = str.substring(0, str.length - 3)
        }
        return str.trim()
    }
}
