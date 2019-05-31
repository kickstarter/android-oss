package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSCurrency
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.RewardsItem
import org.junit.Test
import rx.observers.TestSubscriber

class HorizontalRewardViewHolderViewModelTest: KSRobolectricTestCase() {

    private lateinit var vm: HorizontalRewardViewHolderViewModel.ViewModel
    private val conversionTextViewText = TestSubscriber.create<String>()
    private val conversionSectionIsGone = TestSubscriber.create<Boolean>()
    private val descriptionTextViewText = TestSubscriber<String>()
    private val isClickable = TestSubscriber<Boolean>()
    private val limitAndRemainingTextViewIsGone = TestSubscriber<Boolean>()
    private val limitAndRemainingTextViewText = TestSubscriber<Pair<String, String>>()
    private val minimumTextViewText = TestSubscriber<String>()
    private val reward = TestSubscriber<Reward>()
    private val rewardDescriptionIsGone = TestSubscriber<Boolean>()
    private val rewardEndDateSectionIsGone = TestSubscriber<Boolean>()
    private val rewardItems = TestSubscriber<List<RewardsItem>>()
    private val rewardItemsAreGone = TestSubscriber<Boolean>()
    private val showPledgeFragment = TestSubscriber<Pair<Project, Reward>>()
    private val startBackingActivity = TestSubscriber<Project>()
    private val titleTextViewIsGone = TestSubscriber<Boolean>()
    private val titleTextViewText = TestSubscriber<String>()

    private fun setUpEnvironment(@NonNull environment: Environment) {
        this.vm = HorizontalRewardViewHolderViewModel.ViewModel(environment)
        this.vm.outputs.conversionText().subscribe(this.conversionTextViewText)
        this.vm.outputs.conversionTextViewIsGone().subscribe(this.conversionSectionIsGone)
        this.vm.outputs.descriptionText().subscribe(this.descriptionTextViewText)
        this.vm.outputs.isClickable().subscribe(this.isClickable)
        this.vm.outputs.limitAndRemainingText().subscribe(this.limitAndRemainingTextViewText)
        this.vm.outputs.limitAndRemainingTextViewIsGone().subscribe(this.limitAndRemainingTextViewIsGone)
        this.vm.outputs.minimumText().subscribe(this.minimumTextViewText)
        this.vm.outputs.reward().subscribe(this.reward)
        this.vm.outputs.rewardDescriptionIsGone().subscribe(this.rewardDescriptionIsGone)
        this.vm.outputs.rewardEndDateSectionIsGone().subscribe(this.rewardEndDateSectionIsGone)
        this.vm.outputs.rewardItems().subscribe(this.rewardItems)
        this.vm.outputs.rewardItemsAreGone().subscribe(this.rewardItemsAreGone)
        this.vm.outputs.showPledgeFragment().subscribe(this.showPledgeFragment)
        this.vm.outputs.startBackingActivity().subscribe(this.startBackingActivity)
        this.vm.outputs.titleTextViewIsGone().subscribe(this.titleTextViewIsGone)
        this.vm.outputs.titleText().subscribe(this.titleTextViewText)
    }

    @Test
    fun testConversionHiddenForProject() {
        // Set the project currency and the user's chosen currency to the same value
        setUpEnvironment(environment())
        val project = ProjectFactory.project().toBuilder().currency("USD").currentCurrency("USD").build()
        val reward = RewardFactory.reward()

        // the conversion should be hidden.
        this.vm.inputs.projectAndReward(project, reward)
        this.conversionTextViewText.assertValueCount(1)
        this.conversionSectionIsGone.assertValue(true)
    }

    @Test
    fun testConversionShownForProject() {
        // Set the project currency and the user's chosen currency to different values
        setUpEnvironment(environment())
        val project = ProjectFactory.project().toBuilder().currency("CAD").currentCurrency("USD").build()
        val reward = RewardFactory.reward()

        // USD conversion should shown.
        this.vm.inputs.projectAndReward(project, reward)
        this.conversionTextViewText.assertValueCount(1)
        this.conversionSectionIsGone.assertValue(false)
    }

