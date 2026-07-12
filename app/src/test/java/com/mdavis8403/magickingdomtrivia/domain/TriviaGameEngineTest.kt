package com.mdavis8403.magickingdomtrivia.domain

import com.mdavis8403.magickingdomtrivia.data.Difficulty
import com.mdavis8403.magickingdomtrivia.data.InMemoryQuestionRepository
import com.mdavis8403.magickingdomtrivia.data.QuestionCatalog
import com.mdavis8403.magickingdomtrivia.data.QuestionRepository
import com.mdavis8403.magickingdomtrivia.data.TriviaCategory
import com.mdavis8403.magickingdomtrivia.data.TriviaQuestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class TriviaGameEngineTest {
    private val questions = (1..60).map { index ->
        val category = if (index <= 30) "Disney Animation" else "Pixar"
        question(
            id = "question_${index.toString().padStart(3, '0')}",
            category = category,
            difficulty = Difficulty.entries[(index - 1) % 3],
            correctIndex = index % 4,
        )
    }
    private val repository = InMemoryQuestionRepository(
        QuestionCatalog(
            packId = "test",
            questions = questions,
            categories = listOf(
                TriviaCategory(QuestionRepository.MIXED_CATEGORY_ID, "Mixed", "All", 0xFFFFFFFF),
                TriviaCategory("disney-animation", "Disney Animation", "Animation", 0xFF00AA00),
                TriviaCategory("pixar", "Pixar", "Pixar", 0xFF0000AA),
            ),
            validationErrors = emptyList(),
        ),
    )

    @Test
    fun initialState_usesPersistedSelectionsWhenValid() {
        val settings = GameSettings(categoryId = "pixar", difficulty = Difficulty.HARD, questionCount = 20)
        val state = engine().initialState(settings)

        assertEquals("pixar", state.selectedCategoryId)
        assertEquals(Difficulty.HARD, state.selectedDifficulty)
        assertEquals(3, state.categories.size)
        assertNull(state.session)
    }

    @Test
    fun gameCreation_filtersCategoryAndDifficulty_withoutDuplicates() {
        val settings = GameSettings(
            categoryId = "disney-animation",
            difficulty = Difficulty.EASY,
            questionCount = 10,
            randomizeAnswerOrder = false,
        )
        val state = engine().startGame(engine().initialState(settings))
        val session = requireNotNull(state.session)

        assertEquals(10, session.questions.size)
        assertEquals(10, session.questions.map { it.id }.distinct().size)
        assertTrue(session.questions.all { it.category == "Disney Animation" })
        assertTrue(session.questions.all { it.difficulty == Difficulty.EASY })
    }

    @Test
    fun requestStart_reportsWhenTooFewQuestionsAreEligible() {
        val settings = GameSettings(categoryId = "pixar", difficulty = Difficulty.HARD, questionCount = 50)
        val state = engine().requestStart(engine().initialState(settings))

        assertEquals(50, state.startNotice?.requestedCount)
        assertEquals(10, state.startNotice?.availableCount)
        assertNull(state.session)
    }

    @Test
    fun answerRandomization_preservesCorrectAnswerMapping() {
        val settings = GameSettings(questionCount = 10, randomizeAnswerOrder = true)
        val session = requireNotNull(engine(seed = 7).startGame(engine(seed = 7).initialState(settings)).session)
        val sourceById = questions.associateBy(TriviaQuestion::id)

        session.questions.forEach { presented ->
            val source = requireNotNull(sourceById[presented.id])
            val correctText = source.answers[source.correctAnswerIndex]
            assertEquals(correctText, presented.choices.single { it.isCorrect }.text)
            assertEquals(1, presented.choices.count { it.isCorrect })
        }
    }

    @Test
    fun recentlyPlayedQuestions_areAvoidedWhenEnoughFreshQuestionsExist() {
        val settings = GameSettings(
            questionCount = 10,
            randomizeQuestionOrder = false,
            avoidRecentlyPlayed = true,
        )
        val recent = questions.take(50).mapTo(mutableSetOf(), TriviaQuestion::id)
        val session = requireNotNull(
            engine().startGame(engine().initialState(settings), recentlyPlayedIds = recent).session,
        )

        assertTrue(session.questions.none { it.id in recent })
    }

    @Test
    fun submitAnswer_updatesScoreAndBuildsDetailedSummary() {
        val settings = GameSettings(questionCount = 10, randomizeAnswerOrder = true)
        var state = engine().startGame(engine().initialState(settings))

        repeat(10) { index ->
            val session = requireNotNull(state.session)
            val answer = if (index < 8) session.correctAnswerIndex else {
                session.currentQuestion.choices.indices.first { it != session.correctAnswerIndex }
            }
            state = engine().submitAnswer(state, answer)
            state = engine().next(state)
        }

        val summary = requireNotNull(state.summary)
        assertEquals(8, summary.correctAnswers)
        assertEquals(2, summary.incorrectAnswers)
        assertEquals(80, summary.accuracyPercent)
        assertEquals("Disney Expert", summary.resultMessage)
        assertEquals(10, summary.answeredQuestions.size)
    }

    @Test
    fun timerExpiration_recordsAnIncorrectTimedOutAnswer() {
        val settings = GameSettings(questionCount = 10, timerSeconds = 10)
        var state = engine().startGame(engine().initialState(settings))

        repeat(10) { state = engine().tickTimer(state) }

        val session = requireNotNull(state.session)
        assertTrue(session.answerRevealed)
        assertTrue(session.timedOut)
        assertNull(session.selectedAnswerIndex)
        assertFalse(session.answeredQuestions.single().isCorrect)
    }

    private fun engine(seed: Int = 1) = TriviaGameEngine(repository, Random(seed))

    private fun question(
        id: String,
        category: String,
        difficulty: Difficulty,
        correctIndex: Int,
    ) = TriviaQuestion(
        id = id,
        prompt = "Question $id?",
        answers = listOf("Answer A", "Answer B", "Answer C", "Answer D"),
        correctAnswerIndex = correctIndex,
        category = category,
        difficulty = difficulty,
        explanation = "Explanation",
        sourceTitle = "Source",
    )
}
