package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MainActivityLaunchRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `main activity cold starts cleanly as standard app and renders dock and seeded tags`() {
        composeRule.waitForIdle()
        // Validate the current home dock contract: global search and the app drawer.
        // The previous five-button dock tags belonged to an older UI implementation.
        composeRule.onNodeWithTag("home_global_search_bar").assertIsDisplayed()
        composeRule.onNodeWithTag("home_open_drawer_button").assertIsDisplayed()
        composeRule.onNodeWithTag("home_canvas_1").assertIsDisplayed()
    }
}
