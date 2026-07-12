package com.mdavis8403.magickingdomtrivia.domain

import com.mdavis8403.magickingdomtrivia.data.Difficulty
import kotlinx.serialization.Serializable

@Serializable
data class PerformanceStatistics(
    val answered: Int = 0,
    val correct: Int = 0,
) {
    val accuracyPercent: Int
        get() = if (answered == 0) 0 else (correct * 100) / answered
}

@Serializable
data class TriviaStatistics(
    val gamesPlayed: Int = 0,
    val questionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val categoryPerformance: Map<String, PerformanceStatistics> = emptyMap(),
    val difficultyPerformance: Map<Difficulty, PerformanceStatistics> = emptyMap(),
    val highScoresByGameLength: Map<Int, Int> = emptyMap(),
    val recentlyPlayedQuestionIds: List<String> = emptyList(),
) {
    val accuracyPercent: Int
        get() = if (questionsAnswered == 0) 0 else (correctAnswers * 100) / questionsAnswered
}

object StatisticsAccumulator {
    const val RECENT_QUESTION_LIMIT = 200

    fun record(
        current: TriviaStatistics,
        summary: TriviaSummary,
        recentQuestionLimit: Int = RECENT_QUESTION_LIMIT,
    ): TriviaStatistics {
        val categoryPerformance = current.categoryPerformance.toMutableMap()
        val difficultyPerformance = current.difficultyPerformance.toMutableMap()
        summary.answeredQuestions.forEach { answer ->
            categoryPerformance[answer.category] = categoryPerformance[answer.category]
                .orEmpty()
                .withAnswer(answer.isCorrect)
            difficultyPerformance[answer.difficulty] = difficultyPerformance[answer.difficulty]
                .orEmpty()
                .withAnswer(answer.isCorrect)
        }

        val highScores = current.highScoresByGameLength.toMutableMap()
        highScores[summary.totalQuestions] = maxOf(
            highScores[summary.totalQuestions] ?: 0,
            summary.correctAnswers,
        )

        val newQuestionIds = summary.answeredQuestions.map(AnsweredQuestion::questionId)
        val recentIds = (current.recentlyPlayedQuestionIds + newQuestionIds)
            .distinctLast()
            .takeLast(recentQuestionLimit)

        return current.copy(
            gamesPlayed = current.gamesPlayed + 1,
            questionsAnswered = current.questionsAnswered + summary.totalQuestions,
            correctAnswers = current.correctAnswers + summary.correctAnswers,
            categoryPerformance = categoryPerformance,
            difficultyPerformance = difficultyPerformance,
            highScoresByGameLength = highScores,
            recentlyPlayedQuestionIds = recentIds,
        )
    }

    private fun PerformanceStatistics?.orEmpty() = this ?: PerformanceStatistics()

    private fun PerformanceStatistics.withAnswer(isCorrect: Boolean) = copy(
        answered = answered + 1,
        correct = correct + if (isCorrect) 1 else 0,
    )

    private fun List<String>.distinctLast(): List<String> {
        val seen = mutableSetOf<String>()
        return asReversed().filter { seen.add(it) }.asReversed()
    }
}
