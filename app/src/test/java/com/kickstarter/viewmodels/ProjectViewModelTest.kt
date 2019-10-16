package com.kickstarter.viewmodels

import android.content.Intent
import android.net.Uri
import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KoalaEvent
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.preferences.BooleanPreferenceType
import com.kickstarter.libs.preferences.MockBooleanPreference
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.*
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class ProjectViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: ProjectViewModel.ViewModel
    private val backingDetails = TestSubscriber<String>()
    private val backingDetailsIsVisible = TestSubscriber<Boolean>()
    private val expandPledgeSheet = TestSubscriber<Pair<Boolean, Boolean>>()
    private val heartDrawableId = TestSubscriber<Int>()
    private val managePledgeMenu = TestSubscriber<Int?>()
    private val prelaunchUrl = TestSubscriber<String>()
    private val progressBarIsGone = TestSubscriber<Boolean>()
    private val projectActionButtonContainerIsGone = TestSubscriber<Boolean>()
    private val projectTest = TestSubscriber<Project>()
    private val reloadProjectContainerIsGone = TestSubscriber<Boolean>()
    private val revealRewardsFragment = TestSubscriber<Void>()
    private val rewardsButtonColor = TestSubscriber<Int>()
    private val rewardsButtonText = TestSubscriber<Int>()
    private val rewardsToolbarTitle = TestSubscriber<Int>()
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
    private val startBackingActivity = TestSubscriber<Pair<Project, User>>()
    private val startCampaignWebViewActivity = TestSubscriber<Project>()
    private val startCommentsActivity = TestSubscriber<Project>()
    private val startCreatorBioWebViewActivity = TestSubscriber<Project>()
    private val startLoginToutActivity = TestSubscriber<Void>()
    private val startManagePledgeActivity = TestSubscriber<Project>()
    private val startMessagesActivity = TestSubscriber<Project>()
    private val startProjectUpdatesActivity = TestSubscriber<Project>()
    private val startThanksActivity = TestSubscriber<Project>()
    private val startVideoActivity = TestSubscriber<Project>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ProjectViewModel.ViewModel(environment)
        this.vm.outputs.backingDetails().subscribe(this.backingDetails)
        this.vm.outputs.backingDetailsIsVisible().subscribe(this.backingDetailsIsVisible)
        this.vm.outputs.expandPledgeSheet().subscribe(this.expandPledgeSheet)
        this.vm.outputs.heartDrawableId().subscribe(this.heartDrawableId)
        this.vm.outputs.managePledgeMenu().subscribe(this.managePledgeMenu)
        this.vm.outputs.prelaunchUrl().subscribe(this.prelaunchUrl)
        this.vm.outputs.progressBarIsGone().subscribe(this.progressBarIsGone)
        this.vm.outputs.projectActionButtonContainerIsGone().subscribe(this.projectActionButtonContainerIsGone)
        this.vm.outputs.projectAndUserCountry().map { pc -> pc.first }.subscribe(this.projectTest)
        this.vm.outputs.reloadProjectContainerIsGone().subscribe(this.reloadProjectContainerIsGone)
        this.vm.outputs.revealRewardsFragment().subscribe(this.revealRewardsFragment)
        this.vm.outputs.rewardsButtonColor().subscribe(this.rewardsButtonColor)
        this.vm.outputs.rewardsButtonText().subscribe(this.rewardsButtonText)
        this.vm.outputs.rewardsToolbarTitle().subscribe(this.rewardsToolbarTitle)
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
        this.vm.outputs.projectAndUserCountry().map { pc -> pc.first.isStarred }.subscribe(this.savedTest)
        this.vm.outputs.startBackingActivity().subscribe(this.startBackingActivity)
        this.vm.outputs.startCampaignWebViewActivity().subscribe(this.startCampaignWebViewActivity)
        this.vm.outputs.startCommentsActivity().subscribe(this.startCommentsActivity)
        this.vm.outputs.startCreatorBioWebViewActivity().subscribe(this.startCreatorBioWebViewActivity)
        this.vm.outputs.startManagePledgeActivity().subscribe(this.startManagePledgeActivity)
        this.vm.outputs.startMessagesActivity().subscribe(this.startMessagesActivity)
        this.vm.outputs.startProjectUpdatesActivity().subscribe(this.startProjectUpdatesActivity)
        this.vm.outputs.startThanksActivity().subscribe(this.startThanksActivity)
        this.vm.outputs.startVideoActivity().subscribe(this.startVideoActivity)
    }

    @Test
    fun testEmitsProjectWithStandardSetUp() {
        val project = ProjectFactory.project()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(ConfigFactory.config())

        setUpEnvironment(environment().toBuilder().currentConfig(currentConfig).build())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.projectTest.assertValues(project, project)
        this.koalaTest.assertValues(KoalaEvent.PROJECT_PAGE)
    }

    @Test
    fun testEmitsProjectWithDeepLink() {
        val project = ProjectFactory.project()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(ConfigFactory.config())

        val environment = environment().toBuilder()
                .currentConfig(currentConfig)
                .apiClient(object : MockApiClient(){
                    override fun fetchProject(param: String): Observable<Project> {
                        return Observable.just(project)
                    }
                })
                .build()

        setUpEnvironment(environment)
        val intent = deepLinkIntent()
        this.vm.intent(intent)

        this.projectTest.assertValues(project)
        this.prelaunchUrl.assertNoValues()
        this.koalaTest.assertValues(KoalaEvent.PROJECT_PAGE)
    }

    @Test
    fun testEmitsProjectWithDeepLink_preLaunchActivated() {
        val url = "https://www.kickstarter.com/projects/1186238668/skull-graphic-tee"
        val project = ProjectFactory.prelaunchProject(url)
        val currentConfig = MockCurrentConfig()
        currentConfig.config(ConfigFactory.config())

        val environment = environment().toBuilder()
                .currentConfig(currentConfig)
                .apiClient(object : MockApiClient(){
                    override fun fetchProject(param: String): Observable<Project> {
                        return Observable.just(project)
                    }
                })
                .build()

        setUpEnvironment(environment)
        val uri = Uri.parse(url)
        this.vm.intent(Intent(Intent.ACTION_VIEW, uri))

        this.projectTest.assertNoValues()
        this.prelaunchUrl.assertValue(url)
        this.koalaTest.assertNoValues()
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

        this.savedTest.assertValues(false, false)
        this.heartDrawableId.assertValues(R.drawable.icon__heart_outline, R.drawable.icon__heart_outline)

        // Try starring while logged out
        this.vm.inputs.heartButtonClicked()

        // The project shouldn't be saved, and a login prompt should be shown.
        this.savedTest.assertValues(false, false)
        this.heartDrawableId.assertValues(R.drawable.icon__heart_outline, R.drawable.icon__heart_outline)
        this.showSavedPromptTest.assertValueCount(0)
        this.startLoginToutActivity.assertValueCount(1)

        // A koala event for starring should NOT be tracked
        this.koalaTest.assertValues(KoalaEvent.PROJECT_PAGE)

        // Login
        currentUser.refresh(UserFactory.user())

        // The project should be saved, and a star prompt should be shown.
        this.savedTest.assertValues(false, false, true)
        this.heartDrawableId.assertValues(R.drawable.icon__heart_outline, R.drawable.icon__heart_outline, R.drawable.icon__heart)
        this.showSavedPromptTest.assertValueCount(1)

        // A koala event for starring should be tracked
        this.koalaTest.assertValues(
                KoalaEvent.PROJECT_PAGE, KoalaEvent.PROJECT_STAR, KoalaEvent.STARRED_PROJECT
        )
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
        this.koalaTest.assertValues(
                KoalaEvent.PROJECT_PAGE, KoalaEvent.PROJECT_SHOW_SHARE_SHEET_LEGACY, KoalaEvent.SHOWED_SHARE_SHEET
        )
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
        this.savedTest.assertValues(false, false, true)
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
        this.savedTest.assertValues(false, false, true)
        this.heartDrawableId.assertValues(R.drawable.icon__heart_outline, R.drawable.icon__heart_outline, R.drawable.icon__heart)
        this.showSavedPromptTest.assertValueCount(0)
    }

    @Test
    fun testStartBackingActivity() {
        val project = ProjectFactory.project()
        val user = UserFactory.user()

        setUpEnvironment(environment().toBuilder().currentUser(MockCurrentUser(user)).build())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.viewPledgeButtonClicked()
        this.startBackingActivity.assertValues(Pair.create(project, user))
    }

    @Test
    fun testStartCampaignWebViewActivity() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.blurbTextViewClicked()
        this.startCampaignWebViewActivity.assertValues(project)
    }

    @Test
    fun testStartCreatorBioWebViewActivity() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.creatorNameTextViewClicked()
        this.startCreatorBioWebViewActivity.assertValues(project)
    }

    @Test
    fun testStartCommentsActivity() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.commentsTextViewClicked()
        this.startCommentsActivity.assertValues(project)
    }

    @Test
    fun testStartManagePledgeActivity() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        // Click on Manage pledge button.
        this.vm.inputs.managePledgeButtonClicked()
        this.startManagePledgeActivity.assertValues(project)
    }

    @Test
    fun testStartProjectUpdatesActivity() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        // Click on Updates button.
        this.vm.inputs.updatesTextViewClicked()
        this.startProjectUpdatesActivity.assertValues(project)
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
    fun testRewardsButtonUIOutputs() {
        setUpEnvironment(environment().toBuilder().nativeCheckoutPreference(MockBooleanPreference(true)).build())

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.rewardsButtonColor.assertValuesAndClear(R.color.button_pledge_live)
        this.rewardsButtonText.assertValuesAndClear(R.string.Back_this_project)

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.rewardsButtonColor.assertValuesAndClear(R.color.button_pledge_manage)
        this.rewardsButtonText.assertValuesAndClear(R.string.Manage)

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.successfulProject()))

        this.rewardsButtonColor.assertValue(R.color.button_pledge_ended)
        this.rewardsButtonText.assertValuesAndClear(R.string.View_rewards)

        val backedSuccessfulProject = ProjectFactory.backedProject()
                .toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedSuccessfulProject))

        this.rewardsButtonColor.assertValue(R.color.button_pledge_ended)
        this.rewardsButtonText.assertValue(R.string.View_your_pledge)
    }

    @Test
    fun testRewardsToolbarTitle() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.rewardsToolbarTitle.assertNoValues()

        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.rewardsToolbarTitle.assertValuesAndClear(R.string.Back_this_project)

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.rewardsToolbarTitle.assertValuesAndClear(R.string.Manage_your_pledge)

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.successfulProject()))

        this.rewardsToolbarTitle.assertValuesAndClear(R.string.View_rewards)

        val backedSuccessfulProject = ProjectFactory.backedProject()
                .toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedSuccessfulProject))

        this.rewardsToolbarTitle.assertValue(R.string.View_your_pledge)
    }

    @Test
    fun testExpandPledgeSheet_whenCollapsingSheet() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))
        this.vm.inputs.collapsePledgeSheet()
        this.expandPledgeSheet.assertValue(Pair(false, true))
    }

    @Test
    fun testExpandPledgeSheet_whenProjectLiveAndNotBacked() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.vm.inputs.nativeProjectActionButtonClicked()

        this.expandPledgeSheet.assertValue(Pair(true, true))
        this.koalaTest.assertValues("Project Page", "Back this Project Button Clicked")
    }

    @Test
    fun testExpandPledgeSheet_whenProjectLiveAndBacked() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.vm.inputs.nativeProjectActionButtonClicked()

        this.expandPledgeSheet.assertValue(Pair(true, true))
        this.koalaTest.assertValues("Project Page", "Manage Pledge Button Clicked")
    }

    @Test
    fun testExpandPledgeSheet_whenProjectEndedAndNotBacked() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.successfulProject()))

        this.vm.inputs.nativeProjectActionButtonClicked()

        this.expandPledgeSheet.assertValue(Pair(true, true))
        this.koalaTest.assertValues("Project Page", "View Rewards Button Clicked")
    }

    @Test
    fun testExpandPledgeSheet_whenProjectEndedAndBacked() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedSuccessfulProject()))

        this.vm.inputs.nativeProjectActionButtonClicked()

        this.expandPledgeSheet.assertValue(Pair(true, true))
        this.koalaTest.assertValues("Project Page", "View Your Pledge Button Clicked")
    }

    @Test
    fun testSetInitialRewardsContainerY() {
        setUpEnvironment(environment())
        this.vm.inputs.onGlobalLayout()
        this.setInitialRewardsContainerY.assertValueCount(1)
    }

    @Test
    fun testBackingDetails_whenProjectNotBacked() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))
        this.backingDetailsIsVisible.assertValue(false)
        this.backingDetails.assertNoValues()
    }

    @Test
    fun testBackingDetails_whenShippableRewardBacked() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        val reward = RewardFactory.reward()
                .toBuilder()
                .id(4)
                .build()

        val backing = BackingFactory.backing()
                .toBuilder()
                .amount(34.0)
                .shippingAmount(4f)
                .rewardId(4)
                .build()

        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .rewards(listOf(reward))
                .build()

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))
        this.backingDetails.assertValuesAndClear("$34 • Digital Bundle")
        this.backingDetailsIsVisible.assertValue(true)
    }

    @Test
    fun testBackingDetails_whenDigitalReward() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        val noRewardBacking = BackingFactory.backing()
                .toBuilder()
                .amount(13.5)
                .reward(RewardFactory.noReward())
                .build()

        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(noRewardBacking)
                .build()

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))
        this.backingDetails.assertValuesAndClear("$13.50")
        this.backingDetailsIsVisible.assertValue(true)
    }

    @Test
    fun testScrimIsVisible_whenNativeCheckoutDisabled() {
        setUpEnvironment(environment())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.vm.inputs.fragmentStackCount(0)
        this.scrimIsVisible.assertNoValues()
    }

    @Test
    fun testScrimIsVisible_whenNotBackedProject() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.vm.inputs.fragmentStackCount(0)
        this.scrimIsVisible.assertValue(false)

        this.vm.inputs.fragmentStackCount(1)
        this.scrimIsVisible.assertValue(false)

        this.vm.inputs.fragmentStackCount(2)
        this.scrimIsVisible.assertValues(false, true)

        this.vm.inputs.fragmentStackCount(1)
        this.scrimIsVisible.assertValues(false, true, false)
    }

    @Test
    fun testScrimIsVisible_whenBackedProject() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.vm.inputs.fragmentStackCount(0)
        this.scrimIsVisible.assertValue(false)

        this.vm.inputs.fragmentStackCount(1)
        this.scrimIsVisible.assertValue(false)

        this.vm.inputs.fragmentStackCount(2)
        this.scrimIsVisible.assertValues(false)

        this.vm.inputs.fragmentStackCount(3)
        this.scrimIsVisible.assertValues(false, true)

        this.vm.inputs.fragmentStackCount(2)
        this.scrimIsVisible.assertValues(false, true, false)
    }

    @Test
    fun testCancelPledgeSuccess() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.projectTest.assertValueCount(2)

        this.vm.inputs.pledgeSuccessfullyCancelled()
        this.expandPledgeSheet.assertValue(Pair(false, false))
        this.showCancelPledgeSuccess.assertValueCount(1)
        this.projectTest.assertValueCount(3)
    }

    @Test
    fun testManagePledgeMenu_whenProjectBackedAndLive() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.managePledgeMenu.assertValue(R.menu.manage_pledge_live)
    }

    @Test
    fun testManagePledgeMenu_whenProjectBackedAndNotLive() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

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
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.managePledgeMenu.assertValue(null)
    }

    @Test
    fun testManagePledgeMenu_whenManaging() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

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
    fun testProjectActionButtonContainerIsGone_whenProjectFromIntentSuccessfullyLoads() {
        val environment = environmentWithNativeCheckoutEnabled()
                .toBuilder()
                .apiClient(apiClientWithSuccessFetchingProject())
                .build()
        setUpEnvironment(environment)

        val projectWithNoRewards = ProjectFactory.project()
                .toBuilder()
                .rewards(null)
                .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, projectWithNoRewards))

        this.projectActionButtonContainerIsGone.assertValues(true, false)
    }

    @Test
    fun testProjectActionButtonContainerIsGone_whenProjectFromIntentUnsuccessfullyLoads() {
        val environment = environmentWithNativeCheckoutEnabled()
                .toBuilder()
                .apiClient(apiClientWithErrorFetchingProject())
                .build()
        setUpEnvironment(environment)

        val projectWithNoRewards = ProjectFactory.project()
                .toBuilder()
                .rewards(null)
                .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, projectWithNoRewards))

        this.projectActionButtonContainerIsGone.assertValues(true)
    }

    @Test
    fun testProjectActionButtonContainerIsGone_whenProjectFromDeepLinkSuccessfullyLoads() {
        val environment = environmentWithNativeCheckoutEnabled()
                .toBuilder()
                .apiClient(apiClientWithSuccessFetchingProjectFromParam())
                .build()
        setUpEnvironment(environment)

        this.vm.intent(deepLinkIntent())

        this.projectActionButtonContainerIsGone.assertValues(false)
    }

    @Test
    fun testProjectActionButtonContainerIsGone_whenProjectFromDeepLinkUnsuccessfullyLoads() {
        val environment = environmentWithNativeCheckoutEnabled()
                .toBuilder()
                .apiClient(apiClientWithErrorFetchingProjectFromParam())
                .build()
        setUpEnvironment(environment)

        this.vm.intent(deepLinkIntent())

        this.projectActionButtonContainerIsGone.assertNoValues()
    }

    @Test
    fun testProgressBarIsGone_whenProjectFromIntentSuccessfullyLoads() {
        val environment = environment()
                .toBuilder()
                .apiClient(apiClientWithSuccessFetchingProject())
                .build()
        setUpEnvironment(environment)

        val projectWithNoRewards = ProjectFactory.project()
                .toBuilder()
                .rewards(null)
                .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, projectWithNoRewards))

        this.progressBarIsGone.assertValues(false, true)
    }

    @Test
    fun testProgressBarIsGone_whenProjectFromIntentUnsuccessfullyLoads() {
        val environment = environment()
                .toBuilder()
                .apiClient(apiClientWithErrorFetchingProject())
                .build()
        setUpEnvironment(environment)

        val projectWithNoRewards = ProjectFactory.project()
                .toBuilder()
                .rewards(null)
                .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, projectWithNoRewards))

        this.progressBarIsGone.assertValues(false, true)
    }

    @Test
    fun testProgressBarIsGone_whenProjectFromDeepLinkSuccessfullyLoads() {
        val environment = environment()
                .toBuilder()
                .apiClient(apiClientWithSuccessFetchingProjectFromParam())
                .build()
        setUpEnvironment(environment)

        this.vm.intent(deepLinkIntent())

        this.progressBarIsGone.assertValues(false, true)
    }

    @Test
    fun testProgressBarIsGone_whenProjectFromDeepLinkUnsuccessfullyLoads() {
        val environment = environment()
                .toBuilder()
                .apiClient(apiClientWithErrorFetchingProjectFromParam())
                .build()
        setUpEnvironment(environment)

        this.vm.intent(deepLinkIntent())

        this.progressBarIsGone.assertValues(false, true)
    }

    @Test
    fun testReloadProjectContainerIsGone_whenProjectFromIntentSuccessfullyLoads() {
        val environment = environmentWithNativeCheckoutEnabled()
                .toBuilder()
                .apiClient(apiClientWithSuccessFetchingProject())
                .build()
        setUpEnvironment(environment)

        val projectWithNoRewards = ProjectFactory.project()
                .toBuilder()
                .rewards(null)
                .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, projectWithNoRewards))

        this.reloadProjectContainerIsGone.assertValue(true)
    }

    @Test
    fun testReloadProjectContainerIsGone_whenProjectFromIntentUnsuccessfullyLoads() {
        val environment = environmentWithNativeCheckoutEnabled()
                .toBuilder()
                .apiClient(apiClientWithErrorFetchingProject())
                .build()
        setUpEnvironment(environment)

        val projectWithNoRewards = ProjectFactory.project()
                .toBuilder()
                .rewards(null)
                .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, projectWithNoRewards))

        this.reloadProjectContainerIsGone.assertValues(false)
    }

    @Test
    fun testReloadProjectContainerIsGone_whenProjectFromDeepLinkSuccessfullyLoads() {
        val environment = environmentWithNativeCheckoutEnabled()
                .toBuilder()
                .apiClient(apiClientWithSuccessFetchingProjectFromParam())
                .build()
        setUpEnvironment(environment)

        this.vm.intent(deepLinkIntent())

        this.reloadProjectContainerIsGone.assertValue(true)
    }

    @Test
    fun testReloadProjectContainerIsGone_whenProjectFromDeepLinkUnsuccessfullyLoads() {
        val environment = environmentWithNativeCheckoutEnabled()
                .toBuilder()
                .apiClient(apiClientWithErrorFetchingProjectFromParam())
                .build()
        setUpEnvironment(environment)

        this.vm.intent(deepLinkIntent())

        this.reloadProjectContainerIsGone.assertValue(false)
    }

    @Test
    fun testReloadingProject_fromIntent() {
        var error = true

        val environment = environmentWithNativeCheckoutEnabled()
                .toBuilder()
                .apiClient(object : MockApiClient() {
                    override fun fetchProject(project: Project): Observable<Project> {
                        val observable = when {
                            error -> Observable.error(Throwable("boop"))
                            else -> Observable.just(ProjectFactory.project())
                        }
                        return observable
                    }
                })
                .build()
        setUpEnvironment(environment)

        val projectWithNoRewards = ProjectFactory.project()
                .toBuilder()
                .rewards(null)
                .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, projectWithNoRewards))

        this.projectActionButtonContainerIsGone.assertValue(true)
        this.progressBarIsGone.assertValues(false, true)
        this.reloadProjectContainerIsGone.assertValue(false)

        error = false
        this.vm.inputs.reloadProjectContainerClicked()

        this.projectActionButtonContainerIsGone.assertValues(true, false)
        this.progressBarIsGone.assertValues(false, true, false, true)
        this.reloadProjectContainerIsGone.assertValues(false, true, true)
    }

    @Test
    fun testReloadingProject_fromDeepLink() {
        var error = true

        val environment = environmentWithNativeCheckoutEnabled()
                .toBuilder()
                .apiClient(object : MockApiClient() {
                    override fun fetchProject(param: String): Observable<Project> {
                        val observable = when {
                            error -> Observable.error(Throwable("boop"))
                            else -> Observable.just(ProjectFactory.project())
                        }
                        return observable
                    }
                })
                .build()
        setUpEnvironment(environment)

        this.vm.intent(deepLinkIntent())

        this.projectActionButtonContainerIsGone.assertNoValues()
        this.progressBarIsGone.assertValues(false, true)
        this.reloadProjectContainerIsGone.assertValues(false)

        error = false
        this.vm.inputs.reloadProjectContainerClicked()

        this.projectActionButtonContainerIsGone.assertValue(false)
        this.progressBarIsGone.assertValues(false, true, false, true)
        this.reloadProjectContainerIsGone.assertValues(false, true, true)
    }

    @Test
    fun testShowCancelPledgeFragment_whenBackingIsCancelable() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
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
        this.koalaTest.assertValues("Project Page", "Manage Pledge Button Clicked", "Manage Pledge Option Clicked")
    }

    @Test
    fun testShowCancelPledgeFragment_whenBackingIsNotCancelable() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
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
        this.koalaTest.assertValues("Project Page", "Manage Pledge Button Clicked", "Manage Pledge Option Clicked")
    }

    @Test
    fun testShowConversation() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        val backedProject = ProjectFactory.backedProject()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.nativeProjectActionButtonClicked()
        this.vm.inputs.contactCreatorClicked()

        this.startMessagesActivity.assertValue(backedProject)
        this.koalaTest.assertValues("Project Page", "Manage Pledge Button Clicked", "Manage Pledge Option Clicked")
    }

    @Test
    fun testShowPledgeNotCancelableDialog_whenBackingIsCancelable() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
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
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
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
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.vm.inputs.nativeProjectActionButtonClicked()
        this.vm.inputs.viewRewardsClicked()

        this.revealRewardsFragment.assertValueCount(1)
        this.koalaTest.assertValues("Project Page", "Manage Pledge Button Clicked", "Manage Pledge Option Clicked")
    }

    @Test
    fun testRevealRewardsFragment_whenBackedProjectEnded() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedSuccessfulProject()))

        this.vm.inputs.nativeProjectActionButtonClicked()
        this.vm.inputs.viewRewardsClicked()

        this.revealRewardsFragment.assertValueCount(1)
        this.koalaTest.assertValues("Project Page", "View Your Pledge Button Clicked", "Manage Pledge Option Clicked")
    }

    @Test
    fun testShowUpdatePledge_whenUpdatingPledge() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

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

        this.showUpdatePledge.assertValuesAndClear(Pair(PledgeData(reward, backedProject), PledgeReason.UPDATE_PLEDGE))
        this.koalaTest.assertValues("Project Page", "Manage Pledge Option Clicked")
    }

    @Test
    fun testShowUpdatePledge_whenUpdatingPaymentMethod() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

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

        this.showUpdatePledge.assertValuesAndClear(Pair(PledgeData(reward, backedProject), PledgeReason.UPDATE_PAYMENT))
        this.koalaTest.assertValues("Project Page", "Manage Pledge Option Clicked")
    }

    @Test
    fun testShowUpdatePledgeSuccess_whenUpdatingPayment() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.projectTest.assertValueCount(2)

        this.vm.inputs.pledgePaymentSuccessfullyUpdated()
        this.showUpdatePledgeSuccess.assertValueCount(1)
        this.projectTest.assertValueCount(3)
    }

    @Test
    fun testShowUpdatePledgeSuccess_whenUpdatingPledge() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.projectTest.assertValueCount(2)

        this.vm.inputs.pledgeSuccessfullyUpdated()
        this.showUpdatePledgeSuccess.assertValueCount(1)
        this.projectTest.assertValueCount(3)
    }

    @Test
    fun testStartThanksActivity() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a unbacked project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.projectTest.assertValueCount(2)

        this.vm.inputs.pledgeSuccessfullyCreated()
        this.expandPledgeSheet.assertValue(Pair(false, false))
        this.startThanksActivity.assertValueCount(1)
        this.projectTest.assertValueCount(3)
    }

    private fun apiClientWithErrorFetchingProject(): MockApiClient {
        return object : MockApiClient() {
            override fun fetchProject(project: Project): Observable<Project> {
                return Observable.error(Throwable("boop"))
            }
        }
    }

    private fun apiClientWithSuccessFetchingProject(): MockApiClient {
        return object : MockApiClient() {
            override fun fetchProject(project: Project): Observable<Project> {
                return Observable.just(ProjectFactory.project())
            }
        }
    }

    private fun apiClientWithErrorFetchingProjectFromParam(): MockApiClient {
        return object : MockApiClient() {
            override fun fetchProject(param: String): Observable<Project> {
                return Observable.error(Throwable("boop"))
            }
        }
    }

    private fun apiClientWithSuccessFetchingProjectFromParam(): MockApiClient {
        return object : MockApiClient() {
            override fun fetchProject(param: String): Observable<Project> {
                return Observable.just(ProjectFactory.project())
            }
        }
    }

    private fun deepLinkIntent(): Intent {
        val uri = Uri.parse("https://www.kickstarter.com/projects/1186238668/skull-graphic-tee")
        return Intent(Intent.ACTION_VIEW, uri)
    }

    private fun environmentWithNativeCheckoutEnabled() : Environment {
        val currentUser = MockCurrentUser()
        val rewardsEnabled: BooleanPreferenceType = MockBooleanPreference(true)
        val currentConfig = MockCurrentConfig()
        currentConfig.config(ConfigFactory.config())

        return environment().toBuilder()
                .currentConfig(currentConfig)
                .currentUser(currentUser)
                .nativeCheckoutPreference(rewardsEnabled)
                .build()
    }
}
