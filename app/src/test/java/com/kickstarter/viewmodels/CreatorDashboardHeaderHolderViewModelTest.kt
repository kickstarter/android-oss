package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.RefTag.Companion.dashboard
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.ProgressBarUtils
import com.kickstarter.libs.utils.extensions.deadlineCountdownValue
import com.kickstarter.mock.MockExperimentsClientType
import com.kickstarter.mock.factories.ProjectFactory.project
import com.kickstarter.mock.factories.ProjectStatsEnvelopeFactory.projectStatsEnvelope
import com.kickstarter.mock.factories.UserFactory.collaborator
import com.kickstarter.mock.factories.UserFactory.creator
import com.kickstarter.models.Project
import com.kickstarter.ui.adapters.data.ProjectDashboardData
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.junit.Test
import rx.observers.TestSubscriber

class CreatorDashboardHeaderHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: CreatorDashboardHeaderHolderViewModel.ViewModel

    private val messagesButtonIsGone = TestSubscriber<Boolean>()
    private val otherProjectsButtonIsGone = TestSubscriber<Boolean>()
    private val percentageFunded = TestSubscriber<String>()
    private val percentageFundedProgress = TestSubscriber<Int>()
    private val projectBackersCountText = TestSubscriber<String>()
    private val projectNameTextViewText = TestSubscriber<String>()
    private val progressBarBackground = TestSubscriber<Int>()
    private val startMessageThreadsActivity = TestSubscriber<Pair<Project, RefTag>>()
    private val startProjectActivity = TestSubscriber<Pair<Project, RefTag>>()
    private val viewProjectButtonIsGone = TestSubscriber<Boolean>()
    private val timeRemainingText = TestSubscriber<String>()

    protected fun setUpEnvironment(environment: Environment) {
        vm = CreatorDashboardHeaderHolderViewModel.ViewModel(environment)
        vm.outputs.messagesButtonIsGone().subscribe(messagesButtonIsGone)
        vm.outputs.otherProjectsButtonIsGone().subscribe(otherProjectsButtonIsGone)
        vm.outputs.projectBackersCountText().subscribe(projectBackersCountText)
        vm.outputs.projectNameTextViewText().subscribe(projectNameTextViewText)
        vm.outputs.percentageFunded().subscribe(percentageFunded)
        vm.outputs.percentageFundedProgress().subscribe(percentageFundedProgress)
        vm.outputs.progressBarBackground().subscribe(progressBarBackground)
        vm.outputs.startMessageThreadsActivity().subscribe(startMessageThreadsActivity)
        vm.outputs.startProjectActivity().subscribe(startProjectActivity)
        vm.outputs.timeRemainingText().subscribe(timeRemainingText)
        vm.outputs.viewProjectButtonIsGone().subscribe(viewProjectButtonIsGone)
    }

    @Test
    fun testMessagesButtonIsGone_whenCurrentUserIsCollaborator() {
        val creator = creator()
        val currentUser: CurrentUserType = MockCurrentUser(collaborator())
        val project = project().toBuilder().creator(creator).build()
        val projectStatsEnvelope = projectStatsEnvelope()
        setUpEnvironment(environment().toBuilder().currentUser(currentUser).build())
        vm.inputs.configureWith(ProjectDashboardData(project, projectStatsEnvelope, false))

        // Messages button is gone if current user is not the project creator (e.g. a collaborator).
        messagesButtonIsGone.assertValue(true)
    }

    @Test
    fun testMessagesButtonIsGone_whenCurrentUserIsProjectCreator() {
        val creator = creator()
        val currentUser: CurrentUserType = MockCurrentUser(creator)
        val project = project().toBuilder().creator(creator).build()
        setUpEnvironment(environment().toBuilder().currentUser(currentUser).build())
        vm.inputs.configureWith(ProjectDashboardData(project, projectStatsEnvelope(), false))

        // Messages button is shown to project creator.
        messagesButtonIsGone.assertValues(false)
    }

    @Test
    fun testOtherProjectsButtonIsGone_whenCurrentUserIsMemberOf1Project() {
        val collaboratorWith1Project = collaborator()
            .toBuilder()
            .memberProjectsCount(1)
            .build()
        val collaborator: CurrentUserType = MockCurrentUser(collaboratorWith1Project)
        setUpEnvironment(environment().toBuilder().currentUser(collaborator).build())
        vm.inputs.configureWith(ProjectDashboardData(project(), projectStatsEnvelope(), false))
        otherProjectsButtonIsGone.assertValue(true)
    }

    @Test
    fun testOtherProjectsButtonIsGone_whenCurrentUserIsMemberOfManyProjects_viewingAllProjects() {
        val collaborator: CurrentUserType = MockCurrentUser(collaborator())
        setUpEnvironment(environment().toBuilder().currentUser(collaborator).build())
        vm.inputs.configureWith(ProjectDashboardData(project(), projectStatsEnvelope(), false))
        otherProjectsButtonIsGone.assertValue(false)
    }

    @Test
    fun testOtherProjectsButtonIsGone_whenCurrentUserIsMemberOfManyProjects_viewingSingleProject() {
        val collaborator: CurrentUserType = MockCurrentUser(collaborator())
        setUpEnvironment(environment().toBuilder().currentUser(collaborator).build())
        vm.inputs.configureWith(ProjectDashboardData(project(), projectStatsEnvelope(), true))
        otherProjectsButtonIsGone.assertValue(true)
    }

    @Test
    fun testProjectBackersCountText() {
        val project = project().toBuilder().backersCount(10).build()
        val projectStatsEnvelope = projectStatsEnvelope()
        setUpEnvironment(environment())
        vm.inputs.configureWith(ProjectDashboardData(project, projectStatsEnvelope, false))
        projectBackersCountText.assertValue("10")
    }

    @Test
    fun testProjectNameTextViewText() {
        val project = project().toBuilder().name("somebody once told me").build()
        val projectStatsEnvelope = projectStatsEnvelope()
        setUpEnvironment(environment())
        vm.inputs.configureWith(ProjectDashboardData(project, projectStatsEnvelope, false))
        projectNameTextViewText.assertValue("somebody once told me")
    }

    @Test
    fun testPercentageFunded() {
        setUpEnvironment(environment())
        val project = project()
        val projectStatsEnvelope = projectStatsEnvelope()
        vm.inputs.configureWith(ProjectDashboardData(project, projectStatsEnvelope, false))
        val percentageFundedOutput = NumberUtils.flooredPercentage(project.percentageFunded())
        percentageFunded.assertValues(percentageFundedOutput)
        val percentageFundedProgressOutput = ProgressBarUtils.progress(project.percentageFunded())
        percentageFundedProgress.assertValue(percentageFundedProgressOutput)
    }

    @Test
    fun testProgressBarBackground_LiveProject() {
        setUpEnvironment(environment())
        vm.inputs.configureWith(getDashboardDataForProjectState(Project.STATE_LIVE))
        progressBarBackground.assertValue(R.drawable.progress_bar_green_horizontal)
    }

    @Test
    fun testProgressBarBackground_SubmittedProject() {
        setUpEnvironment(environment())
        vm.inputs.configureWith(getDashboardDataForProjectState(Project.STATE_SUBMITTED))
        progressBarBackground.assertValue(R.drawable.progress_bar_green_horizontal)
    }

    @Test
    fun testProgressBarBackground_StartedProject() {
        setUpEnvironment(environment())
        vm.inputs.configureWith(getDashboardDataForProjectState(Project.STATE_STARTED))
        progressBarBackground.assertValue(R.drawable.progress_bar_green_horizontal)
    }

    @Test
    fun testProgressBarBackground_SuccessfulProject() {
        setUpEnvironment(environment())
        vm.inputs.configureWith(getDashboardDataForProjectState(Project.STATE_SUCCESSFUL))
        progressBarBackground.assertValue(R.drawable.progress_bar_green_horizontal)
    }

    @Test
    fun testProgressBarBackground_FailedProject() {
        setUpEnvironment(environment())
        vm.inputs.configureWith(getDashboardDataForProjectState(Project.STATE_FAILED))
        progressBarBackground.assertValue(R.drawable.progress_bar_grey_horizontal)
    }

    @Test
    fun testProgressBarBackground_CanceledProject() {
        setUpEnvironment(environment())
        vm.inputs.configureWith(getDashboardDataForProjectState(Project.STATE_CANCELED))
        progressBarBackground.assertValue(R.drawable.progress_bar_grey_horizontal)
    }

    @Test
    fun testProgressBarBackground_SuspendedProject() {
        setUpEnvironment(environment())
        vm.inputs.configureWith(getDashboardDataForProjectState(Project.STATE_SUSPENDED))
        progressBarBackground.assertValue(R.drawable.progress_bar_grey_horizontal)
    }

    @Test
    fun testStartMessagesActivity() {
        val creator = creator()
        val currentUser: CurrentUserType = MockCurrentUser(creator)
        val project = project().toBuilder().creator(creator).build()
        setUpEnvironment(environment().toBuilder().currentUser(currentUser).build())
        vm.inputs.configureWith(ProjectDashboardData(project, projectStatsEnvelope(), false))
        vm.inputs.messagesButtonClicked()
        startMessageThreadsActivity.assertValue(Pair.create(project, dashboard()))
    }

    @Test
    fun testStartProjectActivity() {
        val project = project()
        val projectStatsEnvelope = projectStatsEnvelope()
        setUpEnvironment(environment())
        vm.inputs.configureWith(ProjectDashboardData(project, projectStatsEnvelope, false))
        vm.inputs.projectButtonClicked()
        startProjectActivity.assertValueCount(1)
        assertEquals(startProjectActivity.onNextEvents[0].first, project)
        assertEquals(startProjectActivity.onNextEvents[0].second, dashboard())
    }

    @Test
    fun testStartProjectActivity_whenFeatureFlagOn_shouldEmitProjectPage() {
        val project = project()
        val projectStatsEnvelope = projectStatsEnvelope()
        val currentUser: CurrentUserType = MockCurrentUser()
        val mockExperimentsClientType: MockExperimentsClientType =
            object : MockExperimentsClientType() {
                override fun isFeatureEnabled(feature: OptimizelyFeature.Key): Boolean {
                    return true
                }
            }
        setUpEnvironment(
            environment().toBuilder().currentUser(currentUser).optimizely(mockExperimentsClientType)
                .build()
        )
        vm.inputs.configureWith(ProjectDashboardData(project, projectStatsEnvelope, false))
        vm.inputs.projectButtonClicked()
        startProjectActivity.assertValueCount(1)
        assertEquals(startProjectActivity.onNextEvents[0].first, project)
        assertEquals(startProjectActivity.onNextEvents[0].second, dashboard())
    }

    @Test
    fun testTimeRemainingText() {
        setUpEnvironment(environment())
        DateTimeUtils.setCurrentMillisFixed(DateTime().millis)
        val project = project().toBuilder().deadline(DateTime().plusDays(10)).build()
        val projectStatsEnvelope = projectStatsEnvelope()
        val deadlineVal = project.deadlineCountdownValue()
        vm.inputs.configureWith(ProjectDashboardData(project, projectStatsEnvelope, false))
        timeRemainingText.assertValue(NumberUtils.format(deadlineVal))
    }

    @Test
    fun testViewProjectButtonIsGone_whenViewingSingleProject() {
        setUpEnvironment(environment())
        vm.inputs.configureWith(ProjectDashboardData(project(), projectStatsEnvelope(), true))
        viewProjectButtonIsGone.assertValue(true)
    }

    @Test
    fun testViewProjectButtonIsGone_whenViewingAllProjects() {
        setUpEnvironment(environment())
        vm.inputs.configureWith(ProjectDashboardData(project(), projectStatsEnvelope(), false))
        viewProjectButtonIsGone.assertValue(false)
    }

    private fun getDashboardDataForProjectState(@Project.State state: String): ProjectDashboardData {
        val projectStatsEnvelope = projectStatsEnvelope()
        val project = project().toBuilder().state(state).build()
        return ProjectDashboardData(project, projectStatsEnvelope, false)
    }
}
