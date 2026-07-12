package com.mdavis8403.magickingdomtrivia.domain

import com.mdavis8403.magickingdomtrivia.data.Difficulty
import com.mdavis8403.magickingdomtrivia.data.InMemoryQuestionRepository
import com.mdavis8403.magickingdomtrivia.data.QuestionCatalog
import com.mdavis8403.magickingdomtrivia.data.TriviaCategory
import com.mdavis8403.magickingdomtrivia.data.TriviaQuestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class TriviaGameEngineTest {
    private val rides = TriviaCategory("rides", "Rides", "Ride questions", 0xFF00AA00)
    private val animation = TriviaCategory("animation", "Animation", "Animation questions", 0xFF0000AA)
    private val repository = InMemoryQuestionRepository(
        QuestionCatalog(
            packId = "test",
            questions = listOf(
                question("rides_1", "Rides", Difficulty.EASY),
                question("rides_2", "Rides", Difficulty.MEDIUM),
                question("rides_3", "Rides", Difficulty.HARD),
                question("animation_1", "Animation", Difficulty.EASY),
            ),
            categories = listOf(rides, animation),
            validationErrors = emptyList(),
        ),
    )
    private val engine = TriviaGameEngine(repository, roundSize = 3, random = Random(1))

    @Test
    fun initialState_selectsFirstCategory() {
        val state = engine.initialState()

        assertEquals("rides", state.selectedCategoryId)
        assertEquals(2, state.categories.size)
        assertNull(state.session)
    }

    @Test
    fun startGame_createsRoundForSelectedCategory() {
        val state = engine.startGame(engine.initialState())

        val session = requireNotNull(state.session)
        assertEquals("rides", session.category.id)
        assertEquals(3, session.questions.size)
        assertTrue(session.questions.all { it.category == "Rides" })
        assertEquals(3, session.questions.map { it.id }.distinct().size)
    }

    @Test
    fun submitAnswer_updatesScoreAndSummary() {
        var state = engine.startGame(engine.initialState())

        repeat(3) { roundIndex ->
            val session = requireNotNull(state.session)
            state = engine.submitAnswer(state, session.correctAnswerIndex)

            val answered = requireNotNull(state.session)
            assertNotNull(answered.selectedAnswerIndex)
            assertEquals(roundIndex + 1, answered.score)
            state = engine.next(state)
        }

        assertNull(state.session)
        val summary = requireNotNull(state.summary)
        assertEquals(3, summary.correctAnswers)
        assertEquals(100, summary.accuracyPercent)
        assertEquals(3, summary.bestStreak)
    }

    @Test
    fun wrongAnswer_breaksStreak() {
        var state = engine.startGame(engine.initialState())
        state = engine.submitAnswer(state, requireNotNull(state.session).correctAnswerIndex)
        state = engine.next(state)

        val session = requireNotNull(state.session)
        val wrongIndex = session.currentQuestion.choices.indices.first { it != session.correctAnswerIndex }
        state = engine.submitAnswer(state, wrongIndex)

        val updated = requireNotNull(state.session)
        assertEquals(1, updated.score)
        assertEquals(0, updated.currentStreak)
        assertEquals(1, updated.bestStreak)
    }

    private fun question(id: String, category: String, difficulty: Difficulty) = TriviaQuestion(
        id = id,
        prompt = "Question $id?",
        answers = listOf("Correct", "Wrong one", "Wrong two", "Wrong three"),
        correctAnswerIndex = 0,
        category = category,
        difficulty = difficulty,
        explanation = "Explanation",
        sourceTitle = "Source",
    )
}
