package com.kickstarter.features.videofeed.viewmodel

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.videofeed.data.VideoFeedEnvelope
import com.kickstarter.features.videofeed.data.VideoFeedItem
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Project
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

    private fun setUpEnvironment(environment: Environment, dispatcher: CoroutineDispatcher) {
        viewModel = VideoFeedViewModel.Factory(environment, dispatcher)
            .create(VideoFeedViewModel::class.java)
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
}
