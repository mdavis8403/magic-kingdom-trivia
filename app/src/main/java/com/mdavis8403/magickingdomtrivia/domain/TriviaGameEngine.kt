package com.mdavis8403.magickingdomtrivia.domain

import com.mdavis8403.magickingdomtrivia.data.Difficulty
import com.mdavis8403.magickingdomtrivia.data.PresentedQuestion
import com.mdavis8403.magickingdomtrivia.data.QuestionRepository
import com.mdavis8403.magickingdomtrivia.data.TriviaCategory
import com.mdavis8403.magickingdomtrivia.data.TriviaChoice
import com.mdavis8403.magickingdomtrivia.data.TriviaQuestion
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
data class AnsweredQuestion(
    val questionId: String,
    val selectedIndex: Int?,
    val correctIndex: Int,
    val isCorrect: Boolean,
    val category: String,
    val difficulty: Difficulty,
)

@Serializable
data class TriviaSession(
    val category: TriviaCategory,
    val questions: List<PresentedQuestion>,
    val settings: GameSettings,
    val currentIndex: Int = 0,
    val selectedAnswerIndex: Int? = null,
    val answerRevealed: Boolean = false,
    val timedOut: Boolean = false,
    val secondsRemaining: Int = settings.timerSeconds,
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

@Serializable
data class TriviaSummary(
    val category: TriviaCategory,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val accuracyPercent: Int,
    val bestStreak: Int,
    val bestCategory: String,
    val mostDifficultCategory: String,
    val resultMessage: String,
    val answeredQuestions: List<AnsweredQuestion>,
) {
    val incorrectAnswers: Int
        get() = totalQuestions - correctAnswers
}

@Serializable
data class StartNotice(
    val requestedCount: Int,
    val availableCount: Int,
)

@Serializable
data class TriviaGameState(
    val categories: List<TriviaCategory>,
    val settings: GameSettings,
    val session: TriviaSession? = null,
    val summary: TriviaSummary? = null,
    val startNotice: StartNotice? = null,
    val errorMessage: String? = null,
) {
    val selectedCategoryId: String
        get() = settings.categoryId

    val selectedDifficulty: Difficulty
        get() = settings.difficulty
}

class TriviaGameEngine(
    private val repository: QuestionRepository,
    private val random: Random = Random.Default,
) {
    fun initialState(settings: GameSettings = GameSettings()): TriviaGameState {
        val categories = repository.catalog.categories
        val validCategory = categories.any { it.id == settings.categoryId }
        val initialSettings = if (validCategory) settings else {
            settings.copy(categoryId = categories.firstOrNull()?.id.orEmpty())
        }
        return TriviaGameState(
            categories = categories,
            settings = initialSettings,
            errorMessage = when {
                repository.catalog.questions.isEmpty() -> "No valid questions are available."
                categories.isEmpty() -> "No question categories are available."
                else -> null
            },
        )
    }

    fun updateSettings(state: TriviaGameState, settings: GameSettings): TriviaGameState {
        val categoryId = settings.categoryId.takeIf { candidate -> state.categories.any { it.id == candidate } }
            ?: state.categories.firstOrNull()?.id.orEmpty()
        return state.copy(
            settings = settings.copy(categoryId = categoryId),
            startNotice = null,
            errorMessage = null,
        )
    }

    fun selectCategory(state: TriviaGameState, categoryId: String): TriviaGameState {
        if (state.categories.none { it.id == categoryId }) return state
        return updateSettings(state, state.settings.copy(categoryId = categoryId))
    }

    fun selectDifficulty(state: TriviaGameState, difficulty: Difficulty): TriviaGameState =
        updateSettings(state, state.settings.copy(difficulty = difficulty))

    fun requestStart(
        state: TriviaGameState,
        recentlyPlayedIds: Set<String> = emptySet(),
    ): TriviaGameState {
        val eligibleCount = eligibleQuestions(state.settings).size
        if (eligibleCount == 0) {
            return state.copy(
                startNotice = null,
                errorMessage = "No questions match the selected category and difficulty.",
            )
        }
        if (eligibleCount < state.settings.questionCount) {
            return state.copy(
                startNotice = StartNotice(state.settings.questionCount, eligibleCount),
                errorMessage = null,
            )
        }
        return startGame(state, recentlyPlayedIds)
    }

    fun confirmStart(
        state: TriviaGameState,
        recentlyPlayedIds: Set<String> = emptySet(),
    ): TriviaGameState = startGame(state.copy(startNotice = null), recentlyPlayedIds)

    fun cancelStart(state: TriviaGameState): TriviaGameState = state.copy(startNotice = null)

    fun startGame(
        state: TriviaGameState,
        recentlyPlayedIds: Set<String> = emptySet(),
    ): TriviaGameState {
        val category = state.categories.firstOrNull { it.id == state.settings.categoryId }
            ?: return state.copy(errorMessage = "Select a valid category before starting.")
        val eligible = eligibleQuestions(state.settings)
        if (eligible.isEmpty()) {
            return state.copy(errorMessage = "No questions match the selected category and difficulty.")
        }

        val ordered = selectQuestions(
            eligible = eligible,
            requestedCount = state.settings.questionCount,
            recentlyPlayedIds = recentlyPlayedIds,
            randomize = state.settings.randomizeQuestionOrder,
            avoidRecentlyPlayed = state.settings.avoidRecentlyPlayed,
        )
        val presented = ordered.map { question ->
            question.toPresentedQuestion(randomizeAnswers = state.settings.randomizeAnswerOrder)
        }

        return state.copy(
            session = TriviaSession(
                category = category,
                questions = presented,
                settings = state.settings,
            ),
            summary = null,
            startNotice = null,
            errorMessage = null,
        )
    }

    fun submitAnswer(state: TriviaGameState, answerIndex: Int?): TriviaGameState {
        val session = state.session ?: return state
        if (session.answerRevealed) return state
        if (answerIndex != null && answerIndex !in session.currentQuestion.choices.indices) return state

        val correctIndex = session.correctAnswerIndex
        val isCorrect = answerIndex == correctIndex
        val newStreak = if (isCorrect) session.currentStreak + 1 else 0
        return state.copy(
            session = session.copy(
                selectedAnswerIndex = answerIndex,
                answerRevealed = true,
                timedOut = answerIndex == null,
                score = session.score + if (isCorrect) 1 else 0,
                currentStreak = newStreak,
                bestStreak = maxOf(session.bestStreak, newStreak),
                answeredQuestions = session.answeredQuestions + AnsweredQuestion(
                    questionId = session.currentQuestion.id,
                    selectedIndex = answerIndex,
                    correctIndex = correctIndex,
                    isCorrect = isCorrect,
                    category = session.currentQuestion.category,
                    difficulty = session.currentQuestion.difficulty,
                ),
            ),
        )
    }

    fun tickTimer(state: TriviaGameState): TriviaGameState {
        val session = state.session ?: return state
        if (session.settings.timerSeconds == 0 || session.answerRevealed) return state
        if (session.secondsRemaining <= 1) return submitAnswer(state, answerIndex = null)
        return state.copy(session = session.copy(secondsRemaining = session.secondsRemaining - 1))
    }

    fun next(state: TriviaGameState): TriviaGameState {
        val session = state.session ?: return state
        if (!session.answerRevealed) return state

        if (session.currentIndex == session.questions.lastIndex) {
            return state.copy(session = null, summary = session.toSummary())
        }

        return state.copy(
            session = session.copy(
                currentIndex = session.currentIndex + 1,
                selectedAnswerIndex = null,
                answerRevealed = false,
                timedOut = false,
                secondsRemaining = session.settings.timerSeconds,
            ),
        )
    }

    fun playAgain(state: TriviaGameState, recentlyPlayedIds: Set<String> = emptySet()): TriviaGameState =
        startGame(state.copy(summary = null, session = null), recentlyPlayedIds)

    fun returnHome(state: TriviaGameState): TriviaGameState =
        state.copy(session = null, summary = null, startNotice = null, errorMessage = null)

    private fun eligibleQuestions(settings: GameSettings): List<TriviaQuestion> =
        repository.questions(settings.categoryId, settings.difficulty)

    private fun selectQuestions(
        eligible: List<TriviaQuestion>,
        requestedCount: Int,
        recentlyPlayedIds: Set<String>,
        randomize: Boolean,
        avoidRecentlyPlayed: Boolean,
    ): List<TriviaQuestion> {
        val requiredCount = requestedCount.coerceAtMost(eligible.size)
        val orderedEligible = eligible.ordered(randomize)
        if (!avoidRecentlyPlayed || recentlyPlayedIds.isEmpty()) return orderedEligible.take(requiredCount)

        val fresh = orderedEligible.filterNot { it.id in recentlyPlayedIds }
        val recent = orderedEligible.filter { it.id in recentlyPlayedIds }
        return (fresh + recent).take(requiredCount)
    }

    private fun List<TriviaQuestion>.ordered(randomize: Boolean): List<TriviaQuestion> =
        if (randomize) shuffled(random) else this

    private fun TriviaQuestion.toPresentedQuestion(randomizeAnswers: Boolean): PresentedQuestion {
        val choices = answers.mapIndexed { index, answer ->
            TriviaChoice(text = answer, isCorrect = index == correctAnswerIndex)
        }
        return PresentedQuestion(
            id = id,
            prompt = prompt,
            choices = if (randomizeAnswers) choices.shuffled(random) else choices,
            category = category,
            difficulty = difficulty,
            explanation = explanation,
            sourceTitle = sourceTitle,
        )
    }

    private fun TriviaSession.toSummary(): TriviaSummary {
        val categoryAccuracy = answeredQuestions
            .groupBy(AnsweredQuestion::category)
            .mapValues { (_, answers) -> answers.count(AnsweredQuestion::isCorrect).toDouble() / answers.size }
        val bestCategory = categoryAccuracy.maxByOrNull { it.value }?.key ?: category.title
        val mostDifficultCategory = categoryAccuracy.minByOrNull { it.value }?.key ?: category.title
        val accuracy = (score * 100) / totalQuestions
        return TriviaSummary(
            category = category,
            totalQuestions = totalQuestions,
            correctAnswers = score,
            accuracyPercent = accuracy,
            bestStreak = bestStreak,
            bestCategory = bestCategory,
            mostDifficultCategory = mostDifficultCategory,
            resultMessage = resultMessage(accuracy),
            answeredQuestions = answeredQuestions,
        )
    }

    private fun resultMessage(accuracy: Int): String = when (accuracy) {
        in 90..100 -> "Trivia Legend"
        in 75..89 -> "Disney Expert"
        in 50..74 -> "Rising Star"
        else -> "Time for a Rewatch"
    }
}
