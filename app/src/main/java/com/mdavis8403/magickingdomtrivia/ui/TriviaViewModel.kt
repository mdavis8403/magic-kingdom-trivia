package com.mdavis8403.magickingdomtrivia.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.mdavis8403.magickingdomtrivia.data.TriviaRepository
import com.mdavis8403.magickingdomtrivia.domain.TriviaGameEngine
import com.mdavis8403.magickingdomtrivia.domain.TriviaGameState

class TriviaViewModel(
    private val engine: TriviaGameEngine = TriviaGameEngine(TriviaRepository()),
) : ViewModel() {
    var uiState: TriviaGameState by mutableStateOf(engine.initialState())
        private set

    fun selectCategory(categoryId: String) {
        uiState = engine.selectCategory(uiState, categoryId)
    }

    fun startGame() {
        uiState = engine.startGame(uiState)
    }

    fun submitAnswer(answerIndex: Int) {
        uiState = engine.submitAnswer(uiState, answerIndex)
    }

    fun next() {
        uiState = engine.next(uiState)
    }

    fun playAgain() {
        uiState = engine.playAgain(uiState)
    }

    fun returnHome() {
        uiState = engine.returnHome(uiState)
    }
}

