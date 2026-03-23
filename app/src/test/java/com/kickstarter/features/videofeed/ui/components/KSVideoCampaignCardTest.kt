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
    fun `KSVideoCampaignCard indicator when funded progress less than 100`() {
        composeTestRule.setContent {
            KSTheme {
                KSVideoCampaignCard(
                    title = "Title",
                    subtitle = "Subtitle",
                    buttonText = "Button",
                    onButtonClick = {},
                    progress = 0.5f
                )
            }
        }

    }

    @Test
    fun `KSVideoCampaignCard indicator when funded progress equals of more than 100`() {
        composeTestRule.setContent {
            KSTheme {
                KSVideoCampaignCard(
                    title = "Title",
                    subtitle = "Subtitle",
                    buttonText = "Button",
                    onButtonClick = {},
                    progress = 1.0f
                )
            }
        }

    }
}
