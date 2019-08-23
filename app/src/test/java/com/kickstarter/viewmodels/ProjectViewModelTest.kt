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
    private val expandPledgeSheet = TestSubscriber<Boolean>()
    private val heartDrawableId = TestSubscriber<Int>()
    private val managePledgeMenuIsVisible = TestSubscriber<Boolean>()
    private val prelaunchUrl = TestSubscriber<String>()
    private val projectTest = TestSubscriber<Project>()
    private val revealRewardsFragment = TestSubscriber<Void>()
    private val rewardsButtonColor = TestSubscriber<Int>()
    private val rewardsButtonText = TestSubscriber<Int>()
    private val rewardsToolbarTitle = TestSubscriber<Int>()
    private val savedTest = TestSubscriber<Boolean>()
    private val scrimIsVisible = TestSubscriber<Boolean>()
    private val setInitialRewardsContainerY = TestSubscriber<Void>()
    private val showCancelPledgeFragment = TestSubscriber<Project>()
    private val showCancelPledgeSuccess = TestSubscriber<Void>()
    private val showSavedPromptTest = TestSubscriber<Void>()
    private val showShareSheet = TestSubscriber<Project>()
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
    private val startVideoActivity = TestSubscriber<Project>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ProjectViewModel.ViewModel(environment)
        this.vm.outputs.backingDetails().subscribe(this.backingDetails)
        this.vm.outputs.backingDetailsIsVisible().subscribe(this.backingDetailsIsVisible)
        this.vm.outputs.expandPledgeSheet().subscribe(this.expandPledgeSheet)
        this.vm.outputs.heartDrawableId().subscribe(this.heartDrawableId)
        this.vm.outputs.managePledgeMenuIsVisible().subscribe(this.managePledgeMenuIsVisible)
        this.vm.outputs.prelaunchUrl().subscribe(this.prelaunchUrl)
        this.vm.outputs.projectAndUserCountry().map { pc -> pc.first }.subscribe(this.projectTest)
        this.vm.outputs.revealRewardsFragment().subscribe(this.revealRewardsFragment)
        this.vm.outputs.rewardsButtonColor().subscribe(this.rewardsButtonColor)
        this.vm.outputs.rewardsButtonText().subscribe(this.rewardsButtonText)
        this.vm.outputs.rewardsToolbarTitle().subscribe(this.rewardsToolbarTitle)
        this.vm.outputs.scrimIsVisible().subscribe(this.scrimIsVisible)
        this.vm.outputs.setInitialRewardsContainerY().subscribe(this.setInitialRewardsContainerY)
        this.vm.outputs.showCancelPledgeFragment().subscribe(this.showCancelPledgeFragment)
        this.vm.outputs.showCancelPledgeSuccess().subscribe(this.showCancelPledgeSuccess)
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
        this.koalaTest.assertValues(KoalaEvent.PROJECT_PAGE, KoalaEvent.VIEWED_PROJECT_PAGE)
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
        val uri = Uri.parse("https://www.kickstarter.com/projects/1186238668/skull-graphic-tee")
        this.vm.intent(Intent(Intent.ACTION_VIEW, uri))

        this.projectTest.assertValues(project)
        this.prelaunchUrl.assertNoValues()
        this.koalaTest.assertValues(KoalaEvent.PROJECT_PAGE, KoalaEvent.VIEWED_PROJECT_PAGE)
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
        this.koalaTest.assertValues(KoalaEvent.PROJECT_PAGE, KoalaEvent.VIEWED_PROJECT_PAGE)

        // Login
        currentUser.refresh(UserFactory.user())

        // The project should be saved, and a star prompt should be shown.
        this.savedTest.assertValues(false, false, true)
        this.heartDrawableId.assertValues(R.drawable.icon__heart_outline, R.drawable.icon__heart_outline, R.drawable.icon__heart)
        this.showSavedPromptTest.assertValueCount(1)

        // A koala event for starring should be tracked
        this.koalaTest.assertValues(
                KoalaEvent.PROJECT_PAGE, KoalaEvent.VIEWED_PROJECT_PAGE, KoalaEvent.PROJECT_STAR, KoalaEvent.STARRED_PROJECT
        )
    }

    @Test
    fun testShowShareSheet() {
        val project = ProjectFactory.project()
        val user = UserFactory.user()

        setUpEnvironment(environment().toBuilder().currentUser(MockCurrentUser(user)).build())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.shareButtonClicked()
        this.showShareSheet.assertValues(project)
        this.koalaTest.assertValues(
                KoalaEvent.PROJECT_PAGE, KoalaEvent.VIEWED_PROJECT_PAGE,
                KoalaEvent.PROJECT_SHOW_SHARE_SHEET_LEGACY, KoalaEvent.SHOWED_SHARE_SHEET
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
        this.expandPledgeSheet.assertValue(false)
    }

    @Test
    fun testExpandPledgeSheet_whenExpandingSheet() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))
        this.vm.inputs.nativeProjectActionButtonClicked()
        this.expandPledgeSheet.assertValue(true)
        this.vm.inputs.nativeProjectActionButtonClicked()
        this.expandPledgeSheet.assertValues(true, true)
    }

    @Test
    fun testSetInitialRewardsContainerY() {
        setUpEnvironment(environment())
        this.vm.inputs.onGlobalLayout()
        this.setInitialRewardsContainerY.assertValueCount(1)
    }

    @Test
    fun testBackingDetailsOutputs() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))
        this.backingDetailsIsVisible.assertValue(false)
        this.backingDetails.assertNoValues()

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.successfulProject()))
        this.backingDetailsIsVisible.assertValue(false)
        this.backingDetails.assertNoValues()

        val backedSuccessfulProject = ProjectFactory.backedProject()
                .toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedSuccessfulProject))
        this.backingDetailsIsVisible.assertValuesAndClear(false)
        this.backingDetails.assertNoValues()

        val reward = RewardFactory.reward()
                .toBuilder()
                .id(4)
                .build()

        val backing = BackingFactory.backing()
                .toBuilder()
                .amount(34.0)
                .rewardId(4)
                .build()

        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .rewards(listOf(reward))
                .build()

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))
        this.backingDetails.assertValuesAndClear("$20 • Digital Bundle")
        this.backingDetailsIsVisible.assertValue(true)

        val noRewardBacking = BackingFactory.backing()
                .toBuilder()
                .amount(13.5)
                .reward(RewardFactory.noReward())
                .build()

        val backedProjectdNoReward = ProjectFactory.backedProject()
                .toBuilder()
                .backing(noRewardBacking)
                .build()

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProjectdNoReward))
        this.backingDetails.assertValuesAndClear("$13.50")
        this.backingDetailsIsVisible.assertValue(true)

        val noRewardWholeBacking = BackingFactory.backing()
                .toBuilder()
                .amount(15.0)
                .reward(RewardFactory.noReward())
                .build()

        val backedProjectNoRewardWhole = ProjectFactory.backedProject()
                .toBuilder()
                .backing(noRewardWholeBacking)
                .build()

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProjectNoRewardWhole))
        this.backingDetails.assertValue("$15")
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
        this.expandPledgeSheet.assertValue(false)
        this.showCancelPledgeSuccess.assertValueCount(1)
        this.projectTest.assertValueCount(3)
    }

    @Test
    fun testManagePledgeMenuIsVisible_whenProjectBacked() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.managePledgeMenuIsVisible.assertValue(true)
    }

    @Test
    fun testManagePledgeMenuIsVisible_whenProjectNotBacked() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.managePledgeMenuIsVisible.assertValue(false)
    }

    @Test
    fun testManagePledgeMenuIsVisible_whenManaging() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.managePledgeMenuIsVisible.assertValue(true)

        this.vm.inputs.cancelPledgeClicked()
        this.vm.inputs.fragmentStackCount(1)
        this.managePledgeMenuIsVisible.assertValues(true, false)

        this.vm.inputs.fragmentStackCount(0)
        this.managePledgeMenuIsVisible.assertValues(true, false, true)
    }

    @Test
    fun testShowCancelPledgeFragment() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        val backedProject = ProjectFactory.backedProject()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.cancelPledgeClicked()
        this.showCancelPledgeFragment.assertValue(backedProject)
    }

    @Test
    fun testShowConversation() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        val backedProject = ProjectFactory.backedProject()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.contactCreatorClicked()
        this.startMessagesActivity.assertValue(backedProject)
    }

    @Test
    fun testRevealRewardsFragment() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.vm.inputs.viewRewardsClicked()
        this.revealRewardsFragment.assertValueCount(1)
    }

    @Test
    fun testShowUpdatePledge() {
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

        this.showUpdatePledge.assertNoValues()

        this.vm.inputs.updatePledgeClicked()
        this.showUpdatePledge.assertValuesAndClear(Pair(PledgeData(reward, backedProject), PledgeReason.UPDATE_PLEDGE))

        this.vm.inputs.updatePaymentClicked()
        this.showUpdatePledge.assertValuesAndClear(Pair(PledgeData(reward, backedProject), PledgeReason.UPDATE_PAYMENT))
    }

    @Test
    fun testShowUpdatePledgeSuccess() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.projectTest.assertValueCount(2)

        this.vm.inputs.pledgeSuccessfullyUpdated()
        this.showUpdatePledgeSuccess.assertValueCount(1)
        this.projectTest.assertValueCount(3)
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
