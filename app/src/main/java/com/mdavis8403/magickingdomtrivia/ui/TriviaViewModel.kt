package com.mdavis8403.magickingdomtrivia.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.mdavis8403.magickingdomtrivia.data.AssetQuestionRepository
import com.mdavis8403.magickingdomtrivia.domain.TriviaGameEngine
import com.mdavis8403.magickingdomtrivia.domain.TriviaGameState

class TriviaViewModel(application: Application) : AndroidViewModel(application) {
    private val engine = TriviaGameEngine(AssetQuestionRepository(application))

    var uiState: TriviaGameState by mutableStateOf(engine.initialState())
        private set

    fun selectCategory(categoryId: String) {
        uiState = engine.selectCategory(uiState, categoryId)
    }

    fun startGame() {
        val requested = engine.requestStart(uiState)
        uiState = if (requested.startNotice != null) engine.confirmStart(requested) else requested
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
