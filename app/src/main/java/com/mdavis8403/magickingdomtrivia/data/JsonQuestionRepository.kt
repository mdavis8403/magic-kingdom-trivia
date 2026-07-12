package com.mdavis8403.magickingdomtrivia.data

import android.content.Context
import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AssetQuestionRepository(
    context: Context,
    assetName: String = "questions/core_questions.json",
) : QuestionRepository {
    override val catalog: QuestionCatalog

    init {
        val json = context.assets.open(assetName).bufferedReader().use { it.readText() }
        catalog = QuestionJsonParser().parse(json)
        catalog.validationErrors.forEach { error -> Log.e(TAG, error) }
    }

    private companion object {
        const val TAG = "QuestionRepository"
    }
}

class QuestionJsonParser(
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    fun parse(content: String): QuestionCatalog {
        val root = try {
            json.parseToJsonElement(content).jsonObject
        } catch (error: Exception) {
            return emptyCatalog("Question pack is not valid JSON: ${error.message}")
        }

        val packId = root["packId"]?.jsonPrimitive?.content.orEmpty()
        val errors = mutableListOf<String>()
        if (packId.isBlank()) errors += "Question pack has a blank or missing packId."

        val entries = root["questions"] as? JsonArray
        if (entries == null) {
            return emptyCatalog("Question pack '$packId' has no questions array.")
        }

        val questions = mutableListOf<TriviaQuestion>()
        val seenIds = mutableSetOf<String>()
        entries.forEachIndexed { index, element ->
            val dto = try {
                json.decodeFromJsonElement<QuestionDto>(element)
            } catch (error: SerializationException) {
                errors += "Question entry $index could not be decoded: ${error.message}"
                return@forEachIndexed
            } catch (error: IllegalArgumentException) {
                errors += "Question entry $index is malformed: ${error.message}"
                return@forEachIndexed
            }

            val validationError = validate(dto, seenIds)
            if (validationError != null) {
                errors += "Question entry $index (${dto.id.ifBlank { "missing id" }}): $validationError"
                return@forEachIndexed
            }

            seenIds += dto.id
            questions += dto.toModel()
        }

        return QuestionCatalog(
            packId = packId,
            questions = questions,
            categories = buildCategories(questions),
            validationErrors = errors,
        )
    }

    private fun validate(dto: QuestionDto, seenIds: Set<String>): String? = when {
        dto.id.isBlank() -> "ID must not be blank."
        dto.id in seenIds -> "ID must be unique."
        dto.question.isBlank() -> "Question text must not be blank."
        dto.answers.size != ANSWER_COUNT -> "Exactly four answers are required."
        dto.answers.any(String::isBlank) -> "Answers must not be blank."
        dto.answers.distinct().size != ANSWER_COUNT -> "Answers must be distinct."
        dto.correctAnswerIndex !in 0 until ANSWER_COUNT -> "Correct answer index must be between 0 and 3."
        dto.category.isBlank() -> "Category must not be blank."
        dto.difficulty.toDifficultyOrNull() == null -> "Difficulty must be Easy, Medium, or Hard."
        dto.explanation.isBlank() -> "Explanation must not be blank."
        dto.sourceTitle.isBlank() -> "Source title must not be blank."
        else -> null
    }

    private fun buildCategories(questions: List<TriviaQuestion>): List<TriviaCategory> {
        val dataCategories = questions
            .map(TriviaQuestion::category)
            .distinct()
            .mapIndexed { index, title ->
                TriviaCategory(
                    id = categoryIdFor(title),
                    title = title,
                    description = "Questions from $title",
                    accentColor = CATEGORY_COLORS[index % CATEGORY_COLORS.size],
                )
            }

        return listOf(
            TriviaCategory(
                id = QuestionRepository.MIXED_CATEGORY_ID,
                title = "Mixed",
                description = "A little magic from every category",
                accentColor = 0xFFF3C969,
            ),
        ) + dataCategories
    }

    private fun emptyCatalog(error: String) = QuestionCatalog(
        packId = "",
        questions = emptyList(),
        categories = emptyList(),
        validationErrors = listOf(error),
    )

    private companion object {
        const val ANSWER_COUNT = 4
        val CATEGORY_COLORS = listOf(
            0xFF4FD1C5,
            0xFFF6AD55,
            0xFFF687B3,
            0xFF68D391,
            0xFFB794F4,
            0xFF63B3ED,
            0xFFFC8181,
            0xFFF6E05E,
        )
    }
}

@Serializable
private data class QuestionDto(
    val id: String = "",
    val question: String = "",
    val answers: List<String> = emptyList(),
    val correctAnswerIndex: Int = -1,
    val category: String = "",
    val difficulty: String = "",
    val explanation: String = "",
    val sourceTitle: String = "",
) {
    fun toModel() = TriviaQuestion(
        id = id,
        prompt = question,
        answers = answers,
        correctAnswerIndex = correctAnswerIndex,
        category = category,
        difficulty = requireNotNull(difficulty.toDifficultyOrNull()),
        explanation = explanation,
        sourceTitle = sourceTitle,
    )
}

private fun String.toDifficultyOrNull(): Difficulty? = when (trim().lowercase()) {
    "easy" -> Difficulty.EASY
    "medium" -> Difficulty.MEDIUM
    "hard" -> Difficulty.HARD
    else -> null
}
