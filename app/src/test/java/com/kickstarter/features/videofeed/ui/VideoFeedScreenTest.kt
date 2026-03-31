package com.kickstarter.features.videofeed.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class VideoFeedScreenTest : KSRobolectricTestCase() {

    @Test
    fun `VideoFeedScreen displays pager and verifies beyondViewportPageCount and key usage`() {
        val project1Id = 101L
        val project2Id = 102L
        val project3Id = 103L

        val projects = listOf(
            ProjectFactory.project().toBuilder().id(project1Id).build(),
            ProjectFactory.caProject().toBuilder().id(project2Id).build(),
            ProjectFactory.ukProject().toBuilder().id(project3Id).build()
        )

        composeTestRule.setContent {
            KSTheme {
                VideoFeedScreen(projectsList = projects)
            }
        }

        // --- Initial State (Page 0) ---
        // Verify Pager exists
        composeTestRule.onNodeWithTag(VideoFeedScreenTestTag.VIDEO_FEED_PAGER.name)
            .assertIsDisplayed()

        // Verify Page 0 is displayed and associated with correct Project ID
        composeTestRule.onNodeWithTag("${VideoFeedScreenTestTag.VIDEO_FEED_OVERLAY_CONTAINER.name}_$project1Id", useUnmergedTree = true)
            .assertIsDisplayed()

        // Verify beyondViewportPageCount = 1: Page 1 should exist in composition but not necessarily be fully displayed
        composeTestRule.onNodeWithTag("${VideoFeedScreenTestTag.VIDEO_FEED_OVERLAY_CONTAINER.name}_$project2Id", useUnmergedTree = true)
            .assertExists()

        // 3. Verify Page 2 does NOT exist (it's beyond the viewport + 1)
        composeTestRule.onNodeWithTag("${VideoFeedScreenTestTag.VIDEO_FEED_OVERLAY_CONTAINER.name}_$project3Id", useUnmergedTree = true)
            .assertDoesNotExist()

        // --- Swipe to Page 1 ---
        composeTestRule.onNodeWithTag(VideoFeedScreenTestTag.VIDEO_FEED_PAGER.name)
            .performTouchInput { swipeUp() }
        composeTestRule.waitForIdle()

        // 4. Verify Page 1 is now "displayed" (active page)
        composeTestRule.onNodeWithTag("${VideoFeedScreenTestTag.VIDEO_FEED_OVERLAY_CONTAINER.name}_$project2Id", useUnmergedTree = true)
            .assertExists()

        // 5. Verify Page 0 still exists (it's 1 page behind now)
        composeTestRule.onNodeWithTag("${VideoFeedScreenTestTag.VIDEO_FEED_OVERLAY_CONTAINER.name}_$project1Id", useUnmergedTree = true)
            .assertExists()

        // 6. Verify Page 2 now exists (it's 1 page ahead)
        composeTestRule.onNodeWithTag("${VideoFeedScreenTestTag.VIDEO_FEED_OVERLAY_CONTAINER.name}_$project3Id", useUnmergedTree = true)
            .assertExists()
    }
}
