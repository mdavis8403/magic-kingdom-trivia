package com.mdavis8403.magickingdomtrivia.data

enum class Difficulty {
    EASY,
    MEDIUM,
    HARD,
}

data class TriviaCategory(
    val id: String,
    val title: String,
    val description: String,
    val accentColor: Long,
)

data class TriviaChoice(
    val text: String,
    val isCorrect: Boolean,
)

data class TriviaQuestion(
    val id: String,
    val categoryId: String,
    val prompt: String,
    val choices: List<TriviaChoice>,
    val explanation: String,
    val difficulty: Difficulty,
)

