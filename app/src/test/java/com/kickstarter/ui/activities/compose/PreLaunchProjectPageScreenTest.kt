package com.kickstarter.ui.activities.compose

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import com.kickstarter.KSRobolectricTestCase
import org.junit.Test

class PreLaunchProjectPageScreenTest : KSRobolectricTestCase() {

    @Test
    fun verify_if_all_views_exists() {
        composeTestRule.setContent { // setting our composable as content for test
            MaterialTheme {
                val projectState = remember { mutableStateOf(null) }
                PreLaunchProjectPageScreen(projectState)
            }
        }
        composeTestRule.onNodeWithTag("Project Image").assertExists()
        composeTestRule.onNodeWithTag("Coming soon badge").assertExists()
        composeTestRule.onNodeWithTag("Project name").assertTextEquals("")
        composeTestRule.onNodeWithTag("Project name").assertIsNotDisplayed()
    }
}
