package com.mdavis8403.magickingdomtrivia.domain

import com.mdavis8403.magickingdomtrivia.data.TriviaRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class TriviaGameEngineTest {
    private val engine = TriviaGameEngine(
        repository = TriviaRepository(),
        roundSize = 3,
        random = Random(1),
    )

    @Test
    fun initialState_selectsFirstCategory() {
        val state = engine.initialState()

        assertEquals("lands", state.selectedCategoryId)
        assertEquals(5, state.categories.size)
        assertNull(state.session)
    }

    @Test
    fun startGame_createsRoundForSelectedCategory() {
        val state = engine.startGame(
            engine.selectCategory(
                state = engine.initialState(),
                categoryId = "rides",
            ),
        )

        val session = requireNotNull(state.session)
        assertEquals("rides", session.category.id)
        assertEquals(3, session.questions.size)
        assertTrue(session.questions.all { it.categoryId == "rides" })
    }

    @Test
    fun submitAnswer_updatesScoreAndSummary() {
        var state = engine.startGame(engine.initialState())

        repeat(3) { roundIndex ->
            val session = requireNotNull(state.session)
            val correctIndex = session.correctAnswerIndex
            state = engine.submitAnswer(state, correctIndex)

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
        val firstCorrectIndex = requireNotNull(state.session).correctAnswerIndex
        state = engine.submitAnswer(state, firstCorrectIndex)
        state = engine.next(state)

        val secondSession = requireNotNull(state.session)
        val wrongIndex = secondSession.currentQuestion.choices.indices.first { it != secondSession.correctAnswerIndex }
        state = engine.submitAnswer(state, wrongIndex)

        val updatedSession = requireNotNull(state.session)
        assertEquals(1, updatedSession.score)
        assertEquals(0, updatedSession.currentStreak)
        assertEquals(1, updatedSession.bestStreak)
    }
}
