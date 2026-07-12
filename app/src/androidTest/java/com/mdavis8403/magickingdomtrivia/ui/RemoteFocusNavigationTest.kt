package com.mdavis8403.magickingdomtrivia.ui

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mdavis8403.magickingdomtrivia.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RemoteFocusNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launch_placesFocusOnPlay() {
        composeRule.onNodeWithText("Play").assertIsFocused()
    }

    @Test
    fun dpadDownAndSelect_opensCategories() {
        composeRule.onNodeWithText("Play").performKeyInput {
            pressKey(Key.DirectionDown)
        }
        composeRule.onNodeWithText("Categories").assertIsFocused().performKeyInput {
            pressKey(Key.DirectionCenter)
        }

        composeRule.onNodeWithText("Choose category").assertIsDisplayed()
        composeRule.onNodeWithText("Mixed").assertIsFocused()
    }
}
