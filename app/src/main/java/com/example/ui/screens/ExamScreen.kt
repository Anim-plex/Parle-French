package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CefrBadge
import com.example.ui.exam.ExamViewModel
import com.example.ui.theme.FrenchRed
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextSecondary

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
                        text = if (selectedPrompt != null) selectedPrompt!!.title else "Exams & DELF",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    if (selectedPrompt != null) {
                        IconButton(onClick = { viewModel.exitEssay() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to topics")
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
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = RoyalBlue
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("DELF Writing", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Grammar Sprint", fontWeight = FontWeight.SemiBold) }
                    )
                }

                if (selectedTab == 0) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = "Prepare for DELF/DALF written production with AI feedback that follows the official marking scheme.",
                                fontSize = 13.sp,
                                color = TextSecondary,
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
                                        CefrBadge(level = prompt.type)
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFF1F5F9)
                                        ) {
                                            Text(
                                                text = prompt.targetWordCount,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextSecondary,
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
                                        color = TextSecondary,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = { viewModel.selectPrompt(prompt) },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text("Write essay", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    GrammarQuizView(
                        viewModel = viewModel,
                        currentQuizIndex = currentQuizIndex,
                        selectedOption = selectedQuizOption,
                        quizScore = quizScore,
                        isCompleted = isQuizCompleted
                    )
                }
            } else {
                val prompt = selectedPrompt!!
                val wordCount = essayText.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
                // Parse the prompt's own target (e.g. "60-80 words" -> 60) so the
                // indicator turns green at the recommended length.
                val targetWords = remember(prompt.id) {
                    Regex("\\d+").find(prompt.targetWordCount)?.value?.toIntOrNull() ?: 0
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
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
                                    CefrBadge(level = prompt.type)
                                    Text(
                                        text = "Target: ${prompt.targetWordCount}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RoyalBlue
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

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Your writing in French:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "$wordCount words" +
                                    if (targetWords > 0) " (target ~$targetWords)" else "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (targetWords > 0 && wordCount >= targetWords) SuccessGreen else TextSecondary
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = essayText,
                            onValueChange = { viewModel.updateEssayText(it) },
                            placeholder = { Text("Write your text here…") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .testTag("essay_editor_input"),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    item {
                        Button(
                            onClick = { viewModel.submitEssayForGrading() },
                            enabled = essayText.isNotBlank() && !isGrading,
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("submit_essay_btn")
                        ) {
                            if (isGrading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Marking against the DELF rubric…")
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Grade with the DELF rubric (/25)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

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
                                                text = "Unofficial DELF result",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Assessment: ${result.cefrAssessment}",
                                                fontSize = 12.sp,
                                                color = RoyalBlue,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (result.score >= 15) SuccessGreen.copy(alpha = 0.15f) else GoldDark.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "${result.score} / ${result.maxScore}",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (result.score >= 15) SuccessGreen else GoldDark,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    RubricScoreBar(label = "Grammar & syntax", score = result.grammarScore, max = 7)
                                    RubricScoreBar(label = "Vocabulary range", score = result.vocabularyScore, max = 6)
                                    RubricScoreBar(label = "Coherence & connectors", score = result.coherenceScore, max = 6)
                                    RubricScoreBar(label = "Task completion", score = result.taskFulfillmentScore, max = 6)

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text(
                                        text = "Examiner comments:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = result.detailedFeedback,
                                        fontSize = 13.sp,
                                        color = TextSecondary,
                                        lineHeight = 18.sp
                                    )

                                    if (result.suggestedCorrections.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text(
                                            text = "Key corrections:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = FrenchRed
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
            Text(text = label, fontSize = 12.sp, color = TextSecondary)
            Text(text = "$score / $max", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { score.toFloat() / max.coerceAtLeast(1) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = RoyalBlue,
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
                    tint = SuccessGreen,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Quiz complete!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Your score: $quizScore / ${questions.size} correct",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalBlue
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.restartQuiz() },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("restart_quiz_btn")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restart quiz")
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
                        text = "Question ${currentQuizIndex + 1} of ${questions.size}",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Score: $quizScore",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlue
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (currentQuizIndex + 1).toFloat() / questions.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = RoyalBlue,
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
                        color = RoyalBlue,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) RoyalBlue else Color(0xFFE2E8F0)
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
                                    Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen)
                                } else if (isSelected) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = FrenchRed)
                                }
                            }
                        }
                    }
                }

                if (selectedOption != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = GoldLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Explanation: ${q.explanation}",
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
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("next_quiz_q_btn")
                ) {
                    Text(
                        text = if (currentQuizIndex + 1 < questions.size) "Next question" else "See results",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
