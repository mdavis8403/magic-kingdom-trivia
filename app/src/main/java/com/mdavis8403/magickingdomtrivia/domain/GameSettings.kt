package com.mdavis8403.magickingdomtrivia.domain

import com.mdavis8403.magickingdomtrivia.data.Difficulty
import com.mdavis8403.magickingdomtrivia.data.QuestionRepository

data class GameSettings(
    val categoryId: String = QuestionRepository.MIXED_CATEGORY_ID,
    val difficulty: Difficulty = Difficulty.MIXED,
    val questionCount: Int = 10,
    val timerSeconds: Int = 0,
    val randomizeQuestionOrder: Boolean = true,
    val randomizeAnswerOrder: Boolean = true,
    val showExplanations: Boolean = true,
    val automaticallyAdvance: Boolean = false,
    val avoidRecentlyPlayed: Boolean = true,
    val soundEffects: Boolean = true,
) {
    init {
        require(questionCount in VALID_QUESTION_COUNTS)
        require(timerSeconds in VALID_TIMER_SECONDS)
    }

    companion object {
        val VALID_QUESTION_COUNTS = listOf(10, 20, 30, 50)
        val VALID_TIMER_SECONDS = listOf(0, 10, 15, 30)
    }
}
