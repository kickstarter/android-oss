package com.kickstarter.features.videofeed.ui.components

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
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

        // Funding value is exposed as a stateDescription (not a contentDescription), and there's
        // no progressBarRangeInfo — so TalkBack no longer reads the raw ring value or "progress bar".
        val expectedState = context().getString(R.string.fpo_percent_funded, progress.toInt())
        composeTestRule.onNodeWithTag(KSVideoCampaignCardTestTag.PROGRESS_INDICATOR.name)
            .assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, expectedState))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ProgressBarRangeInfo))
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

        // Fully funded announces only "Campaign goal reached" (as a state) — no "one point oh"
        // prefix from a progressBarRangeInfo value.
        composeTestRule.onNodeWithTag(KSVideoCampaignCardTestTag.PROGRESS_INDICATOR.name)
            .assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Campaign goal reached"))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ProgressBarRangeInfo))
    }
}
