package com.kickstarter.features.videofeed.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Assert.assertTrue
import org.junit.Test

class KSVideoCampaignCardTest : KSRobolectricTestCase() {

    @Test
    fun `KSVideoCampaignCard shows title, subtitle and button`() {
        var buttonClicked = false
        val title = "Test Project"
        val subtitle = "Test Subtitle"
        val buttonText = "Back this project"

        composeTestRule.setContent {
            KSTheme {
                KSVideoCampaignCard(
                    title = title,
                    subtitle = subtitle,
                    buttonText = buttonText,
                    onButtonClick = { buttonClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(buttonText).assertIsDisplayed()

        composeTestRule.onNodeWithText(buttonText).performClick()
        assertTrue(buttonClicked)
    }

    @Test
    fun `KSVideoCampaignCard shows backed indicator when isBacked is true`() {
        composeTestRule.setContent {
            KSTheme {
                KSVideoCampaignCard(
                    title = "Title",
                    subtitle = "Subtitle",
                    buttonText = "Button",
                    onButtonClick = {},
                    isBacked = true
                )
            }
        }

        val backedDescription = context().getString(R.string.fpo_You_have_backed_this_project)
        composeTestRule.onNodeWithContentDescription(backedDescription).assertIsDisplayed()
    }

    @Test
    fun `KSVideoCampaignCard shows progress indicator when not backed but progress provided`() {
        composeTestRule.setContent {
            KSTheme {
                KSVideoCampaignCard(
                    title = "Title",
                    subtitle = "Subtitle",
                    buttonText = "Button",
                    onButtonClick = {},
                    isBacked = false,
                    progress = 0.5f,
                    progressText = "50"
                )
            }
        }

        composeTestRule.onNodeWithText("50").assertIsDisplayed()
        
        val backedDescription = context().getString(R.string.fpo_You_have_backed_this_project)
        composeTestRule.onNodeWithContentDescription(backedDescription).assertDoesNotExist()
    }

    @Test
    fun `KSVideoCampaignCard does not show backed indicator when isBacked is false`() {
        composeTestRule.setContent {
            KSTheme {
                KSVideoCampaignCard(
                    title = "Title",
                    subtitle = "Subtitle",
                    buttonText = "Button",
                    onButtonClick = {},
                    isBacked = false
                )
            }
        }

        val backedDescription = context().getString(R.string.fpo_You_have_backed_this_project)
        composeTestRule.onNodeWithContentDescription(backedDescription).assertDoesNotExist()
    }
}
