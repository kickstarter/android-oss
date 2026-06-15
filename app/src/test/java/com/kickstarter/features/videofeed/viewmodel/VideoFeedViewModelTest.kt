package com.kickstarter.features.videofeed.viewmodel

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.videofeed.data.VideoFeedEnvelope
import com.kickstarter.features.videofeed.data.VideoFeedItem
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.utils.EventContextValues.CtaContextName
import com.kickstarter.libs.utils.EventName
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VideoFeedViewModelTest : KSRobolectricTestCase() {

    private lateinit var viewModel: VideoFeedViewModel

    private fun setUpEnvironment(environment: Environment, dispatcher: CoroutineDispatcher, entrySurface: String = "") {
        viewModel = VideoFeedViewModel.Factory(environment, entrySurface, dispatcher)
            .create(VideoFeedViewModel::class.java)
    }

    @Test
    fun `loadVideoFeed emits hasMore false when pageInfo hasNextPage is false`() = runTest {
        val item = VideoFeedItem(badges = emptyList(), project = ProjectFactory.project(), hlsUrl = null)
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val pageInfo = PageInfoEnvelope.builder().hasNextPage(false).endCursor("cursor1").build()

        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override suspend fun getVideoFeed(first: Int, cursor: String?, categoryId: String?): Result<VideoFeedEnvelope> {
                    return Result.success(VideoFeedEnvelope(items = listOf(item), pageInfo = pageInfo))
                }
            })
            .build()

        setUpEnvironment(environment, dispatcher)
        viewModel.loadVideoFeed()
        advanceUntilIdle()

        assertFalse(viewModel.videoFeedUIState.value.hasMore)
    }

    @Test
    fun `loadVideoFeed emits hasMore true when pageInfo hasNextPage is true`() = runTest {
        val item = VideoFeedItem(badges = emptyList(), project = ProjectFactory.project(), hlsUrl = null)
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val pageInfo = PageInfoEnvelope.builder().hasNextPage(true).endCursor("cursor1").build()

        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override suspend fun getVideoFeed(first: Int, cursor: String?, categoryId: String?): Result<VideoFeedEnvelope> {
                    return Result.success(VideoFeedEnvelope(items = listOf(item), pageInfo = pageInfo))
                }
            })
            .build()

        setUpEnvironment(environment, dispatcher)
        viewModel.loadVideoFeed()
        advanceUntilIdle()

        assertTrue(viewModel.videoFeedUIState.value.hasMore)
    }

    @Test
    fun `loadVideoFeed on success emits items`() = runTest {
        val project = ProjectFactory.project()
        val item = VideoFeedItem(badges = emptyList(), project = project, hlsUrl = null)
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override suspend fun getVideoFeed(first: Int, cursor: String?, categoryId: String?): Result<VideoFeedEnvelope> {
                    return Result.success(VideoFeedEnvelope(items = listOf(item)))
                }
            })
            .build()

        setUpEnvironment(environment, dispatcher)
        viewModel.loadVideoFeed()
        advanceUntilIdle()

        assertEquals(listOf(item), viewModel.videoFeedUIState.value.items)
        assertFalse(viewModel.videoFeedUIState.value.isLoading)
    }

    @Test
    fun `loadVideoFeed sets isLoading true while fetching and false once items arrive`() = runTest {
        val item = VideoFeedItem(badges = emptyList(), project = ProjectFactory.project(), hlsUrl = null)
        val dispatcher = StandardTestDispatcher(testScheduler)
        val feedSignal = CompletableDeferred<Result<VideoFeedEnvelope>>()

        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override suspend fun getVideoFeed(first: Int, cursor: String?, categoryId: String?): Result<VideoFeedEnvelope> =
                    feedSignal.await()
            })
            .build()

        setUpEnvironment(environment, dispatcher)
        viewModel.loadVideoFeed()
        // Run the coroutine up to the suspended network call, not beyond
        testScheduler.runCurrent()

        // While the request is in flight, isLoading is true and no items are present yet
        assertTrue(viewModel.videoFeedUIState.value.isLoading)
        assertTrue(viewModel.videoFeedUIState.value.items.isEmpty())

        feedSignal.complete(Result.success(VideoFeedEnvelope(items = listOf(item))))
        advanceUntilIdle()

        assertFalse(viewModel.videoFeedUIState.value.isLoading)
        assertEquals(listOf(item), viewModel.videoFeedUIState.value.items)
    }

    @Test
    fun `loadVideoFeed does not start a second request while one is already in flight`() = runTest {
        val item = VideoFeedItem(badges = emptyList(), project = ProjectFactory.project(), hlsUrl = null)
        val dispatcher = StandardTestDispatcher(testScheduler)
        val feedSignal = CompletableDeferred<Result<VideoFeedEnvelope>>()
        var fetchCount = 0

        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override suspend fun getVideoFeed(first: Int, cursor: String?, categoryId: String?): Result<VideoFeedEnvelope> {
                    fetchCount++
                    return feedSignal.await()
                }
            })
            .build()

        setUpEnvironment(environment, dispatcher)
        viewModel.loadVideoFeed()
        testScheduler.runCurrent()

        // A second call while loading is a no-op
        viewModel.loadVideoFeed()
        testScheduler.runCurrent()

        feedSignal.complete(Result.success(VideoFeedEnvelope(items = listOf(item))))
        advanceUntilIdle()

        assertEquals(1, fetchCount)
    }

    @Test
    fun `bookmarkProject on unstarred project calls watchProjectSuspend and sets isStarred true`() = runTest {
        val project = ProjectFactory.project().toBuilder().id(1L).isStarred(false).build()
        val item = VideoFeedItem(badges = emptyList(), project = project, hlsUrl = null)
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        var watchCalled = false
        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override suspend fun getVideoFeed(first: Int, cursor: String?, categoryId: String?): Result<VideoFeedEnvelope> =
                    Result.success(VideoFeedEnvelope(items = listOf(item)))

                override suspend fun watchProjectSuspend(project: Project): Result<Project> {
                    watchCalled = true
                    return Result.success(project.toBuilder().isStarred(true).build())
                }
            })
            .build()

        setUpEnvironment(environment, dispatcher)
        viewModel.loadVideoFeed()
        advanceUntilIdle()

        viewModel.bookmarkProject(project, 0)
        advanceUntilIdle()

        assertTrue(watchCalled)
        val updated = viewModel.videoFeedUIState.value.items.first { it.project.id() == project.id() }
        assertTrue(updated.project.isStarred())
    }

    @Test
    fun `bookmarkProject on starred project calls unWatchProjectSuspend and sets isStarred false`() = runTest {
        val project = ProjectFactory.project().toBuilder().id(2L).isStarred(true).build()
        val item = VideoFeedItem(badges = emptyList(), project = project, hlsUrl = null)
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        var unWatchCalled = false
        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override suspend fun getVideoFeed(first: Int, cursor: String?, categoryId: String?): Result<VideoFeedEnvelope> =
                    Result.success(VideoFeedEnvelope(items = listOf(item)))

                override suspend fun unWatchProjectSuspend(project: Project): Result<Project> {
                    unWatchCalled = true
                    return Result.success(project.toBuilder().isStarred(false).build())
                }
            })
            .build()

        setUpEnvironment(environment, dispatcher)
        viewModel.loadVideoFeed()
        advanceUntilIdle()

        viewModel.bookmarkProject(project, 0)
        advanceUntilIdle()

        assertTrue(unWatchCalled)
        val updated = viewModel.videoFeedUIState.value.items.first { it.project.id() == project.id() }
        assertFalse(updated.project.isStarred())
    }

    @Test
    fun `bookmarkProject applies optimistic state before API response`() = runTest {
        val project = ProjectFactory.project().toBuilder().id(3L).isStarred(false).build()
        val item = VideoFeedItem(badges = emptyList(), project = project, hlsUrl = null)
        val dispatcher = StandardTestDispatcher(testScheduler)
        val apiSignal = CompletableDeferred<Result<Project>>()

        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override suspend fun getVideoFeed(first: Int, cursor: String?, categoryId: String?): Result<VideoFeedEnvelope> =
                    Result.success(VideoFeedEnvelope(items = listOf(item)))

                override suspend fun watchProjectSuspend(project: Project): Result<Project> =
                    apiSignal.await()
            })
            .build()

        setUpEnvironment(environment, dispatcher)
        viewModel.loadVideoFeed()
        advanceUntilIdle()

        viewModel.bookmarkProject(project, 0)
        // Run the coroutine only up to the suspended API call, not beyond
        testScheduler.runCurrent()

        // Optimistic update should be visible before the API responds
        val optimisticItem = viewModel.videoFeedUIState.value.items.first { it.project.id() == project.id() }
        assertTrue(optimisticItem.project.isStarred())

        // API succeeds — final state stays starred
        apiSignal.complete(Result.success(project.toBuilder().isStarred(true).build()))
        advanceUntilIdle()

        val finalItem = viewModel.videoFeedUIState.value.items.first { it.project.id() == project.id() }
        assertTrue(finalItem.project.isStarred())
    }

    @Test
    fun `bookmarkProject reverts optimistic state when API fails`() = runTest {
        val project = ProjectFactory.project().toBuilder().id(4L).isStarred(false).build()
        val item = VideoFeedItem(badges = emptyList(), project = project, hlsUrl = null)
        val dispatcher = StandardTestDispatcher(testScheduler)
        val apiSignal = CompletableDeferred<Result<Project>>()

        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override suspend fun getVideoFeed(first: Int, cursor: String?, categoryId: String?): Result<VideoFeedEnvelope> =
                    Result.success(VideoFeedEnvelope(items = listOf(item)))

                override suspend fun watchProjectSuspend(project: Project): Result<Project> =
                    apiSignal.await()
            })
            .build()

        var errorCalled = false
        setUpEnvironment(environment, dispatcher)
        viewModel.provideErrorAction { errorCalled = true }
        viewModel.loadVideoFeed()
        advanceUntilIdle()

        viewModel.bookmarkProject(project, 0)
        testScheduler.runCurrent()

        // Optimistic state is applied
        assertTrue(viewModel.videoFeedUIState.value.items.first { it.project.id() == project.id() }.project.isStarred())

        // API fails — state is reverted
        apiSignal.complete(Result.failure(Exception("network error")))
        advanceUntilIdle()

        assertTrue(errorCalled)
        val reverted = viewModel.videoFeedUIState.value.items.first { it.project.id() == project.id() }
        assertFalse(reverted.project.isStarred())
    }

    @Test
    fun `bookmarkProject on failure reverts to original state`() = runTest {
        val project = ProjectFactory.project().toBuilder().id(5L).isStarred(false).build()
        val item = VideoFeedItem(badges = emptyList(), project = project, hlsUrl = null)
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override suspend fun getVideoFeed(first: Int, cursor: String?, categoryId: String?): Result<VideoFeedEnvelope> =
                    Result.success(VideoFeedEnvelope(items = listOf(item)))

                override suspend fun watchProjectSuspend(project: Project): Result<Project> =
                    Result.failure(Exception("network error"))
            })
            .build()

        setUpEnvironment(environment, dispatcher)

        var errorCalled = false
        viewModel.provideErrorAction { errorCalled = true }
        viewModel.loadVideoFeed()
        advanceUntilIdle()

        viewModel.bookmarkProject(project, 0)
        advanceUntilIdle()

        assertTrue(errorCalled)
        val unchanged = viewModel.videoFeedUIState.value.items.first { it.project.id() == project.id() }
        assertFalse(unchanged.project.isStarred())
    }

    @Test
    fun `bookmarkProject only updates the matching project in the list`() = runTest {
        val project1 = ProjectFactory.project().toBuilder().id(10L).isStarred(false).build()
        val project2 = ProjectFactory.caProject().toBuilder().id(11L).isStarred(false).build()
        val items = listOf(
            VideoFeedItem(badges = emptyList(), project = project1, hlsUrl = null),
            VideoFeedItem(badges = emptyList(), project = project2, hlsUrl = null)
        )
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override suspend fun getVideoFeed(first: Int, cursor: String?, categoryId: String?): Result<VideoFeedEnvelope> =
                    Result.success(VideoFeedEnvelope(items = items))
            })
            .build()

        setUpEnvironment(environment, dispatcher)
        viewModel.loadVideoFeed()
        advanceUntilIdle()

        viewModel.bookmarkProject(project1, 0)
        advanceUntilIdle()

        val updatedItems = viewModel.videoFeedUIState.value.items
        assertTrue(updatedItems.first { it.project.id() == project1.id() }.project.isStarred())
        assertFalse(updatedItems.first { it.project.id() == project2.id() }.project.isStarred())
    }

    @Test
    fun `isUserLoggedIn is true when a user is logged in`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val environment = environment().toBuilder()
            .currentUserV2(MockCurrentUserV2(UserFactory.user()))
            .build()

        setUpEnvironment(environment, dispatcher)
        advanceUntilIdle()

        assertTrue(viewModel.isUserLoggedIn.value)
    }

    @Test
    fun `isUserLoggedIn is false when no user is logged in`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val environment = environment().toBuilder()
            .currentUserV2(MockCurrentUserV2())
            .build()

        setUpEnvironment(environment, dispatcher)
        advanceUntilIdle()

        assertFalse(viewModel.isUserLoggedIn.value)
    }

    @Test
    fun `isUserLoggedIn updates when user logs in`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val currentUser = MockCurrentUserV2()
        val environment = environment().toBuilder()
            .currentUserV2(currentUser)
            .build()

        setUpEnvironment(environment, dispatcher)
        advanceUntilIdle()

        assertFalse(viewModel.isUserLoggedIn.value)

        currentUser.login(UserFactory.user())
        advanceUntilIdle()

        assertTrue(viewModel.isUserLoggedIn.value)
    }

    // region Analytics

    @Test
    fun `onVideoImpression sends PAGE_VIEWED`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val item = VideoFeedItem(badges = emptyList(), project = ProjectFactory.project(), hlsUrl = null)
        setUpEnvironment(environment(), dispatcher)

        viewModel.onVideoImpression(item, position = 0)

        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun `onVideoPageSettled sends PAGE_VIEWED`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val toItem = VideoFeedItem(badges = emptyList(), project = ProjectFactory.project(), hlsUrl = null)
        val fromItem = VideoFeedItem(badges = emptyList(), project = ProjectFactory.caProject(), hlsUrl = null)
        setUpEnvironment(environment(), dispatcher)

        viewModel.onVideoPageSettled(toItem, 1, fromItem, 4000L, 30000L)

        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun `onCTAClicked sends CTA_CLICKED`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val project = ProjectFactory.project()
        setUpEnvironment(environment(), dispatcher)

        viewModel.onCTAClicked(project, CtaContextName.VIDEO_SAVE)

        segmentTrack.assertValue(EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun `onProgressBarTapped sends CTA_CLICKED`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val item = VideoFeedItem(badges = emptyList(), project = ProjectFactory.project(), hlsUrl = null)
        setUpEnvironment(environment(), dispatcher)

        viewModel.onProgressBarTapped(item, percentageWatched = 0.5f)

        segmentTrack.assertValue(EventName.CTA_CLICKED.eventName)
    }

    // endregion
}
