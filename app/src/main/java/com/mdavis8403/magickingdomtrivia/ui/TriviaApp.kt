package com.mdavis8403.magickingdomtrivia.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.mdavis8403.magickingdomtrivia.data.TriviaCategory
import com.mdavis8403.magickingdomtrivia.domain.TriviaGameState
import com.mdavis8403.magickingdomtrivia.domain.TriviaSession
import com.mdavis8403.magickingdomtrivia.domain.TriviaSummary

@Composable
fun TriviaApp(viewModel: TriviaViewModel) {
    val state = viewModel.uiState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF07101F),
                        Color(0xFF122A52),
                        Color(0xFF2C1451),
                    ),
                ),
            )
            .padding(horizontal = 48.dp, vertical = 32.dp),
    ) {
        AnimatedContent(
            targetState = screenFor(state),
            label = "screen-animation",
        ) { screen ->
            when (screen) {
                TriviaScreen.Home -> HomeScreen(
                    state = state,
                    onSelectCategory = viewModel::selectCategory,
                    onStart = viewModel::startGame,
                )

                TriviaScreen.Question -> QuestionScreen(
                    session = requireNotNull(state.session),
                    onSubmit = viewModel::submitAnswer,
                    onNext = viewModel::next,
                )

                TriviaScreen.Results -> ResultsScreen(
                    summary = requireNotNull(state.summary),
                    onPlayAgain = viewModel::playAgain,
                    onReturnHome = viewModel::returnHome,
                )
            }
        }
    }
}

private enum class TriviaScreen {
    Home,
    Question,
    Results,
}

private fun screenFor(state: TriviaGameState): TriviaScreen =
    when {
        state.session != null -> TriviaScreen.Question
        state.summary != null -> TriviaScreen.Results
        else -> TriviaScreen.Home
    }

@Composable
private fun HomeScreen(
    state: TriviaGameState,
    onSelectCategory: (String) -> Unit,
    onStart: () -> Unit,
) {
    val startFocusRequester = FocusRequester()

    LaunchedEffect(Unit) {
        startFocusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Magic Kingdom Trivia",
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = "A living-room-ready Disney challenge built for the TV remote. Pick a topic, settle in, and chase your best streak.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(0.78f),
            )
        }

        Surface(
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Tonight's round",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFF6C95E),
                    )
                    Text(
                        text = state.categories.first { it.id == state.selectedCategoryId }.title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = state.categories.first { it.id == state.selectedCategoryId }.description,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                Spacer(modifier = Modifier.width(24.dp))
                Button(
                    onClick = onStart,
                    modifier = Modifier.focusRequester(startFocusRequester),
                ) {
                    Text("Start 5-question round")
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "Choose a category",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                items(state.categories) { category ->
                    CategoryCard(
                        category = category,
                        isSelected = category.id == state.selectedCategoryId,
                        onClick = { onSelectCategory(category.id) },
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "What this app is optimized for",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                )
                Text(
                    text = "Large-type prompts, remote-first focus targets, quick answer reveals, and a recap screen that keeps the pace moving from the couch.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: TriviaCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(280.dp),
        border = CardDefaults.border(
            focusedBorder = Border(
                border = androidx.compose.foundation.BorderStroke(3.dp, Color(category.accentColor)),
                shape = RoundedCornerShape(24.dp),
            ),
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = category.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(category.accentColor) else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = category.description,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun QuestionScreen(
    session: TriviaSession,
    onSubmit: (Int) -> Unit,
    onNext: () -> Unit,
) {
    val question = session.currentQuestion
    val selectionLocked = session.selectedAnswerIndex != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = session.category.title,
                color = Color(session.category.accentColor),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "Question ${session.currentIndex + 1} of ${session.totalQuestions}  •  Score ${session.score}",
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Surface(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = difficultyLabel(question.difficulty),
                    color = Color(0xFFF6C95E),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = question.prompt,
                    fontSize = 34.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            question.choices.forEachIndexed { index, choice ->
                val isSelected = session.selectedAnswerIndex == index
                val isCorrect = session.correctAnswerIndex == index
                val revealLabel = when {
                    !selectionLocked -> null
                    isCorrect -> "Correct answer"
                    isSelected -> "Your choice"
                    else -> null
                }

                OutlinedButton(
                    onClick = { onSubmit(index) },
                    enabled = !selectionLocked,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = choice.text,
                            fontSize = 24.sp,
                            modifier = Modifier.weight(1f),
                        )
                        if (revealLabel != null) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = revealLabel,
                                color = when {
                                    isCorrect -> Color(0xFF8CF5B3)
                                    isSelected -> Color(0xFFFFA8A8)
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                            )
                        }
                    }
                }
            }
        }

        if (selectionLocked) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = if (session.selectedAnswerIndex == session.correctAnswerIndex) "Correct!" else "Not quite",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = question.explanation,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            Button(
                onClick = onNext,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(
                    if (session.currentIndex == session.questions.lastIndex) {
                        "See results"
                    } else {
                        "Next question"
                    },
                )
            }
        }
    }
}

@Composable
private fun ResultsScreen(
    summary: TriviaSummary,
    onPlayAgain: () -> Unit,
    onReturnHome: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "Round complete",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(summary.category.accentColor),
                )
                Text(
                    text = "${summary.correctAnswers} / ${summary.totalQuestions}",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = "${summary.accuracyPercent}% accuracy in ${summary.category.title}",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = "Best streak: ${summary.bestStreak}",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = onPlayAgain) {
                Text("Play again")
            }
            OutlinedButton(onClick = onReturnHome) {
                Text("Choose another category")
            }
        }

        Surface(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Next couch challenge",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Try another category to rotate from park map knowledge to attraction facts, or replay this deck to chase a perfect score.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

private fun difficultyLabel(difficulty: com.mdavis8403.magickingdomtrivia.data.Difficulty): String =
    when (difficulty) {
        com.mdavis8403.magickingdomtrivia.data.Difficulty.EASY -> "Warm-up"
        com.mdavis8403.magickingdomtrivia.data.Difficulty.MEDIUM -> "Challenge"
        com.mdavis8403.magickingdomtrivia.data.Difficulty.HARD -> "Deep cut"
    }
