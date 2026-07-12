package com.mdavis8403.magickingdomtrivia.domain

import com.mdavis8403.magickingdomtrivia.data.Difficulty
import com.mdavis8403.magickingdomtrivia.data.PresentedQuestion
import com.mdavis8403.magickingdomtrivia.data.TriviaCategory
import com.mdavis8403.magickingdomtrivia.data.TriviaChoice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameStateCodecTest {
    private val codec = GameStateCodec()

    @Test
    fun encodeAndDecode_preservesActiveRoundState() {
        val settings = GameSettings(categoryId = "pixar", timerSeconds = 15)
        val category = TriviaCategory("pixar", "Pixar", "Pixar questions", 0xFF123456)
        val question = PresentedQuestion(
            id = "pixar_001",
            prompt = "A question?",
            choices = listOf(
                TriviaChoice("Wrong", false),
                TriviaChoice("Correct", true),
                TriviaChoice("Wrong again", false),
                TriviaChoice("Still wrong", false),
            ),
            category = "Pixar",
            difficulty = Difficulty.EASY,
            explanation = "Explanation",
            sourceTitle = "Source",
        )
        val state = TriviaGameState(
            categories = listOf(category),
            settings = settings,
            session = TriviaSession(
                category = category,
                questions = listOf(question),
                settings = settings,
                selectedAnswerIndex = 1,
                answerRevealed = true,
                secondsRemaining = 8,
                score = 1,
            ),
        )

        assertEquals(state, codec.decode(codec.encode(state)))
    }

    @Test
    fun decode_corruptState_returnsNull() {
        assertNull(codec.decode("not json"))
    }
}
