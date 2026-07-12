package com.mdavis8403.magickingdomtrivia.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestionJsonParserTest {
    private val parser = QuestionJsonParser()

    @Test
    fun parse_validQuestion_createsCatalogAndMixedCategory() {
        val catalog = parser.parse(pack(question()))

        assertEquals(1, catalog.questions.size)
        assertEquals("sample_001", catalog.questions.single().id)
        assertEquals(Difficulty.EASY, catalog.questions.single().difficulty)
        assertEquals(listOf("Mixed", "Disney Animation"), catalog.categories.map { it.title })
        assertTrue(catalog.validationErrors.isEmpty())
    }

    @Test
    fun parse_invalidEntries_skipsThemAndReportsEveryError() {
        val invalidAnswerCount = question(id = "bad_count", answers = "[\"A\",\"B\"]")
        val invalidCorrectIndex = question(id = "bad_index", correctIndex = 7)
        val duplicate = question(id = "sample_001")
        val catalog = parser.parse(pack(question(), invalidAnswerCount, invalidCorrectIndex, duplicate))

        assertEquals(1, catalog.questions.size)
        assertEquals(3, catalog.validationErrors.size)
        assertTrue(catalog.validationErrors.any { it.contains("Exactly four answers") })
        assertTrue(catalog.validationErrors.any { it.contains("between 0 and 3") })
        assertTrue(catalog.validationErrors.any { it.contains("unique") })
    }

    @Test
    fun repository_filtersByCategoryAndDifficulty() {
        val first = parser.parse(pack(question(), question(id = "sample_002", difficulty = "Hard")))
        val repository = InMemoryQuestionRepository(first)

        assertEquals(2, repository.questions(QuestionRepository.MIXED_CATEGORY_ID, Difficulty.MIXED).size)
        assertEquals(1, repository.questions("disney-animation", Difficulty.HARD).size)
        assertTrue(repository.questions("pixar", Difficulty.MIXED).isEmpty())
    }

    private fun pack(vararg questions: String) =
        """{"packId":"test-pack","questions":[${questions.joinToString(",")}]}"""

    private fun question(
        id: String = "sample_001",
        answers: String = "[\"A\",\"B\",\"C\",\"D\"]",
        correctIndex: Int = 0,
        difficulty: String = "Easy",
    ) = """
        {
          "id":"$id",
          "question":"A clear question?",
          "answers":$answers,
          "correctAnswerIndex":$correctIndex,
          "category":"Disney Animation",
          "difficulty":"$difficulty",
          "explanation":"A concise explanation.",
          "sourceTitle":"Sample"
        }
    """.trimIndent()
}
