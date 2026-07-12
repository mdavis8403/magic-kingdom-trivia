package com.mdavis8403.magickingdomtrivia.data

import kotlinx.serialization.Serializable

@Serializable
enum class Difficulty(val displayName: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard"),
    MIXED("Mixed"),
}

@Serializable
data class TriviaCategory(
    val id: String,
    val title: String,
    val description: String,
    val accentColor: Long,
)

@Serializable
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

@Serializable
data class TriviaChoice(
    val text: String,
    val isCorrect: Boolean,
)

@Serializable
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
