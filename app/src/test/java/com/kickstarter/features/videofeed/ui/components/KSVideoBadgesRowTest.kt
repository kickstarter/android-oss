package com.kickstarter.features.videofeed.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.features.videofeed.data.KSVideoBadgeType
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class KSVideoBadgesRowTest : KSRobolectricTestCase() {

    @Test
    fun `KSVideoBadgesRow shows only 2 badges if more received`() {
        val badges = listOf(
            KSVideoBadgeType.ProjectWeLove,
            KSVideoBadgeType.DaysLeft("3 days left"),
            KSVideoBadgeType.JustLaunched,
            KSVideoBadgeType.Trending
        )

        composeTestRule.setContent {
            KSTheme {
                KSVideoBadgesRow(badges = badges)
            }
        }

        val projectWeLoveText = context().getString(R.string.fpo_Project_We_Love)
        composeTestRule.onNodeWithText(projectWeLoveText).assertIsDisplayed()

        composeTestRule.onNodeWithText("3 days left").assertIsDisplayed()

        // - Third badge (and beyond) should NOT exist/be displayed
        val justLaunchedText = context().getString(R.string.fpo_Just_launched)
        composeTestRule.onNodeWithText(justLaunchedText).assertDoesNotExist()

        val trendingText = context().getString(R.string.fpo_Trending)
        composeTestRule.onNodeWithText(trendingText).assertDoesNotExist()
    }

    @Test
    fun `KSVideoBadgesRow shows only 1 badge`() {
        val badges = listOf(KSVideoBadgeType.NSFW)

        composeTestRule.setContent {
            KSTheme {
                KSVideoBadgesRow(badges = badges)
            }
        }

        composeTestRule.onNodeWithText("NSFW").assertIsDisplayed()
    }
}
