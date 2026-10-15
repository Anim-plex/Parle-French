package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.FrenchLearningRepository
import com.example.domain.FrenchTtsManager
import com.example.firebase.configureAppCheck
import com.example.ui.AuthViewModel
import com.example.ui.exam.ExamViewModel
import com.example.ui.flashcards.FlashcardViewModel
import com.example.ui.roleplay.RoleplayViewModel
import com.example.ui.screens.ExamScreen
import com.example.ui.screens.FlashcardsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RoleplayScreen
import com.example.ui.screens.VisionVocabularyScreen
import com.example.ui.theme.ParleFrenchTheme
import com.example.ui.theme.RoyalBlue
import com.example.ui.vision.VisionVocabularyViewModel

enum class ParleScreen {
    HOME,
    ROLEPLAY,
    FLASHCARDS,
    VISION,
    EXAM,
    PROFILE
}

private data class NavItem(
    val screen: ParleScreen,
    val label: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val testTag: String
)

private val NAV_ITEMS = listOf(
    NavItem(ParleScreen.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    NavItem(ParleScreen.ROLEPLAY, "Roleplay", Icons.Filled.Chat, Icons.Outlined.Chat, "nav_roleplay"),
    NavItem(ParleScreen.FLASHCARDS, "Cards", Icons.Filled.Style, Icons.Outlined.Style, "nav_flashcards"),
    NavItem(ParleScreen.VISION, "Vision", Icons.Filled.CameraAlt, Icons.Outlined.CameraAlt, "nav_vision"),
    NavItem(ParleScreen.EXAM, "Exams", Icons.Filled.Description, Icons.Outlined.Description, "nav_exam"),
    NavItem(ParleScreen.PROFILE, "Profile", Icons.Filled.Person, Icons.Outlined.Person, "nav_profile")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Step 1: App Check must be configured before any UI or Firebase use.
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        val repository = FrenchLearningRepository(this)
        val authViewModel = AuthViewModel(repository)
        val roleplayViewModel = RoleplayViewModel(repository)
        val flashcardViewModel = FlashcardViewModel(repository)
        val visionViewModel = VisionVocabularyViewModel(repository)
        val examViewModel = ExamViewModel(repository)

        setContent {
            val ttsManager = remember { FrenchTtsManager(this) }

            DisposableEffect(Unit) {
                onDispose { ttsManager.shutdown() }
            }

            LaunchedEffect(Unit) {
                authViewModel.attemptAutoSignIn(this@MainActivity)
            }

            ParleFrenchTheme {
                MainAppNavigation(
                    authViewModel = authViewModel,
                    roleplayViewModel = roleplayViewModel,
                    flashcardViewModel = flashcardViewModel,
                    visionViewModel = visionViewModel,
                    examViewModel = examViewModel,
                    repository = repository,
                    ttsManager = ttsManager
                )
            }
        }
    }
}

@Composable
fun MainAppNavigation(
    authViewModel: AuthViewModel,
    roleplayViewModel: RoleplayViewModel,
    flashcardViewModel: FlashcardViewModel,
    visionViewModel: VisionVocabularyViewModel,
    examViewModel: ExamViewModel,
    repository: FrenchLearningRepository,
    ttsManager: FrenchTtsManager
) {
    var currentScreen by remember { mutableStateOf(ParleScreen.HOME) }
    val currentUser by authViewModel.currentUser.collectAsState()
    val userStats by repository.observeUserStats().collectAsState(initial = null)
    val dueCards by flashcardViewModel.dueFlashcards.collectAsState()

    BackHandler(enabled = currentScreen != ParleScreen.HOME) {
        currentScreen = ParleScreen.HOME
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NAV_ITEMS.forEach { item ->
                    NavigationBarItem(
                        selected = currentScreen == item.screen,
                        onClick = { currentScreen = item.screen },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == item.screen) item.filledIcon else item.outlinedIcon,
                                contentDescription = item.label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text(item.label, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RoyalBlue,
                            selectedTextColor = RoyalBlue,
                            indicatorColor = RoyalBlue.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            ParleScreen.HOME -> HomeScreen(
                userStats = userStats,
                dueCardsCount = dueCards.size,
                isSignedIn = currentUser != null,
                onNavigateToRoleplay = { currentScreen = ParleScreen.ROLEPLAY },
                onNavigateToFlashcards = { currentScreen = ParleScreen.FLASHCARDS },
                onNavigateToVision = { currentScreen = ParleScreen.VISION },
                onNavigateToExam = { currentScreen = ParleScreen.EXAM },
                onNavigateToProfile = { currentScreen = ParleScreen.PROFILE },
                modifier = Modifier.padding(innerPadding)
            )

            ParleScreen.ROLEPLAY -> RoleplayScreen(
                viewModel = roleplayViewModel,
                ttsManager = ttsManager,
                onBack = { currentScreen = ParleScreen.HOME },
                modifier = Modifier.padding(innerPadding)
            )

            ParleScreen.FLASHCARDS -> FlashcardsScreen(
                viewModel = flashcardViewModel,
                ttsManager = ttsManager,
                modifier = Modifier.padding(innerPadding)
            )

            ParleScreen.VISION -> VisionVocabularyScreen(
                viewModel = visionViewModel,
                ttsManager = ttsManager,
                modifier = Modifier.padding(innerPadding)
            )

            ParleScreen.EXAM -> ExamScreen(
                viewModel = examViewModel,
                modifier = Modifier.padding(innerPadding)
            )

            ParleScreen.PROFILE -> ProfileScreen(
                authViewModel = authViewModel,
                repository = repository,
                userStats = userStats,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
