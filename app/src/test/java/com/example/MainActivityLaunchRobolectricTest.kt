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
        composeRule.onNodeWithTag("dock_drawer_button").assertIsDisplayed()
        composeRule.onNodeWithTag("dock_search_button").assertIsDisplayed()
        composeRule.onNodeWithTag("dock_tags_button").assertIsDisplayed()
        composeRule.onNodeWithTag("dock_builder_button").assertIsDisplayed()
        composeRule.onNodeWithTag("dock_settings_button").assertIsDisplayed()
        composeRule.onNodeWithTag("home_tag_chip_1").assertIsDisplayed()
    }
}
