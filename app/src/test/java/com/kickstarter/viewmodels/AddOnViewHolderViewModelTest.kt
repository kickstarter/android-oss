package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.RewardsItem
import org.junit.Test
import rx.observers.TestSubscriber

class AddOnViewHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: AddOnViewHolderViewModel.ViewModel

    private val quantityIsGone = TestSubscriber.create<Boolean>()
    private val conversion = TestSubscriber.create<String>()
    private val conversionIsGone = TestSubscriber.create<Boolean>()
    private val descriptionForNoReward = TestSubscriber.create<Int>()
    private val descriptionForReward = TestSubscriber.create<String?>()
    private val rewardItems = TestSubscriber.create<List<RewardsItem>>()
    private val rewardItemsAreGone = TestSubscriber.create<Boolean>()
    private val titleForReward = TestSubscriber.create<String>()
    private val titleForNoReward = TestSubscriber.create<Int>()
    private val titleForAddOn = TestSubscriber.create<Pair<String, Int>>()

    private fun setUpEnvironment(@NonNull environment: Environment) {
        this.vm = AddOnViewHolderViewModel.ViewModel(environment)
        this.vm.outputs.isAddonTitleGone().subscribe(this.quantityIsGone)
        this.vm.outputs.conversion().subscribe(this.conversion)
        this.vm.outputs.conversionIsGone().subscribe(this.conversionIsGone)
        this.vm.outputs.descriptionForNoReward().subscribe(this.descriptionForNoReward)
        this.vm.outputs.descriptionForReward().subscribe(this.descriptionForReward)
        this.vm.outputs.rewardItems().subscribe(this.rewardItems)
        this.vm.outputs.rewardItemsAreGone().subscribe(this.rewardItemsAreGone)
        this.vm.outputs.titleForNoReward().subscribe(this.titleForNoReward)
        this.vm.outputs.titleForReward().subscribe(this.titleForReward)
        this.vm.outputs.titleForAddOn().subscribe(this.titleForAddOn)
    }

    @Test
    fun testRewardWithoutAddon() {
        setUpEnvironment(environment())

        val reward = RewardFactory.rewardWithShipping().toBuilder().isAddOn(false).build()
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.project()), reward)

        this.titleForAddOn.assertNoValues()
        this.titleForReward.assertValue(reward.title())
        this.descriptionForReward.assertValue(reward.description())
    }

    @Test
    fun testRewardAndAddOnWithItems() {
        setUpEnvironment(environment())

        val reward = RewardFactory.itemizedAddOn().toBuilder().quantity(1).build()
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.project()), reward)

        val title = reward.title()?.let { it } ?: ""
        val quantity = reward.quantity()?.let { it } ?: 0

        this.titleForAddOn.assertValue(Pair(title, quantity))
        this.titleForReward.assertNoValues()
        this.quantityIsGone.assertNoValues()
        this.rewardItems.assertValue(reward.addOnsItems())
        this.rewardItemsAreGone.assertValue(false)
        this.descriptionForReward.assertValue(reward.description())
    }

    @Test
    fun testNoReward() {
        setUpEnvironment(environment())

        val reward = RewardFactory.noReward()
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.project()), reward)

        this.titleForAddOn.assertNoValues()
        this.titleForReward.assertNoValues()
        this.titleForNoReward.assertValue(R.string.You_pledged_without_a_reward)
        this.rewardItemsAreGone.assertValue(true)
        this.descriptionForReward.assertNoValues()
        this.descriptionForNoReward.assertValue(R.string.Thanks_for_bringing_this_project_one_step_closer_to_becoming_a_reality)
    }

    @Test
    fun testCurrencyIsGone() {
        val environment = environment()
        setUpEnvironment(environment)

        val usProject = ProjectFactory.project()
            .toBuilder()
            .currentCurrency("USD")
            .build()
        val minimum = 50.0
        val reward = RewardFactory.reward()
            .toBuilder()
            .minimum(minimum)
            .convertedMinimum(minimum)
            .build()

        this.vm.inputs.configureWith(ProjectDataFactory.project(usProject), reward)

        this.conversionIsGone.assertValue(true)
        this.conversion.assertValue("$50")
    }
}
