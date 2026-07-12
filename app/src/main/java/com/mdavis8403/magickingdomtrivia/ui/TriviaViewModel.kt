package com.mdavis8403.magickingdomtrivia.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.mdavis8403.magickingdomtrivia.data.AssetQuestionRepository
import com.mdavis8403.magickingdomtrivia.data.Difficulty
import com.mdavis8403.magickingdomtrivia.data.SettingsRepository
import com.mdavis8403.magickingdomtrivia.data.StatisticsRepository
import com.mdavis8403.magickingdomtrivia.domain.GameSettings
import com.mdavis8403.magickingdomtrivia.domain.GameStateCodec
import com.mdavis8403.magickingdomtrivia.domain.StatisticsAccumulator
import com.mdavis8403.magickingdomtrivia.domain.TriviaGameEngine
import com.mdavis8403.magickingdomtrivia.domain.TriviaGameState
import com.mdavis8403.magickingdomtrivia.domain.TriviaStatistics
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TriviaViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {
    private val engine = TriviaGameEngine(AssetQuestionRepository(application))
    private val settingsRepository = SettingsRepository(application)
    private val statisticsRepository = StatisticsRepository(application)
    private val stateCodec = GameStateCodec()

    var uiState: TriviaGameState by mutableStateOf(
        stateCodec.decode(savedStateHandle[GAME_STATE_KEY]) ?: engine.initialState(),
    )
        private set

    var statistics: TriviaStatistics by mutableStateOf(TriviaStatistics())
        private set

    var currentScreen: TriviaScreen by mutableStateOf(
        when {
            uiState.session != null -> TriviaScreen.QUESTION
            uiState.summary != null -> TriviaScreen.RESULTS
            else -> TriviaScreen.HOME
        },
    )
        private set

    init {
        viewModelScope.launch {
            settingsRepository.settings.collectLatest { settings ->
                if (uiState.session == null && uiState.summary == null) {
                    setState(engine.updateSettings(uiState, settings))
                }
            }
        }
        viewModelScope.launch {
            statisticsRepository.statistics.collectLatest { statistics = it }
        }
    }

    fun selectCategory(categoryId: String) {
        updateAndPersistSettings(engine.selectCategory(uiState, categoryId))
    }

    fun selectDifficulty(difficulty: Difficulty) {
        updateAndPersistSettings(engine.selectDifficulty(uiState, difficulty))
    }

    fun updateSettings(settings: GameSettings) {
        updateAndPersistSettings(engine.updateSettings(uiState, settings))
    }

    fun startGame() {
        if (uiState.session != null) {
            currentScreen = TriviaScreen.QUESTION
            return
        }
        setState(engine.requestStart(uiState, statistics.recentlyPlayedQuestionIds.toSet()))
        if (uiState.session != null) currentScreen = TriviaScreen.QUESTION
    }

    fun confirmStart() {
        setState(engine.confirmStart(uiState, statistics.recentlyPlayedQuestionIds.toSet()))
        if (uiState.session != null) currentScreen = TriviaScreen.QUESTION
    }

    fun cancelStart() {
        setState(engine.cancelStart(uiState))
    }

    fun submitAnswer(answerIndex: Int?) {
        setState(engine.submitAnswer(uiState, answerIndex))
    }

    fun tickTimer() {
        setState(engine.tickTimer(uiState))
    }

    fun next() {
        val previousSummary = uiState.summary
        val nextState = engine.next(uiState)
        setState(nextState)
        val completedSummary = nextState.summary
        if (previousSummary == null && completedSummary != null) {
            currentScreen = TriviaScreen.RESULTS
            statistics = StatisticsAccumulator.record(statistics, completedSummary)
            viewModelScope.launch { statisticsRepository.recordGame(completedSummary) }
        }
    }

    fun playAgain() {
        setState(engine.playAgain(uiState, statistics.recentlyPlayedQuestionIds.toSet()))
        if (uiState.session != null) currentScreen = TriviaScreen.QUESTION
    }

    fun returnHome() {
        setState(engine.returnHome(uiState))
        currentScreen = TriviaScreen.HOME
    }

    fun navigateTo(screen: TriviaScreen) {
        currentScreen = screen
    }

    fun handleBack() {
        when (currentScreen) {
            TriviaScreen.HOME -> Unit
            TriviaScreen.QUESTION -> currentScreen = TriviaScreen.HOME
            TriviaScreen.RESULTS -> returnHome()
            else -> currentScreen = TriviaScreen.HOME
        }
    }

    fun resetStatistics() {
        statistics = TriviaStatistics()
        viewModelScope.launch { statisticsRepository.reset() }
    }

    private fun updateAndPersistSettings(state: TriviaGameState) {
        setState(state)
        viewModelScope.launch { settingsRepository.save(state.settings) }
    }

    private fun setState(state: TriviaGameState) {
        uiState = state
        if (state.session != null || state.summary != null) {
            savedStateHandle[GAME_STATE_KEY] = stateCodec.encode(state)
        } else {
            savedStateHandle.remove<String>(GAME_STATE_KEY)
        }
    }

    private companion object {
        const val GAME_STATE_KEY = "active_game_state"
    }
}
