package com.mdavis8403.magickingdomtrivia.ui

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.Button
import androidx.tv.material3.Border
import androidx.tv.material3.Glow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.OutlinedButtonDefaults
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

    // The home screen is a full-bleed image concept, so it renders edge-to-edge.
    // Every other screen keeps the padded MagicalBackdrop it was designed for.
    Box(modifier = Modifier.fillMaxSize()) {
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

                else -> MagicalBackdrop {
                    when (destination) {
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

                        TriviaScreen.HOME -> Unit
                    }
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

/**
 * Fractional bounds (0..1) of an interactive region within the 16:9 design
 * canvas, refined visually against the approved home-screen artwork. Positions
 * are proportional so the overlay stays aligned at every 16:9 resolution.
 */
private data class HomeRegion(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val cornerPercent: Int,
)

private object HomeLayout {
    val Play = HomeRegion(0.155f, 0.420f, 0.455f, 0.548f, 42)
    val Matt = HomeRegion(0.086f, 0.610f, 0.188f, 0.808f, 16)
    val Meg = HomeRegion(0.198f, 0.610f, 0.300f, 0.808f, 16)
    val Mia = HomeRegion(0.305f, 0.610f, 0.405f, 0.808f, 16)
    val Guest = HomeRegion(0.415f, 0.610f, 0.515f, 0.808f, 16)
    val Profiles = HomeRegion(0.080f, 0.840f, 0.285f, 0.918f, 50)
    val Categories = HomeRegion(0.295f, 0.840f, 0.495f, 0.918f, 50)
    val Statistics = HomeRegion(0.505f, 0.840f, 0.700f, 0.918f, 50)
    val Settings = HomeRegion(0.710f, 0.840f, 0.930f, 0.918f, 50)
}

/**
 * Image-based interactive home screen. The approved artwork is the exact
 * full-screen background; transparent Compose focus targets are overlaid on top
 * of the baked-in controls and mapped to the existing navigation and game
 * actions. Nothing is redrawn in Compose beyond focus/selection treatments.
 */
@Composable
private fun HomeScreen(
    state: TriviaGameState,
    onPlay: () -> Unit,
    onNavigate: (TriviaScreen) -> Unit,
) {
    val profileNames = listOf("Matt", "Meg", "Mia", "Guest")
    // Local selected-profile state. The artwork shows Matt selected by default;
    // this hook lets the selection move to Meg/Mia/Guest without new artwork and
    // is where a real profile system would plug in later.
    var selectedProfile by rememberSaveable { mutableIntStateOf(0) }

    val playFocus = remember { FocusRequester() }
    val profileFocus = remember { List(4) { FocusRequester() } }
    val bottomFocus = remember { List(4) { FocusRequester() } }
    LaunchedEffect(Unit) { requestFocusAfterFrame(playFocus) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                // Fallback + letterbox fill if the image cannot decode or the
                // screen is not exactly 16:9.
                Brush.linearGradient(colors = listOf(Ink, Color(0xFF102A4B), Color(0xFF23113C))),
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Lock artwork and overlay controls to a shared 16:9 canvas so the
        // interactive regions align at 1920x1080, 3840x2160, and any 16:9
        // resolution. Only non-16:9 screens letterbox onto the gradient above.
        val target = 16f / 9f
        val screenRatio = maxWidth.value / maxHeight.value
        val canvasW: Dp = if (screenRatio >= target) maxHeight * target else maxWidth
        val canvasH: Dp = if (screenRatio >= target) maxHeight else maxWidth / target

        Box(modifier = Modifier.size(canvasW, canvasH)) {
            Image(
                painter = painterResource(R.drawable.home_screen_background),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
            )

            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = Rose,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = canvasH * 0.02f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xCC0A1220))
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                )
            }

            HomeOverlayControl(
                region = HomeLayout.Play,
                canvasW = canvasW,
                canvasH = canvasH,
                contentLabel = if (state.session == null) "Play" else "Resume game",
                onClick = onPlay,
                modifier = Modifier
                    .focusRequester(playFocus)
                    .focusProperties {
                        up = playFocus
                        down = profileFocus[0]
                        left = playFocus
                        right = playFocus
                    },
            )

            val profileRegions = listOf(HomeLayout.Matt, HomeLayout.Meg, HomeLayout.Mia, HomeLayout.Guest)
            // Down from each profile goes to the nearest bottom action.
            val profileDown = listOf(bottomFocus[0], bottomFocus[0], bottomFocus[1], bottomFocus[1])
            profileNames.forEachIndexed { index, name ->
                HomeOverlayControl(
                    region = profileRegions[index],
                    canvasW = canvasW,
                    canvasH = canvasH,
                    contentLabel = "$name profile",
                    onClick = { selectedProfile = index },
                    selected = selectedProfile == index,
                    isProfile = true,
                    modifier = Modifier
                        .focusRequester(profileFocus[index])
                        .focusProperties {
                            up = playFocus
                            down = profileDown[index]
                            left = profileFocus[(index - 1).coerceAtLeast(0)]
                            right = profileFocus[(index + 1).coerceAtMost(3)]
                        },
                )
            }

            val bottomRegions = listOf(HomeLayout.Profiles, HomeLayout.Categories, HomeLayout.Statistics, HomeLayout.Settings)
            val bottomLabels = listOf("Profiles", "Categories", "Statistics", "Settings")
            // Up from each bottom action returns to the nearest profile card.
            val bottomUp = listOf(profileFocus[0], profileFocus[2], profileFocus[3], profileFocus[3])
            val bottomActions = listOf<() -> Unit>(
                // No dedicated profiles screen exists yet; the Profiles action moves
                // focus to the profile chooser as a functional placeholder.
                { profileFocus[selectedProfile].requestFocus() },
                { onNavigate(TriviaScreen.CATEGORIES) },
                { onNavigate(TriviaScreen.STATISTICS) },
                { onNavigate(TriviaScreen.SETTINGS) },
            )
            bottomLabels.forEachIndexed { index, label ->
                HomeOverlayControl(
                    region = bottomRegions[index],
                    canvasW = canvasW,
                    canvasH = canvasH,
                    contentLabel = label,
                    onClick = bottomActions[index],
                    modifier = Modifier
                        .focusRequester(bottomFocus[index])
                        .focusProperties {
                            up = bottomUp[index]
                            down = bottomFocus[index]
                            left = bottomFocus[(index - 1).coerceAtLeast(0)]
                            right = bottomFocus[(index + 1).coerceAtMost(3)]
                        },
                )
            }
        }
    }
}

