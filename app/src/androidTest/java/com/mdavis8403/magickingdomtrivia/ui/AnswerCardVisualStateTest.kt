package com.mdavis8403.magickingdomtrivia.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.mdavis8403.magickingdomtrivia.ui.theme.MagicKingdomTriviaTheme
import org.junit.Rule
import org.junit.Test

class AnswerCardVisualStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun focusedAnswer_keepsReadableTextOnJewelToneFill() {
        renderCard(status = AnswerCardStatus.IDLE, requestFocus = true)

        card()
            .assertIsFocused()
            .assertIsDisplayed()
            .assertTextContains(ANSWER_TEXT, substring = true)
            .assertState("FOCUSED")
            .assertColors(
                container = "#FF214F73",
                text = "#FFFFFFFF",
                border = "#FFF5C86A",
            )
    }

    @Test
    fun selectedAnswer_remainsDistinctFromFocusedAnswer() {
        renderCard(status = AnswerCardStatus.SELECTED)

        card()
            .assertTextContains("SELECTED", substring = true)
            .assertState("SELECTED")
            .assertColors(
                container = "#FF39375F",
                text = "#FFFFF1C7",
                border = "#FFF5C86A",
            )
    }

    @Test
    fun selectedCorrectAnswer_hasExplicitReadableSuccessTreatment() {
        renderCard(status = AnswerCardStatus.SELECTED_CORRECT)

        card()
            .assertIsNotEnabled()
            .assertTextContains("YOUR ANSWER - CORRECT", substring = true)
            .assertTextContains(ANSWER_TEXT, substring = true)
            .assertState("SELECTED_CORRECT")
            .assertColors(
                container = "#FF174A3A",
                text = "#FFE0FFF0",
                border = "#FF79E6BE",
            )
    }

    @Test
    fun selectedIncorrectAnswer_staysMarkedAndReadable() {
        renderCard(status = AnswerCardStatus.SELECTED_INCORRECT)

        card()
            .assertIsNotEnabled()
            .assertTextContains("YOUR ANSWER - INCORRECT", substring = true)
            .assertTextContains(ANSWER_TEXT, substring = true)
            .assertState("SELECTED_INCORRECT")
            .assertColors(
                container = "#FF542936",
                text = "#FFFFE4E9",
                border = "#FFFF8E9E",
            )
    }

    @Test
    fun revealedCorrectAnswer_isSeparateFromWrongSelection() {
        renderCard(status = AnswerCardStatus.REVEALED_CORRECT)

        card()
            .assertIsNotEnabled()
            .assertTextContains("CORRECT ANSWER", substring = true)
            .assertTextContains(ANSWER_TEXT, substring = true)
            .assertState("REVEALED_CORRECT")
            .assertColors(
                container = "#FF174A3A",
                text = "#FFE0FFF0",
                border = "#FF79E6BE",
            )
    }

    @Test
    fun disabledAnswer_isDeemphasizedButReadable() {
        renderCard(status = AnswerCardStatus.DISABLED)

        card()
            .assertIsNotEnabled()
            .assertTextContains(ANSWER_TEXT, substring = true)
            .assertState("DISABLED")
            .assertColors(
                container = "#FF111D2D",
                text = "#FFB4C1D3",
                border = "#FF3D506A",
            )
    }

    private fun renderCard(
        status: AnswerCardStatus,
        requestFocus: Boolean = false,
    ) {
        lateinit var focusRequester: FocusRequester
        composeRule.setContent {
            focusRequester = remember { FocusRequester() }
            MagicKingdomTriviaTheme {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF07111F))
                        .padding(24.dp),
                ) {
                    AnswerCard(
                        text = ANSWER_TEXT,
                        status = status,
                        onClick = {},
                        modifier = Modifier
                            .testTag(CARD_TAG)
                            .focusRequester(focusRequester),
                    )
                }
            }
        }
        if (requestFocus) {
            composeRule.runOnIdle { focusRequester.requestFocus() }
        }
        composeRule.waitForIdle()
    }

    private fun card() = composeRule.onNodeWithTag(CARD_TAG)

    private fun androidx.compose.ui.test.SemanticsNodeInteraction.assertState(state: String) =
        assert(SemanticsMatcher.expectValue(AnswerCardStateKey, state))

    private fun androidx.compose.ui.test.SemanticsNodeInteraction.assertColors(
        container: String,
        text: String,
        border: String,
    ) = assert(SemanticsMatcher.expectValue(AnswerCardContainerColorKey, container))
        .assert(SemanticsMatcher.expectValue(AnswerCardTextColorKey, text))
        .assert(SemanticsMatcher.expectValue(AnswerCardBorderColorKey, border))

    private companion object {
        const val CARD_TAG = "answer-card-under-test"
        const val ANSWER_TEXT = "Readable answer text"
    }
}
