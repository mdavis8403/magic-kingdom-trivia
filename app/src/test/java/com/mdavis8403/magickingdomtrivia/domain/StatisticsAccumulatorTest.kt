package com.mdavis8403.magickingdomtrivia.domain

import com.mdavis8403.magickingdomtrivia.data.Difficulty
import com.mdavis8403.magickingdomtrivia.data.TriviaCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StatisticsAccumulatorTest {
    @Test
    fun record_accumulatesTotalsBreakdownsHighScoresAndRecentIds() {
        val first = StatisticsAccumulator.record(TriviaStatistics(), summary(score = 2, ids = listOf("q1", "q2", "q3")))
        val second = StatisticsAccumulator.record(first, summary(score = 1, ids = listOf("q3", "q4", "q5")))

        assertEquals(2, second.gamesPlayed)
        assertEquals(6, second.questionsAnswered)
        assertEquals(3, second.correctAnswers)
        assertEquals(50, second.accuracyPercent)
        assertEquals(3, second.categoryPerformance.getValue("Pixar").correct)
        assertEquals(6, second.categoryPerformance.getValue("Pixar").answered)
        assertEquals(2, second.highScoresByGameLength[3])
        assertEquals(listOf("q1", "q2", "q3", "q4", "q5"), second.recentlyPlayedQuestionIds)
    }

    @Test
    fun record_capsRecentQuestionHistoryAndKeepsNewestOccurrence() {
        val current = TriviaStatistics(recentlyPlayedQuestionIds = listOf("old", "repeat"))
        val updated = StatisticsAccumulator.record(
            current = current,
            summary = summary(score = 0, ids = listOf("new", "repeat", "latest")),
            recentQuestionLimit = 3,
        )

        assertEquals(listOf("new", "repeat", "latest"), updated.recentlyPlayedQuestionIds)
        assertFalse("old" in updated.recentlyPlayedQuestionIds)
        assertTrue("latest" in updated.recentlyPlayedQuestionIds)
    }

    private fun summary(score: Int, ids: List<String>): TriviaSummary {
        val answers = ids.mapIndexed { index, id ->
            AnsweredQuestion(
                questionId = id,
                selectedIndex = if (index < score) 0 else 1,
                correctIndex = 0,
                isCorrect = index < score,
                category = "Pixar",
                difficulty = Difficulty.MEDIUM,
            )
        }
        return TriviaSummary(
            category = TriviaCategory("pixar", "Pixar", "Pixar", 0xFF000000),
            totalQuestions = answers.size,
            correctAnswers = score,
            accuracyPercent = if (answers.isEmpty()) 0 else score * 100 / answers.size,
            bestStreak = score,
            bestCategory = "Pixar",
            mostDifficultCategory = "Pixar",
            resultMessage = "Result",
            answeredQuestions = answers,
        )
    }
}
