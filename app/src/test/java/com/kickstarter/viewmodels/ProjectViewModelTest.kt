package com.kickstarter.viewmodels

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.Either
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.models.OptimizelyExperiment
import com.kickstarter.libs.utils.EventName
import com.kickstarter.mock.MockExperimentsClientType
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.CheckoutDataFactory
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.ActivityResult
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import java.math.RoundingMode

class ProjectViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: ProjectViewModel.ViewModel
    private val backingDetailsIsVisible = TestSubscriber<Boolean>()
    private val backingDetailsSubtitle = TestSubscriber<Either<String, Int>?>()
    private val backingDetailsTitle = TestSubscriber<Int>()
    private val expandPledgeSheet = TestSubscriber<Pair<Boolean, Boolean>>()
    private val goBack = TestSubscriber<Void>()
    private val heartDrawableId = TestSubscriber<Int>()
    private val managePledgeMenu = TestSubscriber<Int?>()
    private val pledgeActionButtonColor = TestSubscriber<Int>()
    private val pledgeActionButtonContainerIsGone = TestSubscriber<Boolean>()
    private val pledgeActionButtonText = TestSubscriber<Int>()
    private val pledgeToolbarNavigationIcon = TestSubscriber<Int>()
    private val pledgeToolbarTitle = TestSubscriber<Int>()
    private val prelaunchUrl = TestSubscriber<String>()
    private val projectData = TestSubscriber<ProjectData>()
    private val reloadProjectContainerIsGone = TestSubscriber<Boolean>()
    private val reloadProgressBarIsGone = TestSubscriber<Boolean>()
    private val revealRewardsFragment = TestSubscriber<Void>()
    private val savedTest = TestSubscriber<Boolean>()
    private val scrimIsVisible = TestSubscriber<Boolean>()
    private val setInitialRewardsContainerY = TestSubscriber<Void>()
    private val showCancelPledgeFragment = TestSubscriber<Project>()
    private val showCancelPledgeSuccess = TestSubscriber<Void>()
    private val showPledgeNotCancelableDialog = TestSubscriber<Void>()
    private val showSavedPromptTest = TestSubscriber<Void>()
    private val showShareSheet = TestSubscriber<Pair<String, String>>()
    private val showUpdatePledge = TestSubscriber<Pair<PledgeData, PledgeReason>>()
    private val showUpdatePledgeSuccess = TestSubscriber<Void>()
    private val startCampaignWebViewActivity = TestSubscriber<ProjectData>()
    private val startCommentsActivity = TestSubscriber<Pair<Project, ProjectData>>()
    private val startCreatorBioWebViewActivity = TestSubscriber<Project>()
    private val startCreatorDashboardActivity = TestSubscriber<Project>()
    private val startLoginToutActivity = TestSubscriber<Void>()
    private val startMessagesActivity = TestSubscriber<Project>()
    private val startProjectUpdatesActivity = TestSubscriber<Pair<Project, ProjectData>>()
    private val startThanksActivity = TestSubscriber<Pair<CheckoutData, PledgeData>>()
    private val startVideoActivity = TestSubscriber<Project>()
    private val updateFragments = TestSubscriber<ProjectData>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ProjectViewModel.ViewModel(environment)
        this.vm.outputs.backingDetailsIsVisible().subscribe(this.backingDetailsIsVisible)
        this.vm.outputs.backingDetailsSubtitle().subscribe(this.backingDetailsSubtitle)
        this.vm.outputs.backingDetailsTitle().subscribe(this.backingDetailsTitle)
        this.vm.outputs.expandPledgeSheet().subscribe(this.expandPledgeSheet)
        this.vm.outputs.goBack().subscribe(this.goBack)
        this.vm.outputs.heartDrawableId().subscribe(this.heartDrawableId)
        this.vm.outputs.managePledgeMenu().subscribe(this.managePledgeMenu)
        this.vm.outputs.pledgeActionButtonColor().subscribe(this.pledgeActionButtonColor)
        this.vm.outputs.pledgeActionButtonContainerIsGone().subscribe(this.pledgeActionButtonContainerIsGone)
        this.vm.outputs.pledgeActionButtonText().subscribe(this.pledgeActionButtonText)
        this.vm.outputs.pledgeToolbarNavigationIcon().subscribe(this.pledgeToolbarNavigationIcon)
        this.vm.outputs.pledgeToolbarTitle().subscribe(this.pledgeToolbarTitle)
        this.vm.outputs.prelaunchUrl().subscribe(this.prelaunchUrl)
        this.vm.outputs.projectData().subscribe(this.projectData)
        this.vm.outputs.reloadProgressBarIsGone().subscribe(this.reloadProgressBarIsGone)
        this.vm.outputs.reloadProjectContainerIsGone().subscribe(this.reloadProjectContainerIsGone)
        this.vm.outputs.revealRewardsFragment().subscribe(this.revealRewardsFragment)
        this.vm.outputs.scrimIsVisible().subscribe(this.scrimIsVisible)
        this.vm.outputs.setInitialRewardsContainerY().subscribe(this.setInitialRewardsContainerY)
        this.vm.outputs.showCancelPledgeFragment().subscribe(this.showCancelPledgeFragment)
        this.vm.outputs.showCancelPledgeSuccess().subscribe(this.showCancelPledgeSuccess)
        this.vm.outputs.showPledgeNotCancelableDialog().subscribe(this.showPledgeNotCancelableDialog)
        this.vm.outputs.showSavedPrompt().subscribe(this.showSavedPromptTest)
        this.vm.outputs.showShareSheet().subscribe(this.showShareSheet)
        this.vm.outputs.showUpdatePledge().subscribe(this.showUpdatePledge)
        this.vm.outputs.showUpdatePledgeSuccess().subscribe(this.showUpdatePledgeSuccess)
        this.vm.outputs.startLoginToutActivity().subscribe(this.startLoginToutActivity)
        this.vm.outputs.projectData().map { pD -> pD.project().isStarred }.subscribe(this.savedTest)
        this.vm.outputs.startCampaignWebViewActivity().subscribe(this.startCampaignWebViewActivity)
        this.vm.outputs.startCommentsActivity().subscribe(this.startCommentsActivity)
        this.vm.outputs.startCreatorBioWebViewActivity().subscribe(this.startCreatorBioWebViewActivity)
        this.vm.outputs.startCreatorDashboardActivity().subscribe(this.startCreatorDashboardActivity)
        this.vm.outputs.startMessagesActivity().subscribe(this.startMessagesActivity)
        this.vm.outputs.startProjectUpdatesActivity().subscribe(this.startProjectUpdatesActivity)
        this.vm.outputs.startThanksActivity().subscribe(this.startThanksActivity)
        this.vm.outputs.startVideoActivity().subscribe(this.startVideoActivity)
        this.vm.outputs.updateFragments().subscribe(this.updateFragments)
    }

    @Test
    fun testUIOutputs_whenFetchProjectFromIntent_isSuccessful() {
        val initialProject = ProjectFactory.initialProject()
        val refreshedProject = ProjectFactory.project()
        val environment = environment()
            .toBuilder()
            .apiClient(apiClientWithSuccessFetchingProject(refreshedProject))
            .build()

        setUpEnvironment(environment)

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, initialProject))

        this.pledgeActionButtonContainerIsGone.assertValues(true, false)
        this.prelaunchUrl.assertNoValues()
        this.projectData.assertValues(ProjectDataFactory.project(refreshedProject))
        this.reloadProjectContainerIsGone.assertValue(true)
        this.reloadProgressBarIsGone.assertValues(false, true)
        this.updateFragments.assertValue(ProjectDataFactory.project(refreshedProject))
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testUIOutputs_whenFetchProjectFromIntent_isUnsuccessful() {
        var error = true
        val initialProject = ProjectFactory.initialProject()
        val refreshedProject = ProjectFactory.project()

        val environment = environment()
            .toBuilder()
            .apiClient(object : MockApiClient() {
                override fun fetchProject(project: Project): Observable<Project> {
                    val observable = when {
                        error -> Observable.error(Throwable("boop"))
                        else -> {
                            Observable.just(refreshedProject)
                        }
                    }
                    return observable
                }
            })
            .build()
        setUpEnvironment(environment)

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, initialProject))

        this.pledgeActionButtonContainerIsGone.assertValues(true)
        this.prelaunchUrl.assertNoValues()
        this.projectData.assertValues(ProjectDataFactory.project(initialProject))
        this.reloadProjectContainerIsGone.assertValue(false)
        this.reloadProgressBarIsGone.assertValues(false, true)
        this.updateFragments.assertNoValues()

        error = false
        this.vm.inputs.reloadProjectContainerClicked()

        this.pledgeActionButtonContainerIsGone.assertValues(true, false)
        this.prelaunchUrl.assertNoValues()
        this.projectData.assertValues(
            ProjectDataFactory.project(initialProject),
            ProjectDataFactory.project(initialProject),
            ProjectDataFactory.project(refreshedProject)
        )
        this.reloadProjectContainerIsGone.assertValues(false, true, true)
        this.reloadProgressBarIsGone.assertValues(false, true, false, true)
        this.updateFragments.assertValue(ProjectDataFactory.project(refreshedProject))
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testUIOutputs_whenFetchProjectFromDeepLink_isSuccessful() {
        val project = ProjectFactory.project()

        val environment = environment()
            .toBuilder()
            .apiClient(object : MockApiClient() {
                override fun fetchProject(param: String): Observable<Project> {
                    return Observable.just(project)
                }
            })
            .build()

        setUpEnvironment(environment)
        val intent = deepLinkIntent()
        this.vm.intent(intent)

        this.pledgeActionButtonContainerIsGone.assertValues(true, false)
        this.prelaunchUrl.assertNoValues()
        this.projectData.assertValue(ProjectDataFactory.project(project))
        this.reloadProgressBarIsGone.assertValues(false, true)
        this.updateFragments.assertValue(ProjectDataFactory.project(project))
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testUIOutputs_whenFetchProjectFromDeepLink_isUnsuccessful() {
        var error = true
        val refreshedProject = ProjectFactory.project()

        val environment = environment()
            .toBuilder()
            .apiClient(object : MockApiClient() {
                override fun fetchProject(param: String): Observable<Project> {
                    val observable = when {
                        error -> Observable.error(Throwable("boop"))
                        else -> Observable.just(refreshedProject)
                    }
                    return observable
                }
            })
            .build()
        setUpEnvironment(environment)

        this.vm.intent(deepLinkIntent())

        this.pledgeActionButtonContainerIsGone.assertNoValues()
        this.prelaunchUrl.assertNoValues()
        this.projectData.assertNoValues()
        this.reloadProgressBarIsGone.assertValues(false, true)
        this.reloadProjectContainerIsGone.assertValue(false)
        this.updateFragments.assertNoValues()

        error = false
        this.vm.inputs.reloadProjectContainerClicked()

        this.pledgeActionButtonContainerIsGone.assertValues(true, false)
        this.prelaunchUrl.assertNoValues()
        this.projectData.assertValue(ProjectDataFactory.project(refreshedProject))
        this.reloadProgressBarIsGone.assertValues(false, true, false, true)
        this.reloadProjectContainerIsGone.assertValues(false, true, true)
        this.updateFragments.assertValue(ProjectDataFactory.project(refreshedProject))
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testUIOutputs_whenFetchProjectReturnsPrelaunchActivatedProject() {
        val url = "https://www.kickstarter.com/projects/1186238668/skull-graphic-tee"
        val project = ProjectFactory.prelaunchProject(url)

        val environment = environment()
            .toBuilder()
            .apiClient(object : MockApiClient() {
                override fun fetchProject(param: String): Observable<Project> {
                    return Observable.just(project)
                }
            })
            .build()

        setUpEnvironment(environment)
        val uri = Uri.parse(url)
        this.vm.intent(Intent(Intent.ACTION_VIEW, uri))

        this.pledgeActionButtonContainerIsGone.assertNoValues()
        this.prelaunchUrl.assertValue(url)
        this.projectData.assertNoValues()
        this.reloadProgressBarIsGone.assertValues(false, true)
        this.reloadProjectContainerIsGone.assertNoValues()
        this.updateFragments.assertNoValues()
        this.lakeTest.assertNoValues()
    }

    @Test
    fun testLoggedOutStarProjectFlow() {
        val currentUser = MockCurrentUser()
        val environment = environment().toBuilder()
            .currentUser(currentUser)
            .build()
        environment.currentConfig().config(ConfigFactory.config())

        setUpEnvironment(environment)

        // Start the view model with a project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.halfWayProject()))

        this.savedTest.assertValues(false)
        this.heartDrawableId.assertValues(R.drawable.icon__heart_outline, R.drawable.icon__heart_outline)

        // Try starring while logged out
        this.vm.inputs.heartButtonClicked()

        // The project shouldn't be saved, and a login prompt should be shown.
        this.savedTest.assertValues(false)
        this.heartDrawableId.assertValues(R.drawable.icon__heart_outline, R.drawable.icon__heart_outline)
        this.showSavedPromptTest.assertValueCount(0)
        this.startLoginToutActivity.assertValueCount(1)

        // A koala event for starring should NOT be tracked

        // Login
        currentUser.refresh(UserFactory.user())

        // The project should be saved, and a star prompt should be shown.
        this.savedTest.assertValues(false, true)
        this.heartDrawableId.assertValues(R.drawable.icon__heart_outline, R.drawable.icon__heart_outline, R.drawable.icon__heart)
        this.showSavedPromptTest.assertValueCount(1)

        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testShowShareSheet() {
        val creator = UserFactory.creator()
        val slug = "best-project-2k19"
        val projectUrl = "https://www.kck.str/projects/" + creator.id().toString() + "/" + slug

        val webUrls = Project.Urls.Web.builder()
            .project(projectUrl)
            .rewards("$projectUrl/rewards")
            .updates("$projectUrl/posts")
            .build()

        val project = ProjectFactory.project()
            .toBuilder()
            .name("Best Project 2K19")
            .urls(Project.Urls.builder().web(webUrls).build())
            .build()

        setUpEnvironment(environment())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.shareButtonClicked()
        val expectedName = "Best Project 2K19"
        val expectedShareUrl = "https://www.kck.str/projects/" + creator.id().toString() + "/" + slug + "?ref=android_project_share"
        this.showShareSheet.assertValues(Pair(expectedName, expectedShareUrl))

        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testStarProjectThatIsAlmostCompleted() {
        val project = ProjectFactory.almostCompletedProject()

        val currentUser = MockCurrentUser()
        val environment = environment().toBuilder()
            .currentUser(currentUser)
            .build()
        environment.currentConfig().config(ConfigFactory.config())

        setUpEnvironment(environment)

        // Start the view model with an almost completed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        // Login
        currentUser.refresh(UserFactory.user())

        // Star the project
        this.vm.inputs.heartButtonClicked()

        // The project should be saved, and a save prompt should NOT be shown.
        this.savedTest.assertValues(false, true)
        this.heartDrawableId.assertValues(R.drawable.icon__heart_outline, R.drawable.icon__heart_outline, R.drawable.icon__heart)
        this.showSavedPromptTest.assertValueCount(0)
    }

    @Test
    fun testSaveProjectThatIsSuccessful() {
        val currentUser = MockCurrentUser()
        val environment = environment().toBuilder()
            .currentUser(currentUser)
            .build()
        environment.currentConfig().config(ConfigFactory.config())

        setUpEnvironment(environment)

        // Start the view model with a successful project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.successfulProject()))

        // Login
        currentUser.refresh(UserFactory.user())

        // Star the project
        this.vm.inputs.heartButtonClicked()

        // The project should be saved, and a save prompt should NOT be shown.
        this.savedTest.assertValues(false, true)
        this.heartDrawableId.assertValues(R.drawable.icon__heart_outline, R.drawable.icon__heart_outline, R.drawable.icon__heart)
        this.showSavedPromptTest.assertValueCount(0)
    }

    @Test
    fun testStartCampaignWebViewActivity_whenBlurbClicked_liveNotBackedProject() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.blurbTextViewClicked()
        this.startCampaignWebViewActivity.assertValues(ProjectDataFactory.project(project))
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName, "Campaign Details Button Clicked")
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName, "Campaign Details Button Clicked")
        this.experimentsTest.assertValues("Project Page Viewed", "Campaign Details Button Clicked")
    }

    @Test
    fun testStartCampaignWebViewActivity_whenBlurbClicked_backedProject() {
        val project = ProjectFactory.backedProject()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.blurbTextViewClicked()
        this.startCampaignWebViewActivity.assertValues(ProjectDataFactory.project(project))
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.experimentsTest.assertValue("Project Page Viewed")
    }

    @Test
    fun testStartCampaignWebViewActivity_whenBlurbClicked_endedProject() {
        val project = ProjectFactory.successfulProject()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.blurbTextViewClicked()
        this.startCampaignWebViewActivity.assertValues(ProjectDataFactory.project(project))
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.experimentsTest.assertValue("Project Page Viewed")
    }

    @Test
    fun testStartCampaignWebViewActivity_whenBlurbVariantClicked_liveNotBackedProject() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.blurbVariantClicked()
        this.startCampaignWebViewActivity.assertValues(ProjectDataFactory.project(project))
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName, "Campaign Details Button Clicked")
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName, "Campaign Details Button Clicked")
        this.experimentsTest.assertValues("Project Page Viewed", "Campaign Details Button Clicked")
    }

    @Test
    fun testStartCampaignWebViewActivity_whenBlurbVariantClicked_backedProject() {
        val project = ProjectFactory.backedProject()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.blurbVariantClicked()
        this.startCampaignWebViewActivity.assertValues(ProjectDataFactory.project(project))
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.experimentsTest.assertValue("Project Page Viewed")
    }

    @Test
    fun testStartCampaignWebViewActivity_whenBlurbVariantClicked_endedProject() {
        val project = ProjectFactory.successfulProject()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.blurbVariantClicked()
        this.startCampaignWebViewActivity.assertValues(ProjectDataFactory.project(project))
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.experimentsTest.assertValue("Project Page Viewed")
    }

    @Test
    fun testStartCreatorBioWebViewActivity_whenClickingControlCreatorDetails_liveNotBackedProject() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.creatorNameTextViewClicked()
        this.startCreatorBioWebViewActivity.assertValues(project)
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName, "Creator Details Clicked")
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName, "Creator Details Clicked")
        this.experimentsTest.assertValues("Project Page Viewed", "Creator Details Clicked")
    }

    @Test
    fun testStartCreatorBioWebViewActivity_whenClickingControlCreatorDetails_backedProject() {
        val project = ProjectFactory.backedProject()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.creatorNameTextViewClicked()
        this.startCreatorBioWebViewActivity.assertValues(project)
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.experimentsTest.assertValue("Project Page Viewed")
    }

    @Test
    fun testStartCreatorBioWebViewActivity_whenClickingControlCreatorDetails_endedProject() {
        val project = ProjectFactory.successfulProject()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.creatorNameTextViewClicked()
        this.startCreatorBioWebViewActivity.assertValues(project)
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.experimentsTest.assertValue("Project Page Viewed")
    }

    @Test
    fun testStartCreatorBioWebViewActivity_whenClickingVariantCreatorDetails_liveNotBackedProject() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.creatorInfoVariantClicked()
        this.startCreatorBioWebViewActivity.assertValues(project)
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName, "Creator Details Clicked")
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName, "Creator Details Clicked")
        this.experimentsTest.assertValues("Project Page Viewed", "Creator Details Clicked")
    }

    @Test
    fun testStartCreatorBioWebViewActivity_whenClickingVariantCreatorDetails_backedProject() {
        val project = ProjectFactory.backedProject()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.creatorInfoVariantClicked()
        this.startCreatorBioWebViewActivity.assertValues(project)
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.experimentsTest.assertValue("Project Page Viewed")
    }

    @Test
    fun testStartCreatorBioWebViewActivity_whenClickingVariantCreatorDetails_endedProject() {
        val project = ProjectFactory.successfulProject()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.creatorInfoVariantClicked()
        this.startCreatorBioWebViewActivity.assertValues(project)
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.experimentsTest.assertValue("Project Page Viewed")
    }

    @Test
    fun testStartCreatorDashboardActivity() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.creatorDashboardButtonClicked()
        this.startCreatorDashboardActivity.assertValues(project)
    }

    @Test
    fun testStartCommentsActivity() {
        val project = ProjectFactory.project()
        val projectData = ProjectDataFactory.project(project)
        val projectAndData = Pair.create(project, projectData)

        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.commentsTextViewClicked()
        this.startCommentsActivity.assertValues(projectAndData)
    }

    @Test
    fun testStartProjectUpdatesActivity() {
        val project = ProjectFactory.project()
        val projectData = ProjectDataFactory.project(project)
        val projectAndData = Pair.create(project, projectData)
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        // Click on Updates button.
        this.vm.inputs.updatesTextViewClicked()
        this.startProjectUpdatesActivity.assertValues(projectAndData)
    }

    @Test
    fun testStartVideoActivity() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.playVideoButtonClicked()
        this.startVideoActivity.assertValues(project)
    }

    @Test
    fun testManageButtonClicked_whenProjectIsLiveAndBacked() {
        setUpEnvironment(environment())

        // Start the view model with a backed project
        val reward = RewardFactory.reward()
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .backing(
                BackingFactory.backing()
                    .toBuilder()
                    .rewardId(reward.id())
                    .build()
            )
            .rewards(listOf(RewardFactory.noReward(), reward))
            .build()

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.nativeProjectActionButtonClicked()

        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testPledgeActionButtonUIOutputs_whenProjectIsLiveAndBacked() {
        setUpEnvironment(environment())

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.pledgeActionButtonColor.assertValuesAndClear(R.color.button_pledge_manage)
        this.pledgeActionButtonText.assertValuesAndClear(R.string.Manage)
    }

    @Test
    fun testPledgeActionButtonUIOutputs_projectIsLiveAndNotBacked() {
        setUpEnvironment(environment())

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.pledgeActionButtonColor.assertValue(R.color.button_pledge_live)
        this.pledgeActionButtonText.assertValue(R.string.Back_this_project)
    }

    @Test
    fun testPledgeActionButtonUIOutputs_projectIsLiveAndNotBacked_control() {
        setUpEnvironment(environment())

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.pledgeActionButtonColor.assertValue(R.color.button_pledge_live)
        this.pledgeActionButtonText.assertValue(R.string.Back_this_project)
    }

    @Test
    fun testPledgeActionButtonUIOutputs_projectIsLiveAndNotBacked_variant1() {
        setUpEnvironment(
            environment().toBuilder()
                .optimizely(MockExperimentsClientType(OptimizelyExperiment.Variant.VARIANT_1))
                .build()
        )

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.pledgeActionButtonColor.assertValue(R.color.button_pledge_live)
        this.pledgeActionButtonText.assertValue(R.string.See_the_rewards)
    }

    @Test
    fun testPledgeActionButtonUIOutputs_projectIsLiveAndNotBacked_variant2() {
        setUpEnvironment(
            environment().toBuilder()
                .optimizely(MockExperimentsClientType(OptimizelyExperiment.Variant.VARIANT_2))
                .build()
        )

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.pledgeActionButtonColor.assertValue(R.color.button_pledge_live)
        this.pledgeActionButtonText.assertValue(R.string.View_the_rewards)
    }

    @Test
    fun testPledgeActionButtonUIOutputs_whenProjectIsEndedAndBacked() {
        setUpEnvironment(environment())
        val backedSuccessfulProject = ProjectFactory.backedProject()
            .toBuilder()
            .state(Project.STATE_SUCCESSFUL)
            .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedSuccessfulProject))

        this.pledgeActionButtonColor.assertValue(R.color.button_pledge_ended)
        this.pledgeActionButtonText.assertValue(R.string.View_your_pledge)
    }

    @Test
    fun testPledgeActionButtonUIOutputs_whenProjectIsEndedAndNotBacked() {
        setUpEnvironment(environment())

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.successfulProject()))

        this.pledgeActionButtonColor.assertValue(R.color.button_pledge_ended)
        this.pledgeActionButtonText.assertValuesAndClear(R.string.View_rewards)
    }

    @Test
    fun testPledgeActionButtonUIOutputs_whenCurrentUserIsProjectCreator() {
        val creator = UserFactory.creator()
        val creatorProject = ProjectFactory.project()
            .toBuilder()
            .creator(creator)
            .build()
        val environment = environment()
            .toBuilder()
            .currentUser(MockCurrentUser(creator))
            .build()
        setUpEnvironment(environment)

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, creatorProject))

        this.pledgeActionButtonColor.assertValue(R.color.button_pledge_ended)
        this.pledgeActionButtonText.assertValuesAndClear(R.string.View_your_rewards)
    }

    @Test
    fun testPledgeActionButtonUIOutputs_whenBackingIsErrored() {
        setUpEnvironment(environment())
        val backedSuccessfulProject = ProjectFactory.backedProject()
            .toBuilder()
            .backing(BackingFactory.backing(Backing.STATUS_ERRORED))
            .state(Project.STATE_SUCCESSFUL)
            .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedSuccessfulProject))

        this.pledgeActionButtonColor.assertValue(R.color.button_pledge_error)
        this.pledgeActionButtonText.assertValue(R.string.Manage)
    }

    @Test
    fun testPledgeToolbarNavigationIcon() {
        setUpEnvironment(environment())

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.pledgeToolbarNavigationIcon.assertValue(R.drawable.ic_arrow_down)

        this.vm.inputs.fragmentStackCount(1)

        this.pledgeToolbarNavigationIcon.assertValues(R.drawable.ic_arrow_down, R.drawable.ic_arrow_back)

        this.vm.inputs.fragmentStackCount(0)

        this.pledgeToolbarNavigationIcon.assertValues(R.drawable.ic_arrow_down, R.drawable.ic_arrow_back, R.drawable.ic_arrow_down)
    }

    @Test
    fun testPledgeToolbarTitle_whenProjectIsLiveAndUnbacked() {
        setUpEnvironment(environment())

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.pledgeToolbarTitle.assertValue(R.string.Back_this_project)
    }

    @Test
    fun testPledgeToolbarTitle_whenProjectIsLiveAndBacked() {
        setUpEnvironment(environment())

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.pledgeToolbarTitle.assertValue(R.string.Manage_your_pledge)
    }

    @Test
    fun testPledgeToolbarTitle_whenProjectIsEndedAndUnbacked() {
        setUpEnvironment(environment())

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.successfulProject()))

        this.pledgeToolbarTitle.assertValuesAndClear(R.string.View_rewards)
    }

    @Test
    fun testPledgeToolbarTitle_whenProjectIsEndedAndBacked() {
        setUpEnvironment(environment())

        val backedSuccessfulProject = ProjectFactory.backedProject()
            .toBuilder()
            .state(Project.STATE_SUCCESSFUL)
            .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedSuccessfulProject))

        this.pledgeToolbarTitle.assertValue(R.string.View_your_pledge)
    }

    @Test
    fun testExpandPledgeSheet_whenCollapsingSheet() {
        setUpEnvironment(environment())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.vm.inputs.nativeProjectActionButtonClicked()
        this.expandPledgeSheet.assertValue(Pair(true, true))

        this.vm.inputs.pledgeToolbarNavigationClicked()
        this.expandPledgeSheet.assertValues(Pair(true, true), Pair(false, true))
        this.goBack.assertNoValues()
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName, "Project Page Pledge Button Clicked", EventName.CTA_CLICKED.eventName)
        this.experimentsTest.assertValues("Project Page Viewed", "Project Page Pledge Button Clicked")
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName, "Project Page Pledge Button Clicked", EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testExpandPledgeSheet_whenProjectLiveAndNotBacked() {
        setUpEnvironment(environment())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.vm.inputs.nativeProjectActionButtonClicked()

        this.expandPledgeSheet.assertValue(Pair(true, true))
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName, "Project Page Pledge Button Clicked", EventName.CTA_CLICKED.eventName)
        this.experimentsTest.assertValues("Project Page Viewed", "Project Page Pledge Button Clicked")
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName, "Project Page Pledge Button Clicked", EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testExpandPledgeSheet_whenProjectLiveAndBacked() {
        setUpEnvironment(environment())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.vm.inputs.nativeProjectActionButtonClicked()

        this.expandPledgeSheet.assertValue(Pair(true, true))
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testExpandPledgeSheet_whenProjectBackedAndErrored() {
        setUpEnvironment(environment())
        val backing = BackingFactory.backing(Backing.STATUS_ERRORED)
        val project = ProjectFactory.backedSuccessfulProject().toBuilder().backing(backing).build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.nativeProjectActionButtonClicked()

        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName, "Manage Pledge Button Clicked")
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName, "Manage Pledge Button Clicked")
    }

    @Test
    fun testExpandPledgeSheet_whenProjectEndedAndNotBacked() {
        setUpEnvironment(environment())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.successfulProject()))

        this.vm.inputs.nativeProjectActionButtonClicked()

        this.expandPledgeSheet.assertValue(Pair(true, true))
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testExpandPledgeSheet_whenProjectEndedAndBacked() {
        setUpEnvironment(environment())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedSuccessfulProject()))

        this.vm.inputs.nativeProjectActionButtonClicked()

        this.expandPledgeSheet.assertValue(Pair(true, true))
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testExpandPledgeSheet_whenComingBackFromProjectPage_OKResult() {
        setUpEnvironment(environment())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.vm.activityResult(ActivityResult.create(ActivityRequestCodes.SHOW_REWARDS, Activity.RESULT_OK, null))

        this.expandPledgeSheet.assertValue(Pair(true, true))
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testExpandPledgeSheet_whenComingBackFromProjectPage_CanceledResult() {
        setUpEnvironment(environment())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.vm.activityResult(ActivityResult.create(ActivityRequestCodes.SHOW_REWARDS, Activity.RESULT_CANCELED, null))

        this.expandPledgeSheet.assertNoValues()
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testExpandPledgeSheet_whenIntentExpandPledgeSheet_isTrue() {
        setUpEnvironment(environment())
        val intent = Intent()
            .putExtra(IntentKey.PROJECT, ProjectFactory.project())
            .putExtra(IntentKey.EXPAND_PLEDGE_SHEET, true)
        this.vm.intent(intent)

        this.expandPledgeSheet.assertValues(Pair(true, true))
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testExpandPledgeSheet_whenIntentExpandPledgeSheet_isFalse() {
        setUpEnvironment(environment())
        val intent = Intent()
            .putExtra(IntentKey.PROJECT, ProjectFactory.project())
            .putExtra(IntentKey.EXPAND_PLEDGE_SHEET, false)
        this.vm.intent(intent)

        this.expandPledgeSheet.assertNoValues()
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testExpandPledgeSheet_whenIntentExpandPledgeSheet_isNull() {
        setUpEnvironment(environment())
        val intent = Intent()
            .putExtra(IntentKey.PROJECT, ProjectFactory.project())
        this.vm.intent(intent)

        this.expandPledgeSheet.assertNoValues()
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testGoBack_whenFragmentBackStackIsEmpty() {
        setUpEnvironment(environment())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.vm.inputs.pledgeToolbarNavigationClicked()
        this.goBack.assertNoValues()
    }

    @Test
    fun testGoBack_whenFragmentBackStackIsNotEmpty() {
        setUpEnvironment(environment())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.vm.inputs.fragmentStackCount(3)
        this.vm.inputs.pledgeToolbarNavigationClicked()
        this.goBack.assertValueCount(1)

        this.vm.inputs.fragmentStackCount(2)
        this.vm.inputs.pledgeToolbarNavigationClicked()
        this.goBack.assertValueCount(2)
        this.expandPledgeSheet.assertNoValues()
    }

    @Test
    fun testSetInitialRewardsContainerY() {
        setUpEnvironment(environment())
        this.vm.inputs.onGlobalLayout()
        this.setInitialRewardsContainerY.assertValueCount(1)
    }

    @Test
    fun testBackingDetails_whenProjectNotBacked() {
        setUpEnvironment(environment())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))
        this.backingDetailsIsVisible.assertValue(false)
        this.backingDetailsSubtitle.assertNoValues()
        this.backingDetailsTitle.assertNoValues()
    }

    @Test
    fun testBackingDetails_whenShippableRewardBacked() {
        val environment = environment()
        setUpEnvironment(environment)
        val reward = RewardFactory.reward()
            .toBuilder()
            .id(4)
            .build()

        val amount = 34.0
        val backing = BackingFactory.backing()
            .toBuilder()
            .amount(amount)
            .shippingAmount(4f)
            .rewardId(4)
            .build()

        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .backing(backing)
            .rewards(listOf(reward))
            .build()

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))
        this.backingDetailsIsVisible.assertValue(true)
        val expectedCurrency = expectedCurrency(environment, backedProject, amount)
        this.backingDetailsSubtitle.assertValue(Either.Left("$expectedCurrency • Digital Bundle"))
        this.backingDetailsTitle.assertValue(R.string.Youre_a_backer)
    }

    @Test
    fun testBackingDetails_whenDigitalReward() {
        val environment = environment()
        setUpEnvironment(environment)
        val amount = 13.5
        val noRewardBacking = BackingFactory.backing()
            .toBuilder()
            .amount(amount)
            .reward(RewardFactory.noReward())
            .build()

        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .backing(noRewardBacking)
            .build()

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))
        this.backingDetailsIsVisible.assertValue(true)
        val expectedCurrency = expectedCurrency(environment, backedProject, amount)
        this.backingDetailsSubtitle.assertValue(Either.Left(expectedCurrency))
        this.backingDetailsTitle.assertValue(R.string.Youre_a_backer)
    }

    @Test
    fun testBackingDetails_whenBackingIsErrored() {
        setUpEnvironment(environment())

        val backedSuccessfulProject = ProjectFactory.backedProject()
            .toBuilder()
            .backing(BackingFactory.backing(Backing.STATUS_ERRORED))
            .state(Project.STATE_SUCCESSFUL)
            .build()

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedSuccessfulProject))
        this.backingDetailsIsVisible.assertValue(true)
        this.backingDetailsSubtitle.assertValue(Either.Right(R.string.We_cant_process_your_pledge))
        this.backingDetailsTitle.assertValue(R.string.Payment_failure)
    }

    @Test
    fun testScrimIsVisible_whenNotBackedProject() {
        setUpEnvironment(environment())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.vm.inputs.fragmentStackCount(0)
        this.scrimIsVisible.assertValue(false)

        this.vm.inputs.fragmentStackCount(1)
        this.scrimIsVisible.assertValue(false)

        this.vm.inputs.fragmentStackCount(2)
        this.scrimIsVisible.assertValues(false)

        this.vm.inputs.fragmentStackCount(3)
        this.scrimIsVisible.assertValues(false)

        this.vm.inputs.fragmentStackCount(1)
        this.scrimIsVisible.assertValues(false)
    }

    @Test
    fun testScrimIsVisible_whenBackedProject() {
        setUpEnvironment(environment())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.vm.inputs.fragmentStackCount(0)
        this.scrimIsVisible.assertValue(false)

        this.vm.inputs.fragmentStackCount(1)
        this.scrimIsVisible.assertValue(false)

        this.vm.inputs.fragmentStackCount(2)
        this.scrimIsVisible.assertValues(false)

        this.vm.inputs.fragmentStackCount(3)
        this.scrimIsVisible.assertValues(false)

        this.vm.inputs.fragmentStackCount(2)
        this.scrimIsVisible.assertValues(false)
    }

    @Test
    fun testCancelPledgeSuccess() {
        setUpEnvironment(environment())

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.projectData.assertValueCount(1)

        this.vm.inputs.pledgeSuccessfullyCancelled()
        this.expandPledgeSheet.assertValue(Pair(false, false))
        this.showCancelPledgeSuccess.assertValueCount(1)
        this.projectData.assertValueCount(2)
    }

    @Test
    fun testManagePledgeMenu_whenProjectBackedAndLive_backingIsPledged() {
        setUpEnvironment(environment())

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.managePledgeMenu.assertValue(R.menu.manage_pledge_live)
    }

    @Test
    fun testManagePledgeMenu_whenProjectBackedAndLive_backingIsPreauth() {
        setUpEnvironment(environment())

        // Start the view model with a backed project
        val backing = BackingFactory.backing()
            .toBuilder()
            .status(Backing.STATUS_PREAUTH)
            .build()
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .backing(backing)
            .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.managePledgeMenu.assertValue(R.menu.manage_pledge_preauth)
    }

    @Test
    fun testManagePledgeMenu_whenProjectBackedAndNotLive() {
        setUpEnvironment(environment())

        // Start the view model with a backed project
        val successfulBackedProject = ProjectFactory.backedProject()
            .toBuilder()
            .state(Project.STATE_SUCCESSFUL)
            .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, successfulBackedProject))

        this.managePledgeMenu.assertValue(R.menu.manage_pledge_ended)
    }

    @Test
    fun testManagePledgeMenu_whenProjectNotBacked() {
        setUpEnvironment(environment())

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.managePledgeMenu.assertValue(null)
    }

    @Test
    fun testManagePledgeMenu_whenManaging() {
        setUpEnvironment(environment())

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.managePledgeMenu.assertValue(R.menu.manage_pledge_live)

        this.vm.inputs.cancelPledgeClicked()
        this.vm.inputs.fragmentStackCount(1)
        this.managePledgeMenu.assertValues(R.menu.manage_pledge_live, null)

        this.vm.inputs.fragmentStackCount(0)
        this.managePledgeMenu.assertValues(R.menu.manage_pledge_live, null, R.menu.manage_pledge_live)
    }

    @Test
    fun testShowCancelPledgeFragment_whenBackingIsCancelable() {
        setUpEnvironment(environment())
        val backing = BackingFactory.backing()
            .toBuilder()
            .cancelable(true)
            .build()

        // Start the view model with a backed project
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .backing(backing)
            .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.nativeProjectActionButtonClicked()
        this.vm.inputs.cancelPledgeClicked()

        this.showCancelPledgeFragment.assertValue(backedProject)
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testShowCancelPledgeFragment_whenBackingIsNotCancelable() {
        setUpEnvironment(environment())
        val backing = BackingFactory.backing()
            .toBuilder()
            .cancelable(false)
            .build()

        // Start the view model with a backed project
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .backing(backing)
            .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.nativeProjectActionButtonClicked()
        this.vm.inputs.cancelPledgeClicked()

        this.showCancelPledgeFragment.assertNoValues()
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testShowConversation() {
        setUpEnvironment(environment())

        // Start the view model with a backed project
        val backedProject = ProjectFactory.backedProject()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.nativeProjectActionButtonClicked()
        this.vm.inputs.contactCreatorClicked()

        this.startMessagesActivity.assertValue(backedProject)
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testShowPledgeNotCancelableDialog_whenBackingIsCancelable() {
        setUpEnvironment(environment())
        val backing = BackingFactory.backing()
            .toBuilder()
            .cancelable(true)
            .build()

        // Start the view model with a backed project
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .backing(backing)
            .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.cancelPledgeClicked()
        this.showPledgeNotCancelableDialog.assertNoValues()
    }

    @Test
    fun testShowPledgeNotCancelableDialog_whenBackingIsNotCancelable() {
        setUpEnvironment(environment())
        val backing = BackingFactory.backing()
            .toBuilder()
            .cancelable(false)
            .build()

        // Start the view model with a backed project
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .backing(backing)
            .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.cancelPledgeClicked()
        this.showPledgeNotCancelableDialog.assertValueCount(1)
    }

    @Test
    fun testRevealRewardsFragment_whenBackedProjectLive() {
        setUpEnvironment(environment())

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.vm.inputs.nativeProjectActionButtonClicked()
        this.vm.inputs.viewRewardsClicked()

        this.revealRewardsFragment.assertValueCount(1)
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testRevealRewardsFragment_whenBackedProjectEnded() {
        setUpEnvironment(environment())

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedSuccessfulProject()))

        this.vm.inputs.nativeProjectActionButtonClicked()
        this.vm.inputs.viewRewardsClicked()

        this.revealRewardsFragment.assertValueCount(1)
        this.lakeTest.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
        this.segmentTrack.assertValues("Project Page Viewed", EventName.PAGE_VIEWED.eventName)
    }

    // TODO this will be fixed in https://kickstarter.atlassian.net/browse/NT-1390
    /*
    @Test
    fun testShowUpdatePledge_whenFixingPaymentMethod() {
        setUpEnvironment(environment())

        // Start the view model with a backed project
        val reward = RewardFactory.reward()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(BackingFactory.backing()
                        .toBuilder()
                        .rewardId(reward.id())
                        .build())
                .rewards(listOf(RewardFactory.noReward(), reward))
                .build()

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.fixPaymentMethodButtonClicked()

        this.showUpdatePledge.assertValuesAndClear(Pair(PledgeData.builder()
                .pledgeFlowContext(PledgeFlowContext.FIX_ERRORED_PLEDGE)
                .reward(reward)
                .projectData(ProjectDataFactory.project(backedProject))
                .build(), PledgeReason.FIX_PLEDGE))
        this.koalaTest.assertValue("Project Page")
        this.lakeTest.assertValues("Project Page Viewed","Fix Pledge Button Clicked")
    }

    @Test
    fun testShowUpdatePledge_whenUpdatingPledge() {
        setUpEnvironment(environment())

        // Start the view model with a backed project
        val reward = RewardFactory.reward()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(BackingFactory.backing()
                        .toBuilder()
                        .rewardId(reward.id())
                        .build())
                .rewards(listOf(RewardFactory.noReward(), reward))
                .build()

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.updatePledgeClicked()

        this.showUpdatePledge.assertValuesAndClear(Pair(PledgeData.builder()
                .pledgeFlowContext(PledgeFlowContext.MANAGE_REWARD)
                .reward(reward)
                .projectData(ProjectDataFactory.project(backedProject))
                .build(), PledgeReason.UPDATE_PLEDGE))
        this.koalaTest.assertValues("Project Page", "Manage Pledge Option Clicked")
        this.lakeTest.assertValue("Project Page Viewed")
    }

    @Test
    fun testShowUpdatePledge_whenUpdatingPaymentMethod() {
        setUpEnvironment(environment())

        // Start the view model with a backed project
        val reward = RewardFactory.reward()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(BackingFactory.backing()
                        .toBuilder()
                        .rewardId(reward.id())
                        .build())
                .rewards(listOf(RewardFactory.noReward(), reward))
                .build()

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.updatePaymentClicked()

        this.showUpdatePledge.assertValuesAndClear(Pair(PledgeData.builder()
                .pledgeFlowContext(PledgeFlowContext.MANAGE_REWARD)
                .reward(reward)
                .projectData(ProjectDataFactory.project(backedProject))
                .build(), PledgeReason.UPDATE_PAYMENT))
        this.koalaTest.assertValues("Project Page", "Manage Pledge Option Clicked")
        this.lakeTest.assertValue("Project Page Viewed")
    }*/

    @Test
    fun testSendingAnalyticsEvents_whenUpdatingPaymentMethod() {
        setUpEnvironment(environment())

        // Start the view model with a backed project
        val reward = RewardFactory.reward()
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .backing(
                BackingFactory.backing()
                    .toBuilder()
                    .rewardId(reward.id())
                    .build()
            )
            .rewards(listOf(RewardFactory.noReward(), reward))
            .build()

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.updatePaymentClicked()

        this.lakeTest.assertValues("Project Page Viewed", "Page Viewed", "Page Viewed")
        this.segmentTrack.assertValues("Project Page Viewed", "Page Viewed", "Page Viewed")
    }

    @Test
    fun testShowUpdatePledgeSuccess_whenUpdatingPayment() {
        val initialBackedProject = ProjectFactory.backedProject()
        val refreshedProject = ProjectFactory.backedProject()
        val environment = environment()
            .toBuilder()
            .apiClient(apiClientWithSuccessFetchingProjectFromSlug(refreshedProject))
            .build()
        setUpEnvironment(environment)

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, initialBackedProject))

        this.projectData.assertValues(ProjectDataFactory.project(initialBackedProject))
        this.showUpdatePledgeSuccess.assertNoValues()
        this.updateFragments.assertValue(ProjectDataFactory.project(initialBackedProject))

        this.vm.inputs.pledgePaymentSuccessfullyUpdated()
        this.projectData.assertValues(
            ProjectDataFactory.project(initialBackedProject),
            ProjectDataFactory.project(refreshedProject)
        )
        this.showUpdatePledgeSuccess.assertValueCount(1)
        this.updateFragments.assertValues(
            ProjectDataFactory.project(initialBackedProject),
            ProjectDataFactory.project(refreshedProject),
            ProjectDataFactory.project(refreshedProject)
        )
    }

    @Test
    fun testShowUpdatePledgeSuccess_whenUpdatingPledge() {
        val initialBackedProject = ProjectFactory.backedProject()
        val refreshedProject = ProjectFactory.backedProject()
        val environment = environment()
            .toBuilder()
            .apiClient(apiClientWithSuccessFetchingProjectFromSlug(refreshedProject))
            .build()
        setUpEnvironment(environment)

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, initialBackedProject))

        this.projectData.assertValues(ProjectDataFactory.project(initialBackedProject))
        this.showUpdatePledgeSuccess.assertNoValues()
        this.updateFragments.assertValue(ProjectDataFactory.project(initialBackedProject))

        this.vm.inputs.pledgeSuccessfullyUpdated()
        this.projectData.assertValues(
            ProjectDataFactory.project(initialBackedProject),
            ProjectDataFactory.project(refreshedProject)
        )
        this.showUpdatePledgeSuccess.assertValueCount(1)
        this.updateFragments.assertValues(
            ProjectDataFactory.project(initialBackedProject),
            ProjectDataFactory.project(refreshedProject),
            ProjectDataFactory.project(refreshedProject)
        )
    }

    @Test
    fun testStartThanksActivity() {
        setUpEnvironment(environment())

        // Start the view model with a unbacked project
        val project = ProjectFactory.project()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.projectData.assertValueCount(1)

        val checkoutData = CheckoutDataFactory.checkoutData(3L, 20.0, 30.0)
        val pledgeData = PledgeData.with(PledgeFlowContext.NEW_PLEDGE, ProjectDataFactory.project(project), RewardFactory.reward())
        this.vm.inputs.pledgeSuccessfullyCreated(Pair(checkoutData, pledgeData))
        this.expandPledgeSheet.assertValue(Pair(false, false))
        this.startThanksActivity.assertValue(Pair(checkoutData, pledgeData))
        this.projectData.assertValueCount(2)
    }

    @Test
    fun testProjectData_whenRefreshProjectIsCalled() {
        setUpEnvironment(environment())

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.projectData.assertValueCount(1)

        this.vm.inputs.refreshProject()
        this.projectData.assertValueCount(2)
    }

    private fun apiClientWithSuccessFetchingProject(refreshedProject: Project): MockApiClient {
        return object : MockApiClient() {
            override fun fetchProject(project: Project): Observable<Project> {
                return Observable.just(refreshedProject)
            }
        }
    }

    private fun apiClientWithSuccessFetchingProjectFromSlug(refreshedProject: Project): MockApiClient {
        return object : MockApiClient() {
            override fun fetchProject(slug: String): Observable<Project> {
                return Observable.just(refreshedProject)
            }
        }
    }

    private fun deepLinkIntent(): Intent {
        val uri = Uri.parse("https://www.kickstarter.com/projects/1186238668/skull-graphic-tee")
        return Intent(Intent.ACTION_VIEW, uri)
    }

    private fun expectedCurrency(environment: Environment, project: Project, amount: Double): String =
        environment.ksCurrency().format(amount, project, RoundingMode.HALF_UP)
}
