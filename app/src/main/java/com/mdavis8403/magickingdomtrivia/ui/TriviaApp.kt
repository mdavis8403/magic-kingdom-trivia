package com.mdavis8403.magickingdomtrivia.ui

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.mdavis8403.magickingdomtrivia.R
import com.mdavis8403.magickingdomtrivia.data.Difficulty
import com.mdavis8403.magickingdomtrivia.data.TriviaCategory
import com.mdavis8403.magickingdomtrivia.domain.GameSettings
import com.mdavis8403.magickingdomtrivia.domain.PerformanceStatistics
import com.mdavis8403.magickingdomtrivia.domain.TriviaGameState
import com.mdavis8403.magickingdomtrivia.domain.TriviaSession
import com.mdavis8403.magickingdomtrivia.domain.TriviaStatistics
import com.mdavis8403.magickingdomtrivia.domain.TriviaSummary
import kotlinx.coroutines.delay

private val Gold = Color(0xFFF5C86A)
private val Mint = Color(0xFF79E6BE)
private val Rose = Color(0xFFFF8E9E)
private val Ink = Color(0xFF07111F)
private val Panel = Color(0xE6192941)
private val PanelSoft = Color(0xB3233150)

@Composable
fun TriviaApp(viewModel: TriviaViewModel) {
    val state = viewModel.uiState
    val screen = viewModel.currentScreen

    BackHandler(enabled = screen != TriviaScreen.HOME) { viewModel.handleBack() }

    MagicalBackdrop {
        AnimatedContent(
            targetState = screen,
            label = "screen-transition",
        ) { destination ->
            when (destination) {
                TriviaScreen.HOME -> HomeScreen(
                    state = state,
                    onPlay = viewModel::startGame,
                    onNavigate = viewModel::navigateTo,
                )

                TriviaScreen.CATEGORIES -> CategoryScreen(
                    categories = state.categories,
                    selectedCategoryId = state.selectedCategoryId,
                    onSelect = viewModel::selectCategory,
                )

                TriviaScreen.DIFFICULTY -> DifficultyScreen(
                    selectedDifficulty = state.selectedDifficulty,
                    onSelect = viewModel::selectDifficulty,
                )

                TriviaScreen.SETTINGS -> SettingsScreen(
                    settings = state.settings,
                    onUpdate = viewModel::updateSettings,
                )

                TriviaScreen.STATISTICS -> StatisticsScreen(
                    statistics = viewModel.statistics,
                    onReturnHome = { viewModel.navigateTo(TriviaScreen.HOME) },
                    onReset = viewModel::resetStatistics,
                )

                TriviaScreen.QUESTION -> state.session?.let { session ->
                    QuestionScreen(
                        session = session,
                        onSubmit = viewModel::submitAnswer,
                        onNext = viewModel::next,
                        onTick = viewModel::tickTimer,
                    )
                }

                TriviaScreen.RESULTS -> state.summary?.let { summary ->
                    ResultsScreen(
                        summary = summary,
                        onPlayAgain = viewModel::playAgain,
                        onReturnHome = viewModel::returnHome,
                    )
                }
            }
        }

        state.startNotice?.let { notice ->
            ConfirmationDialog(
                title = "Fewer questions available",
                message = "This setup has ${notice.availableCount} eligible questions instead of ${notice.requestedCount}. Play all ${notice.availableCount}?",
                confirmLabel = "Play ${notice.availableCount}",
                onConfirm = viewModel::confirmStart,
                onDismiss = viewModel::cancelStart,
            )
        }
    }
}

