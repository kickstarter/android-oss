<<<<<<< HEAD
=======
package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.mock.factories.ProjectFactory.project
import com.kickstarter.mock.factories.ProjectStatsEnvelopeFactory.projectStatsEnvelope
import com.kickstarter.mock.factories.ProjectsEnvelopeFactory
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope
import com.kickstarter.services.apiresponses.ProjectsEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.data.ProjectDashboardData
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class CreatorDashboardViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: CreatorDashboardViewModel.ViewModel

    private val bottomSheetShouldExpand = TestSubscriber<Boolean>()
    private val projectDashboardData = TestSubscriber<ProjectDashboardData>()
    private val progressBarIsVisible = TestSubscriber<Boolean>()
    private val projectsForBottomSheet = TestSubscriber<List<Project>>()
    private val projectName = TestSubscriber<String>()

    protected fun setUpEnvironment(environment: Environment) {
        vm = CreatorDashboardViewModel.ViewModel(environment)

        vm.outputs.bottomSheetShouldExpand().subscribe(bottomSheetShouldExpand)
        vm.outputs.projectDashboardData().subscribe(projectDashboardData)
        vm.outputs.projectsForBottomSheet().subscribe(projectsForBottomSheet)
        vm.outputs.progressBarIsVisible().subscribe(progressBarIsVisible)
        vm.outputs.projectName().subscribe(projectName)
    }

    @Test
    fun testBottomSheetShouldExpand_whenBackClicked() {
        setUpEnvironment(environment())

        vm.intent(Intent())
        vm.inputs.backClicked()
        bottomSheetShouldExpand.assertValue(false)
    }

    @Test
    fun testBottomSheetShouldExpand_whenNewProjectSelected() {
        val project = project()
        val projectStatsEnvelope = projectStatsEnvelope()
        val apiClient: MockApiClient = object : MockApiClient() {
            override fun fetchProjectStats(project: Project): Observable<ProjectStatsEnvelope> {
                return Observable.just(projectStatsEnvelope)
            }
        }

        setUpEnvironment(environment().toBuilder().apiClient(apiClient).build())

        vm.intent(Intent().putExtra(IntentKey.PROJECT, project))
        vm.inputs.projectSelectionInput(project())
        bottomSheetShouldExpand.assertValue(false)
    }

    @Test
    fun testBottomSheetShouldExpand_whenProjectsListButtonClicked() {
        setUpEnvironment(environment())

        vm.intent(Intent())
        vm.inputs.projectsListButtonClicked()
        bottomSheetShouldExpand.assertValue(true)
    }

    @Test
    fun testBottomSheetShouldExpand_whenScrimClicked() {
        setUpEnvironment(environment())

        vm.intent(Intent())
        vm.inputs.scrimClicked()
        bottomSheetShouldExpand.assertValue(false)
    }

    @Test
    fun testProjectDashboardData_whenViewingAllProjects() {
        val projects = listOf(project())
        val projectStatsEnvelope = projectStatsEnvelope()
        val apiClient: MockApiClient = object : MockApiClient() {
            override fun fetchProjects(member: Boolean): Observable<ProjectsEnvelope> {
                return Observable.just(ProjectsEnvelopeFactory.projectsEnvelope(projects))
            }

            override fun fetchProjectStats(project: Project): Observable<ProjectStatsEnvelope> {
                return Observable.just(projectStatsEnvelope)
            }
        }

        setUpEnvironment(environment().toBuilder().apiClient(apiClient).build())

        vm.intent(Intent())

        progressBarIsVisible.assertValues(true, false)
        projectDashboardData.assertValue(
            ProjectDashboardData(
                requireNotNull(
                    ListUtils.first(
                        projects
                    )
                ),
                projectStatsEnvelope, false
            )
        )
    }

    @Test
    fun testProjectDashboardData_whenViewingSingleProjects() {
        val project = project()
        val projectStatsEnvelope = projectStatsEnvelope()
        val apiClient: MockApiClient = object : MockApiClient() {
            override fun fetchProjectStats(project: Project): Observable<ProjectStatsEnvelope> {
                return Observable.just(projectStatsEnvelope)
            }
        }

        setUpEnvironment(environment().toBuilder().apiClient(apiClient).build())

        vm.intent(Intent().putExtra(IntentKey.PROJECT, project))
        projectDashboardData.assertValue(ProjectDashboardData(project, projectStatsEnvelope, true))
    }

    @Test
    fun testProjectsForBottomSheet_With1Project() {
        val projects = listOf(project())
        val apiClient: MockApiClient = object : MockApiClient() {
            override fun fetchProjects(member: Boolean): Observable<ProjectsEnvelope> {
                return Observable.just(ProjectsEnvelopeFactory.projectsEnvelope(projects))
            }
        }

        setUpEnvironment(environment().toBuilder().apiClient(apiClient).build())

        projectsForBottomSheet.assertNoValues()
    }

    @Test
    fun testProjectsForBottomSheet_WithManyProjects() {
        val project1 = project()
        val project2 = project()
        val projects = listOf(
            project1,
            project2
        )

        val apiClient: MockApiClient = object : MockApiClient() {
            override fun fetchProjects(member: Boolean): Observable<ProjectsEnvelope> {
                return Observable.just(ProjectsEnvelopeFactory.projectsEnvelope(projects))
            }
        }

        setUpEnvironment(environment().toBuilder().apiClient(apiClient).build())

        vm.intent(Intent())
        projectsForBottomSheet.assertValue(listOf(project2))
    }

    @Test
    fun testProjectSwitcherProjectClickOutput() {
        DateTimeUtils.setCurrentMillisFixed(DateTime().millis)
        val project1 = project()
        val project2 = project()
        val projects = listOf(
            project1,
            project2
        )
        val projectStatsEnvelope = projectStatsEnvelope()
        val apiClient: MockApiClient = object : MockApiClient() {
            override fun fetchProjects(member: Boolean): Observable<ProjectsEnvelope> {
                return Observable.just(ProjectsEnvelopeFactory.projectsEnvelope(projects))
            }

            override fun fetchProjectStats(project: Project): Observable<ProjectStatsEnvelope> {
                return Observable.just(projectStatsEnvelope)
            }
        }

        setUpEnvironment(environment().toBuilder().apiClient(apiClient).build())

        vm.intent(Intent())
        vm.inputs.projectSelectionInput(project2)

        projectDashboardData.assertValues(
            ProjectDashboardData(project1, projectStatsEnvelope(), false),
            ProjectDashboardData(project2, projectStatsEnvelope(), false)
        )
    }

    @Test
    fun testProjectName_whenMultipleProjects() {
        val project1 = project()
            .toBuilder()
            .name("Best Project 2K19")
            .build()
        val project2 = project()
        val projects = listOf(
            project1,
            project2
        )
        val apiClient: MockApiClient = object : MockApiClient() {
            override fun fetchProjects(member: Boolean): Observable<ProjectsEnvelope> {
                return Observable.just(ProjectsEnvelopeFactory.projectsEnvelope(projects))
            }
        }

        setUpEnvironment(environment().toBuilder().apiClient(apiClient).build())
        vm.intent(Intent())

        projectName.assertValue("Best Project 2K19")
    }

    @Test
    fun testProjectName_whenSingleProject() {
        val project = project()
            .toBuilder()
            .name("Best Project 2K19")
            .build()
        val apiClient: MockApiClient = object : MockApiClient() {
            override fun fetchProjects(member: Boolean): Observable<ProjectsEnvelope> {
                return Observable.just(ProjectsEnvelopeFactory.projectsEnvelope(listOf(project)))
            }
        }

        setUpEnvironment(environment().toBuilder().apiClient(apiClient).build())

        vm.intent(Intent().putExtra(IntentKey.PROJECT, project))
        projectName.assertValue("Best Project 2K19")
    }
}
>>>>>>> 94ef702c6 (Remove all instances of object utils)
