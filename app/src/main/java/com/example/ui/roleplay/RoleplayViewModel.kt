package com.example.ui.roleplay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.FrenchAiService
import com.example.data.ai.RoleplayTurnResult
import com.example.data.repository.FrenchLearningRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * [title] / [description] / [aiRole] / [userRole] are the English interface text,
 * while [frenchTitle] and [initialMessage] carry the French the learner is here
 * to practise.
 */
data class RoleplayScenario(
    val id: String,
    val title: String,
    val frenchTitle: String,
    val level: String,
    val category: String,
    val description: String,
    val aiRole: String,
    val userRole: String,
    val initialMessage: String,
    val initialMessageEnglish: String
)

data class ChatMessage(
    val id: String,
    val sender: String, // "user" or "partner"
    val textFrench: String,
    val textEnglish: String = "",
    val feedback: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

class RoleplayViewModel(
    private val repository: FrenchLearningRepository,
    private val aiService: FrenchAiService = FrenchAiService()
) : ViewModel() {

    val scenarios = listOf(
        RoleplayScenario(
            id = "sc_cafe",
            title = "Ordering at a Parisian Café",
            frenchTitle = "Commander au Café Parisien",
            level = "A1-A2",
            category = "Food & Daily life",
            description = "Order coffee and pastries, ask about the specials, and request the bill like a local.",
            aiRole = "Friendly Parisian waiter",
            userRole = "Customer at the café",
            initialMessage = "Bonjour ! Bienvenue à la terrasse. Qu'est-ce qui vous ferait plaisir aujourd'hui ?",
            initialMessageEnglish = "Hello! Welcome to the terrace. What would you like today?"
        ),
        RoleplayScenario(
            id = "sc_train",
            title = "Train Station & Metro",
            frenchTitle = "À la Gare et au Métro",
            level = "A2",
            category = "Travel",
            description = "Buy a TGV ticket to Lyon and ask about platforms, delays and connections.",
            aiRole = "SNCF ticket agent",
            userRole = "Traveller",
            initialMessage = "Bonjour, bienvenue à la SNCF. Pour quelle destination voyagez-vous aujourd'hui ?",
            initialMessageEnglish = "Hello, welcome to SNCF. Which destination are you travelling to today?"
        ),
        RoleplayScenario(
            id = "sc_interview",
            title = "French Job Interview",
            frenchTitle = "L'Entretien d'Embauche",
            level = "B1-B2",
            category = "Professional",
            description = "Pitch your background and describe your strengths and career goals in professional French.",
            aiRole = "HR manager",
            userRole = "Job candidate",
            initialMessage = "Bonjour et merci de vous être déplacé(e). Pourriez-vous commencer par vous présenter brièvement ?",
            initialMessageEnglish = "Hello and thank you for coming. Could you start by introducing yourself briefly?"
        ),
        RoleplayScenario(
            id = "sc_friend",
            title = "Catching up in Montmartre",
            frenchTitle = "Rencontre Amicale à Montmartre",
            level = "B1",
            category = "Conversational",
            description = "Chat with Julien about weekend plans and hobbies, and pick up natural colloquial French.",
            aiRole = "Julien, a Parisian friend",
            userRole = "A friend visiting",
            initialMessage = "Salut ! Ça fait un bail, super de te voir ! Comment tu vas depuis la dernière fois ?",
            initialMessageEnglish = "Hi! It's been ages, great to see you! How have you been since last time?"
        ),
        RoleplayScenario(
            id = "sc_pharmacy",
            title = "At the French Pharmacy",
            frenchTitle = "Conseils à la Pharmacie",
            level = "A2-B1",
            category = "Health & Travel",
            description = "Describe symptoms such as a sore throat or fatigue and understand dosage instructions.",
            aiRole = "Advising pharmacist",
            userRole = "Customer",
            initialMessage = "Bonjour. Je vous écoute, comment puis-je vous aider aujourd'hui ?",
            initialMessageEnglish = "Hello. I'm listening — how can I help you today?"
        ),
        RoleplayScenario(
            id = "sc_debate",
            title = "Debate: Remote Work & Climate",
            frenchTitle = "Débat : Télétravail et Écologie",
            level = "B2-C1",
            category = "DELF/DALF Prep",
            description = "Build a complex argument with connectors such as néanmoins, quant à and force est de constater.",
            aiRole = "Moderator and sociologist",
            userRole = "Debater",
            initialMessage = "Bonjour. Selon vous, le télétravail représente-t-il une vraie réponse écologique aux transports urbains ?",
            initialMessageEnglish = "Hello. In your view, does remote work represent a real ecological answer to urban transport?"
        )
    )

    private val _selectedScenario = MutableStateFlow<RoleplayScenario?>(null)
    val selectedScenario: StateFlow<RoleplayScenario?> = _selectedScenario.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _latestFeedback = MutableStateFlow<String?>(null)
    val latestFeedback: StateFlow<String?> = _latestFeedback.asStateFlow()

    private val _suggestedReplies = MutableStateFlow<List<String>>(emptyList())
    val suggestedReplies: StateFlow<List<String>> = _suggestedReplies.asStateFlow()

    fun selectScenario(scenario: RoleplayScenario) {
        _selectedScenario.value = scenario
        val initialChat = ChatMessage(
            id = "init_msg",
            sender = "partner",
            textFrench = scenario.initialMessage,
            textEnglish = scenario.initialMessageEnglish,
            feedback = "Say hello and reply politely to get the conversation started!"
        )
        _messages.value = listOf(initialChat)
        _latestFeedback.value = initialChat.feedback
        _suggestedReplies.value = when (scenario.id) {
            "sc_cafe" -> listOf(
                "Bonjour ! Je voudrais un café au lait et un croissant, s'il vous plaît.",
                "Bonjour, qu'est-ce que vous recommandez aujourd'hui ?"
            )
            "sc_train" -> listOf(
                "Bonjour, je voudrais un aller-retour pour Lyon en TGV.",
                "Bonjour, à quelle heure part le prochain train pour Lyon ?"
            )
            "sc_interview" -> listOf(
                "Bonjour, enchanté(e). J'ai cinq ans d'expérience dans le numérique.",
                "Bonjour, merci pour votre accueil. Ce secteur me passionne."
            )
            else -> listOf(
                "Bonjour ! Très content(e) de discuter avec vous.",
                "Bonjour, merci beaucoup."
            )
        }
    }

    fun exitScenario() {
        _selectedScenario.value = null
        _messages.value = emptyList()
        _latestFeedback.value = null
        _suggestedReplies.value = emptyList()
    }

    fun sendMessage(userText: String) {
        val scenario = _selectedScenario.value ?: return
        if (userText.isBlank()) return

        val userMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            sender = "user",
            textFrench = userText.trim()
        )
        _messages.value = _messages.value + userMsg
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val history = _messages.value.map { it.sender to it.textFrench }
                val result: RoleplayTurnResult = aiService.conductRoleplayTurn(
                    scenarioTitle = scenario.frenchTitle,
                    aiRole = scenario.aiRole,
                    userRole = scenario.userRole,
                    level = scenario.level,
                    conversationHistory = history,
                    userMessage = userText.trim()
                )

                val partnerMsg = ChatMessage(
                    id = "partner_${System.currentTimeMillis()}",
                    sender = "partner",
                    textFrench = result.partnerReplyFrench,
                    textEnglish = result.partnerReplyEnglish,
                    feedback = result.grammarFeedback
                )

                _messages.value = _messages.value + partnerMsg
                _latestFeedback.value = result.grammarFeedback
                _suggestedReplies.value = result.suggestedReplies

                // Record progress once the conversation is underway.
                if (_messages.value.size >= 6) {
                    repository.recordRoleplayCompleted(scenario.title, 90)
                }
            } catch (e: Exception) {
                val fallbackMsg = ChatMessage(
                    id = "partner_err_${System.currentTimeMillis()}",
                    sender = "partner",
                    textFrench = "D'accord, je vous écoute ! Que voulez-vous ajouter ?",
                    textEnglish = "Alright, I'm listening! What would you like to add?",
                    feedback = "You appear to be offline or the connection is slow. You can keep practising and try again."
                )
                _messages.value = _messages.value + fallbackMsg
            } finally {
                _isLoading.value = false
            }
        }
    }
}