@Composable
private fun MagicalBackdrop(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Ink, Color(0xFF102A4B), Color(0xFF321848)),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 44.dp, end = 90.dp)
                .size(220.dp)
                .background(Color(0x1FF5C86A), CircleShape),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 36.dp, bottom = 28.dp)
                .size(150.dp)
                .background(Color(0x1829D3C2), CircleShape),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 56.dp, vertical = 38.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun HomeScreen(
    state: TriviaGameState,
    onPlay: () -> Unit,
    onNavigate: (TriviaScreen) -> Unit,
) {
    val playFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { requestFocusAfterFrame(playFocus) }
    val selectedCategory = state.categories.firstOrNull { it.id == state.selectedCategoryId }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(42.dp),
    ) {
        Column(
            modifier = Modifier
                .width(360.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 46.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Gold,
            )
            Text(
                text = "A family trivia night, made for the big screen.",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 30.dp),
            )
            HomeAction(
                label = if (state.session == null) "Play" else "Resume game",
                onClick = onPlay,
                modifier = Modifier.focusRequester(playFocus),
                primary = true,
            )
            HomeAction("Categories", { onNavigate(TriviaScreen.CATEGORIES) })
            HomeAction("Difficulty", { onNavigate(TriviaScreen.DIFFICULTY) })
            HomeAction("Game settings", { onNavigate(TriviaScreen.SETTINGS) })
            HomeAction("Statistics", { onNavigate(TriviaScreen.STATISTICS) })
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                colors = SurfaceDefaults.colors(containerColor = Panel),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(34.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp),
                ) {
                    Text("Tonight's game", color = Gold, style = MaterialTheme.typography.titleLarge)
                    SetupLine("Category", selectedCategory?.title ?: "Unavailable")
                    SetupLine("Difficulty", state.selectedDifficulty.displayName)
                    SetupLine("Questions", state.settings.questionCount.toString())
                    SetupLine("Timer", timerLabel(state.settings.timerSeconds))
                    Text(
                        text = if (state.session == null) {
                            "Choose your options, then press Play. Everything runs offline."
                        } else {
                            "Your round is paused and ready to continue."
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFCFD9E8),
                    )
                }
            }
            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = Rose,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 18.dp),
                )
            }
        }
    }
}

@Composable
private fun HomeAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
) {
    val buttonModifier = modifier
        .fillMaxWidth()
        .height(60.dp)
        .padding(bottom = 8.dp)
    if (primary) {
        Button(onClick = onClick, modifier = buttonModifier) {
            Text(label, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        }
    } else {
        OutlinedButton(onClick = onClick, modifier = buttonModifier) {
            Text(label, fontSize = 20.sp)
        }
    }
}

@Composable
private fun SetupLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color(0xFFAEC0D8), fontSize = 21.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 22.sp, textAlign = TextAlign.End)
    }
}