    @Test
    fun testConversionTextRoundsUp_USUser_prefersUSD() {
        // Set user's country to US.
        val currentConfig = MockCurrentConfig()
        currentConfig.config(ConfigFactory.configForUSUser())
        val environment = environment().toBuilder()
                .currentConfig(currentConfig)
                .ksCurrency(KSCurrency(currentConfig))
                .build()
        setUpEnvironment(environment)

        // Set project's country to CA with USD preference and reward minimum to $0.30.
        val project = ProjectFactory.caProject().toBuilder().currentCurrency("USD").build()
        val reward = RewardFactory.reward().toBuilder().minimum(0.3).build()

        // USD conversion should be rounded up.
        this.vm.inputs.projectAndReward(project, reward)
        this.conversionTextViewText.assertValue("$1")
    }

    @Test
    fun testConversionTextRoundsUp_ITUser_prefersUSD() {
        // Set user's country to IT.
        val currentConfig = MockCurrentConfig()
        currentConfig.config(ConfigFactory.configForITUser())
        val environment = environment().toBuilder()
                .currentConfig(currentConfig)
                .ksCurrency(KSCurrency(currentConfig))
                .build()
        setUpEnvironment(environment)

        // Set project's country to CA with USD preference and reward minimum to $0.30.
        val project = ProjectFactory.caProject().toBuilder().currentCurrency("USD").build()
        val reward = RewardFactory.reward().toBuilder().minimum(0.3).build()

        // USD conversion should be rounded up.
        this.vm.inputs.projectAndReward(project, reward)
        this.conversionTextViewText.assertValue("US$ 1")
    }

