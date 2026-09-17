package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClinicalQuizEngine
import com.example.model.QuizQuestion
import com.example.model.ResourceItem
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizPracticeSheet(
    resource: ResourceItem,
    onSaveResult: (resourceId: String, resourceTitle: String, score: Int, total: Int, mode: String) -> Unit,
    onDismiss: () -> Unit
) {
    val questions = remember(resource.id) {
        ClinicalQuizEngine.generateQuiz(resource)
    }

    var isExamMode by remember { mutableStateOf(false) } // false = Practice (instant feedback), true = Exam
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    val userAnswers = remember { mutableStateMapOf<Int, Int>() } // questionIndex -> selectedOption
    var isQuizCompleted by remember { mutableStateOf(false) }

    val currentQuestion = questions.getOrNull(currentQuestionIndex)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("sheet_quiz")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isExamMode) "NCLEX Examination Mode" else "Clinical Practice Mode",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = resource.title.take(35) + if (resource.title.length > 35) "..." else "",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            HorizontalDivider()

            if (!isQuizCompleted) {
                // In-Progress Quiz Screen
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Progress & Mode Switcher
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Question ${currentQuestionIndex + 1} of ${questions.size}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Exam Mode", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.width(4.dp))
                                Switch(
                                    checked = isExamMode,
                                    onCheckedChange = {
                                        isExamMode = it
                                        selectedOptionIndex = null
                                    },
                                    modifier = Modifier.scale(0.8f)
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { (currentQuestionIndex + 1).toFloat() / questions.size.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = TealPrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    if (currentQuestion != null) {
                        // Question Card
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Surface(shape = RoundedCornerShape(4.dp), color = TealContainerLight) {
                                            Text(
                                                currentQuestion.category.uppercase(),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TealPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(currentQuestion.difficulty, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = currentQuestion.question,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }

                        // Options
                        items(currentQuestion.options.size) { optIndex ->
                            val optionText = currentQuestion.options[optIndex]
                            val isSelected = selectedOptionIndex == optIndex
                            val isAnswerRevealed = !isExamMode && selectedOptionIndex != null
                            val isCorrect = optIndex == currentQuestion.correctAnswerIndex

                            val (bgColor, borderColor, textColor) = when {
                                isAnswerRevealed && isCorrect -> Triple(Color(0xFFD1FAE5), ClinicalAlertGreen, Color(0xFF065F46))
                                isAnswerRevealed && isSelected && !isCorrect -> Triple(Color(0xFFFEE2E2), ClinicalAlertRed, Color(0xFF991B1B))
                                isSelected -> Triple(TealContainerLight, TealPrimary, TealPrimaryDark)
                                else -> Triple(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), MaterialTheme.colorScheme.onSurface)
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = !isAnswerRevealed) {
                                        selectedOptionIndex = optIndex
                                        userAnswers[currentQuestionIndex] = optIndex
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = bgColor),
                                border = BorderStroke(1.5.dp, borderColor)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${('A' + optIndex)}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = optionText,
                                        fontSize = 13.sp,
                                        color = textColor,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isAnswerRevealed && isCorrect) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Correct", tint = ClinicalAlertGreen)
                                    } else if (isAnswerRevealed && isSelected && !isCorrect) {
                                        Icon(Icons.Default.Cancel, contentDescription = "Incorrect", tint = ClinicalAlertRed)
                                    }
                                }
                            }
                        }

                        // Immediate Clinical Rationale in Practice Mode
                        if (!isExamMode && selectedOptionIndex != null) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = TealContainerLight,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Psychology, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Clinical Rationale & Evidence:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TealPrimary)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(currentQuestion.explanation, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("💡 ${currentQuestion.clinicalPearl}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AmberTertiary)
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (currentQuestionIndex > 0) {
                        OutlinedButton(
                            onClick = {
                                currentQuestionIndex--
                                selectedOptionIndex = userAnswers[currentQuestionIndex]
                            },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Previous")
                        }
                    }

                    Button(
                        onClick = {
                            if (currentQuestionIndex < questions.size - 1) {
                                currentQuestionIndex++
                                selectedOptionIndex = userAnswers[currentQuestionIndex]
                            } else {
                                // Finalize
                                isQuizCompleted = true
                                var score = 0
                                questions.forEachIndexed { i, q ->
                                    if (userAnswers[i] == q.correctAnswerIndex) score++
                                }
                                onSaveResult(resource.id, resource.title, score, questions.size, if (isExamMode) "exam" else "practice")
                            }
                        },
                        enabled = selectedOptionIndex != null,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Text(if (currentQuestionIndex < questions.size - 1) "Next Question" else "Submit Exam")
                    }
                }
            } else {
                // Quiz Completed / Score Summary View
                var score = 0
                questions.forEachIndexed { i, q ->
                    if (userAnswers[i] == q.correctAnswerIndex) score++
                }
                val total = questions.size
                val percentage = if (total > 0) (score * 100) / total else 0
                val isPassed = percentage >= 75

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isPassed) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)),
                            border = BorderStroke(1.5.dp, if (isPassed) ClinicalAlertGreen else ClinicalAlertRed)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isPassed) "EXAMINATION PASSED" else "NEEDS REMEDIATION",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isPassed) ClinicalAlertGreen else ClinicalAlertRed,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "$percentage%",
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isPassed) Color(0xFF065F46) else Color(0xFF991B1B)
                                )
                                Text(
                                    text = "Score: $score of $total Correct (${if (isExamMode) "Formal Exam" else "Practice Mode"})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isPassed) "Meets GNCZ / NMCZ / NCLEX clinical competency threshold (≥ 75%)."
                                    else "Review clinical rationale rationales below and re-study module competencies.",
                                    fontSize = 12.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item {
                        Text("Detailed Question Breakdown", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    items(questions.size) { qIndex ->
                        val q = questions[qIndex]
                        val userAns = userAnswers[qIndex]
                        val isUserCorrect = userAns == q.correctAnswerIndex

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            border = BorderStroke(1.dp, if (isUserCorrect) ClinicalAlertGreen else ClinicalAlertRed)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Question ${qIndex + 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(
                                        if (isUserCorrect) "✓ Correct" else "✗ Incorrect",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        color = if (isUserCorrect) ClinicalAlertGreen else ClinicalAlertRed
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(q.question, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Rationale: ${q.explanation}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            currentQuestionIndex = 0
                            selectedOptionIndex = null
                            userAnswers.clear()
                            isQuizCompleted = false
                        },
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Retake Quiz")
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Text("Finish")
                    }
                }
            }
        }
    }
}
