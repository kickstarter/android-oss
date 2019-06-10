package com.kickstarter.viewmodels

import android.content.Intent
import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KoalaEvent
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.ui.IntentKey
import org.junit.Test
import rx.observers.TestSubscriber

class ProjectViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: ProjectViewModel.ViewModel
    private val heartDrawableId = TestSubscriber<Int>()
    private val projectTest = TestSubscriber<Project>()
    private val showShareSheet = TestSubscriber<Project>()
    private val showSavedPromptTest = TestSubscriber<Void>()
    private val startLoginToutActivity = TestSubscriber<Void>()
    private val savedTest = TestSubscriber<Boolean>()
    private val setActionButtonId = TestSubscriber<Int>()
    private val setInitialRewardsContainerY = TestSubscriber<Void>()
    private val setRewardsButtonColor = TestSubscriber<Int>()
    private val setRewardsButtonText = TestSubscriber<Int>()
    private val showRewardsFragment = TestSubscriber<Boolean>()
    private val startBackingActivity = TestSubscriber<Pair<Project, User>>()
    private val startCampaignWebViewActivity = TestSubscriber<Project>()
    private val startCommentsActivity = TestSubscriber<Project>()
    private val startCreatorBioWebViewActivity = TestSubscriber<Project>()
    private val startManagePledgeActivity = TestSubscriber<Project>()
    private val startProjectUpdatesActivity = TestSubscriber<Project>()
    private val startVideoActivity = TestSubscriber<Project>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ProjectViewModel.ViewModel(environment)
        this.vm.outputs.heartDrawableId().subscribe(this.heartDrawableId)
        this.vm.outputs.projectAndUserCountryAndIsFeatureEnabled().map { pc -> pc.first.first }.subscribe(this.projectTest)
        this.vm.outputs.setActionButtonId().subscribe(this.setActionButtonId)
        this.vm.outputs.setInitialRewardsContainerY().subscribe(this.setInitialRewardsContainerY)
        this.vm.outputs.setRewardsButtonColor().subscribe(this.setRewardsButtonColor)
        this.vm.outputs.setRewardsButtonText().subscribe(this.setRewardsButtonText)
        this.vm.outputs.showShareSheet().subscribe(this.showShareSheet)
        this.vm.outputs.showRewardsFragment().subscribe(this.showRewardsFragment)
        this.vm.outputs.showSavedPrompt().subscribe(this.showSavedPromptTest)
        this.vm.outputs.startLoginToutActivity().subscribe(this.startLoginToutActivity)
        this.vm.outputs.projectAndUserCountryAndIsFeatureEnabled().map { pc -> pc.first.first.isStarred }.subscribe(this.savedTest)
        this.vm.outputs.startBackingActivity().subscribe(this.startBackingActivity)
        this.vm.outputs.startCampaignWebViewActivity().subscribe(this.startCampaignWebViewActivity)
        this.vm.outputs.startCommentsActivity().subscribe(this.startCommentsActivity)
        this.vm.outputs.startCreatorBioWebViewActivity().subscribe(this.startCreatorBioWebViewActivity)
        this.vm.outputs.startManagePledgeActivity().subscribe(this.startManagePledgeActivity)
        this.vm.outputs.startProjectUpdatesActivity().subscribe(this.startProjectUpdatesActivity)
        this.vm.outputs.startVideoActivity().subscribe(this.startVideoActivity)
    }

    @Test
    fun testProjectViewModel_EmitsProjectWithStandardSetUp() {
        val project = ProjectFactory.project()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(ConfigFactory.config())

        setUpEnvironment(environment().toBuilder().currentConfig(currentConfig).build())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.projectTest.assertValues(project, project)
        this.koalaTest.assertValues(KoalaEvent.PROJECT_PAGE, KoalaEvent.VIEWED_PROJECT_PAGE)
    }

    @Test
    fun testProjectViewModel_LoggedOutStarProjectFlow() {
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
    fun testProjectViewModel_ShowShareSheet() {
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
    fun testProjectViewModel_StarProjectThatIsAlmostCompleted() {
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
    fun testProjectViewModel_SaveProjectThatIsSuccessful() {
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
    fun testProjectViewModel_StartBackingActivity() {
        val project = ProjectFactory.project()
        val user = UserFactory.user()

        setUpEnvironment(environment().toBuilder().currentUser(MockCurrentUser(user)).build())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.viewPledgeButtonClicked()
        this.startBackingActivity.assertValues(Pair.create(project, user))
    }

    @Test
    fun testProjectViewModel_StartCampaignWebViewActivity() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.blurbTextViewClicked()
        this.startCampaignWebViewActivity.assertValues(project)
    }

    @Test
    fun testProjectViewModel_StartCreatorBioWebViewActivity() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.creatorNameTextViewClicked()
        this.startCreatorBioWebViewActivity.assertValues(project)
    }

    @Test
    fun testProjectViewModel_StartCommentsActivity() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.commentsTextViewClicked()
        this.startCommentsActivity.assertValues(project)
    }

    @Test
    fun testProjectViewModel_StartManagePledgeActivity() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        // Click on Manage pledge button.
        this.vm.inputs.managePledgeButtonClicked()
        this.startManagePledgeActivity.assertValues(project)
    }

    @Test
    fun testProjectViewModel_StartProjectUpdatesActivity() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        // Click on Updates button.
        this.vm.inputs.updatesTextViewClicked()
        this.startProjectUpdatesActivity.assertValues(project)
    }

    @Test
    fun testProjectViewModel_StartVideoActivity() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.playVideoButtonClicked()
        this.startVideoActivity.assertValues(project)
    }

    @Test
    fun testProjectViewModel_SetActionButtonId_NonBacked_Live_Project() {
        setUpEnvironment(environment())

        val project = ProjectFactory.project()

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.setActionButtonId.assertValue(R.id.back_project_button)
    }

    @Test
    fun testProjectViewModel_SetActionButtonId_Backed_Live_Project() {
        setUpEnvironment(environment())

        val project = ProjectFactory.backedProject()

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.setActionButtonId.assertValue(R.id.manage_pledge_button)
    }

    @Test
    fun testProjectViewModel_SetActionButtonId_Backed_Ended_Project() {
        setUpEnvironment(environment())

        val project = ProjectFactory.successfulProject()
                .toBuilder()
                .isBacking(true)
                .build()

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.setActionButtonId.assertValue(R.id.view_pledge_button)
    }

    @Test
    fun testProjectViewModel_SetActionButtonIdIsNull_NonBacked_Ended_Project() {
        setUpEnvironment(environment())

        val project = ProjectFactory.successfulProject()
                .toBuilder()
                .isBacking(false)
                .build()

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.setActionButtonId.assertValue(null)
    }

    @Test
    fun testProjectViewModel_SetRewardButtonStringAndColor_NonBacked_Live_Project() {
        setUpEnvironment(environment())

        val project = ProjectFactory.project()

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.setRewardsButtonColor.assertValue(R.color.primary)
        this.setRewardsButtonText.assertValue(R.string.Back_this_project)
    }

    @Test
    fun testProjectViewModel_SetRewardButtonStringAndColor_Backed_Ended_Project() {
        setUpEnvironment(environment())

        val project = ProjectFactory.successfulProject()
                .toBuilder()
                .isBacking(true)
                .build()

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.setRewardsButtonColor.assertValue(R.color.black)
        this.setRewardsButtonText.assertValue(R.string.View_your_pledge)
    }

    @Test
    fun testProjectViewModel_HideRewardsFragment() {
        setUpEnvironment(environment())
        this.vm.inputs.hideRewardsFragmentClicked()
        this.showRewardsFragment.assertValue(false)
    }

    @Test
    fun testProjectViewModel_ShowRewardsFragment() {
        setUpEnvironment(environment())
        this.vm.inputs.nativeCheckoutBackProjectButtonClicked()
        this.showRewardsFragment.assertValue(true)
    }

    @Test
    fun testProjectViewModel_SetInitialRewardsContainerY() {
        setUpEnvironment(environment())
        this.vm.inputs.onGlobalLayout()
        this.setInitialRewardsContainerY.assertValueCount(1)
    }
}
