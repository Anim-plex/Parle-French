package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CefrLevelBadge
import com.example.ui.exam.ExamPrompt
import com.example.ui.exam.ExamViewModel
import com.example.ui.theme.BleuRoy
import com.example.ui.theme.OrAccent
import com.example.ui.theme.OrClair
import com.example.ui.theme.OrParis
import com.example.ui.theme.RougeFrance
import com.example.ui.theme.TextSecondaire
import com.example.ui.theme.VertSucces

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamScreen(
    viewModel: ExamViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val selectedPrompt by viewModel.selectedPrompt.collectAsState()
    val essayText by viewModel.essayText.collectAsState()
    val isGrading by viewModel.isGrading.collectAsState()
    val gradingResult by viewModel.latestGradingResult.collectAsState()

    // Quiz states
    val currentQuizIndex by viewModel.currentQuizIndex.collectAsState()
    val selectedQuizOption by viewModel.quizSelectedOption.collectAsState()
    val quizScore by viewModel.quizScore.collectAsState()
    val isQuizCompleted by viewModel.quizCompleted.collectAsState()

    BackHandler(enabled = selectedPrompt != null) {
        viewModel.exitEssay()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedPrompt != null) selectedPrompt!!.title else "Examens & DELF",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    if (selectedPrompt != null) {
                        IconButton(onClick = { viewModel.exitEssay() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour aux sujets")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (selectedPrompt == null) {
                // Tab Row for Exam Modes
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = BleuRoy
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Production Écrite (DELF)", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Sprint Grammaire", fontWeight = FontWeight.SemiBold) }
                    )
                }

                if (selectedTab == 0) {
                    // DELF Essay Prompts List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = "Préparez les épreuves de production écrite DELF/DALF avec évaluation automatique par IA selon les barèmes officiels.",
                                fontSize = 13.sp,
                                color = TextSecondaire,
                                lineHeight = 18.sp
                            )
                        }

                        items(viewModel.essayPrompts) { prompt ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectPrompt(prompt) }
                                    .testTag("exam_prompt_${prompt.id}"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CefrLevelBadge(level = prompt.type)
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFF1F5F9)
                                        ) {
                                            Text(
                                                text = prompt.targetWordCount,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextSecondaire,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = prompt.title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = prompt.instructions,
                                        fontSize = 13.sp,
                                        color = TextSecondaire,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = { viewModel.selectPrompt(prompt) },
                                        colors = ButtonDefaults.buttonColors(containerColor = BleuRoy),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text("Rédiger l'essai", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Sprint Grammaire Mode
                    GrammarQuizView(
                        viewModel = viewModel,
                        currentQuizIndex = currentQuizIndex,
                        selectedOption = selectedQuizOption,
                        quizScore = quizScore,
                        isCompleted = isQuizCompleted
                    )
                }
            } else {
                // Active Essay Writing & Grading View
                val prompt = selectedPrompt!!
                val wordCount = essayText.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Prompt Instructions Card
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CefrLevelBadge(level = prompt.type)
                                    Text(
                                        text = "Objectif : ${prompt.targetWordCount}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BleuRoy
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = prompt.instructions,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    // Word Count Indicator
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Votre Rédaction en Français :",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "$wordCount mots rédigés",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (wordCount >= 30) VertSucces else TextSecondaire
                            )
                        }
                    }

                    // Essay Editor
                    item {
                        OutlinedTextField(
                            value = essayText,
                            onValueChange = { viewModel.updateEssayText(it) },
                            placeholder = { Text("Rédigez votre texte ici...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .testTag("essay_editor_input"),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    // Submit for AI Grading
                    item {
                        Button(
                            onClick = { viewModel.submitEssayForGrading() },
                            enabled = essayText.isNotBlank() && !isGrading,
                            colors = ButtonDefaults.buttonColors(containerColor = BleuRoy),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("submit_essay_btn")
                        ) {
                            if (isGrading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Correction et notation DELF en cours...")
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Évaluer avec le barème DELF (/25)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // AI Grading Result Rubric
                    if (gradingResult != null) {
                        val result = gradingResult!!
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("grading_result_card")
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Résultat Officieux DELF",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Appréciation : ${result.cefrAssessment}",
                                                fontSize = 12.sp,
                                                color = BleuRoy,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (result.score >= 15) VertSucces.copy(alpha = 0.15f) else OrParis.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "${result.score} / ${result.maxScore}",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (result.score >= 15) VertSucces else OrAccent,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Rubric Breakdown
                                    RubricScoreBar(label = "Grammaire & Syntaxe", score = result.grammarScore, max = 7)
                                    RubricScoreBar(label = "Richesse du Vocabulaire", score = result.vocabularyScore, max = 6)
                                    RubricScoreBar(label = "Cohérence & Connecteurs", score = result.coherenceScore, max = 6)
                                    RubricScoreBar(label = "Respect de la Consigne", score = result.taskFulfillmentScore, max = 6)

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Detailed Comments
                                    Text(
                                        text = "Commentaire du Correcteur :",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = result.detailedFeedback,
                                        fontSize = 13.sp,
                                        color = TextSecondaire,
                                        lineHeight = 18.sp
                                    )

                                    // Suggested Corrections
                                    if (result.suggestedCorrections.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text(
                                            text = "Points de correction clés :",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = RougeFrance
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        result.suggestedCorrections.forEach { correction ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFFEF2F2),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "• $correction",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF991B1B),
                                                    modifier = Modifier.padding(8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
fun RubricScoreBar(label: String, score: Int, max: Int) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 12.sp, color = TextSecondaire)
            Text(text = "$score / $max", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BleuRoy)
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { score.toFloat() / max.coerceAtLeast(1) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = BleuRoy,
            trackColor = Color(0xFFE2E8F0)
        )
    }
}

@Composable
fun GrammarQuizView(
    viewModel: ExamViewModel,
    currentQuizIndex: Int,
    selectedOption: Int?,
    quizScore: Int,
    isCompleted: Boolean
) {
    val questions = viewModel.grammarQuizQuestions

    if (isCompleted) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.AssignmentTurnedIn,
                    contentDescription = null,
                    tint = VertSucces,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Quiz Terminé !",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Votre score : $quizScore / ${questions.size} bonnes réponses",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BleuRoy
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.restartQuiz() },
                    colors = ButtonDefaults.buttonColors(containerColor = BleuRoy),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("restart_quiz_btn")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Recommencer le quiz")
                }
            }
        }
    } else {
        val q = questions[currentQuizIndex.coerceIn(questions.indices)]

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Question ${currentQuizIndex + 1} sur ${questions.size}",
                        fontSize = 12.sp,
                        color = TextSecondaire,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Score : $quizScore",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BleuRoy
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (currentQuizIndex + 1).toFloat() / questions.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = BleuRoy,
                    trackColor = Color(0xFFDBEAFE)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = q.question,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = q.contextSentence,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BleuRoy,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Options list
                q.options.forEachIndexed { index, option ->
                    val isSelected = selectedOption == index
                    val isCorrect = index == q.correctIndex
                    val containerColor = when {
                        selectedOption == null -> MaterialTheme.colorScheme.surface
                        isSelected && isCorrect -> Color(0xFFDCFCE7)
                        isSelected && !isCorrect -> Color(0xFFFEE2E2)
                        !isSelected && isCorrect -> Color(0xFFDCFCE7)
                        else -> MaterialTheme.colorScheme.surface
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable(enabled = selectedOption == null) {
                                viewModel.answerQuizQuestion(index)
                            }
                            .testTag("quiz_opt_$index"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = containerColor),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) BleuRoy else Color(0xFFE2E8F0)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = option, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            if (selectedOption != null) {
                                if (isCorrect) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = VertSucces)
                                } else if (isSelected) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = RougeFrance)
                                }
                            }
                        }
                    }
                }

                // Explanation
                if (selectedOption != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = OrClair,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Explication : ${q.explanation}",
                            fontSize = 12.sp,
                            color = Color(0xFF78350F),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            if (selectedOption != null) {
                Button(
                    onClick = { viewModel.nextQuizQuestion() },
                    colors = ButtonDefaults.buttonColors(containerColor = BleuRoy),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("next_quiz_q_btn")
                ) {
                    Text(
                        text = if (currentQuizIndex + 1 < questions.size) "Question suivante" else "Voir les résultats",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
