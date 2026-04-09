package com.kickstarter.features.videofeed.ui

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.VideoFactory
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class VideoFeedScreenTest : KSRobolectricTestCase() {

    @Test
    fun `VideoFeedScreen displays pager and verifies beyondViewportPageCount and key usage`() {
        val project1Id = 101L
        val project2Id = 102L
        val project3Id = 103L

        // KSVideoPlayer needs a valid hls url
        val video = VideoFactory.hlsVideo()

        val projects = listOf(
            ProjectFactory.project().toBuilder().id(project1Id).video(video).build(),
            ProjectFactory.caProject().toBuilder().id(project2Id).video(video).build(),
            ProjectFactory.ukProject().toBuilder().id(project3Id).video(video).build()
        )

        composeTestRule.setContent {
            KSTheme {
                VideoFeedScreen(projectsList = projects)
            }
        }

        composeTestRule.onNodeWithTag(VideoFeedScreenTestTag.VIDEO_FEED_PAGER.name)
            .assertExists()

        // Page 0 is in the tree and associated with correct Project ID (key test), and displayed
        composeTestRule.onNodeWithTag("${VideoFeedScreenTestTag.VIDEO_FEED_OVERLAY_CONTAINER.name}_$project1Id", useUnmergedTree = true)
            .assertExists()
            .assertIsDisplayed()

        // beyondViewportPageCount = 1: Page 1 should exist in composition, not yet displayed
        composeTestRule.onNodeWithTag("${VideoFeedScreenTestTag.VIDEO_FEED_OVERLAY_CONTAINER.name}_$project2Id", useUnmergedTree = true)
            .assertExists()
            .assertIsNotDisplayed()

        // Page 2 does NOT exist (it's beyond the viewport + 1)
        composeTestRule.onNodeWithTag("${VideoFeedScreenTestTag.VIDEO_FEED_OVERLAY_CONTAINER.name}_$project3Id", useUnmergedTree = true)
            .assertDoesNotExist()

        composeTestRule.onNodeWithTag(VideoFeedScreenTestTag.VIDEO_FEED_PAGER.name)
            .performTouchInput { swipeUp() }
        composeTestRule.waitForIdle()

        // Page 1 exists and displayed
        composeTestRule.onNodeWithTag("${VideoFeedScreenTestTag.VIDEO_FEED_OVERLAY_CONTAINER.name}_$project2Id", useUnmergedTree = true)
            .assertExists()
            .assertIsDisplayed()

        // Page 0 still exists (1 page behind) and not displayed
        composeTestRule.onNodeWithTag("${VideoFeedScreenTestTag.VIDEO_FEED_OVERLAY_CONTAINER.name}_$project1Id", useUnmergedTree = true)
            .assertExists()
            .assertIsNotDisplayed()

        // Page 2 now exists (1 page ahead)
        composeTestRule.onNodeWithTag("${VideoFeedScreenTestTag.VIDEO_FEED_OVERLAY_CONTAINER.name}_$project3Id", useUnmergedTree = true)
            .assertExists()
            .assertIsNotDisplayed()
    }

    @Test
    fun `close button is displayed with correct accessibility role`() {
        val video = VideoFactory.hlsVideo()
        val projects = listOf(
            ProjectFactory.project().toBuilder().video(video).build()
        )

        composeTestRule.setContent {
            KSTheme {
                VideoFeedScreen(projectsList = projects)
            }
        }

        composeTestRule.onNodeWithTag(VideoFeedScreenTestTag.VIDEO_FEED_CLOSE_BUTTON.name)
            .assertIsDisplayed()
            .assert(
                SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
            )
    }

    @Test
    fun `close button triggers onClose callback`() {
        var closeCalled = false
        val video = VideoFactory.hlsVideo()
        val projects = listOf(
            ProjectFactory.project().toBuilder().video(video).build()
        )

        composeTestRule.setContent {
            KSTheme {
                VideoFeedScreen(
                    projectsList = projects,
                    onClose = { closeCalled = true }
                )
            }
        }

        composeTestRule.onNodeWithTag(VideoFeedScreenTestTag.VIDEO_FEED_CLOSE_BUTTON.name)
            .performClick()

        assertTrue(closeCalled)
    }
}