/**
 * A transparent, focusable overlay target sized to a [HomeRegion] on the 16:9
 * canvas. It never paints over the baked-in artwork except for a tasteful focus
 * treatment (soft gold glow, gold border, slight scale, faint gold wash) and a
 * steady gold selection ring for the active profile.
 */
@Composable
private fun HomeOverlayControl(
    region: HomeRegion,
    canvasW: Dp,
    canvasH: Dp,
    contentLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    isProfile: Boolean = false,
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(region.cornerPercent)
    val scale = if (focused) 1.03f else 1f

    Box(
        modifier = modifier
            .offset(x = canvasW * region.left, y = canvasH * region.top)
            .size(
                width = canvasW * (region.right - region.left),
                height = canvasH * (region.bottom - region.top),
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .onFocusChanged { focused = it.isFocused }
            .shadow(
                elevation = if (focused) 18.dp else 0.dp,
                shape = shape,
                clip = false,
                ambientColor = Gold,
                spotColor = Gold,
            )
            .clip(shape)
            .then(if (focused) Modifier.background(Gold.copy(alpha = 0.10f)) else Modifier)
            .then(
                when {
                    focused -> Modifier.border(width = 3.5.dp, color = Gold, shape = shape)
                    selected -> Modifier.border(width = 2.5.dp, color = Gold.copy(alpha = 0.85f), shape = shape)
                    else -> Modifier
                },
            )
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = contentLabel
                if (isProfile) this.selected = selected
            },
    ) {
        if (isProfile && selected) {
            // Steady selection badge, distinct from the focus treatment and
            // visible even when another control currently holds focus.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(canvasW * 0.007f)
                    .size(canvasW * 0.014f)
                    .drawBehind {
                        drawRoundRect(
                            color = Gold,
                            cornerRadius = CornerRadius(size.minDimension / 2f),
                        )
                    },
            )
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
                    AnswerCard(
                        text = question.choices[index].text,
                        status = answerCardStatus(index, session),
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

internal enum class AnswerCardStatus {
    IDLE,
    SELECTED,
    SELECTED_CORRECT,
    SELECTED_INCORRECT,
    REVEALED_CORRECT,
    DISABLED,
}

internal data class AnswerCardStyle(
    val containerColor: Color,
    val textColor: Color,
    val borderColor: Color,
    val indicator: String?,
)

internal val AnswerCardStateKey = SemanticsPropertyKey<String>("AnswerCardState")
internal val AnswerCardContainerColorKey = SemanticsPropertyKey<String>("AnswerCardContainerColor")
internal val AnswerCardTextColorKey = SemanticsPropertyKey<String>("AnswerCardTextColor")
internal val AnswerCardBorderColorKey = SemanticsPropertyKey<String>("AnswerCardBorderColor")

private var SemanticsPropertyReceiver.answerCardState by AnswerCardStateKey
private var SemanticsPropertyReceiver.answerCardContainerColor by AnswerCardContainerColorKey
private var SemanticsPropertyReceiver.answerCardTextColor by AnswerCardTextColorKey
private var SemanticsPropertyReceiver.answerCardBorderColor by AnswerCardBorderColorKey

private val AnswerIdleContainer = Color(0xFF17263D)
private val AnswerIdleText = Color(0xFFF4F7FC)
private val AnswerIdleBorder = Color(0xFF617594)
private val AnswerFocusedContainer = Color(0xFF214F73)
private val AnswerFocusedText = Color(0xFFFFFFFF)
private val AnswerSelectedContainer = Color(0xFF39375F)
private val AnswerSelectedText = Color(0xFFFFF1C7)
private val AnswerCorrectContainer = Color(0xFF174A3A)
private val AnswerCorrectText = Color(0xFFE0FFF0)
private val AnswerIncorrectContainer = Color(0xFF542936)
private val AnswerIncorrectText = Color(0xFFFFE4E9)
private val AnswerDisabledContainer = Color(0xFF111D2D)
private val AnswerDisabledText = Color(0xFFB4C1D3)
private val AnswerDisabledBorder = Color(0xFF3D506A)

internal fun answerCardStyle(
    status: AnswerCardStatus,
    focused: Boolean,
): AnswerCardStyle {
    if (focused && (status == AnswerCardStatus.IDLE || status == AnswerCardStatus.SELECTED)) {
        return AnswerCardStyle(
            containerColor = AnswerFocusedContainer,
            textColor = AnswerFocusedText,
            borderColor = Gold,
            indicator = if (status == AnswerCardStatus.SELECTED) "SELECTED" else null,
        )
    }

    return when (status) {
        AnswerCardStatus.IDLE -> AnswerCardStyle(
            containerColor = AnswerIdleContainer,
            textColor = AnswerIdleText,
            borderColor = AnswerIdleBorder,
            indicator = null,
        )

        AnswerCardStatus.SELECTED -> AnswerCardStyle(
            containerColor = AnswerSelectedContainer,
            textColor = AnswerSelectedText,
            borderColor = Gold,
            indicator = "SELECTED",
        )

        AnswerCardStatus.SELECTED_CORRECT -> AnswerCardStyle(
            containerColor = AnswerCorrectContainer,
            textColor = AnswerCorrectText,
            borderColor = Mint,
            indicator = "YOUR ANSWER - CORRECT",
        )

        AnswerCardStatus.SELECTED_INCORRECT -> AnswerCardStyle(
            containerColor = AnswerIncorrectContainer,
            textColor = AnswerIncorrectText,
            borderColor = Rose,
            indicator = "YOUR ANSWER - INCORRECT",
        )

        AnswerCardStatus.REVEALED_CORRECT -> AnswerCardStyle(
            containerColor = AnswerCorrectContainer,
            textColor = AnswerCorrectText,
            borderColor = Mint,
            indicator = "CORRECT ANSWER",
        )

        AnswerCardStatus.DISABLED -> AnswerCardStyle(
            containerColor = AnswerDisabledContainer,
            textColor = AnswerDisabledText,
            borderColor = AnswerDisabledBorder,
            indicator = null,
        )
    }
}

private fun answerCardStatus(index: Int, session: TriviaSession): AnswerCardStatus {
    val isCorrect = index == session.correctAnswerIndex
    val isSelected = index == session.selectedAnswerIndex
    return when {
        !session.answerRevealed && isSelected -> AnswerCardStatus.SELECTED
        !session.answerRevealed -> AnswerCardStatus.IDLE
        isCorrect && isSelected -> AnswerCardStatus.SELECTED_CORRECT
        isSelected -> AnswerCardStatus.SELECTED_INCORRECT
        isCorrect -> AnswerCardStatus.REVEALED_CORRECT
        else -> AnswerCardStatus.DISABLED
    }
}

@Composable
internal fun AnswerCard(
    text: String,
    status: AnswerCardStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val enabled = status == AnswerCardStatus.IDLE || status == AnswerCardStatus.SELECTED
    val style = answerCardStyle(status = status, focused = focused)
    val shape = RoundedCornerShape(20.dp)

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        colors = OutlinedButtonDefaults.colors(
            containerColor = style.containerColor,
            contentColor = style.textColor,
            focusedContainerColor = AnswerFocusedContainer,
            focusedContentColor = AnswerFocusedText,
            pressedContainerColor = AnswerSelectedContainer,
            pressedContentColor = AnswerSelectedText,
            disabledContainerColor = style.containerColor,
            disabledContentColor = style.textColor,
        ),
        border = OutlinedButtonDefaults.border(
            border = Border(BorderStroke(2.dp, style.borderColor), shape = shape),
            focusedBorder = Border(BorderStroke(4.dp, Gold), shape = shape),
            pressedBorder = Border(BorderStroke(4.dp, Gold), shape = shape),
            disabledBorder = Border(BorderStroke(2.dp, style.borderColor), shape = shape),
            focusedDisabledBorder = Border(BorderStroke(3.dp, style.borderColor), shape = shape),
        ),
        glow = OutlinedButtonDefaults.glow(
            glow = Glow.None,
            focusedGlow = Glow(elevationColor = Gold.copy(alpha = 0.45f), elevation = 12.dp),
            pressedGlow = Glow(elevationColor = Gold.copy(alpha = 0.3f), elevation = 8.dp),
        ),
        modifier = modifier
            .heightIn(min = 92.dp)
            .onFocusChanged { focused = it.isFocused }
            .semantics {
                answerCardState = if (focused && status == AnswerCardStatus.IDLE) "FOCUSED" else status.name
                answerCardContainerColor = style.containerColor.toHexString()
                answerCardTextColor = style.textColor.toHexString()
                answerCardBorderColor = style.borderColor.toHexString()
                if (!enabled) disabled()
            }
            .graphicsLayer {
                val scale = if (focused) 1.04f else 1f
                scaleX = scale
                scaleY = scale
            },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            style.indicator?.let { indicator ->
                Text(
                    text = indicator,
                    color = style.textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                )
            }
            Text(
                text = text,
                color = style.textColor,
                fontSize = 22.sp,
                fontWeight = if (style.indicator == null) FontWeight.Medium else FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun Color.toHexString(): String = "#%08X".format(toArgb())

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
