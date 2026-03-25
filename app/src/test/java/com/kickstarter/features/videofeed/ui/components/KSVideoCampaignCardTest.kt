package com.kickstarter.features.videofeed.ui.components

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class KSVideoCampaignCardTest : KSRobolectricTestCase() {

    @Test
    fun `KSVideoCampaignCard shows title, subtitle and button`() {
        val onButtonClick: () -> Unit = mock()
        val title = "Test Project"
        val subtitle = "Test Subtitle"
        val buttonText = "Back this project"

        composeTestRule.setContent {
            KSTheme {
                KSVideoCampaignCard(
                    title = title,
                    subtitle = subtitle,
                    buttonText = buttonText,
                    onButtonClick = onButtonClick
                )
            }
        }

        // Verify title and subtitle container existence and merged semantics
        composeTestRule.onNodeWithTag(KSVideoCampaignCardTestTag.TITLE_SUBTITLE_CONTAINER.name)
            .assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ContentDescription, listOf("$title, $subtitle")))

        // Verify button
        composeTestRule.onNodeWithTag(KSVideoCampaignCardTestTag.BUTTON.name)
            .assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))

        composeTestRule.onNodeWithTag(KSVideoCampaignCardTestTag.BUTTON.name).performClick()
        verify(onButtonClick).invoke()
    }

    @Test
    fun `KSVideoCampaignCard progress indicator when progress less than 100`() {
        val progress = 50f
        composeTestRule.setContent {
            KSTheme {
                KSVideoCampaignCard(
                    title = "Title",
                    subtitle = "Subtitle",
                    buttonText = "Button",
                    onButtonClick = {},
                    progress = progress
                )
            }
        }

        composeTestRule.onNodeWithTag(KSVideoCampaignCardTestTag.PROGRESS_INDICATOR.name)
            .assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ContentDescription, listOf(""))) // No content description when not fully funded
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "50"))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo(0.5f, 0f..1f)))
    }

    @Test
    fun `KSVideoCampaignCard progress indicator when progress equals or more than 100`() {
        val progress = 100f
        composeTestRule.setContent {
            KSTheme {
                KSVideoCampaignCard(
                    title = "Title",
                    subtitle = "Subtitle",
                    buttonText = "Button",
                    onButtonClick = {},
                    progress = progress
                )
            }
        }

        composeTestRule.onNodeWithTag(KSVideoCampaignCardTestTag.PROGRESS_INDICATOR.name)
            .assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ContentDescription, listOf("Campaign goal reached")))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, ""))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo(1f, 0f..1f)))
    }
}