@Composable
private fun CategoryScreen(
    categories: List<TriviaCategory>,
    selectedCategoryId: String,
    onSelect: (String) -> Unit,
) {
    val selectedFocus = remember { FocusRequester() }
    LaunchedEffect(selectedCategoryId) { requestFocusAfterFrame(selectedFocus) }

    ScreenColumn(
        title = "Choose category",
        subtitle = "Pick one world, or choose Mixed for the full challenge.",
    ) {
        categories.chunked(3).forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                rowCategories.forEach { category ->
                    val selected = category.id == selectedCategoryId
                    SelectionButton(
                        label = category.title,
                        supporting = if (selected) "Selected" else category.description,
                        selected = selected,
                        onClick = { onSelect(category.id) },
                        modifier = Modifier
                            .weight(1f)
                            .then(if (selected) Modifier.focusRequester(selectedFocus) else Modifier),
                    )
                }
                repeat(3 - rowCategories.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun DifficultyScreen(
    selectedDifficulty: Difficulty,
    onSelect: (Difficulty) -> Unit,
) {
    val selectedFocus = remember { FocusRequester() }
    LaunchedEffect(selectedDifficulty) { requestFocusAfterFrame(selectedFocus) }
    val descriptions = mapOf(
        Difficulty.EASY to "Welcoming questions for the whole family",
        Difficulty.MEDIUM to "A balanced challenge with deeper details",
        Difficulty.HARD to "Tough questions for seasoned fans",
        Difficulty.MIXED to "A blend of every difficulty level",
    )

    ScreenColumn("Choose difficulty", "You can change this before any new round.") {
        Difficulty.entries.forEach { difficulty ->
            val selected = difficulty == selectedDifficulty
            SelectionButton(
                label = difficulty.displayName,
                supporting = descriptions.getValue(difficulty),
                selected = selected,
                onClick = { onSelect(difficulty) },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (selected) Modifier.focusRequester(selectedFocus) else Modifier),
            )
        }
    }
}

@Composable
private fun SettingsScreen(
    settings: GameSettings,
    onUpdate: (GameSettings) -> Unit,
) {
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { requestFocusAfterFrame(firstFocus) }

    ScreenColumn(
        title = "Game settings",
        subtitle = "Tune the pace and presentation for your family.",
        scrollable = true,
    ) {
        SettingsHeading("Question count")
        ChoiceRow(
            values = GameSettings.VALID_QUESTION_COUNTS,
            selected = settings.questionCount,
            label = Int::toString,
            onSelect = { onUpdate(settings.copy(questionCount = it)) },
            modifier = Modifier.focusRequester(firstFocus),
        )

        SettingsHeading("Timer")
        ChoiceRow(
            values = GameSettings.VALID_TIMER_SECONDS,
            selected = settings.timerSeconds,
            label = ::timerLabel,
            onSelect = { onUpdate(settings.copy(timerSeconds = it)) },
        )

        SettingsHeading("Round options")
        ToggleSetting("Randomize question order", settings.randomizeQuestionOrder) {
            onUpdate(settings.copy(randomizeQuestionOrder = !settings.randomizeQuestionOrder))
        }
        ToggleSetting("Randomize answer order", settings.randomizeAnswerOrder) {
            onUpdate(settings.copy(randomizeAnswerOrder = !settings.randomizeAnswerOrder))
        }
        ToggleSetting("Show explanations", settings.showExplanations) {
            onUpdate(settings.copy(showExplanations = !settings.showExplanations))
        }
        ToggleSetting("Automatically advance", settings.automaticallyAdvance) {
            onUpdate(settings.copy(automaticallyAdvance = !settings.automaticallyAdvance))
        }
        ToggleSetting("Avoid recently played questions", settings.avoidRecentlyPlayed) {
            onUpdate(settings.copy(avoidRecentlyPlayed = !settings.avoidRecentlyPlayed))
        }
        ToggleSetting("Sound effects", settings.soundEffects) {
            onUpdate(settings.copy(soundEffects = !settings.soundEffects))
        }
    }
}

@Composable
private fun <T> ChoiceRow(
    values: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        values.forEachIndexed { index, value ->
            SelectionButton(
                label = label(value),
                supporting = if (value == selected) "Selected" else "",
                selected = value == selected,
                onClick = { onSelect(value) },
                modifier = Modifier
                    .weight(1f)
                    .then(if (index == 0) modifier else Modifier),
            )
        }
    }
}

@Composable
private fun ToggleSetting(label: String, enabled: Boolean, onToggle: () -> Unit) {
    OutlinedButton(
        onClick = onToggle,
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .semantics { selected = enabled },
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 19.sp)
            Text(if (enabled) "On" else "Off", color = if (enabled) Mint else Color(0xFFB8C3D3), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SettingsHeading(text: String) {
    Text(
        text = text,
        color = Gold,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 10.dp),
    )
}

@Composable
private fun SelectionButton(
    label: String,
    supporting: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 82.dp)
            .semantics { this.selected = selected },
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (selected) "* $label" else label,
                fontSize = 20.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Gold else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            if (supporting.isNotBlank()) {
                Text(
                    text = supporting,
                    fontSize = 13.sp,
                    color = Color(0xFFBBC7D8),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
        }
    }
}

@Composable
private fun StatisticsScreen(
    statistics: TriviaStatistics,
    onReturnHome: () -> Unit,
    onReset: () -> Unit,
) {
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    val homeFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { requestFocusAfterFrame(homeFocus) }

    ScreenColumn(
        title = "Question statistics",
        subtitle = "Progress is stored only on this device.",
        scrollable = true,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Button(onClick = onReturnHome, modifier = Modifier.focusRequester(homeFocus)) { Text("Back to home") }
            OutlinedButton(onClick = { showResetDialog = true }) { Text("Reset statistics") }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatisticCard("Games played", statistics.gamesPlayed.toString(), Modifier.weight(1f))
            StatisticCard("Questions", statistics.questionsAnswered.toString(), Modifier.weight(1f))
            StatisticCard("Correct", statistics.correctAnswers.toString(), Modifier.weight(1f))
            StatisticCard("Accuracy", "${statistics.accuracyPercent}%", Modifier.weight(1f))
        }

        if (statistics.questionsAnswered == 0) {
            Surface(colors = SurfaceDefaults.colors(containerColor = PanelSoft), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Complete a round to begin building your family trivia history.",
                    modifier = Modifier.padding(28.dp),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        } else {
            PerformanceSection("By category", statistics.categoryPerformance)
            PerformanceSection(
                title = "By difficulty",
                values = statistics.difficultyPerformance.mapKeys { it.key.displayName },
            )
            SettingsHeading("High scores")
            statistics.highScoresByGameLength.toSortedMap().forEach { (length, score) ->
                SetupLine("$length questions", "$score / $length")
            }
        }
    }

    if (showResetDialog) {
        ConfirmationDialog(
            title = "Reset all statistics?",
            message = "Games, accuracy, high scores, and recently played history will be removed from this Shield.",
            confirmLabel = "Reset",
            onConfirm = {
                onReset()
                showResetDialog = false
            },
            onDismiss = { showResetDialog = false },
            destructive = true,
        )
    }
}

@Composable
private fun StatisticCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(colors = SurfaceDefaults.colors(containerColor = PanelSoft), shape = RoundedCornerShape(22.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Gold)
            Text(label, color = Color(0xFFBCC8D8), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun PerformanceSection(title: String, values: Map<String, PerformanceStatistics>) {
    SettingsHeading(title)
    values.toSortedMap().forEach { (label, performance) ->
        SetupLine(label, "${performance.correct}/${performance.answered}  ${performance.accuracyPercent}%")
    }
}

@Composable
private fun QuestionScreen(
    session: TriviaSession,
    onSubmit: (Int?) -> Unit,
    onNext: () -> Unit,
    onTick: () -> Unit,
) {
    val question = session.currentQuestion
    val answerFocus = remember(question.id) { FocusRequester() }
    val nextFocus = remember(question.id) { FocusRequester() }
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 55) }
    DisposableEffect(Unit) { onDispose { toneGenerator.release() } }

    LaunchedEffect(question.id, session.answerRevealed) {
        requestFocusAfterFrame(if (session.answerRevealed) nextFocus else answerFocus)
    }
    LaunchedEffect(question.id, session.secondsRemaining, session.answerRevealed) {
        if (session.settings.timerSeconds > 0 && !session.answerRevealed) {
            delay(1_000)
            onTick()
        }
    }
    LaunchedEffect(question.id, session.answerRevealed) {
        if (session.answerRevealed && session.settings.soundEffects) {
            val correct = session.selectedAnswerIndex == session.correctAnswerIndex
            toneGenerator.startTone(
                if (correct) ToneGenerator.TONE_PROP_ACK else ToneGenerator.TONE_PROP_NACK,
                180,
            )
        }
        if (session.answerRevealed && session.settings.automaticallyAdvance) {
            delay(3_000)
            onNext()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "QUESTION ${session.currentIndex + 1} OF ${session.totalQuestions}",
                    color = Gold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
                Text(
                    "${question.category}  /  ${question.difficulty.displayName}",
                    color = Color(0xFFBCC8D8),
                    fontSize = 16.sp,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                if (session.settings.timerSeconds > 0) {
                    HeaderPill("TIME", if (session.answerRevealed) "--" else "${session.secondsRemaining}s")
                }
                HeaderPill("SCORE", session.score.toString())
            }
        }

        Surface(colors = SurfaceDefaults.colors(containerColor = Panel), shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
            Text(
                text = question.prompt,
                modifier = Modifier.padding(horizontal = 30.dp, vertical = 24.dp),
                fontSize = 31.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }

        question.choices.indices.chunked(2).forEach { rowIndices ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                rowIndices.forEach { index ->
                    AnswerButton(
                        text = question.choices[index].text,
                        index = index,
                        session = session,
                        onClick = { onSubmit(index) },
                        modifier = Modifier
                            .weight(1f)
                            .then(if (index == 0) Modifier.focusRequester(answerFocus) else Modifier),
                    )
                }
            }
        }

        if (session.answerRevealed) {
            val correct = session.selectedAnswerIndex == session.correctAnswerIndex
            Surface(
                colors = SurfaceDefaults.colors(
                    containerColor = if (correct) Color(0xD018493C) else Color(0xD04A2230),
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(22.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = when {
                            session.timedOut -> "TIME'S UP"
                            correct -> "CORRECT"
                            else -> "INCORRECT"
                        },
                        color = if (correct) Mint else Rose,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 23.sp,
                    )
                    if (session.settings.showExplanations) {
                        Text(question.explanation, modifier = Modifier.weight(1f), fontSize = 18.sp)
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                    Button(
                        onClick = onNext,
                        modifier = Modifier.focusRequester(nextFocus),
                    ) {
                        Text(
                            if (session.settings.automaticallyAdvance) "Continue now" else if (session.currentIndex == session.questions.lastIndex) "See results" else "Continue",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderPill(label: String, value: String) {
    Surface(colors = SurfaceDefaults.colors(containerColor = PanelSoft), shape = RoundedCornerShape(18.dp)) {
        Row(modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(label, color = Color(0xFFB8C4D4), fontSize = 15.sp)
            Text(value, color = Gold, fontWeight = FontWeight.Bold, fontSize = 19.sp)
        }
    }
}

@Composable
private fun AnswerButton(
    text: String,
    index: Int,
    session: TriviaSession,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val isCorrect = index == session.correctAnswerIndex
    val isSelected = index == session.selectedAnswerIndex
    val revealed = session.answerRevealed
    val label = when {
        revealed && isCorrect -> "Correct answer: $text"
        revealed && isSelected -> "Incorrect choice: $text"
        else -> text
    }
    val labelColor by animateColorAsState(
        targetValue = when {
            revealed && isCorrect -> Mint
            revealed && isSelected -> Rose
            else -> MaterialTheme.colorScheme.onSurface
        },
        label = "answer-color",
    )

    OutlinedButton(
        onClick = onClick,
        enabled = !revealed,
        modifier = modifier
            .heightIn(min = 92.dp)
            .onFocusChanged { focused = it.isFocused }
            .graphicsLayer {
                val scale = if (focused) 1.035f else 1f
                scaleX = scale
                scaleY = scale
            },
    ) {
        Text(
            text = label,
            color = labelColor,
            fontSize = 22.sp,
            fontWeight = if (revealed && (isCorrect || isSelected)) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ResultsScreen(
    summary: TriviaSummary,
    onPlayAgain: () -> Unit,
    onReturnHome: () -> Unit,
) {
    val playAgainFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { requestFocusAfterFrame(playAgainFocus) }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(colors = SurfaceDefaults.colors(containerColor = Panel), shape = RoundedCornerShape(32.dp), modifier = Modifier.weight(1.2f)) {
            Column(
                modifier = Modifier.padding(36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("ROUND COMPLETE", color = Gold, fontWeight = FontWeight.Bold)
                Text(summary.resultMessage, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                Text("${summary.correctAnswers} / ${summary.totalQuestions}", fontSize = 58.sp, fontWeight = FontWeight.ExtraBold, color = Mint)
                Text("${summary.accuracyPercent}% correct", style = MaterialTheme.typography.titleLarge)
                Text("${summary.incorrectAnswers} incorrect  /  Best streak ${summary.bestStreak}", color = Color(0xFFCBD5E3))
            }
        }
        Column(
            modifier = Modifier.weight(0.8f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(colors = SurfaceDefaults.colors(containerColor = PanelSoft), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SetupLine("Best category", summary.bestCategory)
                    SetupLine("Most difficult", summary.mostDifficultCategory)
                }
            }
            Button(
                onClick = onPlayAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .focusRequester(playAgainFocus),
            ) { Text("Play again", fontSize = 20.sp) }
            OutlinedButton(
                onClick = onReturnHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp),
            ) { Text("Return home", fontSize = 20.sp) }
        }
    }
}

@Composable
private fun ScreenColumn(
    title: String,
    subtitle: String,
    scrollable: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollModifier = if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier
    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(scrollModifier),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(title, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold, color = Gold)
        Text(subtitle, style = MaterialTheme.typography.titleMedium, color = Color(0xFFCBD5E3))
        Spacer(Modifier.height(4.dp))
        content()
        Spacer(Modifier.height(20.dp))
        Text("Press Back to return home", color = Color(0xFF9EADC2), fontSize = 14.sp)
    }
}

@Composable
private fun ConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = false,
) {
    val cancelFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { requestFocusAfterFrame(cancelFocus) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(colors = SurfaceDefaults.colors(containerColor = Color(0xFF17263D)), shape = RoundedCornerShape(28.dp), modifier = Modifier.width(560.dp)) {
            Column(modifier = Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = if (destructive) Rose else Gold)
                Text(message, fontSize = 19.sp, lineHeight = 27.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.align(Alignment.End)) {
                    Button(onClick = onDismiss, modifier = Modifier.focusRequester(cancelFocus)) { Text("Cancel") }
                    OutlinedButton(onClick = onConfirm) { Text(confirmLabel, color = if (destructive) Rose else Mint) }
                }
            }
        }
    }
}

private suspend fun requestFocusAfterFrame(requester: FocusRequester) {
    withFrameNanos { }
    requester.requestFocus()
}

private fun timerLabel(seconds: Int): String = if (seconds == 0) "Off" else "$seconds seconds"
