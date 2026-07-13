package com.mdavis8403.magickingdomtrivia.data

import com.mdavis8403.magickingdomtrivia.domain.GameSettings
import com.mdavis8403.magickingdomtrivia.domain.TriviaGameEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File
import kotlin.random.Random

/**
 * Validates the real bundled production question pack
 * (app/src/main/assets/questions/core_questions.json) as part of the normal
 * unit-test suite, so `./gradlew test` (and CI) fails if the shipped bank ever
 * regresses. This complements the standalone bank's Python validator.
 */
class ProductionQuestionBankTest {

    private val catalog = AssetPack.load()

    @Test
    fun pack_parsesWithoutValidationErrors() {
        if (catalog.validationErrors.isNotEmpty()) {
            fail(
                "Production pack has ${catalog.validationErrors.size} validation error(s):\n" +
                    catalog.validationErrors.take(20).joinToString("\n")
            )
        }
        assertTrue("packId must not be blank", catalog.packId.isNotBlank())
    }

    @Test
    fun pack_loadsExpectedTotals() {
        // 5,000 production questions from the standalone bank + 100 retained
        // original seed questions (4 exact duplicates were dropped).
        assertEquals(EXPECTED_TOTAL, catalog.questions.size)
        assertEquals(EXPECTED_PRODUCTION, production().size)
        assertEquals(EXPECTED_RETAINED, catalog.questions.size - production().size)
    }

    @Test
    fun productionBank_hasExactCategoryTotals() {
        val byCategory = production().groupingBy { it.category }.eachCount()
        assertEquals(EXPECTED_CATEGORY_TOTALS, byCategory.toSortedMap())
    }

    @Test
    fun productionBank_hasExactDifficultyTotals() {
        val byDifficulty = production().groupingBy { it.difficulty }.eachCount()
        assertEquals(EXPECTED_DIFFICULTY_TOTALS, byDifficulty)
    }

    @Test
    fun everyQuestion_hasFourDistinctAnswersAndValidCorrectIndex() {
        catalog.questions.forEach { q ->
            assertEquals("${q.id} must have four answers", 4, q.answers.size)
            assertEquals("${q.id} answers must be distinct", 4, q.answers.distinct().size)
            assertTrue(
                "${q.id} correctAnswerIndex ${q.correctAnswerIndex} out of range",
                q.correctAnswerIndex in 0..3,
            )
            assertTrue("${q.id} has blank fields", q.prompt.isNotBlank() && q.explanation.isNotBlank())
        }
    }

    @Test
    fun questionIds_areUnique() {
        val ids = catalog.questions.map { it.id }
        val duplicates = ids.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        assertTrue("Duplicate question ids: $duplicates", duplicates.isEmpty())
    }

    @Test
    fun questionPrompts_haveNoNormalizedDuplicates() {
        val seen = HashMap<String, String>()
        val collisions = mutableListOf<String>()
        catalog.questions.forEach { q ->
            val key = normalize(q.prompt)
            val prior = seen.put(key, q.id)
            if (prior != null) collisions += "$prior <-> ${q.id}"
        }
        assertTrue("Duplicate prompts: ${collisions.take(10)}", collisions.isEmpty())
    }

    @Test
    fun catalog_exposesAllEightCategoriesPlusMixed() {
        val titles = catalog.categories.map { it.title }.toSet()
        assertTrue("Mixed category missing", "Mixed" in titles)
        EXPECTED_CATEGORY_TOTALS.keys.forEach { category ->
            assertTrue("Category '$category' missing from catalog", category in titles)
        }
    }

    @Test
    fun largeRounds_haveEnoughQuestionsInEveryCategoryAndDifficulty() {
        // Confirms 10/20/30/50-question rounds are always fillable: every
        // (category, single difficulty) slice must hold at least 50 questions.
        val counts = production()
            .groupingBy { it.category to it.difficulty }
            .eachCount()
        counts.forEach { (slice, count) ->
            assertTrue("Slice $slice has only $count questions (<50)", count >= 50)
        }
    }

    @Test
    fun engine_createsRoundsOf10_20_30_50() {
        val engine = TriviaGameEngine(InMemoryQuestionRepository(catalog), Random(1))
        // Mixed category + Mixed difficulty (largest pool).
        listOf(10, 20, 30, 50).forEach { count ->
            val state = engine.startGame(engine.initialState(GameSettings(questionCount = count)))
            assertNotNull("No session for a $count-question Mixed round", state.session)
            assertEquals(count, state.session!!.questions.size)
        }
        // A single category + single difficulty at the largest round size.
        val sliceState = engine.startGame(
            engine.initialState(
                GameSettings(
                    categoryId = categoryIdFor("Disney Princesses"),
                    difficulty = Difficulty.HARD,
                    questionCount = 50,
                ),
            ),
        )
        assertEquals(50, sliceState.session!!.questions.size)
    }

    @Test
    fun engine_randomizedAnswerOrder_preservesCorrectAnswer() {
        val engine = TriviaGameEngine(InMemoryQuestionRepository(catalog), Random(7))
        val byId = catalog.questions.associateBy { it.id }
        val state = engine.startGame(
            engine.initialState(GameSettings(questionCount = 50, randomizeAnswerOrder = true)),
        )
        state.session!!.questions.forEach { presented ->
            val correctChoices = presented.choices.filter { it.isCorrect }
            assertEquals("Exactly one correct choice for ${presented.id}", 1, correctChoices.size)
            val original = byId.getValue(presented.id)
            assertEquals(
                "Correct answer must survive shuffling for ${presented.id}",
                original.answers[original.correctAnswerIndex],
                correctChoices.single().text,
            )
        }
    }

    private fun production() = catalog.questions.filter { PRODUCTION_ID.matches(it.id) }

    private companion object {
        const val EXPECTED_TOTAL = 5100
        const val EXPECTED_PRODUCTION = 5000
        const val EXPECTED_RETAINED = 100
        val PRODUCTION_ID = Regex(".*_(easy|medium|hard)_\\d{3}$")

        val EXPECTED_CATEGORY_TOTALS = sortedMapOf(
            "Disney Animation" to 1000,
            "Disney Parks" to 700,
            "Disney Princesses" to 500,
            "Disney Songs" to 500,
            "Live Action Disney" to 500,
            "Marvel" to 600,
            "Pixar" to 700,
            "Star Wars" to 500,
        )
        val EXPECTED_DIFFICULTY_TOTALS = mapOf(
            Difficulty.EASY to 1730,
            Difficulty.MEDIUM to 1790,
            Difficulty.HARD to 1480,
        )

        fun normalize(text: String): String =
            text.lowercase()
                .replace(Regex("[^\\p{L}\\p{Nd}\\s]"), " ")
                .split(Regex("\\s+"))
                .filter { it.isNotBlank() && it !in setOf("a", "an", "the") }
                .joinToString(" ")
    }
}

/** Locates and loads the bundled asset from the JVM test working directory. */
private object AssetPack {
    fun load(): QuestionCatalog {
        val relative = "src/main/assets/questions/core_questions.json"
        val candidates = listOf(
            File(relative),
            File("app/$relative"),
            File(System.getProperty("user.dir"), relative),
            File(System.getProperty("user.dir"), "app/$relative"),
        )
        val file = candidates.firstOrNull { it.exists() }
            ?: error("Could not locate core_questions.json. Tried: ${candidates.map { it.absolutePath }}")
        return QuestionJsonParser().parse(file.readText())
    }
}