    @Test
    fun testDescriptionTextViewText() {
        val project = ProjectFactory.project()
        val reward = RewardFactory.reward()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, reward)
        this.descriptionTextViewText.assertValue(reward.description())
    }

    @Test
    fun testGoToCheckoutWhenProjectIsSuccessful() {
        val project = ProjectFactory.successfulProject()
        val reward = RewardFactory.reward()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, reward)
        this.showPledgeFragment.assertNoValues()

        this.vm.inputs.rewardClicked()
        this.showPledgeFragment.assertNoValues()
    }

    @Test
    fun testGoToCheckoutWhenProjectIsSuccessfulAndHasBeenBacked() {
        val project = ProjectFactory.backedProject().toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        val reward = project.backing()?.reward() as Reward
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, reward)
        this.showPledgeFragment.assertNoValues()

        this.vm.inputs.rewardClicked()
        this.showPledgeFragment.assertNoValues()
    }

    @Test
    fun testGoToPledgeFragmentWhenProjectIsLive() {
        val reward = RewardFactory.reward()
        val liveProject = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(liveProject, reward)
        this.showPledgeFragment.assertNoValues()

        // When a reward from a live project is clicked, start checkout.
        this.vm.inputs.rewardClicked()
        this.showPledgeFragment.assertValue(Pair.create(liveProject, reward))
    }

    @Test
    fun testGoToViewPledge() {
        val liveProject = ProjectFactory.backedProject()
        val successfulProject = ProjectFactory.backedProject().toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()

        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(liveProject, liveProject.backing()?.reward() as Reward)
        this.startBackingActivity.assertNoValues()

        // When the project is still live, don't go to 'view pledge'. Should go to checkout instead.
        this.vm.inputs.rewardClicked()
        this.startBackingActivity.assertNoValues()

        // When project is successful but not backed, don't go to view pledge.
        this.vm.inputs.projectAndReward(successfulProject, RewardFactory.reward())
        this.vm.inputs.rewardClicked()
        this.startBackingActivity.assertNoValues()

        // When project is successful and backed, go to view pledge.
        this.vm.inputs.projectAndReward(successfulProject, successfulProject.backing()?.reward() as Reward)
        this.startBackingActivity.assertNoValues()
        this.vm.inputs.rewardClicked()
        this.startBackingActivity.assertValues(successfulProject)
    }

    @Test
    fun testIsClickable() {
        setUpEnvironment(environment())

        // A reward from a live project should be clickable.
        this.vm.inputs.projectAndReward(ProjectFactory.project(), RewardFactory.reward())
        this.isClickable.assertValue(true)

        // A reward from a successful project should not be clickable.
        this.vm.inputs.projectAndReward(ProjectFactory.successfulProject(), RewardFactory.reward())
        this.isClickable.assertValues(true, false)
        //
        // A backed reward from a live project should be clickable.
        val backedLiveProject = ProjectFactory.backedProject()
        this.vm.inputs.projectAndReward(backedLiveProject, backedLiveProject.backing()?.reward()!!)
        this.isClickable.assertValues(true, false, true)

        // A backed reward from a finished project should be clickable (distinct until changed).
        val backedSuccessfulProject = ProjectFactory.backedProject().toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        this.vm.inputs.projectAndReward(backedSuccessfulProject, backedSuccessfulProject.backing()?.reward()!!)
        this.isClickable.assertValues(true, false, true)

        // A reward with its limit reached should not be clickable.
        this.vm.inputs.projectAndReward(ProjectFactory.project(), RewardFactory.limitReached())
        this.isClickable.assertValues(true, false, true, false)
    }

    @Test
    fun testLimitAndRemaining() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // When reward is limited, quantity should be shown.
        val limitedReward = RewardFactory.reward().toBuilder()
                .limit(10)
                .remaining(5)
                .build()
        this.vm.inputs.projectAndReward(project, limitedReward)
        this.limitAndRemainingTextViewText.assertValue(Pair.create("10", "5"))
        this.limitAndRemainingTextViewIsGone.assertValue(false)

        // When reward's limit has been reached, don't show quantity.
        this.vm.inputs.projectAndReward(project, RewardFactory.limitReached())
        this.limitAndRemainingTextViewIsGone.assertValues(false, true)

        // When reward has no limit, don't show quantity (distinct until changed).
        this.vm.inputs.projectAndReward(project, RewardFactory.reward())
        this.limitAndRemainingTextViewIsGone.assertValues(false, true)
    }

    @Test
    fun testMinimumTextViewText() {
        val project = ProjectFactory.project()
        val reward = RewardFactory.reward().toBuilder()
                .minimum(10.0)
                .build()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, reward)
        this.minimumTextViewText.assertValue("$10")
    }

    @Test
    fun testMinimumTextViewTextCAD() {
        val project = ProjectFactory.caProject()
        val reward = RewardFactory.reward().toBuilder()
                .minimum(10.0)
                .build()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, reward)
        this.minimumTextViewText.assertValue("CA$ 10")
    }

    @Test
    fun testRewardItems() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Items section should be hidden when there are no items.
        this.vm.inputs.projectAndReward(project, RewardFactory.reward())
        this.rewardItemsAreGone.assertValue(true)
        this.rewardItems.assertNoValues()

        val itemizedReward = RewardFactory.itemized()
        this.vm.inputs.projectAndReward(project, itemizedReward)
        this.rewardItemsAreGone.assertValues(true, false)
        this.rewardItems.assertValues(itemizedReward.rewardsItems())
    }

    @Test
    fun testTitleTextViewText() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Reward with no title should be hidden.
        val rewardWithNoTitle = RewardFactory.reward().toBuilder()
                .title(null)
                .build()
        this.vm.inputs.projectAndReward(project, rewardWithNoTitle)
        this.titleTextViewIsGone.assertValues(true)
        this.titleTextViewText.assertNoValues()

        // Reward with title should be visible.
        val title = "Digital bundle"
        val rewardWithTitle = RewardFactory.reward().toBuilder()
                .title(title)
                .build()
        this.vm.inputs.projectAndReward(project, rewardWithTitle)
        this.titleTextViewIsGone.assertValues(true, false)
        this.titleTextViewText.assertValue(title)
    }

    @Test
    fun testNonEmptyRewardsDescriptionAreShown() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, RewardFactory.reward())
        this.rewardDescriptionIsGone.assertValue(false)
    }

    @Test
    fun testEmptyRewardsDescriptionAreGone() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, RewardFactory.noDescription())
        this.rewardDescriptionIsGone.assertValue(true)
    }

    @Test
    fun testRewardEndDateSectionIsGone() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, RewardFactory.rewardWithEndDate())
        this.rewardDescriptionIsGone.assertValue(false)
    }

    @Test
    fun testReward() {
        val project = ProjectFactory.project()
        val reward = RewardFactory.noDescription()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, reward)
        this.reward.assertValue(reward)
    }
}
