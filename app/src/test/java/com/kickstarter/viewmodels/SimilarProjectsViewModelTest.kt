package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Project
import com.kickstarter.viewmodels.projectpage.ParentUiState
import com.kickstarter.viewmodels.projectpage.SimilarProjectsUiState
import com.kickstarter.viewmodels.projectpage.SimilarProjectsViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalStdlibApi::class)
class SimilarProjectsViewModelTest : KSRobolectricTestCase() {

    private lateinit var viewModel: SimilarProjectsViewModel

    private fun setUpEnvironment(environment: Environment, dispatcher: CoroutineDispatcher) {
        viewModel = SimilarProjectsViewModel.Factory(environment, dispatcher)
            .create(SimilarProjectsViewModel::class.java)
    }

    @Test
    fun `test no similar projects are fetched when a project has not been provided`() = runTest {
        val standardDispatcher = coroutineContext[CoroutineDispatcher]!!

        setUpEnvironment(environment(), standardDispatcher)

        val unconfinedDispatcher = UnconfinedTestDispatcher(testScheduler)
        val state = mutableListOf<SimilarProjectsUiState>()
        backgroundScope.launch(unconfinedDispatcher) {
            viewModel.similarProjectsUiState.toList(state)
        }

        advanceUntilIdle()

        assertEquals(1, state.size)
        assertEquals(null, state.last().data)
    }

    @Test
    fun `test similar projects are fetched when a project has been provided`() = runTest {
        val standardDispatcher = coroutineContext[CoroutineDispatcher]!!

        val project = ProjectFactory.successfulProject()
        val similarProjects = listOf(ProjectFactory.project())

        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override suspend fun fetchSimilarProjects(pid: Long): Result<List<Project>> {
                    return Result.success(similarProjects)
                }
            }).build(),
            standardDispatcher
        )

        val unconfinedDispatcher = UnconfinedTestDispatcher(testScheduler)
        val state = mutableListOf<SimilarProjectsUiState>()
        backgroundScope.launch(unconfinedDispatcher) {
            viewModel.similarProjectsUiState.toList(state)
        }

        viewModel.provideProject(project)

        advanceUntilIdle()

        assertEquals(3, state.size)
        assertEquals(similarProjects, state.last().data)
    }

    @Test
    fun `test similar projects are fetched only once for the same provided project`() = runTest {
        val standardDispatcher = coroutineContext[CoroutineDispatcher]!!

        val project = ProjectFactory.successfulProject()
        val similarProjects = listOf(ProjectFactory.project())

        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override suspend fun fetchSimilarProjects(pid: Long): Result<List<Project>> {
                    return Result.success(similarProjects)
                }
            }).build(),
            standardDispatcher
        )

        val unconfinedDispatcher = UnconfinedTestDispatcher(testScheduler)
        val state = mutableListOf<SimilarProjectsUiState>()
        backgroundScope.launch(unconfinedDispatcher) {
            viewModel.similarProjectsUiState.toList(state)
        }

        viewModel.provideProject(project)
        advanceUntilIdle()

        viewModel.provideProject(project)
        advanceUntilIdle()

        assertEquals(3, state.size)
        assertEquals(similarProjects, state.last().data)
    }

    @Test
    fun `test similar projects are fetched twice for different provided projects`() = runTest {
        val standardDispatcher = coroutineContext[CoroutineDispatcher]!!

        setUpEnvironment(
            environment(),
            standardDispatcher
        )

        val unconfinedDispatcher = UnconfinedTestDispatcher(testScheduler)
        val state = mutableListOf<SimilarProjectsUiState>()
        backgroundScope.launch(unconfinedDispatcher) {
            viewModel.similarProjectsUiState.toList(state)
        }

        val project1 = ProjectFactory.successfulProject()

        viewModel.provideProject(project1)
        advanceUntilIdle()

        val project2 = ProjectFactory.failedProject()

        viewModel.provideProject(project2)
        advanceUntilIdle()

        assertEquals(5, state.size)
    }

    @Test
    fun `test ui state for api error`() = runTest {
        val standardDispatcher = coroutineContext[CoroutineDispatcher]!!

        val throwable = Exception("Oops")

        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override suspend fun fetchSimilarProjects(pid: Long): Result<List<Project>> {
                    return Result.failure(throwable)
                }
            }).build(),
            standardDispatcher
        )

        val unconfinedDispatcher = UnconfinedTestDispatcher(testScheduler)
        val state = mutableListOf<SimilarProjectsUiState>()
        backgroundScope.launch(unconfinedDispatcher) {
            viewModel.similarProjectsUiState.toList(state)
        }

        val project = ProjectFactory.successfulProject()

        viewModel.provideProject(project)

        advanceUntilIdle()

        assertEquals(3, state.size)
        assertEquals("Oops", state.last().error?.message)
        assertEquals(emptyList<Project>(), state.last().data)
    }

    @Test
    fun `test parent ui state`() = runTest {
        val standardDispatcher = coroutineContext[CoroutineDispatcher]!!

        setUpEnvironment(environment(), standardDispatcher)

        val unconfinedDispatcher = UnconfinedTestDispatcher(testScheduler)
        val state = mutableListOf<ParentUiState>()
        backgroundScope.launch(unconfinedDispatcher) {
            viewModel.parentUiState.toList(state)
        }

        assertEquals(1, state.size)
        assertEquals(true, state.last().scrollable)

        viewModel.setParentScrollable(false)

        assertEquals(2, state.size)
        assertEquals(false, state.last().scrollable)

        viewModel.setParentScrollable(true)

        assertEquals(3, state.size)
        assertEquals(true, state.last().scrollable)
    }
}
