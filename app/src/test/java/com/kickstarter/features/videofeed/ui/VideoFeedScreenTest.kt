package com.kickstarter.features.videofeed.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.videofeed.data.KSVideoBadgeType
import com.kickstarter.features.videofeed.data.VideoFeedItem
import com.kickstarter.features.videofeed.ui.components.KSVideoActionsColumnTestTag
import com.kickstarter.features.videofeed.ui.components.KSVideoCampaignCardTestTag
import com.kickstarter.libs.RefTag
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.Project
import com.kickstarter.ui.compose.designsystem.KSSnackbarTypes
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class VideoFeedScreenTest : KSRobolectricTestCase() {

    private val hlsUrl = "https://ksr-video.imgix.net/projects/3275127/video-865539-hls_playlist.m3u8"

    @Test
    fun `VideoFeedScreen displays pager and verifies beyondViewportPageCount and key usage`() {
        val project1Id = 101L
        val project2Id = 102L
        val project3Id = 103L

        val items = listOf(
            VideoFeedItem(
                badges = listOf(KSVideoBadgeType.ProjectWeLove),
                project = ProjectFactory.project().toBuilder().id(project1Id).build(),
                hlsUrl = hlsUrl
            ),
            VideoFeedItem(
                badges = listOf(KSVideoBadgeType.JustLaunched),
                project = ProjectFactory.caProject().toBuilder().id(project2Id).build(),
                hlsUrl = hlsUrl
            ),
            VideoFeedItem(
                badges = listOf(KSVideoBadgeType.Trending),
                project = ProjectFactory.ukProject().toBuilder().id(project3Id).build(),
                hlsUrl = hlsUrl
            )
        )

        composeTestRule.setContent {
            KSTheme {
                VideoFeedScreen(items = items)
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
        val project = ProjectFactory.project().toBuilder().id(201L).build()
        val items = listOf(
            VideoFeedItem(badges = emptyList(), project = project, hlsUrl = hlsUrl)
        )

        composeTestRule.setContent {
            KSTheme {
                VideoFeedScreen(items = items)
            }
        }

        composeTestRule.onNodeWithTag("${VideoFeedScreenTestTag.VIDEO_FEED_CLOSE_BUTTON.name}_${project.id()}")
            .assertIsDisplayed()
            .assert(
                SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
            )
    }

    @Test
    fun `close button triggers onClose callback`() {
        var closeCalled = false
        val project = ProjectFactory.project().toBuilder().id(301L).build()
        val items = listOf(
            VideoFeedItem(badges = emptyList(), project = project, hlsUrl = hlsUrl)
        )

        composeTestRule.setContent {
            KSTheme {
                VideoFeedScreen(
                    items = items,
                    onClose = { closeCalled = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("${VideoFeedScreenTestTag.VIDEO_FEED_CLOSE_BUTTON.name}_${project.id()}")
            .performClick()

        assertTrue(closeCalled)
    }

    @Test
    fun `each page has its own close button`() {
        val project1Id = 401L
        val project2Id = 402L

        val items = listOf(
            VideoFeedItem(
                badges = emptyList(),
                project = ProjectFactory.project().toBuilder().id(project1Id).build(),
                hlsUrl = hlsUrl
            ),
            VideoFeedItem(
                badges = emptyList(),
                project = ProjectFactory.caProject().toBuilder().id(project2Id).build(),
                hlsUrl = hlsUrl
            )
        )

        composeTestRule.setContent {
            KSTheme {
                VideoFeedScreen(items = items)
            }
        }

        // Page 0 close button is displayed
        composeTestRule.onNodeWithTag("${VideoFeedScreenTestTag.VIDEO_FEED_CLOSE_BUTTON.name}_$project1Id", useUnmergedTree = true)
            .assertExists()
            .assertIsDisplayed()

        // Page 1 close button exists (beyondViewportPageCount = 1) but is not displayed
        composeTestRule.onNodeWithTag("${VideoFeedScreenTestTag.VIDEO_FEED_CLOSE_BUTTON.name}_$project2Id", useUnmergedTree = true)
            .assertExists()
            .assertIsNotDisplayed()

        // Swipe to page 1
        composeTestRule.onNodeWithTag(VideoFeedScreenTestTag.VIDEO_FEED_PAGER.name)
            .performTouchInput { swipeUp() }
        composeTestRule.waitForIdle()

        // Page 1 close button is now displayed
        composeTestRule.onNodeWithTag("${VideoFeedScreenTestTag.VIDEO_FEED_CLOSE_BUTTON.name}_$project2Id", useUnmergedTree = true)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun `profile button triggers onProfileClick with the current page project`() {
        var capturedProject: Project? = null

        val project = ProjectFactory.project().toBuilder().id(901L).build()
        val items = listOf(VideoFeedItem(badges = emptyList(), project = project, hlsUrl = hlsUrl))

        composeTestRule.setContent {
            KSTheme {
                VideoFeedScreen(
                    items = items,
                    onProfileClick = { capturedProject = it }
                )
            }
        }

        composeTestRule.onNodeWithTag(KSVideoActionsColumnTestTag.PROFILE_BUTTON.name, useUnmergedTree = true)
            .performClick()

        assertEquals(project, capturedProject)
    }

    @Test
    fun `profile button on second page passes the correct project`() {
        var capturedProject: Project? = null

        val project1 = ProjectFactory.project().toBuilder().id(902L).build()
        val project2 = ProjectFactory.caProject().toBuilder().id(903L).build()
        val items = listOf(
            VideoFeedItem(badges = emptyList(), project = project1, hlsUrl = hlsUrl),
            VideoFeedItem(badges = emptyList(), project = project2, hlsUrl = hlsUrl)
        )

        composeTestRule.setContent {
            KSTheme {
                VideoFeedScreen(
                    items = items,
                    onProfileClick = { capturedProject = it }
                )
            }
        }

        composeTestRule.onNodeWithTag(VideoFeedScreenTestTag.VIDEO_FEED_PAGER.name)
            .performTouchInput { swipeUp() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("${VideoFeedScreenTestTag.VIDEO_FEED_OVERLAY_CONTAINER.name}_${project2.id()}", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.onNode(
            hasTestTag(KSVideoActionsColumnTestTag.PROFILE_BUTTON.name)
                .and(hasAnyAncestor(hasTestTag("${VideoFeedScreenTestTag.VIDEO_FEED_OVERLAY_CONTAINER.name}_${project2.id()}"))),
            useUnmergedTree = true
        ).performClick()

        assertEquals(project2, capturedProject)
    }

    @Test
    fun `back button triggers projectCallback for a live project`() {
        var capturedProject: Project? = null
        var capturedRefTag: RefTag? = null

        val project = ProjectFactory.project().toBuilder().id(501L).build()
        val items = listOf(VideoFeedItem(badges = emptyList(), project = project, hlsUrl = hlsUrl))

        composeTestRule.setContent {
            KSTheme {
                VideoFeedScreen(
                    items = items,
                    projectCallback = { p, ref ->
                        capturedProject = p
                        capturedRefTag = ref
                    }
                )
            }
        }

        composeTestRule.onNodeWithTag(KSVideoCampaignCardTestTag.BUTTON.name, useUnmergedTree = true)
            .performClick()

        assertEquals(project, capturedProject)
        assertEquals(RefTag.videoFeed(), capturedRefTag)
    }

    @Test
    fun `back button triggers preLaunchedCallback for a pre-launch project`() {
        var capturedProject: Project? = null
        var capturedRefTag: RefTag? = null

        val project = ProjectFactory.prelaunchProject("pre-launch-slug").toBuilder().id(601L).build()
        val items = listOf(VideoFeedItem(badges = emptyList(), project = project, hlsUrl = hlsUrl))

        composeTestRule.setContent {
            KSTheme {
                VideoFeedScreen(
                    items = items,
                    preLaunchedCallback = { p, ref ->
                        capturedProject = p
                        capturedRefTag = ref
                    }
                )
            }
        }

        composeTestRule.onNodeWithTag(KSVideoCampaignCardTestTag.BUTTON.name, useUnmergedTree = true)
            .performClick()

        assertEquals(project, capturedProject)
        assertEquals(RefTag.videoFeed(), capturedRefTag)
    }

    @Test
    fun `back button does not trigger preLaunchedCallback for a live project`() {
        var preLaunchedCalled = false

        val project = ProjectFactory.project().toBuilder().id(701L).build()
        val items = listOf(VideoFeedItem(badges = emptyList(), project = project, hlsUrl = hlsUrl))

        composeTestRule.setContent {
            KSTheme {
                VideoFeedScreen(
                    items = items,
                    preLaunchedCallback = { _, _ -> preLaunchedCalled = true }
                )
            }
        }

        composeTestRule.onNodeWithTag(KSVideoCampaignCardTestTag.BUTTON.name, useUnmergedTree = true)
            .performClick()

        assertFalse(preLaunchedCalled)
    }

    @Test
    fun `back button does not trigger projectCallback for a pre-launch project`() {
        var projectCallbackCalled = false

        val project = ProjectFactory.prelaunchProject("pre-launch-slug").toBuilder().id(801L).build()
        val items = listOf(VideoFeedItem(badges = emptyList(), project = project, hlsUrl = hlsUrl))

        composeTestRule.setContent {
            KSTheme {
                VideoFeedScreen(
                    items = items,
                    projectCallback = { _, _ -> projectCallbackCalled = true }
                )
            }
        }

        composeTestRule.onNodeWithTag(KSVideoCampaignCardTestTag.BUTTON.name, useUnmergedTree = true)
            .performClick()

        assertFalse(projectCallbackCalled)
    }

    @Test
    fun `bookmark button triggers onBookmarkClick with the current page project`() {
        var capturedProject: Project? = null

        val project = ProjectFactory.project().toBuilder().id(1001L).build()
        val items = listOf(VideoFeedItem(badges = emptyList(), project = project, hlsUrl = hlsUrl))

        composeTestRule.setContent {
            KSTheme {
                VideoFeedScreen(
                    items = items,
                    onBookmarkClick = { p, _ -> capturedProject = p }
                )
            }
        }

        composeTestRule.onNode(
            hasTestTag(KSVideoActionsColumnTestTag.BOOKMARK_BUTTON.name)
                .and(hasAnyAncestor(hasTestTag("${VideoFeedScreenTestTag.VIDEO_FEED_OVERLAY_CONTAINER.name}_${project.id()}"))),
            useUnmergedTree = true
        ).performClick()

        assertEquals(project, capturedProject)
    }

    @Test
    fun `bookmark button passes the correct page index`() {
        var capturedIndex: Int? = null

        val project1 = ProjectFactory.project().toBuilder().id(1101L).build()
        val project2 = ProjectFactory.caProject().toBuilder().id(1102L).build()
        val items = listOf(
            VideoFeedItem(badges = emptyList(), project = project1, hlsUrl = hlsUrl),
            VideoFeedItem(badges = emptyList(), project = project2, hlsUrl = hlsUrl)
        )

        composeTestRule.setContent {
            KSTheme {
                VideoFeedScreen(
                    items = items,
                    onBookmarkClick = { _, index -> capturedIndex = index }
                )
            }
        }

        composeTestRule.onNode(
            hasTestTag(KSVideoActionsColumnTestTag.BOOKMARK_BUTTON.name)
                .and(hasAnyAncestor(hasTestTag("${VideoFeedScreenTestTag.VIDEO_FEED_OVERLAY_CONTAINER.name}_${project1.id()}"))),
            useUnmergedTree = true
        ).performClick()
        assertEquals(0, capturedIndex)

        composeTestRule.onNodeWithTag(VideoFeedScreenTestTag.VIDEO_FEED_PAGER.name)
            .performTouchInput { swipeUp() }
        composeTestRule.waitForIdle()

        composeTestRule.onNode(
            hasTestTag(KSVideoActionsColumnTestTag.BOOKMARK_BUTTON.name)
                .and(hasAnyAncestor(hasTestTag("${VideoFeedScreenTestTag.VIDEO_FEED_OVERLAY_CONTAINER.name}_${project2.id()}"))),
            useUnmergedTree = true
        ).performClick()
        assertEquals(1, capturedIndex)
    }

    @Test
    fun `bookmark button on second page passes the correct project`() {
        var capturedProject: Project? = null

        val project1 = ProjectFactory.project().toBuilder().id(1002L).build()
        val project2 = ProjectFactory.caProject().toBuilder().id(1003L).build()
        val items = listOf(
            VideoFeedItem(badges = emptyList(), project = project1, hlsUrl = hlsUrl),
            VideoFeedItem(badges = emptyList(), project = project2, hlsUrl = hlsUrl)
        )

        composeTestRule.setContent {
            KSTheme {
                VideoFeedScreen(
                    items = items,
                    onBookmarkClick = { p, _ -> capturedProject = p }
                )
            }
        }

        composeTestRule.onNodeWithTag(VideoFeedScreenTestTag.VIDEO_FEED_PAGER.name)
            .performTouchInput { swipeUp() }
        composeTestRule.waitForIdle()

        composeTestRule.onNode(
            hasTestTag(KSVideoActionsColumnTestTag.BOOKMARK_BUTTON.name)
                .and(hasAnyAncestor(hasTestTag("${VideoFeedScreenTestTag.VIDEO_FEED_OVERLAY_CONTAINER.name}_${project2.id()}"))),
            useUnmergedTree = true
        ).performClick()

        assertEquals(project2, capturedProject)
    }

    @Test
    fun `onLoadMore is triggered when current page reaches the load threshold`() {
        var loadMoreCallCount = 0

        // With 4 items, threshold = items.size - 3 = 1, so page 0 does NOT trigger, page 1 does
        val items = List(4) { i ->
            VideoFeedItem(
                badges = emptyList(),
                project = ProjectFactory.project().toBuilder().id((2000L + i)).build(),
                hlsUrl = hlsUrl
            )
        }

        composeTestRule.setContent {
            KSTheme {
                VideoFeedScreen(
                    items = items,
                    onLoadMore = { loadMoreCallCount++ }
                )
            }
        }

        composeTestRule.waitForIdle()
        assertEquals(0, loadMoreCallCount)

        composeTestRule.onNodeWithTag(VideoFeedScreenTestTag.VIDEO_FEED_PAGER.name)
            .performTouchInput { swipeUp() }
        composeTestRule.waitForIdle()

        assertTrue(loadMoreCallCount > 0)
    }

    @Test
    fun `onLoadMore is triggered immediately when items count is within threshold`() {
        var loadMoreCalled = false

        // With 3 items, threshold = items.size - 3 = 0, so page 0 triggers immediately
        val items = List(3) { i ->
            VideoFeedItem(
                badges = emptyList(),
                project = ProjectFactory.project().toBuilder().id((3000L + i)).build(),
                hlsUrl = hlsUrl
            )
        }

        composeTestRule.setContent {
            KSTheme {
                VideoFeedScreen(
                    items = items,
                    onLoadMore = { loadMoreCalled = true }
                )
            }
        }

        composeTestRule.waitForIdle()
        assertTrue(loadMoreCalled)
    }

    @Test
    fun `error snackbar is displayed when errorSnackBarHostState receives a message`() {
        val snackbarHostState = SnackbarHostState()
        val errorMessage = "Something went wrong, please try again"
        val project = ProjectFactory.project().toBuilder().id(4001L).build()
        val items = listOf(VideoFeedItem(badges = emptyList(), project = project, hlsUrl = hlsUrl))

        composeTestRule.setContent {
            KSTheme {
                VideoFeedScreen(
                    items = items,
                    errorSnackBarHostState = snackbarHostState
                )
                LaunchedEffect(Unit) {
                    snackbarHostState.showSnackbar(
                        message = errorMessage,
                        actionLabel = KSSnackbarTypes.KS_ERROR.name
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun `VideoFeedScreen renders without crash when items list is empty`() {
        composeTestRule.setContent {
            KSTheme {
                VideoFeedScreen(items = emptyList())
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(VideoFeedScreenTestTag.VIDEO_FEED_PAGER.name).assertExists()
    }
}
