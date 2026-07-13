package com.mdavis8403.magickingdomtrivia.ui

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mdavis8403.magickingdomtrivia.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies D-pad focus behaviour of the image-based home screen. Its controls
 * are transparent overlays on the artwork, so they are located by their
 * accessibility content descriptions rather than by visible text.
 */
@RunWith(AndroidJUnit4::class)
class RemoteFocusNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launch_placesFocusOnPlay() {
        composeRule.onNodeWithContentDescription("Play").assertIsFocused()
    }

    @Test
    fun dpadDown_movesFromPlayToMattProfile() {
        composeRule.onNodeWithContentDescription("Play").performKeyInput {
            pressKey(Key.DirectionDown)
        }
        composeRule.onNodeWithContentDescription("Matt profile").assertIsFocused()
    }

    @Test
    fun dpadNavigationAndSelect_opensCategories() {
        composeRule.onNodeWithContentDescription("Play").performKeyInput {
            pressKey(Key.DirectionDown)
        }
        composeRule.onNodeWithContentDescription("Matt profile").assertIsFocused().performKeyInput {
            pressKey(Key.DirectionDown)
        }
        composeRule.onNodeWithContentDescription("Profiles").assertIsFocused().performKeyInput {
            pressKey(Key.DirectionRight)
        }
        composeRule.onNodeWithContentDescription("Categories").assertIsFocused().performKeyInput {
            pressKey(Key.DirectionCenter)
        }

        composeRule.onNodeWithText("Choose category").assertIsDisplayed()
        composeRule.onNodeWithText("Mixed").assertIsFocused()
    }
}
