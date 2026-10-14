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
            category = "Dining & Daily",
            description = "Order coffee, pastries, ask about menu specialties, and request the check like a local.",
            aiRole = "Serveur parisien sympathique",
            userRole = "Client au café",
            initialMessage = "Bonjour ! Bienvenue à la terrasse. Qu'est-ce qui vous ferait plaisir aujourd'hui ?",
            initialMessageEnglish = "Hello! Welcome to the terrace. What would you like to have today?"
        ),
        RoleplayScenario(
            id = "sc_train",
            title = "Train Station & Metro Navigation",
            frenchTitle = "À la Gare et au Métro",
            level = "A2",
            category = "Travel",
            description = "Ask for TGV tickets to Lyon, inquire about platform departures, delays, and transfers.",
            aiRole = "Agent de guichet SNCF",
            userRole = "Voyageur",
            initialMessage = "Bonjour madame/monsieur, bienvenue à la SNCF. Pour quelle destination voyagez-vous aujourd'hui ?",
            initialMessageEnglish = "Hello, welcome to SNCF. Which destination are you traveling to today?"
        ),
        RoleplayScenario(
            id = "sc_interview",
            title = "French Job Interview",
            frenchTitle = "L'Entretien d'Embauche",
            level = "B1-B2",
            category = "Professional",
            description = "Pitch your background, describe strengths and career goals in professional French.",
            aiRole = "Responsable RH",
            userRole = "Candidat(e)",
            initialMessage = "Bonjour et merci de vous être déplacé(e). Pourriez-vous commencer par vous présenter brièvement ?",
            initialMessageEnglish = "Hello and thank you for coming. Could you begin by introducing yourself briefly?"
        ),
        RoleplayScenario(
            id = "sc_friend",
            title = "Casual Meetup in Montmartre",
            frenchTitle = "Rencontre Amicale à Montmartre",
            level = "B1",
            category = "Conversational",
            description = "Catch up with Julien, talk about weekend plans, hobbies, and authentic colloquial slang.",
            aiRole = "Julien, ami parisien",
            userRole = "Ami(e) en visite",
            initialMessage = "Salut ! Ça fait un bail, super de te voir ! Comment tu vas depuis la dernière fois ?",
            initialMessageEnglish = "Hi! It's been a while, great to see you! How have you been since last time?"
        ),
        RoleplayScenario(
            id = "sc_pharmacy",
            title = "At the French Pharmacy",
            frenchTitle = "Conseils à la Pharmacie",
            level = "A2-B1",
            category = "Health & Travel",
            description = "Explain symptoms (sore throat, fatigue), understand dosage instructions and remedies.",
            aiRole = "Pharmacien conseil",
            userRole = "Client(e)",
            initialMessage = "Bonjour. Je vous écoute, comment puis-je vous aider aujourd'hui ?",
            initialMessageEnglish = "Hello. How may I help you today?"
        ),
        RoleplayScenario(
            id = "sc_debate",
            title = "Societal Debate: Remote Work & Climate",
            frenchTitle = "Débat : Télétravail et Écologie",
            level = "B2-C1",
            category = "DELF/DALF Prep",
            description = "Articulate complex opinions with logical connectors (néanmoins, quant à, force est de constater).",
            aiRole = "Modérateur et sociologue",
            userRole = "Débatteur",
            initialMessage = "Bonjour. Selon vous, le télétravail représente-t-il une vraie réponse écologique aux transports urbains ?",
            initialMessageEnglish = "Hello. In your view, does remote work represent a real ecological response to urban transit?"
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
            feedback = "Débutez votre conversation en saluant et en répondant poliment !"
        )
        _messages.value = listOf(initialChat)
        _latestFeedback.value = initialChat.feedback
        _suggestedReplies.value = when (scenario.id) {
            "sc_cafe" -> listOf("Bonjour ! Je voudrais un café au lait et un croissant, s'il vous plaît.", "Bonjour, qu'est-ce que vous recommandez aujourd'hui ?")
            "sc_train" -> listOf("Bonjour, je voudrais un aller-retour pour Lyon en TGV.", "Bonjour, à quelle heure part le prochain train pour Lyon ?")
            "sc_interview" -> listOf("Bonjour, enchanté(e). J'ai cinq ans d'expérience dans le domaine du numérique.", "Bonjour, merci pour votre accueil. Je suis passionné(e) par ce secteur.")
            else -> listOf("Bonjour ! Très content(e) de discuter avec vous.", "Bonjour, merci beaucoup.")
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

                // Record progress
                if (_messages.value.size >= 6) {
                    repository.recordRoleplayCompleted(scenario.frenchTitle, 90)
                }
            } catch (e: Exception) {
                val fallbackMsg = ChatMessage(
                    id = "partner_err_${System.currentTimeMillis()}",
                    sender = "partner",
                    textFrench = "D'accord, je vous écoute ! Que voulez-vous ajouter ?",
                    textEnglish = "Alright, I'm listening! What would you like to add?",
                    feedback = "Mode hors-ligne ou connexion réseau lente. Vous pouvez continuer à vous entraîner."
                )
                _messages.value = _messages.value + fallbackMsg
            } finally {
                _isLoading.value = false
            }
        }
    }
}
