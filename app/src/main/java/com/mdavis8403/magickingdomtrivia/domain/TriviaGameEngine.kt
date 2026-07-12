package com.mdavis8403.magickingdomtrivia.domain

import com.mdavis8403.magickingdomtrivia.data.Difficulty
import com.mdavis8403.magickingdomtrivia.data.PresentedQuestion
import com.mdavis8403.magickingdomtrivia.data.QuestionRepository
import com.mdavis8403.magickingdomtrivia.data.TriviaCategory
import com.mdavis8403.magickingdomtrivia.data.TriviaChoice
import com.mdavis8403.magickingdomtrivia.data.TriviaQuestion
import kotlin.random.Random

data class AnsweredQuestion(
    val questionId: String,
    val selectedIndex: Int,
    val correctIndex: Int,
    val isCorrect: Boolean,
)

data class TriviaSession(
    val category: TriviaCategory,
    val questions: List<PresentedQuestion>,
    val currentIndex: Int = 0,
    val selectedAnswerIndex: Int? = null,
    val score: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val answeredQuestions: List<AnsweredQuestion> = emptyList(),
) {
    val currentQuestion: PresentedQuestion
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
    val selectedDifficulty: Difficulty = Difficulty.MIXED,
    val session: TriviaSession? = null,
    val summary: TriviaSummary? = null,
    val errorMessage: String? = null,
)

class TriviaGameEngine(
    private val repository: QuestionRepository,
    private val roundSize: Int = 5,
    private val random: Random = Random.Default,
) {
    fun initialState(): TriviaGameState {
        val categories = repository.catalog.categories
        return TriviaGameState(
            categories = categories,
            selectedCategoryId = categories.firstOrNull()?.id.orEmpty(),
            errorMessage = if (categories.isEmpty()) "No valid questions are available." else null,
        )
    }

    fun selectCategory(state: TriviaGameState, categoryId: String): TriviaGameState {
        if (state.categories.none { it.id == categoryId }) return state
        return state.copy(selectedCategoryId = categoryId, errorMessage = null)
    }

    fun startGame(state: TriviaGameState): TriviaGameState {
        val category = state.categories.firstOrNull { it.id == state.selectedCategoryId }
            ?: return state.copy(errorMessage = "Select a valid category before starting.")
        val questions = repository.questions(category.id, state.selectedDifficulty)
            .shuffled(random)
            .take(roundSize)
            .map { question -> question.toPresentedQuestion() }

        if (questions.isEmpty()) {
            return state.copy(errorMessage = "No questions match the selected category and difficulty.")
        }

        return state.copy(
            session = TriviaSession(
                category = category,
                questions = questions,
            ),
            summary = null,
            errorMessage = null,
        )
    }

    fun submitAnswer(state: TriviaGameState, answerIndex: Int): TriviaGameState {
        val session = state.session ?: return state
        if (session.selectedAnswerIndex != null || answerIndex !in session.currentQuestion.choices.indices) return state

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

        if (session.currentIndex == session.questions.lastIndex) {
            return state.copy(
                session = null,
                summary = TriviaSummary(
                    category = session.category,
                    totalQuestions = session.totalQuestions,
                    correctAnswers = session.score,
                    accuracyPercent = (session.score * 100) / session.totalQuestions,
                    bestStreak = session.bestStreak,
                ),
            )
        }

        return state.copy(
            session = session.copy(
                currentIndex = session.currentIndex + 1,
                selectedAnswerIndex = null,
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
        state.copy(session = null, summary = null, errorMessage = null)

    private fun TriviaQuestion.toPresentedQuestion() = PresentedQuestion(
        id = id,
        prompt = prompt,
        choices = answers.mapIndexed { index, answer ->
            TriviaChoice(text = answer, isCorrect = index == correctAnswerIndex)
        },
        category = category,
        difficulty = difficulty,
        explanation = explanation,
        sourceTitle = sourceTitle,
    )
}
