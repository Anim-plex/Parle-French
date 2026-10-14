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
import com.example.ui.theme.BleuRoy
import com.example.ui.theme.ParleFrenchTheme
import com.example.ui.theme.TextSecondaire
import com.example.ui.vision.VisionVocabularyViewModel

enum class ParleScreen {
    HOME,
    ROLEPLAY,
    FLASHCARDS,
    VISION,
    EXAM,
    PROFILE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check configuration with launch intent before any UI or Firebase operations
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
                onDispose {
                    ttsManager.shutdown()
                }
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
                NavigationBarItem(
                    selected = currentScreen == ParleScreen.HOME,
                    onClick = { currentScreen = ParleScreen.HOME },
                    icon = {
                        Icon(
                            if (currentScreen == ParleScreen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Accueil",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Accueil", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BleuRoy,
                        selectedTextColor = BleuRoy,
                        indicatorColor = BleuRoy.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_home")
                )

                NavigationBarItem(
                    selected = currentScreen == ParleScreen.ROLEPLAY,
                    onClick = { currentScreen = ParleScreen.ROLEPLAY },
                    icon = {
                        Icon(
                            if (currentScreen == ParleScreen.ROLEPLAY) Icons.Filled.Chat else Icons.Outlined.Chat,
                            contentDescription = "Jeux de Rôle",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Rôle", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BleuRoy,
                        selectedTextColor = BleuRoy,
                        indicatorColor = BleuRoy.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_roleplay")
                )

                NavigationBarItem(
                    selected = currentScreen == ParleScreen.FLASHCARDS,
                    onClick = { currentScreen = ParleScreen.FLASHCARDS },
                    icon = {
                        Icon(
                            if (currentScreen == ParleScreen.FLASHCARDS) Icons.Filled.Style else Icons.Outlined.Style,
                            contentDescription = "Cartes Mémoire",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Cartes", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BleuRoy,
                        selectedTextColor = BleuRoy,
                        indicatorColor = BleuRoy.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_flashcards")
                )

                NavigationBarItem(
                    selected = currentScreen == ParleScreen.VISION,
                    onClick = { currentScreen = ParleScreen.VISION },
                    icon = {
                        Icon(
                            if (currentScreen == ParleScreen.VISION) Icons.Filled.CameraAlt else Icons.Outlined.CameraAlt,
                            contentDescription = "Vision & Objets",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Vision", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BleuRoy,
                        selectedTextColor = BleuRoy,
                        indicatorColor = BleuRoy.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_vision")
                )

                NavigationBarItem(
                    selected = currentScreen == ParleScreen.EXAM,
                    onClick = { currentScreen = ParleScreen.EXAM },
                    icon = {
                        Icon(
                            if (currentScreen == ParleScreen.EXAM) Icons.Filled.Description else Icons.Outlined.Description,
                            contentDescription = "Examens DELF",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("DELF", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BleuRoy,
                        selectedTextColor = BleuRoy,
                        indicatorColor = BleuRoy.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_exam")
                )

                NavigationBarItem(
                    selected = currentScreen == ParleScreen.PROFILE,
                    onClick = { currentScreen = ParleScreen.PROFILE },
                    icon = {
                        Icon(
                            if (currentScreen == ParleScreen.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = "Profil & Sync",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Profil", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BleuRoy,
                        selectedTextColor = BleuRoy,
                        indicatorColor = BleuRoy.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_profile")
                )
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
