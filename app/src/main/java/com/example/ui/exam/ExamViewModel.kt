package com.example.ui.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.ExamGradingResult
import com.example.data.ai.FrenchAiService
import com.example.data.local.ExamResultEntity
import com.example.data.repository.FrenchLearningRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class QuizQuestion(
    val id: String,
    val question: String,
    val contextSentence: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class ExamPrompt(
    val id: String,
    val type: String, // "DELF A2", "DELF B1", "DELF B2", "DALF C1"
    val title: String,
    val targetWordCount: String,
    val instructions: String,
    val sampleStartingPhrase: String
)

class ExamViewModel(
    private val repository: FrenchLearningRepository,
    private val aiService: FrenchAiService = FrenchAiService()
) : ViewModel() {

    val examHistory: StateFlow<List<ExamResultEntity>> = repository.observeExamResults()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val essayPrompts = listOf(
        ExamPrompt(
            id = "delf_a2_invitation",
            type = "DELF A2",
            title = "Réponse à une invitation amicale",
            targetWordCount = "60-80 mots",
            instructions = "Un ami français vous invite à passer le week-end chez lui en Normandie. Écrivez-lui un message pour le remercier, accepter avec enthousiasme, et lui demander l'heure d'arrivée idéale.",
            sampleStartingPhrase = "Salut Thomas ! Merci beaucoup pour ton invitation qui me fait très plaisir..."
        ),
        ExamPrompt(
            id = "delf_b1_reclamation",
            type = "DELF B1",
            title = "Courrier formel : Lettre de réclamation",
            targetWordCount = "160-180 mots",
            instructions = "Vous venez d'emménager dans un appartement en France et le chauffage ne fonctionne pas en plein hiver. Écrivez au propriétaire pour décrire la situation, exposer les désagréments et exiger une réparation rapide.",
            sampleStartingPhrase = "Monsieur, Je me permets de vous contacter au sujet de mon appartement situé au..."
        ),
        ExamPrompt(
            id = "delf_b2_semaine4jours",
            type = "DELF B2",
            title = "Essai argumentatif : La semaine de 4 jours",
            targetWordCount = "250 mots",
            instructions = "Rédigez un texte argumenté pour le journal de votre entreprise. Défendez les avantages de la semaine de 4 jours pour la productivité et la santé, tout en nuançant les défis d'organisation.",
            sampleStartingPhrase = "De nos jours, l'organisation du travail connaît une profonde mutation..."
        )
    )

    val grammarQuizQuestions = listOf(
        QuizQuestion(
            id = "q1",
            question = "Choisissez la forme correcte du subjonctif :",
            contextSentence = "Il est primordial que vous _____ à l'heure pour l'examen.",
            options = listOf("venez", "veniez", "viendrez", "viennent"),
            correctIndex = 1,
            explanation = "Après 'Il est primordial que', on emploie le subjonctif présent : que vous veniez."
        ),
        QuizQuestion(
            id = "q2",
            question = "Passé composé vs Imparfait :",
            contextSentence = "Pendant que je _____ tranquillement dans le parc, un chien m'_____ .",
            options = listOf(
                "me promenais / a surpris",
                "me suis promené / surprenait",
                "me promenais / surprenait",
                "me suis promené / a surpris"
            ),
            correctIndex = 0,
            explanation = "L'action continue en arrière-plan prend l'imparfait (me promenais) et l'événement soudain prend le passé composé (a surpris)."
        ),
        QuizQuestion(
            id = "q3",
            question = "Accord du participe passé avec l'auxiliaire avoir :",
            contextSentence = "Les jolies fleurs que j'ai _____ ce matin sont déjà fanées.",
            options = listOf("acheté", "achetée", "achetés", "achetées"),
            correctIndex = 3,
            explanation = "Le COD 'les fleurs' (féminin pluriel) est placé avant le verbe avoir, donc on accorde : 'achetées'."
        ),
        QuizQuestion(
            id = "q4",
            question = "Pronoms compléments doubles :",
            contextSentence = "Ce livre sur l'histoire de France ? Je _____ prêterai demain.",
            options = listOf("te le", "le te", "te lui", "lui te"),
            correctIndex = 0,
            explanation = "L'ordre des pronoms compléments devant le verbe est COI 2e personne (te) puis COD (le) : 'Je te le prêterai'."
        ),
        QuizQuestion(
            id = "q5",
            question = "Expression de l'hypothèse :",
            contextSentence = "Si j'avais su que tu venais, je _____ un gâteau.",
            options = listOf("préparerai", "préparais", "aurais préparé", "avais préparé"),
            correctIndex = 2,
            explanation = "Si + plus-que-parfait (avais su) entraîne le conditionnel passé (aurais préparé) dans la proposition principale."
        )
    )

    // Essay state
    private val _selectedPrompt = MutableStateFlow<ExamPrompt?>(null)
    val selectedPrompt: StateFlow<ExamPrompt?> = _selectedPrompt.asStateFlow()

    private val _essayText = MutableStateFlow("")
    val essayText: StateFlow<String> = _essayText.asStateFlow()

    private val _isGrading = MutableStateFlow(false)
    val isGrading: StateFlow<Boolean> = _isGrading.asStateFlow()

    private val _latestGradingResult = MutableStateFlow<ExamGradingResult?>(null)
    val latestGradingResult: StateFlow<ExamGradingResult?> = _latestGradingResult.asStateFlow()

    // Quiz state
    private val _currentQuizIndex = MutableStateFlow(0)
    val currentQuizIndex: StateFlow<Int> = _currentQuizIndex.asStateFlow()

    private val _quizSelectedOption = MutableStateFlow<Int?>(null)
    val quizSelectedOption: StateFlow<Int?> = _quizSelectedOption.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _quizCompleted = MutableStateFlow(false)
    val quizCompleted: StateFlow<Boolean> = _quizCompleted.asStateFlow()

    fun selectPrompt(prompt: ExamPrompt) {
        _selectedPrompt.value = prompt
        _essayText.value = prompt.sampleStartingPhrase + "\n\n"
        _latestGradingResult.value = null
    }

    fun updateEssayText(text: String) {
        _essayText.value = text
    }

    fun submitEssayForGrading() {
        val prompt = _selectedPrompt.value ?: return
        val text = _essayText.value.trim()
        if (text.isBlank()) return

        _isGrading.value = true
        viewModelScope.launch {
            try {
                val result = aiService.gradeEssayOrExam(
                    examType = prompt.type,
                    promptTitle = prompt.title,
                    promptInstructions = prompt.instructions,
                    studentAnswer = text,
                    level = prompt.type.takeLast(2)
                )
                _latestGradingResult.value = result

                val attempt = ExamResultEntity(
                    attemptId = "exam_${UUID.randomUUID().toString().take(8)}",
                    examType = prompt.type,
                    title = prompt.title,
                    score = result.score,
                    maxScore = result.maxScore,
                    feedback = result.detailedFeedback,
                    essayText = text
                )
                repository.recordExamResult(attempt)
            } finally {
                _isGrading.value = false
            }
        }
    }

    fun answerQuizQuestion(optionIndex: Int) {
        if (_quizSelectedOption.value != null) return
        _quizSelectedOption.value = optionIndex
        val currentQ = grammarQuizQuestions.getOrNull(_currentQuizIndex.value)
        if (currentQ != null && optionIndex == currentQ.correctIndex) {
            _quizScore.value = _quizScore.value + 1
        }
    }

    fun nextQuizQuestion() {
        if (_currentQuizIndex.value + 1 < grammarQuizQuestions.size) {
            _currentQuizIndex.value = _currentQuizIndex.value + 1
            _quizSelectedOption.value = null
        } else {
            _quizCompleted.value = true
            val finalScore = _quizScore.value
            val max = grammarQuizQuestions.size
            viewModelScope.launch {
                val attempt = ExamResultEntity(
                    attemptId = "quiz_${UUID.randomUUID().toString().take(8)}",
                    examType = "Quiz Grammaire",
                    title = "Sprint de Conjugaison & Syntaxe",
                    score = finalScore * 5,
                    maxScore = max * 5,
                    feedback = "Score : $finalScore / $max bonnes réponses sur la grammaire avancée."
                )
                repository.recordExamResult(attempt)
            }
        }
    }

    fun restartQuiz() {
        _currentQuizIndex.value = 0
        _quizSelectedOption.value = null
        _quizScore.value = 0
        _quizCompleted.value = false
    }

    fun exitEssay() {
        _selectedPrompt.value = null
        _latestGradingResult.value = null
    }
}
