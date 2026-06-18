package com.kickstarter.features.projecstory

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.projectstory.ProjectStoryUiState
import com.kickstarter.features.projectstory.ProjectStoryViewModel
import com.kickstarter.features.projectstory.data.RichTextComponent
import com.kickstarter.features.projectstory.data.StoriedProject
import com.kickstarter.libs.Environment
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Project
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectStoryViewModelTest : KSRobolectricTestCase() {

    private lateinit var viewModel: ProjectStoryViewModel

    private fun setUpEnvironment(environment: Environment, dispatcher: CoroutineDispatcher) {
        viewModel = ProjectStoryViewModel.Factory(environment, dispatcher).create(ProjectStoryViewModel::class.java)
    }

    @Test
    fun `test no provided slug, fetch project, check emissions`() = runTest {
        val standardDispatcher = StandardTestDispatcher(testScheduler)

        setUpEnvironment(environment(), standardDispatcher)

        val uiStates = mutableListOf<ProjectStoryUiState>()
        val unconfinedDispatcher = UnconfinedTestDispatcher(testScheduler)
        backgroundScope.launch(unconfinedDispatcher) {
            viewModel.projectStoryUiState.toList(uiStates)
        }

        viewModel.fetchProject()

        advanceUntilIdle()

        assertEquals(1, uiStates.size)
        assertEquals(null, uiStates.last().storiedProject)
    }

    @Test
    fun `provide invalid slug, fetch project, check emissions`() = runTest {
        val standardDispatcher = StandardTestDispatcher(testScheduler)

        setUpEnvironment(environment(), standardDispatcher)

        val uiStates = mutableListOf<ProjectStoryUiState>()
        val unconfinedDispatcher = UnconfinedTestDispatcher(testScheduler)
        backgroundScope.launch(unconfinedDispatcher) {
            viewModel.projectStoryUiState.toList(uiStates)
        }

        viewModel.provideProjectSlug("\n")
        viewModel.fetchProject()

        advanceUntilIdle()

        assertEquals(1, uiStates.size)
        assertEquals(null, uiStates.last().storiedProject)
    }

    @Test
    fun `provide slug, fetch project, network error, check emissions`() = runTest {
        val standardDispatcher = StandardTestDispatcher(testScheduler)
        val throwable = Exception("Network error")

        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override suspend fun fetchProjectStory(slug: String): Result<StoriedProject> {
                    return Result.failure(throwable)
                }
            }).build(),
            standardDispatcher
        )

        val uiStates = mutableListOf<ProjectStoryUiState>()
        val unconfinedDispatcher = UnconfinedTestDispatcher(testScheduler)
        backgroundScope.launch(unconfinedDispatcher) {
            viewModel.projectStoryUiState.toList(uiStates)
        }

        viewModel.provideProjectSlug("non-existing-slug")
        viewModel.fetchProject()

        advanceUntilIdle()

        assertEquals(3, uiStates.size)
        assertEquals(false, uiStates.last().isLoading)
        assertEquals(throwable, uiStates.last().error)
        assertEquals(null, uiStates.last().storiedProject)
    }

    @Test
    fun `provide slug, fetch nonexistent project, check emissions`() = runTest {
        val standardDispatcher = StandardTestDispatcher(testScheduler)
        val slug = "project-that-does-not-exist"

        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override suspend fun fetchProjectStory(slug: String): Result<StoriedProject> {
                    return Result.success(StoriedProject(Project.builder().build(), null))
                }
            }).build(),
            standardDispatcher
        )

        val uiStates = mutableListOf<ProjectStoryUiState>()
        val unconfinedDispatcher = UnconfinedTestDispatcher(testScheduler)
        backgroundScope.launch(unconfinedDispatcher) {
            viewModel.projectStoryUiState.toList(uiStates)
        }

        viewModel.provideProjectSlug(slug)
        viewModel.fetchProject()

        advanceUntilIdle()

        assertEquals(3, uiStates.size)
        assertEquals(false, uiStates.last().isLoading)
        assertEquals(null, uiStates.last().error)

        assertNotNull(uiStates.last().storiedProject?.project)
        assertNull(uiStates.last().storiedProject?.story)
    }

    @Test
    fun `provide slug, fetch existing project, check emissions`() = runTest {
        val standardDispatcher = StandardTestDispatcher(testScheduler)
        val slug = "creator/project"

        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override suspend fun fetchProjectStory(slug: String): Result<StoriedProject> {
                    return Result.success(StoriedProject(Project.builder().build(), RichTextComponent(emptyList())))
                }
            }).build(),
            standardDispatcher
        )

        val uiStates = mutableListOf<ProjectStoryUiState>()
        val unconfinedDispatcher = UnconfinedTestDispatcher(testScheduler)
        backgroundScope.launch(unconfinedDispatcher) {
            viewModel.projectStoryUiState.toList(uiStates)
        }

        viewModel.provideProjectSlug(slug)
        viewModel.fetchProject()

        advanceUntilIdle()

        assertEquals(3, uiStates.size)
        assertEquals(false, uiStates.last().isLoading)
        assertEquals(null, uiStates.last().error)

        assertNotNull(uiStates.last().storiedProject?.project)
        assertNotNull(uiStates.last().storiedProject?.story)
    }

    @Test
    fun `provide slug, fetch project multiple times, check emissions`() = runTest {
        val standardDispatcher = StandardTestDispatcher(testScheduler)
        val slug = "creator/project"
        val storiedProject = StoriedProject(
            Project.builder().slug(slug).build(),
            RichTextComponent(emptyList())
        )

        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override suspend fun fetchProjectStory(slug: String): Result<StoriedProject> {
                    return Result.success(storiedProject)
                }
            }).build(),
            standardDispatcher
        )

        val uiStates = mutableListOf<ProjectStoryUiState>()
        val unconfinedDispatcher = UnconfinedTestDispatcher(testScheduler)
        backgroundScope.launch(unconfinedDispatcher) {
            viewModel.projectStoryUiState.toList(uiStates)
        }

        viewModel.provideProjectSlug(slug)

        viewModel.fetchProject()
        advanceUntilIdle()

        viewModel.fetchProject()
        advanceUntilIdle()

        assertEquals(3, uiStates.size)
        assertEquals(false, uiStates.last().isLoading)
        assertEquals(null, uiStates.last().error)
        assertNotNull(uiStates.last().storiedProject)
    }

    @Test
    fun `provide slug, fetch project, provide different slug, fetch project, check emissions`() = runTest {
        val standardDispatcher = StandardTestDispatcher(testScheduler)

        val slugToStories = mapOf(
            "creator/project1" to StoriedProject(
                Project.builder().id(1).slug("creator/project1").build(),
                RichTextComponent(emptyList())
            ),
            "creator/project2" to StoriedProject(
                Project.builder().id(2).slug("creator/project2").build(),
                RichTextComponent(emptyList())
            )
        )

        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override suspend fun fetchProjectStory(slug: String): Result<StoriedProject> {
                    return slugToStories[slug]?.let { Result.success(it) }
                        ?: Result.failure(Exception("Project not found"))
                }
            }).build(),
            standardDispatcher
        )

        val uiStates = mutableListOf<ProjectStoryUiState>()
        val unconfinedDispatcher = UnconfinedTestDispatcher(testScheduler)
        backgroundScope.launch(unconfinedDispatcher) {
            viewModel.projectStoryUiState.toList(uiStates)
        }

        val slug1 = slugToStories.keys.toList()[0]
        viewModel.provideProjectSlug(slug1)
        viewModel.fetchProject()
        advanceUntilIdle()

        assertEquals(3, uiStates.size)
        assertEquals(false, uiStates.last().isLoading)
        assertEquals(null, uiStates.last().error)
        assertEquals(slugToStories[slug1], uiStates.last().storiedProject)

        val slug2 = slugToStories.keys.toList()[1]
        viewModel.provideProjectSlug(slug2)
        viewModel.fetchProject()
        advanceUntilIdle()

        assertEquals(5, uiStates.size)
        assertEquals(false, uiStates.last().isLoading)
        assertEquals(null, uiStates.last().error)
        assertEquals(slugToStories[slug2], uiStates.last().storiedProject)
    }
}
