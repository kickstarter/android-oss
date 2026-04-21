package com.kickstarter.features.videofeed.viewmodel

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.videofeed.data.VideoFeedEnvelope
import com.kickstarter.features.videofeed.data.VideoFeedItem
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Project
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VideoFeedViewModelTest : KSRobolectricTestCase() {

    private fun createViewModel(
        apolloClient: MockApolloClientV2 = MockApolloClientV2(),
        dispatcher: CoroutineDispatcher
    ): VideoFeedViewModel {
        val env = environment().toBuilder().apolloClientV2(apolloClient).build()
        return VideoFeedViewModel.Factory(env, dispatcher).create(VideoFeedViewModel::class.java)
    }

    @Test
    fun `loadVideoFeed on success emits items`() = runTest {
        val project = ProjectFactory.project()
        val item = VideoFeedItem(badges = emptyList(), project = project, hlsUrl = null)
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        val vm = createViewModel(
            apolloClient = object : MockApolloClientV2() {
                override suspend fun getVideoFeed(first: Int, cursor: String?, categoryId: String?): Result<VideoFeedEnvelope> {
                    return Result.success(VideoFeedEnvelope(items = listOf(item)))
                }
            },
            dispatcher = dispatcher
        )
        advanceUntilIdle()

        assertEquals(listOf(item), vm.videoFeedUIState.value.items)
        assertFalse(vm.videoFeedUIState.value.isLoading)
    }

//    @Test
//    fun `loadVideoFeed on failure emits empty state and calls error action`() = runTest {
//        var errorCalled = false
//        // StandardTestDispatcher queues coroutines without running them eagerly,
//        // so provideErrorAction is set before loadVideoFeed executes.
//        val dispatcher = StandardTestDispatcher(testScheduler)
//
//        val vm = createViewModel(
//            apolloClient = object : MockApolloClientV2() {
//                override suspend fun getVideoFeed(first: Int, cursor: String?, categoryId: String?): Result<VideoFeedEnvelope> {
//                    return Result.failure(Exception("network error"))
//                }
//            },
//            dispatcher = dispatcher
//        )
//        vm.provideErrorAction { errorCalled = true }
//        advanceUntilIdle()
//
//        assertTrue(vm.videoFeedUIState.value.items.isEmpty())
//        assertFalse(vm.videoFeedUIState.value.isLoading)
//        assertTrue(errorCalled)
//    }

    @Test
    fun `bookmarkProject on unstarred project calls watchProjectSuspend and sets isStarred true`() = runTest {
        val project = ProjectFactory.project().toBuilder().id(1L).isStarred(false).build()
        val item = VideoFeedItem(badges = emptyList(), project = project, hlsUrl = null)
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        var watchCalled = false
        val vm = createViewModel(
            apolloClient = object : MockApolloClientV2() {
                override suspend fun getVideoFeed(first: Int, cursor: String?, categoryId: String?): Result<VideoFeedEnvelope> =
                    Result.success(VideoFeedEnvelope(items = listOf(item)))

                override suspend fun watchProjectSuspend(project: Project): Result<Project> {
                    watchCalled = true
                    return Result.success(project.toBuilder().isStarred(true).build())
                }
            },
            dispatcher = dispatcher
        )
        advanceUntilIdle()

        vm.bookmarkProject(project)
        advanceUntilIdle()

        assertTrue(watchCalled)
        val updated = vm.videoFeedUIState.value.items.first { it.project.id() == project.id() }
        assertTrue(updated.project.isStarred())
    }

    @Test
    fun `bookmarkProject on starred project calls unWatchProjectSuspend and sets isStarred false`() = runTest {
        val project = ProjectFactory.project().toBuilder().id(2L).isStarred(true).build()
        val item = VideoFeedItem(badges = emptyList(), project = project, hlsUrl = null)
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        var unWatchCalled = false
        val vm = createViewModel(
            apolloClient = object : MockApolloClientV2() {
                override suspend fun getVideoFeed(first: Int, cursor: String?, categoryId: String?): Result<VideoFeedEnvelope> =
                    Result.success(VideoFeedEnvelope(items = listOf(item)))

                override suspend fun unWatchProjectSuspend(project: Project): Result<Project> {
                    unWatchCalled = true
                    return Result.success(project.toBuilder().isStarred(false).build())
                }
            },
            dispatcher = dispatcher
        )
        advanceUntilIdle()

        vm.bookmarkProject(project)
        advanceUntilIdle()

        assertTrue(unWatchCalled)
        val updated = vm.videoFeedUIState.value.items.first { it.project.id() == project.id() }
        assertFalse(updated.project.isStarred())
    }

    @Test
    fun `bookmarkProject on failure does not update items and calls error action`() = runTest {
        val project = ProjectFactory.project().toBuilder().id(3L).isStarred(false).build()
        val item = VideoFeedItem(badges = emptyList(), project = project, hlsUrl = null)
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        var errorCalled = false
        val vm = createViewModel(
            apolloClient = object : MockApolloClientV2() {
                override suspend fun getVideoFeed(first: Int, cursor: String?, categoryId: String?): Result<VideoFeedEnvelope> =
                    Result.success(VideoFeedEnvelope(items = listOf(item)))

                override suspend fun watchProjectSuspend(project: Project): Result<Project> =
                    Result.failure(Exception("network error"))
            },
            dispatcher = dispatcher
        )
        vm.provideErrorAction { errorCalled = true }
        advanceUntilIdle()

        vm.bookmarkProject(project)
        advanceUntilIdle()

        assertTrue(errorCalled)
        val unchanged = vm.videoFeedUIState.value.items.first { it.project.id() == project.id() }
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

        val vm = createViewModel(
            apolloClient = object : MockApolloClientV2() {
                override suspend fun getVideoFeed(first: Int, cursor: String?, categoryId: String?): Result<VideoFeedEnvelope> =
                    Result.success(VideoFeedEnvelope(items = items))
            },
            dispatcher = dispatcher
        )
        advanceUntilIdle()

        vm.bookmarkProject(project1)
        advanceUntilIdle()

        val updatedItems = vm.videoFeedUIState.value.items
        assertTrue(updatedItems.first { it.project.id() == project1.id() }.project.isStarred())
        assertFalse(updatedItems.first { it.project.id() == project2.id() }.project.isStarred())
    }
}
