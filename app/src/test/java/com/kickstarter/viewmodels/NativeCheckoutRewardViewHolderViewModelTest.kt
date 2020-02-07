package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FeatureKey
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.preferences.MockBooleanPreference
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.*
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
    private val background = TestSubscriber.create<Int>()
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
    private val shippingSummary = TestSubscriber<Pair<Int, String?>>()
    private val shippingSummaryIsGone = TestSubscriber<Boolean>()
    private val showPledgeFragment = TestSubscriber<Pair<Project, Reward>>()
    private val titleForNoReward = TestSubscriber<Int>()
    private val titleForReward = TestSubscriber<String?>()
    private val titleIsGone = TestSubscriber<Boolean>()

    private fun setUpEnvironment(@NonNull environment: Environment) {
        this.vm = NativeCheckoutRewardViewHolderViewModel.ViewModel(environment)
        this.vm.outputs.backersCount().subscribe(this.backersCount)
        this.vm.outputs.backersCountIsGone().subscribe(this.backersCountIsGone)
        this.vm.outputs.background().subscribe(this.background)
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
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.project()), reward)

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
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.project()), reward)

        this.backersCount.assertNoValues()
        this.backersCountIsGone.assertValue(true)
    }

    @Test
    fun testBackersCount_whenNoReward() {
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.project()), RewardFactory.noReward())

        this.backersCount.assertNoValues()
        this.backersCountIsGone.assertValue(true)
    }

    @Test
    fun testBackground_whenReward_goRewardlessDisabled() {
        setUpEnvironment(environmentWithGoRewardlessDisabled())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.project()), RewardFactory.reward())

        this.background.assertValue(0)
    }

    @Test
    fun testBackground_whenReward_goRewardlessEnabled() {
        setUpEnvironment(environmentWithGoRewardlessEnabled())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.project()), RewardFactory.reward())

        this.background.assertValue(0)
    }

    @Test
    fun testBackground_whenNoReward_goRewardlessDisabled() {
        setUpEnvironment(environmentWithGoRewardlessDisabled())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.project()), RewardFactory.noReward())

        this.background.assertValue(0)
    }

    @Test
    fun testBackground_whenNoReward_goRewardlessEnabled() {
        setUpEnvironment(environmentWithGoRewardlessEnabled())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.project()), RewardFactory.noReward())

        this.background.assertValue(R.drawable.bg_go_rewardless)
    }

    @Test
    fun testButtonUIOutputs_whenProjectIsLiveAndUnbacked_availableReward() {
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.project()), RewardFactory.reward())
        this.buttonIsGone.assertValue(false)
        this.buttonCTA.assertValue(R.string.Select)
    }

    @Test
    fun testButtonUIOutputs_whenProjectIsLiveAndUnbacked_noReward() {
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.project()), RewardFactory.noReward())
        this.buttonIsGone.assertValue(false)
        this.buttonCTA.assertValuesAndClear(R.string.Select)
    }

    @Test
    fun testButtonUIOutputs_whenProjectIsLiveAndUnbacked_soldOutReward() {
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.project()), RewardFactory.limitReached())
        this.buttonIsGone.assertValue(false)
        this.buttonCTA.assertValue(R.string.No_longer_available)
    }

    @Test
    fun testButtonUIOutputs_whenProjectIsLiveAndUnbacked_expiredReward() {
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.project()), RewardFactory.ended())
        this.buttonIsGone.assertValue(false)
        this.buttonCTA.assertValuesAndClear(R.string.No_longer_available)
    }

    @Test
    fun testButtonUIOutputs_whenProjectIsLiveAndBacked_backedReward() {
        setUpEnvironment(environment())

        val backedLiveProject = ProjectFactory.backedProject()
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(backedLiveProject), backedLiveProject.backing()?.reward()
                ?: RewardFactory.reward())
        this.buttonIsGone.assertValue(false)
        this.buttonCTA.assertValuesAndClear(R.string.Selected)
    }

    @Test
    fun testButtonUIOutputs_whenProjectIsLiveAndBacked_availableReward() {
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.backedProject()), RewardFactory.reward())
        this.buttonIsGone.assertValue(false)
        this.buttonCTA.assertValuesAndClear(R.string.Select)
    }

    @Test
    fun testButtonUIOutputs_whenProjectIsLiveAndBacked_soldOutReward() {
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.backedProject()), RewardFactory.limitReached())
        this.buttonIsGone.assertValue(false)
        this.buttonCTA.assertValue(R.string.No_longer_available)
    }

    @Test
    fun testButtonUIOutputs_whenProjectIsLiveAndBacked_soldOutReward_andUnbacked() {
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.backedProject()), RewardFactory.ended())
        this.buttonIsGone.assertValue(false)
        this.buttonCTA.assertValuesAndClear(R.string.No_longer_available)
    }

    @Test
    fun testButtonUIOutputs_whenProjectIsEndedAndUnbacked_availableReward() {
        setUpEnvironment(environment())

        val successfulProject = ProjectFactory.successfulProject()
                .toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(successfulProject), RewardFactory.reward())
        this.buttonIsGone.assertValue(true)
        this.buttonCTA.assertValuesAndClear(R.string.No_longer_available)
    }

    @Test
    fun testButtonUIOutputs_whenProjectIsEndedAndBacked_availableReward() {
        setUpEnvironment(environment())

        val backedSuccessfulProject = ProjectFactory.backedProject()
                .toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(backedSuccessfulProject), RewardFactory.reward())
        this.buttonIsGone.assertValue(true)
        this.buttonCTA.assertValuesAndClear(R.string.No_longer_available)
    }

    @Test
    fun testButtonUIOutputs_whenProjectIsEndedAndBacked_noReward() {
        setUpEnvironment(environment())
        val backedSuccessfulProject = ProjectFactory.backedProject()
                .toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(backedSuccessfulProject), RewardFactory.noReward())
        this.buttonIsGone.assertValue(true)
        this.buttonCTA.assertValuesAndClear(R.string.No_longer_available)
    }

    @Test
    fun testButtonUIOutputs_whenProjectIsEndedAndBacked_backedReward() {
        setUpEnvironment(environment())
        val backedSuccessfulProject = ProjectFactory.backedProject()
                .toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(backedSuccessfulProject), backedSuccessfulProject.backing()?.reward()
                ?: RewardFactory.reward())
        this.buttonIsGone.assertValue(false)
        this.buttonCTA.assertValue(R.string.Selected)
    }

    @Test
    fun testButtonUIOutputs_whenProjectIsEndedAndBacked_backedNoReward() {
        setUpEnvironment(environment())
        val backedNoRewardSuccessfulProject = ProjectFactory.backedProjectWithNoReward()
                .toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()

         this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(backedNoRewardSuccessfulProject), RewardFactory.noReward())
        this.buttonIsGone.assertValue(false)
        this.buttonCTA.assertValue(R.string.Selected)
    }

    @Test
    fun testButtonUIOutputs_whenProjectIsLive_availableReward_currentUserIsCreator() {
        val creator = UserFactory.creator()

        val project = ProjectFactory.project()
                .toBuilder()
                .creator(creator)
                .build()

        val environment = environment()
                .toBuilder()
                .currentUser(MockCurrentUser(creator))
                .build()
        setUpEnvironment(environment)

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(project), RewardFactory.reward())
        this.buttonIsGone.assertValue(true)
        this.buttonCTA.assertValue(R.string.Select)
    }

    @Test
    fun testButtonUIOutputs_whenProjectIsLive_noReward_currentUserIsCreator() {
        val creator = UserFactory.creator()

        val project = ProjectFactory.project()
                .toBuilder()
                .creator(creator)
                .build()

        val environment = environment()
                .toBuilder()
                .currentUser(MockCurrentUser(creator))
                .build()
        setUpEnvironment(environment)

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(project), RewardFactory.noReward())
        this.buttonIsGone.assertValue(true)
        this.buttonCTA.assertValue(R.string.Select)
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

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(usProject), reward)
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

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(caProject), reward)
        this.conversion.assertValue("$40")
        this.conversionIsGone.assertValue(false)
    }

    @Test
    fun testDescriptionOutputs_whenReward_hasNoDescription() {
        setUpEnvironment(environment())

        //Reward with empty description
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.project()), RewardFactory.noDescription())
        this.descriptionForNoReward.assertNoValues()
        this.descriptionForReward.assertValue("")
        this.descriptionIsGone.assertValue(true)
    }

    @Test
    fun testDescriptionOutputs_whenReward_hasNullDescription() {
        setUpEnvironment(environment())

        //Reward with empty description
        val reward = RewardFactory.noDescription()
                .toBuilder()
                .description(null)
                .build()
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.project()), reward)
        this.descriptionForNoReward.assertNoValues()
        this.descriptionForReward.assertValue(null)
        this.descriptionIsGone.assertValue(true)
    }

    @Test
    fun testDescriptionOutputs_whenReward_hasDescription_goRewardlessDisabled() {
        setUpEnvironment(environmentWithGoRewardlessDisabled())

        //Reward with description
        val reward = RewardFactory.reward()
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.project()), reward)
        this.descriptionForNoReward.assertNoValues()
        this.descriptionForReward.assertValue(reward.description())
        this.descriptionIsGone.assertValue(false)
    }

    @Test
    fun testDescriptionOutputs_whenReward_hasDescription_goRewardlessEnabled() {
        setUpEnvironment(environmentWithGoRewardlessEnabled())

        //Reward with description
        val reward = RewardFactory.reward()
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.project()), reward)
        this.descriptionForNoReward.assertNoValues()
        this.descriptionForReward.assertValue(reward.description())
        this.descriptionIsGone.assertValue(false)
    }

    @Test
    fun testDescriptionOutputs_whenNoReward_goRewardlessDisabled() {
        setUpEnvironment(environmentWithGoRewardlessDisabled())

        //No reward
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.project()), RewardFactory.noReward())
        this.descriptionForNoReward.assertValue(R.string.Back_it_because_you_believe_in_it)
        this.descriptionForReward.assertNoValues()
        this.descriptionIsGone.assertValue(false)
    }

    @Test
    fun testDescriptionOutputs_whenNoReward_goRewardlessEnabled() {
        setUpEnvironment(environmentWithGoRewardlessEnabled())

        //No reward
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.project()), RewardFactory.noReward())
        this.descriptionForNoReward.assertValue(R.string.This_holiday_season_support_a_project_for_no_reward)
        this.descriptionForReward.assertNoValues()
        this.descriptionIsGone.assertValue(false)
    }

    @Test
    fun testDescriptionOutputs_whenBackedNoReward_goRewardlessDisabled() {
        setUpEnvironment(environmentWithGoRewardlessDisabled())

        val noRewardBacking = BackingFactory.backing()
                .toBuilder()
                .reward(RewardFactory.noReward())
                .rewardId(null)
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(noRewardBacking)
                .build()
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(backedProject), RewardFactory.noReward())
        this.descriptionForNoReward.assertValue(R.string.Thanks_for_bringing_this_project_one_step_closer_to_becoming_a_reality)
        this.descriptionForReward.assertNoValues()
        this.descriptionIsGone.assertValue(false)
    }

    @Test
    fun testDescriptionOutputs_whenBackedNoReward_goRewardlessEnabled() {
        setUpEnvironment(environmentWithGoRewardlessEnabled())

        val noRewardBacking = BackingFactory.backing()
                .toBuilder()
                .reward(RewardFactory.noReward())
                .rewardId(null)
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(noRewardBacking)
                .build()
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(backedProject), RewardFactory.noReward())
        this.descriptionForNoReward.assertValue(R.string.Thanks_for_bringing_this_project_one_step_closer_to_becoming_a_reality)
        this.descriptionForReward.assertNoValues()
        this.descriptionIsGone.assertValue(false)
    }

    @Test
    fun testEndDateSectionIsGone() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(project), RewardFactory.reward())
        this.endDateSectionIsGone.assertValue(true)

        val expiredReward = RewardFactory.reward()
                .toBuilder()
                .endsAt(DateTime.now().minusDays(2))
                .build()

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(project), expiredReward)
        this.endDateSectionIsGone.assertValue(true)

        val expiringReward = RewardFactory.reward()
                .toBuilder()
                .endsAt(DateTime.now().plusDays(2))
                .build()

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(project), expiringReward)
        this.endDateSectionIsGone.assertValues(true, false)

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.successfulProject()), expiringReward)
        this.endDateSectionIsGone.assertValues(true, false, true)
    }

    @Test
    fun testEstimatedDelivery_whenRewardHasEstimatedDelivery() {
        setUpEnvironment(environment())

        val reward = RewardFactory.reward()
                .toBuilder()
                .estimatedDeliveryOn(DateTime.parse("2019-09-11T20:12:47+00:00"))
                .build()
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.project()), reward)

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
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.project()), reward)

        this.estimatedDelivery.assertNoValues()
        this.estimatedDeliveryIsGone.assertValue(true)
    }

    @Test
    fun testEstimatedDelivery_whenNoReward() {
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.project()), RewardFactory.noReward())

        this.estimatedDelivery.assertNoValues()
        this.estimatedDeliveryIsGone.assertValue(true)
    }

    @Test
    fun testShowPledgeFragment_WhenProjectIsSuccessful() {
        val project = ProjectFactory.successfulProject()
        val reward = RewardFactory.reward()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(project), reward)

        this.vm.inputs.rewardClicked(3)
        this.showPledgeFragment.assertNoValues()
        this.koalaTest.assertNoValues()
    }

    @Test
    fun testShowPledgeFragment_WhenProjectIsSuccessfulAndHasBeenBacked() {
        val project = ProjectFactory.backedProject().toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        val reward = project.backing()?.reward() as Reward
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(project), reward)

        this.vm.inputs.rewardClicked(3)
        this.showPledgeFragment.assertNoValues()
        this.koalaTest.assertNoValues()
    }

    @Test
    fun testShowPledgeFragment_WhenProjectIsLiveNotBacked() {
        val reward = RewardFactory.reward()
        val liveProject = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(liveProject), reward)
        this.showPledgeFragment.assertNoValues()

        // When a reward from a live project is clicked, start checkout.
        this.vm.inputs.rewardClicked(2)
        this.showPledgeFragment.assertValue(Pair.create(liveProject, reward))
        this.koalaTest.assertValue("Select Reward Button Clicked")
        this.lakeTest.assertValue("Select Reward Button Clicked")
    }

    @Test
    fun testShowPledgeFragment_WhenProjectIsLiveBacked() {
        val reward = RewardFactory.reward()
        val backedProject = ProjectFactory.backedProject()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(backedProject), reward)
        this.showPledgeFragment.assertNoValues()

        // When a reward from a live backed project is clicked, start checkout.
        this.vm.inputs.rewardClicked(2)
        this.showPledgeFragment.assertValue(Pair.create(backedProject, reward))
        this.koalaTest.assertValue("Select Reward Button Clicked")
        this.lakeTest.assertNoValues()
    }

    @Test
    fun testButtonIsEnabled() {
        setUpEnvironment(environment())

        // A reward from a live project that is available should be enabled.
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.project()), RewardFactory.reward())
        this.buttonIsEnabled.assertValue(true)

        // A reward from a successful project should not be enabled.
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.successfulProject()), RewardFactory.reward())
        this.buttonIsEnabled.assertValues(true, false)

        // A backed reward from a live project should not be enabled.
        val backedLiveProject = ProjectFactory.backedProject()
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(backedLiveProject), backedLiveProject.backing()?.reward()!!)
        this.buttonIsEnabled.assertValues(true, false)

        // A backed reward from an ended project should not be enabled.
        val backedSuccessfulProject = ProjectFactory.backedProject().toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(backedSuccessfulProject), backedSuccessfulProject.backing()?.reward()!!)
        this.buttonIsEnabled.assertValues(true, false)

        // A reward with its limit reached should not be enabled.
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.project()), RewardFactory.limitReached())
        this.buttonIsEnabled.assertValues(true, false)
    }

    @Test
    fun testLimitContainerIsGone_noLimits() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(project), RewardFactory.reward())
        this.limitContainerIsGone.assertValue(true)
    }

    @Test
    fun testLimitContainerIsGone_whenLimited() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(project), RewardFactory.limited())
        this.limitContainerIsGone.assertValue(false)
    }

    @Test
    fun testLimitContainerIsGone_whenEndingSoon() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(project), RewardFactory.endingSoon())
        this.limitContainerIsGone.assertValue(false)
    }

    @Test
    fun testLimitContainerIsGone_withShipping() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(project), RewardFactory.rewardWithShipping())
        this.limitContainerIsGone.assertValue(false)
    }

    @Test
    fun testLimitContainerIsGone_endedProject() {
        val project = ProjectFactory.successfulProject()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(project), RewardFactory.rewardWithShipping())
        this.limitContainerIsGone.assertValue(true)
    }

    @Test
    fun testMinimumAmountTitle() {
        val project = ProjectFactory.project()
        val reward = RewardFactory.reward()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(project), reward)
        this.minimumAmountTitle.assertValue("$20")
    }

    @Test
    fun testMinimumAmountTitle_whenUKProject() {
        val project = ProjectFactory.ukProject()
        val reward = RewardFactory.reward()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(project), reward)
        this.minimumAmountTitle.assertValue("£20")
    }

    @Test
    fun testRemaining() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // When reward is limited, quantity should be shown.
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(project), RewardFactory.limited())
        this.remaining.assertValue("5")
        this.remainingIsGone.assertValue(false)

        // When reward's limit has been reached, don't show quantity.
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(project), RewardFactory.limitReached())
        this.remainingIsGone.assertValues(false, true)

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.successfulProject()), RewardFactory.limitReached())
        this.remainingIsGone.assertValues(false, true)

        // When reward has no limit, don't show quantity (distinct until changed).
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(project), RewardFactory.reward())
        this.remainingIsGone.assertValues(false, true)
    }

    @Test
    fun testReward() {
        val project = ProjectFactory.project()
        val reward = RewardFactory.reward()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(project), reward)
        this.reward.assertValue(reward)
    }

    @Test
    fun testRewardItems() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Items section should be hidden when there are no items.
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(project), RewardFactory.reward())
        this.rewardItemsAreGone.assertValue(true)
        this.rewardItems.assertNoValues()

        val itemizedReward = RewardFactory.itemized()
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(project), itemizedReward)
        this.rewardItemsAreGone.assertValues(true, false)
        this.rewardItems.assertValues(itemizedReward.rewardsItems())
    }

    @Test
    fun testShippingSummary_whenRewardIsNotShippable() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(project), RewardFactory.reward())
        this.shippingSummary.assertNoValues()
        this.shippingSummaryIsGone.assertValue(true)
    }

    @Test
    fun testShippingSummary_whenRewardHasLimitedShipping() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        val rewardWithShipping = RewardFactory.multipleLocationShipping()
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(project), rewardWithShipping)
        this.shippingSummary.assertValue(Pair(R.string.Limited_shipping, null))
        this.shippingSummaryIsGone.assertValues(false)
    }

    @Test
    fun testShippingSummary_whenRewardShipsToOneLocation() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        val rewardWithShipping = RewardFactory.singleLocationShipping(LocationFactory.nigeria().displayableName())
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(project), rewardWithShipping)
        this.shippingSummary.assertValue(Pair(R.string.location_name_only, "Nigeria"))
        this.shippingSummaryIsGone.assertValues(false)
    }

    @Test
    fun testShippingSummary_whenRewardShipsToOneLocation_withNullLocation() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        val rewardWithShipping = RewardFactory.reward()
                .toBuilder()
                .shippingType(Reward.SHIPPING_TYPE_SINGLE_LOCATION)
                .build()
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(project), rewardWithShipping)
        this.shippingSummary.assertValue(Pair(R.string.Limited_shipping, null))
        this.shippingSummaryIsGone.assertValues(false)
    }

    @Test
    fun testShippingSummary_whenRewardShipsWorldWide() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        val rewardWithShipping = RewardFactory.rewardWithShipping()
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(project), rewardWithShipping)
        this.shippingSummary.assertValue(Pair(R.string.Ships_worldwide, null))
        this.shippingSummaryIsGone.assertValues(false)
    }

    @Test
    fun testTitleOutputs_whenReward_hasNoTitle() {
        setUpEnvironment(environment())

        // Reward with no title should be hidden.
        val rewardWithNoTitle = RewardFactory.reward().toBuilder()
                .title(null)
                .build()
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.project()), rewardWithNoTitle)
        this.titleIsGone.assertValue(true)
        this.titleForReward.assertValue(null)
        this.titleForNoReward.assertNoValues()
    }

    @Test
    fun testTitleOutputs_whenReward_hasTitle() {
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.project()), RewardFactory.reward())
        this.titleIsGone.assertValue(false)
        this.titleForReward.assertValue("Digital Bundle")
        this.titleForNoReward.assertNoValues()
    }

    @Test
    fun testTitleOutputs_whenNoReward_goRewardlessDisabled() {
        setUpEnvironment(environmentWithGoRewardlessDisabled())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.project()), RewardFactory.noReward())
        this.titleIsGone.assertValues(false)
        this.titleForReward.assertNoValues()
        this.titleForNoReward.assertValue(R.string.Pledge_without_a_reward)
    }

    @Test
    fun testTitleOutputs_whenNoReward_goRewardlessEnabled() {
        setUpEnvironment(environmentWithGoRewardlessEnabled())

        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(ProjectFactory.project()), RewardFactory.noReward())
        this.titleIsGone.assertValue(false)
        this.titleForReward.assertNoValues()
        this.titleForNoReward.assertValuesAndClear(R.string.Back_it_because_you_believe_in_it)
    }

    @Test
    fun testTitleOutputs_whenNoReward_backed() {
        setUpEnvironment(environmentWithGoRewardlessDisabled())

        val noRewardBacking = BackingFactory.backing()
                .toBuilder()
                .reward(RewardFactory.noReward())
                .rewardId(null)
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(noRewardBacking)
                .build()
        this.vm.inputs.projectAndReward(ProjectTrackingFactory.project(backedProject), RewardFactory.noReward())
        this.titleIsGone.assertValue(false)
        this.titleForReward.assertNoValues()
        this.titleForNoReward.assertValue(R.string.You_pledged_without_a_reward)
    }

    private fun environmentWithGoRewardlessDisabled(): Environment {
        val mockCurrentConfig = MockCurrentConfig()
        mockCurrentConfig.config(ConfigFactory.config())
        return environment()
                .toBuilder()
                .currentConfig(mockCurrentConfig)
                .goRewardlessPreference(MockBooleanPreference(false))
                .build()
    }

    private fun environmentWithGoRewardlessEnabled(): Environment {
        val mockCurrentConfig = MockCurrentConfig()
        mockCurrentConfig.config(ConfigFactory.configWithFeatureEnabled(FeatureKey.ANDROID_GO_REWARDLESS))
        return environment()
                .toBuilder()
                .currentConfig(mockCurrentConfig)
                .goRewardlessPreference(MockBooleanPreference(true))
                .build()
    }
}
