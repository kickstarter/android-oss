package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.BackingAddOnViewHolderViewModel
import org.junit.Test
import rx.observers.TestSubscriber

class BackingAddOnViewHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: BackingAddOnViewHolderViewModel.ViewModel

    private val backerLimitIsGone = TestSubscriber.create<Boolean>()
    private val remainingQuantityIsGone = TestSubscriber.create<Boolean>()
    private val countdownIsGone = TestSubscriber.create<Boolean>()
    private val shippingAmountIsGone = TestSubscriber.create<Boolean>()
    private val rewardItemsAreGone = TestSubscriber.create<Boolean>()
    private val quantity = TestSubscriber.create<Int>()
    private val disableIncreaseButton = TestSubscriber.create<Boolean>()
    private val addButtonGone = TestSubscriber.create<Boolean>()

    private fun setupEnvironment(@NonNull environment: Environment ) {
        this.vm = BackingAddOnViewHolderViewModel.ViewModel(environment)
        this.vm.outputs.backerLimitPillIsGone().subscribe(this.backerLimitIsGone)
        this.vm.outputs.remainingQuantityPillIsGone().subscribe(this.remainingQuantityIsGone)
        this.vm.outputs.deadlineCountdownIsGone().subscribe(this.countdownIsGone)
        this.vm.outputs.shippingAmountIsGone().subscribe(this.shippingAmountIsGone)
        this.vm.outputs.rewardItemsAreGone().subscribe(this.rewardItemsAreGone)
        this.vm.outputs.quantity().subscribe(this.quantity)
        this.vm.outputs.disableIncreaseButton().subscribe(this.disableIncreaseButton)
        this.vm.outputs.addButtonIsGone().subscribe(this.addButtonGone)
    }

    @Test
    fun testAddOnBackerLimitPillGone(){
        setupEnvironment(environment())

        val addOn = RewardFactory.reward().toBuilder().isAddOn(true).limit(null).build()
        val shippingRule = ShippingRuleFactory.usShippingRule()
        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        this.backerLimitIsGone.assertValue(true)
    }

    @Test
    fun testAddOnRemainingQuantityPillIsGone(){
        setupEnvironment(environment())

        val addOn = RewardFactory.reward().toBuilder().isAddOn(true).remaining(null).build()
        val shippingRule = ShippingRuleFactory.usShippingRule()
        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        this.remainingQuantityIsGone.assertValue(true)
    }

    @Test
    fun testCountdownPillIsGone(){
        setupEnvironment(environment())

        val addOn = RewardFactory.reward().toBuilder().isAddOn(true).endsAt(null).build()
        val shippingRule = ShippingRuleFactory.usShippingRule()
        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        this.countdownIsGone.assertValue(true)
    }

    @Test
    fun testShippingAmountIsGone(){
        setupEnvironment(environment())

        val addOn = RewardFactory.reward().toBuilder().isAddOn(true).shippingRules(emptyList<ShippingRule>()).build()

        val shippingRule = ShippingRuleFactory.usShippingRule()
        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        this.shippingAmountIsGone.assertValue(true)
    }

    @Test
    fun testRewardItemsAreGone(){
        setupEnvironment(environment())

        val addOn = RewardFactory.reward().toBuilder().isAddOn(true).rewardsItems(emptyList()).build()

        val shippingRule = ShippingRuleFactory.usShippingRule()
        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        this.rewardItemsAreGone.assertValue(true)
    }

    @Test
    fun testAddButtonIsGone() {
        setupEnvironment(environment())

        val addOn = RewardFactory.reward().toBuilder().isAddOn(true).rewardsItems(emptyList()).build()

        val shippingRule = ShippingRuleFactory.usShippingRule()
        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        this.addButtonGone.assertValue(false)
        this.quantity.assertValue(0)
    }

    @Test
    fun testAddButtonIsGone_WhenPressing() {
        setupEnvironment(environment())

        val addOn = RewardFactory.reward().toBuilder().isAddOn(true).rewardsItems(emptyList()).build()

        val shippingRule = ShippingRuleFactory.usShippingRule()
        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        this.vm.inputs.increaseButtonPressed()
        this.addButtonGone.assertValues(false, true)
        this.quantity.assertValues(0, 1)
    }

    @Test
    fun increaseStepper(){
        setupEnvironment(environment())

        val addOn = RewardFactory.reward().toBuilder().isAddOn(true).rewardsItems(emptyList()).build()

        val shippingRule = ShippingRuleFactory.usShippingRule()
        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        this.vm.inputs.increaseButtonPressed()
        this.vm.inputs.increaseButtonPressed()
        this.vm.inputs.increaseButtonPressed()

        this.addButtonGone.assertValues(false, true, true, true)
        this.quantity.assertValues(0, 1, 2, 3)
    }

    @Test
    fun increaseStepperDisable_WhenLimitReached() {
        setupEnvironment(environment())

        val addOn = RewardFactory.reward().toBuilder().isAddOn(true).remaining(3).rewardsItems(emptyList()).build()

        val shippingRule = ShippingRuleFactory.usShippingRule()
        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        this.vm.inputs.increaseButtonPressed()
        this.vm.inputs.increaseButtonPressed()
        this.vm.inputs.increaseButtonPressed()

        this.addButtonGone.assertValues(false, true, true, true)
        this.quantity.assertValues(0, 1, 2, 3)
        this.disableIncreaseButton.assertValues(false, false, false, true)
    }

    @Test
    fun increaseStepperDisable_WhenLimitReached_Constant() {
        setupEnvironment(environment())

        val addOn = RewardFactory.reward().toBuilder().isAddOn(true).rewardsItems(emptyList()).build()

        val shippingRule = ShippingRuleFactory.usShippingRule()
        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        this.vm.inputs.increaseButtonPressed()
        this.vm.inputs.increaseButtonPressed()
        this.vm.inputs.increaseButtonPressed()
        this.vm.inputs.increaseButtonPressed()
        this.vm.inputs.increaseButtonPressed()
        this.vm.inputs.increaseButtonPressed()
        this.vm.inputs.increaseButtonPressed()
        this.vm.inputs.increaseButtonPressed()
        this.vm.inputs.increaseButtonPressed()
        this.vm.inputs.increaseButtonPressed()

        this.addButtonGone.assertValues(false, true, true, true, true, true, true, true, true, true, true)
        this.quantity.assertValues(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        this.disableIncreaseButton.assertValues(false, false, false, false, false, false, false,false, false, false, true)
    }

    @Test
    fun decreaseStepper() {
        setupEnvironment(environment())

        val addOn = RewardFactory.reward().toBuilder().isAddOn(true).rewardsItems(emptyList()).build()

        val shippingRule = ShippingRuleFactory.usShippingRule()
        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        this.vm.inputs.increaseButtonPressed()
        this.vm.inputs.increaseButtonPressed()
        this.vm.inputs.increaseButtonPressed()
        this.vm.inputs.decreaseButtonPressed()

        this.addButtonGone.assertValues(false, true, true, true, true)
        this.quantity.assertValues(0, 1, 2, 3, 2)
    }

    @Test
    fun decreaseUntilAddButtonShown() {
        setupEnvironment(environment())

        val addOn = RewardFactory.reward().toBuilder().isAddOn(true).rewardsItems(emptyList()).build()

        val shippingRule = ShippingRuleFactory.usShippingRule()
        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        this.vm.inputs.increaseButtonPressed()
        this.vm.inputs.increaseButtonPressed()
        this.vm.inputs.increaseButtonPressed()
        this.vm.inputs.decreaseButtonPressed()
        this.vm.inputs.decreaseButtonPressed()
        this.vm.inputs.decreaseButtonPressed()

        this.addButtonGone.assertValues(false, true, true, true, true, true, false)
        this.quantity.assertValues(0, 1, 2, 3, 2, 1,0)
    }
}