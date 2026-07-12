package com.mdavis8403.magickingdomtrivia.domain

import com.mdavis8403.magickingdomtrivia.data.TriviaCategory
import com.mdavis8403.magickingdomtrivia.data.TriviaQuestion
import com.mdavis8403.magickingdomtrivia.data.TriviaRepository
import kotlin.random.Random

data class AnsweredQuestion(
    val questionId: String,
    val selectedIndex: Int,
    val correctIndex: Int,
    val isCorrect: Boolean,
)

data class TriviaSession(
    val category: TriviaCategory,
    val questions: List<TriviaQuestion>,
    val currentIndex: Int = 0,
    val selectedAnswerIndex: Int? = null,
    val score: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val answeredQuestions: List<AnsweredQuestion> = emptyList(),
) {
    val currentQuestion: TriviaQuestion
        get() = questions[currentIndex]

    val correctAnswerIndex: Int
        get() = currentQuestion.choices.indexOfFirst { it.isCorrect }

    val totalQuestions: Int
        get() = questions.size
}

data class TriviaSummary(
    val category: TriviaCategory,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val accuracyPercent: Int,
    val bestStreak: Int,
)

data class TriviaGameState(
    val categories: List<TriviaCategory>,
    val selectedCategoryId: String,
    val session: TriviaSession? = null,
    val summary: TriviaSummary? = null,
)

class TriviaGameEngine(
    private val repository: TriviaRepository,
    private val roundSize: Int = 5,
    private val random: Random = Random.Default,
) {
    fun initialState(): TriviaGameState {
        val categories = repository.categories()
        return TriviaGameState(
            categories = categories,
            selectedCategoryId = categories.first().id,
        )
    }

    fun selectCategory(state: TriviaGameState, categoryId: String): TriviaGameState =
        state.copy(selectedCategoryId = categoryId)

    fun startGame(state: TriviaGameState): TriviaGameState {
        val category = repository.categoryById(state.selectedCategoryId)
        val questions = repository
            .questionsFor(category.id)
            .shuffled(random)
            .take(roundSize.coerceAtMost(repository.questionsFor(category.id).size))

        return state.copy(
            session = TriviaSession(
                category = category,
                questions = questions,
            ),
            summary = null,
        )
    }

    fun submitAnswer(state: TriviaGameState, answerIndex: Int): TriviaGameState {
        val session = state.session ?: return state
        if (session.selectedAnswerIndex != null) return state

        val correctIndex = session.correctAnswerIndex
        val isCorrect = answerIndex == correctIndex
        val newScore = if (isCorrect) session.score + 1 else session.score
        val newStreak = if (isCorrect) session.currentStreak + 1 else 0

        return state.copy(
            session = session.copy(
                selectedAnswerIndex = answerIndex,
                score = newScore,
                currentStreak = newStreak,
                bestStreak = maxOf(session.bestStreak, newStreak),
                answeredQuestions = session.answeredQuestions + AnsweredQuestion(
                    questionId = session.currentQuestion.id,
                    selectedIndex = answerIndex,
                    correctIndex = correctIndex,
                    isCorrect = isCorrect,
                ),
            ),
        )
    }

    fun next(state: TriviaGameState): TriviaGameState {
        val session = state.session ?: return state
        if (session.selectedAnswerIndex == null) return state

        val isLastQuestion = session.currentIndex == session.questions.lastIndex
        if (isLastQuestion) {
            val summary = TriviaSummary(
                category = session.category,
                totalQuestions = session.totalQuestions,
                correctAnswers = session.score,
                accuracyPercent = (session.score * 100) / session.totalQuestions,
                bestStreak = session.bestStreak,
            )
            return state.copy(session = null, summary = summary)
        }

        return state.copy(
            session = session.copy(
                currentIndex = session.currentIndex + 1,
                selectedAnswerIndex = null,
                currentStreak = session.currentStreak,
            ),
        )
    }

    fun playAgain(state: TriviaGameState): TriviaGameState {
        val selectedCategoryId = state.summary?.category?.id ?: state.selectedCategoryId
        return startGame(
            state.copy(
                selectedCategoryId = selectedCategoryId,
                summary = null,
                session = null,
            ),
        )
    }

    fun returnHome(state: TriviaGameState): TriviaGameState =
        state.copy(session = null, summary = null)
}

