package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.*
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.data.ProjectData
import org.joda.time.DateTime
import org.junit.Test
import rx.observers.TestSubscriber

class BackingAddOnViewHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: BackingAddOnViewHolderViewModel.ViewModel

    private val backerLimitIsGone = TestSubscriber.create<Boolean>()
    private val remainingQuantityIsGone = TestSubscriber.create<Boolean>()
    private val countdownIsGone = TestSubscriber.create<Boolean>()
    private val shippingAmountIsGone = TestSubscriber.create<Boolean>()
    private val shippingAmount = TestSubscriber.create<String>()
    private val rewardItemsAreGone = TestSubscriber.create<Boolean>()
    private val quantityPerId = TestSubscriber.create<Pair<Int, Long>>()
    private val maxQuantity = TestSubscriber.create<Int>()

    private fun setupEnvironment(@NonNull environment: Environment) {
        this.vm = BackingAddOnViewHolderViewModel.ViewModel(environment)
        this.vm.outputs.backerLimitPillIsGone().subscribe(this.backerLimitIsGone)
        this.vm.outputs.remainingQuantityPillIsGone().subscribe(this.remainingQuantityIsGone)
        this.vm.outputs.deadlineCountdownIsGone().subscribe(this.countdownIsGone)
        this.vm.outputs.shippingAmountIsGone().subscribe(this.shippingAmountIsGone)
        this.vm.outputs.shippingAmount().subscribe(this.shippingAmount)
        this.vm.outputs.rewardItemsAreGone().subscribe(this.rewardItemsAreGone)
        this.vm.outputs.quantityPerId().subscribe(this.quantityPerId)
        this.vm.outputs.maxQuantity().subscribe(this.maxQuantity)
    }

    @Test
    fun testAddOnBackerLimitPillGone() {
        setupEnvironment(environment())

        val addOn = RewardFactory.reward().toBuilder().isAddOn(true).limit(null).build()
        val shippingRule = ShippingRuleFactory.usShippingRule()
        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        this.backerLimitIsGone.assertValue(true)
    }

    @Test
    fun testAddOnRemainingQuantityPillIsGone() {
        setupEnvironment(environment())

        val addOn = RewardFactory.reward().toBuilder().isAddOn(true).remaining(null).build()
        val shippingRule = ShippingRuleFactory.usShippingRule()
        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        this.remainingQuantityIsGone.assertValue(true)
    }

    @Test
    fun testCountdownPillIsGone() {
        setupEnvironment(environment())

        val addOn = RewardFactory.reward().toBuilder().isAddOn(true).endsAt(null).build()
        val shippingRule = ShippingRuleFactory.usShippingRule()
        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        this.countdownIsGone.assertValue(true)
    }

    @Test
    fun testShippingAmountIsGone() {
        setupEnvironment(environment())

        val addOn = RewardFactory.reward().toBuilder().isAddOn(true).shippingRules(emptyList<ShippingRule>()).build()

        val shippingRule = ShippingRuleFactory.usShippingRule()
        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        this.shippingAmountIsGone.assertValue(true)
    }

    @Test
    fun testRewardItemsAreGone() {
        setupEnvironment(environment())

        val addOn = RewardFactory.reward().toBuilder().isAddOn(true).rewardsItems(emptyList()).build()

        val shippingRule = ShippingRuleFactory.usShippingRule()
        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        this.rewardItemsAreGone.assertValue(true)
    }

    @Test
    fun maxLimit_whenAddonNotIsAvailableAndQuantityNotNull_shouldEmitQuantity() {
        setupEnvironment(environment())
        val addOn = RewardFactory.reward().toBuilder()
            .isAddOn(true)
            .isAvailable(false)
            .startsAt(DateTime.now())
            .limit(8)
            .remaining(3)
            .quantity(2)
            .rewardsItems(emptyList()).build()

        val shippingRule = ShippingRuleFactory.usShippingRule()
        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        this.maxQuantity.assertValue(2)
    }

    @Test
    fun maxLimit_whenNoTimeRemainingAndQuantityNotNull_shouldEmitQuantity() {
        setupEnvironment(environment())
        val addOn = RewardFactory.reward().toBuilder()
            .isAddOn(true)
            .isAvailable(true)
            .startsAt(DateTime.now().minusDays(5))
            .endsAt(DateTime.now().minusDays(2))
            .remaining(3)
            .quantity(2)
            .rewardsItems(emptyList()).build()

        val shippingRule = ShippingRuleFactory.usShippingRule()
        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        this.maxQuantity.assertValue(2)
    }

    @Test
    fun maxLimit_whenNoTimeRemainingAndQuantityNull_shouldNotEmit() {
        setupEnvironment(environment())
        val addOn = RewardFactory.reward().toBuilder()
            .isAddOn(true)
            .isAvailable(true)
            .startsAt(DateTime.now().minusDays(5))
            .endsAt(DateTime.now().minusDays(2))
            .remaining(3)
            .quantity(null)
            .rewardsItems(emptyList()).build()

        val shippingRule = ShippingRuleFactory.usShippingRule()
        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        this.maxQuantity.assertNoValues()
    }

    @Test
    fun maxLimit_whenTimeRemainingAndAvailableAndRemainingNull_shouldEmitLimit() {
        setupEnvironment(environment())
        val addOn = RewardFactory.reward().toBuilder()
            .isAddOn(true)
            .isAvailable(true)
            .startsAt(DateTime.now().minusDays(5))
            .endsAt(DateTime.now().plusDays(5))
            .remaining(null)
            .quantity(null)
            .limit(10)
            .rewardsItems(emptyList()).build()

        val shippingRule = ShippingRuleFactory.usShippingRule()
        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        this.maxQuantity.assertValue(10)
    }

    @Test
    fun maxLimit_whenTimeRemainingAndAvailableAndLimitNull_shouldEmitRemaining() {
        setupEnvironment(environment())
        val addOn = RewardFactory.reward().toBuilder()
            .isAddOn(true)
            .isAvailable(true)
            .startsAt(DateTime.now().minusDays(5))
            .endsAt(DateTime.now().plusDays(5))
            .remaining(9)
            .quantity(null)
            .limit(null)
            .rewardsItems(emptyList()).build()

        val shippingRule = ShippingRuleFactory.usShippingRule()
        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        this.maxQuantity.assertValue(9)
    }

    @Test
    fun maxLimit_whenAddonValidAndRemainingAndLimitNull_shouldNotEmit() {
        setupEnvironment(environment())
        val addOn = RewardFactory.reward().toBuilder()
            .isAddOn(true)
            .isAvailable(true)
            .startsAt(DateTime.now().minusDays(5))
            .endsAt(DateTime.now().plusDays(5))
            .remaining(null)
            .quantity(null)
            .limit(null)
            .rewardsItems(emptyList()).build()

        val shippingRule = ShippingRuleFactory.usShippingRule()
        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        this.maxQuantity.assertNoValues()
    }

    @Test
    fun maxLimit_whenAddonValidAndRemainingLessThanLimit_shouldEmitRemaining() {
        setupEnvironment(environment())
        val addOn = RewardFactory.reward().toBuilder()
            .isAddOn(true)
            .isAvailable(true)
            .startsAt(DateTime.now().minusDays(5))
            .endsAt(DateTime.now().plusDays(5))
            .remaining(3)
            .quantity(null)
            .limit(7)
            .rewardsItems(emptyList()).build()

        val shippingRule = ShippingRuleFactory.usShippingRule()
        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        this.maxQuantity.assertValue(3)
    }

    @Test
    fun maxLimit_whenAddonValidAndRemainingMoreThanLimit_shouldEmitLimit() {
        setupEnvironment(environment())
        val addOn = RewardFactory.reward().toBuilder()
            .isAddOn(true)
            .isAvailable(true)
            .startsAt(DateTime.now().minusDays(5))
            .endsAt(DateTime.now().plusDays(5))
            .remaining(8)
            .quantity(null)
            .limit(4)
            .rewardsItems(emptyList()).build()

        val shippingRule = ShippingRuleFactory.usShippingRule()
        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        this.maxQuantity.assertValue(4)
    }

    @Test
    fun getShippingCost_whenDigitalReward_emitsEmptyString() {
        setupEnvironment(environment())
        val addOn = RewardFactory.reward().toBuilder()
            .isAddOn(true)
            .isAvailable(true)
            .shippingRules(null)
            .startsAt(DateTime.now().minusDays(5))
            .endsAt(DateTime.now().plusDays(5))
            .remaining(8)
            .quantity(null)
            .limit(4)
            .rewardsItems(emptyList()).build()

        val shippingRule = ShippingRuleFactory.usShippingRule()

        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        this.shippingAmount.assertValue("")
    }

    @Test
    fun getShippingCost_whenShippingRulesNotNull_emitsFormattedShippingString() {
        setupEnvironment(environment())
        val shippingRule = ShippingRulesEnvelopeFactory.shippingRules()

        val addOn = RewardFactory.reward().toBuilder()
            .isAddOn(true)
            .isAvailable(true)
            .shippingRules(shippingRule.shippingRules())
            .startsAt(DateTime.now().minusDays(5))
            .endsAt(DateTime.now().plusDays(5))
            .remaining(8)
            .quantity(null)
            .limit(4)
            .rewardsItems(emptyList()).build()

        val usShippingRule = ShippingRuleFactory.usShippingRule()

        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, usShippingRule))

        this.shippingAmount.assertValue("$30")
    }

    // quantity per id
    // when id changes, emit quantity per id
    // when quantity changes, emit quantity per id

    @Test
    fun quantityPerId_whenCurrentQuantityChanges_emitToQuantityPerId() {
        setupEnvironment(environment())

        val addOn = RewardFactory.reward().toBuilder()
            .isAddOn(true)
            .isAvailable(true)
            .remaining(3)
            .rewardsItems(emptyList()).build()

        val shippingRule = ShippingRuleFactory.usShippingRule()
        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        quantityPerId.assertValue(Pair(0, addOn.id()))

        this.vm.inputs.currentQuantity(2)
        this.vm.inputs.currentQuantity(3)
        this.vm.inputs.currentQuantity(4)

        this.quantityPerId.assertValues(Pair(0, addOn.id()), Pair(2, addOn.id()), Pair(3, addOn.id()), Pair(4, addOn.id()))

        this.vm.inputs.currentQuantity(3)
        this.vm.inputs.currentQuantity(2)

        this.quantityPerId.assertValues(
            Pair(0, addOn.id()),
            Pair(2, addOn.id()),
            Pair(3, addOn.id()),
            Pair(4, addOn.id()),
            Pair(3, addOn.id()),
            Pair(2, addOn.id())
        )
    }

    @Test
    fun quantityPerId_whenAddonQuantityNotNull_shouldStartAtQuantity() {
        setupEnvironment(environment())

        val addOn = RewardFactory.reward().toBuilder()
            .isAddOn(true)
            .isAvailable(true)
            .remaining(3)
            .quantity(2)
            .rewardsItems(emptyList()).build()

        val shippingRule = ShippingRuleFactory.usShippingRule()
        this.vm.inputs.configureWith(Triple<ProjectData, Reward, ShippingRule>(ProjectDataFactory.project(ProjectFactory.project()), addOn, shippingRule))

        this.quantityPerId.assertValues(Pair(2, addOn.id()))

        this.vm.inputs.currentQuantity(3)
        this.vm.inputs.currentQuantity(4)
        this.vm.inputs.currentQuantity(5)

        this.quantityPerId.assertValues(Pair(2, addOn.id()), Pair(3, addOn.id()), Pair(4, addOn.id()), Pair(5, addOn.id()))
    }
}
