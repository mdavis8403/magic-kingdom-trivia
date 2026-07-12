package com.mdavis8403.magickingdomtrivia.data

interface QuestionRepository {
    val catalog: QuestionCatalog

    fun questions(
        categoryId: String,
        difficulty: Difficulty,
    ): List<TriviaQuestion> {
        return catalog.questions.filter { question ->
            val categoryMatches = categoryId == MIXED_CATEGORY_ID || categoryIdFor(question.category) == categoryId
            val difficultyMatches = difficulty == Difficulty.MIXED || question.difficulty == difficulty
            categoryMatches && difficultyMatches
        }
    }

    companion object {
        const val MIXED_CATEGORY_ID = "mixed"
    }
}

class InMemoryQuestionRepository(
    override val catalog: QuestionCatalog,
) : QuestionRepository

fun categoryIdFor(title: String): String = title
    .lowercase()
    .map { character -> if (character.isLetterOrDigit()) character else '-' }
    .joinToString("")
    .replace(Regex("-+"), "-")
    .trim('-')
