package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.RewardsItem
import org.joda.time.DateTime
import org.junit.Test
import rx.observers.TestSubscriber

class NativeCheckoutRewardViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: NativeCheckoutRewardViewHolderViewModel.ViewModel
    private val backersCount = TestSubscriber.create<Int>()
    private val backersCountIsGone = TestSubscriber.create<Boolean>()
    private val buttonCTA = TestSubscriber.create<Int>()
    private val buttonIsEnabled = TestSubscriber<Boolean>()
    private val buttonIsGone = TestSubscriber.create<Boolean>()
    private val conversion = TestSubscriber.create<String>()
    private val conversionIsGone = TestSubscriber.create<Boolean>()
    private val descriptionForNoReward = TestSubscriber<Int>()
    private val descriptionForReward = TestSubscriber<String?>()
    private val descriptionIsGone = TestSubscriber<Boolean>()
    private val endDateSectionIsGone = TestSubscriber<Boolean>()
    private val estimatedDelivery = TestSubscriber<String>()
    private val estimatedDeliveryIsGone = TestSubscriber<Boolean>()
    private val limitContainerIsGone = TestSubscriber<Boolean>()
    private val minimumAmountTitle = TestSubscriber<String>()
    private val remaining = TestSubscriber<String>()
    private val remainingIsGone = TestSubscriber<Boolean>()
    private val reward = TestSubscriber<Reward>()
    private val rewardItems = TestSubscriber<List<RewardsItem>>()
    private val rewardItemsAreGone = TestSubscriber<Boolean>()
    private val shippingSummary = TestSubscriber<String>()
    private val shippingSummaryIsGone = TestSubscriber<Boolean>()
    private val showPledgeFragment = TestSubscriber<Pair<Project, Reward>>()
    private val startBackingActivity = TestSubscriber<Project>()
    private val titleForNoReward = TestSubscriber<Int>()
    private val titleForReward = TestSubscriber<String?>()
    private val titleIsGone = TestSubscriber<Boolean>()

    private fun setUpEnvironment(@NonNull environment: Environment) {
        this.vm = NativeCheckoutRewardViewHolderViewModel.ViewModel(environment)
        this.vm.outputs.backersCount().subscribe(this.backersCount)
        this.vm.outputs.backersCountIsGone().subscribe(this.backersCountIsGone)
        this.vm.outputs.buttonCTA().subscribe(this.buttonCTA)
        this.vm.outputs.buttonIsEnabled().subscribe(this.buttonIsEnabled)
        this.vm.outputs.buttonIsGone().subscribe(this.buttonIsGone)
        this.vm.outputs.conversion().subscribe(this.conversion)
        this.vm.outputs.conversionIsGone().subscribe(this.conversionIsGone)
        this.vm.outputs.descriptionForNoReward().subscribe(this.descriptionForNoReward)
        this.vm.outputs.descriptionForReward().subscribe(this.descriptionForReward)
        this.vm.outputs.descriptionIsGone().subscribe(this.descriptionIsGone)
        this.vm.outputs.endDateSectionIsGone().subscribe(this.endDateSectionIsGone)
        this.vm.outputs.estimatedDelivery().subscribe(this.estimatedDelivery)
        this.vm.outputs.estimatedDeliveryIsGone().subscribe(this.estimatedDeliveryIsGone)
        this.vm.outputs.remaining().subscribe(this.remaining)
        this.vm.outputs.remainingIsGone().subscribe(this.remainingIsGone)
        this.vm.outputs.limitContainerIsGone().subscribe(this.limitContainerIsGone)
        this.vm.outputs.minimumAmountTitle().map { it.toString() }.subscribe(this.minimumAmountTitle)
        this.vm.outputs.reward().subscribe(this.reward)
        this.vm.outputs.rewardItems().subscribe(this.rewardItems)
        this.vm.outputs.rewardItemsAreGone().subscribe(this.rewardItemsAreGone)
        this.vm.outputs.shippingSummary().subscribe(this.shippingSummary)
        this.vm.outputs.shippingSummaryIsGone().subscribe(this.shippingSummaryIsGone)
        this.vm.outputs.showPledgeFragment().subscribe(this.showPledgeFragment)
        this.vm.outputs.startBackingActivity().subscribe(this.startBackingActivity)
        this.vm.outputs.titleForNoReward().subscribe(this.titleForNoReward)
        this.vm.outputs.titleForReward().subscribe(this.titleForReward)
        this.vm.outputs.titleIsGone().subscribe(this.titleIsGone)
    }

    @Test
    fun testBackersCount_whenReward_withBackers() {
        setUpEnvironment(environment())

        val reward = RewardFactory.reward()
                .toBuilder()
                .backersCount(30)
                .build()
        this.vm.inputs.projectAndReward(ProjectFactory.project(), reward)

        this.backersCount.assertValue(30)
        this.backersCountIsGone.assertValue(false)
    }

    @Test
    fun testBackersCount_whenReward_withNoBackers() {
        setUpEnvironment(environment())

        val reward = RewardFactory.reward()
                .toBuilder()
                .backersCount(0)
                .build()
        this.vm.inputs.projectAndReward(ProjectFactory.project(), reward)

        this.backersCount.assertNoValues()
        this.backersCountIsGone.assertValue(true)
    }

    @Test
    fun testBackersCount_whenNoReward() {
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(ProjectFactory.project(), RewardFactory.noReward())

        this.backersCount.assertNoValues()
        this.backersCountIsGone.assertValue(true)
    }

    @Test
    fun testButtonUIOutputs() {
        setUpEnvironment(environment())
        val project = ProjectFactory.project()
        val reward = RewardFactory.reward()
        val noReward = RewardFactory.noReward()

        //Live project, available reward, not backed
        this.vm.inputs.projectAndReward(project, reward)
        this.buttonIsGone.assertValue(false)
        this.buttonCTA.assertValue(R.string.Select)

        //Live project, no reward, not backed
        this.vm.inputs.projectAndReward(project, noReward)
        this.buttonIsGone.assertValue(false)
        this.buttonCTA.assertValuesAndClear(R.string.Select)

        //Live project, unavailable limited reward, not backed
        this.vm.inputs.projectAndReward(project, RewardFactory.limitReached())
        this.buttonIsGone.assertValue(false)
        this.buttonCTA.assertValue(R.string.No_longer_available)

        //Live project, unavailable expired reward, not backed
        this.vm.inputs.projectAndReward(project, RewardFactory.ended())
        this.buttonIsGone.assertValue(false)
        this.buttonCTA.assertValuesAndClear(R.string.No_longer_available)

        //Live backed project, currently backed reward
        val backedLiveProject = ProjectFactory.backedProject()
        this.vm.inputs.projectAndReward(backedLiveProject, backedLiveProject.backing()?.reward()?: RewardFactory.reward())
        this.buttonIsGone.assertValues(false)
        this.buttonCTA.assertValuesAndClear(R.string.Selected)

        //Live backed project, not backed available reward
        this.vm.inputs.projectAndReward(backedLiveProject, RewardFactory.reward())
        this.buttonIsGone.assertValues(false)
        this.buttonCTA.assertValuesAndClear(R.string.Select)

        //Live backed project, not backed unavailable limited reward
        this.vm.inputs.projectAndReward(backedLiveProject, RewardFactory.limitReached())
        this.buttonIsGone.assertValues(false)
        this.buttonCTA.assertValue(R.string.No_longer_available)

        //Live backed project, not backed unavailable expired reward
        this.vm.inputs.projectAndReward(backedLiveProject, RewardFactory.limitReached())
        this.buttonIsGone.assertValues(false)
        this.buttonCTA.assertValuesAndClear(R.string.No_longer_available)

        //Ended project, available reward, not backed
        val successfulProject = ProjectFactory.successfulProject()
                .toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        this.vm.inputs.projectAndReward(successfulProject, reward)
        this.buttonIsGone.assertValues(false, true)
        this.buttonCTA.assertNoValues()

        //Ended backed project, not pledged
        val backedSuccessfulProject = ProjectFactory.backedProject()
                .toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        this.vm.inputs.projectAndReward(backedSuccessfulProject, reward)
        this.buttonIsGone.assertValues(false, true)
        this.buttonCTA.assertNoValues()

        //Ended backed project, no reward, not pledged
        this.vm.inputs.projectAndReward(backedSuccessfulProject, noReward)
        this.buttonIsGone.assertValues(false, true)
        this.buttonCTA.assertNoValues()

        //Ended backed project, pledged reward
        this.vm.inputs.projectAndReward(backedSuccessfulProject, backedSuccessfulProject.backing()?.reward()?: reward)
        this.buttonIsGone.assertValues(false, true, false)
        this.buttonCTA.assertValue(R.string.Selected)

        val backedNoRewardSuccessfulProject = ProjectFactory.backedProjectWithNoReward()
                .toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        //Ended backed project, no reward, pledged no reward
        this.vm.inputs.projectAndReward(backedNoRewardSuccessfulProject, noReward)
        this.buttonIsGone.assertValues(false, true, false)
        this.buttonCTA.assertValue(R.string.Selected)
    }

    @Test
    fun testConversion_USDProject_currentCurrencyUSD() {
        setUpEnvironment(environment())

        val usProject = ProjectFactory.project()
                .toBuilder()
                .currentCurrency("USD")
                .build()
        val reward = RewardFactory.reward()
                .toBuilder()
                .minimum(50.0)
                .convertedMinimum(50.0)
                .build()

        this.vm.inputs.projectAndReward(usProject, reward)
        this.conversion.assertValue("$50")
        this.conversionIsGone.assertValue(true)
    }

    @Test
    fun testConversion_CADProject_currentCurrencyUSD() {
        setUpEnvironment(environment())

        val caProject = ProjectFactory.caProject()
                .toBuilder()
                .currentCurrency("USD")
                .build()

        val reward = RewardFactory.reward()
                .toBuilder()
                .minimum(50.0)
                .convertedMinimum(40.0)
                .build()

        this.vm.inputs.projectAndReward(caProject, reward)
        this.conversion.assertValue("$40")
        this.conversionIsGone.assertValue(false)
    }

    @Test
    fun testDescriptionOutputs() {
        val project = ProjectFactory.project()
        val reward = RewardFactory.reward()
        setUpEnvironment(environment())

        //Reward with description
        this.vm.inputs.projectAndReward(project, reward)
        this.descriptionForNoReward.assertNoValues()
        this.descriptionForReward.assertValue(reward.description())
        this.descriptionIsGone.assertValue(false)

        //No reward
        this.vm.inputs.projectAndReward(project, RewardFactory.noReward())
        this.descriptionForNoReward.assertValue(R.string.Back_it_because_you_believe_in_it)
        this.descriptionForReward.assertValues(reward.description())
        this.descriptionIsGone.assertValue(false)

        //Backed no reward
        val noRewardBacking = BackingFactory.backing()
                .toBuilder()
                .reward(RewardFactory.noReward())
                .rewardId(null)
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(noRewardBacking)
                .build()
        this.vm.inputs.projectAndReward(backedProject, RewardFactory.noReward())
        this.descriptionForNoReward.assertValues(R.string.Back_it_because_you_believe_in_it,
                R.string.Thanks_for_bringing_this_project_one_step_closer_to_becoming_a_reality)
        this.descriptionForReward.assertValues(reward.description())
        this.descriptionIsGone.assertValue(false)

        //Reward with empty description
        this.vm.inputs.projectAndReward(project, RewardFactory.noDescription())
        this.descriptionForNoReward.assertValues(R.string.Back_it_because_you_believe_in_it,
                R.string.Thanks_for_bringing_this_project_one_step_closer_to_becoming_a_reality)
        this.descriptionForReward.assertValues(reward.description(), "")
        this.descriptionIsGone.assertValues(false, true)

        //Reward with null description
        val nullDescriptionReward = RewardFactory.noDescription().toBuilder().description(null).build()
        this.vm.inputs.projectAndReward(project, nullDescriptionReward)
        this.descriptionForNoReward.assertValues(R.string.Back_it_because_you_believe_in_it,
                R.string.Thanks_for_bringing_this_project_one_step_closer_to_becoming_a_reality)
        this.descriptionForReward.assertValues(reward.description(), "", null)
        this.descriptionIsGone.assertValues(false, true)
    }

    @Test
    fun testEndDateSectionIsGone() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, RewardFactory.reward())
        this.endDateSectionIsGone.assertValue(true)

        val expiredReward = RewardFactory.reward()
                .toBuilder()
                .endsAt(DateTime.now().minusDays(2))
                .build()

        this.vm.inputs.projectAndReward(project, expiredReward)
        this.endDateSectionIsGone.assertValue(true)

        val expiringReward = RewardFactory.reward()
                .toBuilder()
                .endsAt(DateTime.now().plusDays(2))
                .build()

        this.vm.inputs.projectAndReward(project, expiringReward)
        this.endDateSectionIsGone.assertValues(true, false)

        this.vm.inputs.projectAndReward(ProjectFactory.successfulProject(), expiringReward)
        this.endDateSectionIsGone.assertValues(true, false, true)
    }

    @Test
    fun testEstimatedDelivery_whenRewardHasEstimatedDelivery() {
        setUpEnvironment(environment())

        val reward = RewardFactory.reward()
                .toBuilder()
                .estimatedDeliveryOn(DateTime.parse("2019-09-11T20:12:47+00:00"))
                .build()
        this.vm.inputs.projectAndReward(ProjectFactory.project(), reward)

        this.estimatedDelivery.assertValue("September 2019")
        this.estimatedDeliveryIsGone.assertValue(false)
    }

    @Test
    fun testEstimatedDelivery_whenRewardHasNoEstimatedDelivery() {
        setUpEnvironment(environment())

        val reward = RewardFactory.reward()
                .toBuilder()
                .estimatedDeliveryOn(null)
                .build()
        this.vm.inputs.projectAndReward(ProjectFactory.project(), reward)

        this.estimatedDelivery.assertNoValues()
        this.estimatedDeliveryIsGone.assertValue(true)
    }

    @Test
    fun testEstimatedDelivery_whenNoReward() {
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(ProjectFactory.project(), RewardFactory.noReward())

        this.estimatedDelivery.assertNoValues()
        this.estimatedDeliveryIsGone.assertValue(true)
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
    fun testButtonIsEnabled() {
        setUpEnvironment(environment())

        // A reward from a live project that is available should be enabled.
        this.vm.inputs.projectAndReward(ProjectFactory.project(), RewardFactory.reward())
        this.buttonIsEnabled.assertValue(true)

        // A reward from a successful project should not be enabled.
        this.vm.inputs.projectAndReward(ProjectFactory.successfulProject(), RewardFactory.reward())
        this.buttonIsEnabled.assertValues(true, false)

        // A backed reward from a live project should not be enabled.
        val backedLiveProject = ProjectFactory.backedProject()
        this.vm.inputs.projectAndReward(backedLiveProject, backedLiveProject.backing()?.reward()!!)
        this.buttonIsEnabled.assertValues(true, false)

        // A backed reward from an ended project should not be enabled.
        val backedSuccessfulProject = ProjectFactory.backedProject().toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        this.vm.inputs.projectAndReward(backedSuccessfulProject, backedSuccessfulProject.backing()?.reward()!!)
        this.buttonIsEnabled.assertValues(true, false)

        // A reward with its limit reached should not be enabled.
        this.vm.inputs.projectAndReward(ProjectFactory.project(), RewardFactory.limitReached())
        this.buttonIsEnabled.assertValues(true, false)
    }

    @Test
    fun testLimitContainerIsGone_noLimits() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, RewardFactory.reward())
        this.limitContainerIsGone.assertValue(true)
    }

    @Test
    fun testLimitContainerIsGone_whenLimited() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, RewardFactory.limited())
        this.limitContainerIsGone.assertValue(false)
    }

    @Test
    fun testLimitContainerIsGone_whenEndingSoon() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, RewardFactory.endingSoon())
        this.limitContainerIsGone.assertValue(false)
    }

    @Test
    fun testLimitContainerIsGone_withShipping() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, RewardFactory.rewardWithShipping())
        this.limitContainerIsGone.assertValue(false)
    }

    @Test
    fun testLimitContainerIsGone_endedProject() {
        val project = ProjectFactory.successfulProject()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, RewardFactory.rewardWithShipping())
        this.limitContainerIsGone.assertValue(true)
    }

    @Test
    fun testMinimumAmountTitle() {
        val project = ProjectFactory.project()
        val reward = RewardFactory.reward()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, reward)
        this.minimumAmountTitle.assertValue("$20")
    }

    @Test
    fun testMinimumAmountTitle_whenUKProject() {
        val project = ProjectFactory.ukProject()
        val reward = RewardFactory.reward()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, reward)
        this.minimumAmountTitle.assertValue("£20")
    }

    @Test
    fun testRemaining() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // When reward is limited, quantity should be shown.
        this.vm.inputs.projectAndReward(project, RewardFactory.limited())
        this.remaining.assertValue("5")
        this.remainingIsGone.assertValue(false)

        // When reward's limit has been reached, don't show quantity.
        this.vm.inputs.projectAndReward(project, RewardFactory.limitReached())
        this.remainingIsGone.assertValues(false, true)

        this.vm.inputs.projectAndReward(ProjectFactory.successfulProject(), RewardFactory.limitReached())
        this.remainingIsGone.assertValues(false, true)

        // When reward has no limit, don't show quantity (distinct until changed).
        this.vm.inputs.projectAndReward(project, RewardFactory.reward())
        this.remainingIsGone.assertValues(false, true)
    }

    @Test
    fun testReward() {
        val project = ProjectFactory.project()
        val reward = RewardFactory.reward()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, reward)
        this.reward.assertValue(reward)
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
    fun testShippingSummary() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Summary should be hidden when reward is not shippable.
        this.vm.inputs.projectAndReward(project, RewardFactory.reward())
        this.shippingSummaryIsGone.assertValue(true)
        this.shippingSummary.assertNoValues()

        val shippableReward = RewardFactory.rewardWithShipping()
        this.vm.inputs.projectAndReward(project, shippableReward)
        this.shippingSummaryIsGone.assertValues(true, false)
        this.shippingSummary.assertValues(shippableReward.shippingSummary())
    }

    @Test
    fun testTitle() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Reward with no title should be hidden.
        val rewardWithNoTitle = RewardFactory.reward().toBuilder()
                .title(null)
                .build()
        this.vm.inputs.projectAndReward(project, rewardWithNoTitle)
        this.titleIsGone.assertValues(true)
        this.titleForReward.assertValuesAndClear(null)
        this.titleForNoReward.assertNoValues()

        // Reward with title should be visible.
        this.vm.inputs.projectAndReward(project, RewardFactory.noReward())
        this.titleIsGone.assertValues(true, false)
        this.titleForReward.assertNoValues()
        this.titleForNoReward.assertValuesAndClear(R.string.Pledge_without_a_reward)

        val noRewardBacking = BackingFactory.backing()
                .toBuilder()
                .reward(RewardFactory.noReward())
                .rewardId(null)
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(noRewardBacking)
                .build()
        this.vm.inputs.projectAndReward(backedProject, RewardFactory.noReward())
        this.titleIsGone.assertValues(true, false)
        this.titleForReward.assertNoValues()
        this.titleForNoReward.assertValuesAndClear(R.string.You_pledged_without_a_reward)

        val title = "Digital bundle"
        val rewardWithTitle = RewardFactory.reward().toBuilder()
                .title(title)
                .build()
        this.vm.inputs.projectAndReward(project, rewardWithTitle)
        this.titleIsGone.assertValues(true, false)
        this.titleForReward.assertValuesAndClear(title)
        this.titleForNoReward.assertNoValues()
    }
}
