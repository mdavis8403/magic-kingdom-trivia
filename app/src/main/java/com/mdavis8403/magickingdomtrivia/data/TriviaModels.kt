package com.mdavis8403.magickingdomtrivia.data

enum class Difficulty(val displayName: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard"),
    MIXED("Mixed"),
}

data class TriviaCategory(
    val id: String,
    val title: String,
    val description: String,
    val accentColor: Long,
)

data class TriviaQuestion(
    val id: String,
    val prompt: String,
    val answers: List<String>,
    val correctAnswerIndex: Int,
    val category: String,
    val difficulty: Difficulty,
    val explanation: String,
    val sourceTitle: String,
)

data class TriviaChoice(
    val text: String,
    val isCorrect: Boolean,
)

data class PresentedQuestion(
    val id: String,
    val prompt: String,
    val choices: List<TriviaChoice>,
    val category: String,
    val difficulty: Difficulty,
    val explanation: String,
    val sourceTitle: String,
)

data class QuestionCatalog(
    val packId: String,
    val questions: List<TriviaQuestion>,
    val categories: List<TriviaCategory>,
    val validationErrors: List<String>,
)
