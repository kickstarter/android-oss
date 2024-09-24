package com.kickstarter.libs.utils.extensions

import com.kickstarter.mock.factories.ProjectDataFactory.project
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.models.Reward
import com.kickstarter.ui.data.PledgeData.Companion.with
import com.kickstarter.ui.data.PledgeFlowContext
import junit.framework.TestCase

class PledgeDataExtTest : TestCase() {

    fun `test checkoutTotalAmount for reward shipping with AddOns and bonus support on late pledges`() {
        val project = ProjectFactory.project()
        val shippingRule = ShippingRuleFactory.canadaShippingRule()
        val rw = RewardFactory.rewardWithShipping().toBuilder()
            .shippingRules(listOf(shippingRule))
            .latePledgeAmount(8.0).pledgeAmount(2.0).build()

        val addOn1 = RewardFactory.addOn().toBuilder()
            .id(1L)
            .quantity(3)
            .latePledgeAmount(5.0)
            .pledgeAmount(3.0)
            .shippingRules(listOf(shippingRule))
            .shippingPreference(Reward.ShippingPreference.UNRESTRICTED.name)
            .build()
        val addOn2 = RewardFactory.addOn().toBuilder()
            .id(2L)
            .quantity(2)
            .latePledgeAmount(6.0)
            .pledgeAmount(2.0)
            .shippingRules(listOf(shippingRule))
            .shippingPreference(Reward.ShippingPreference.UNRESTRICTED.name)
            .build()
        val addOns = listOf(addOn1, addOn2)

        val pledgeData1 = with(
            PledgeFlowContext.LATE_PLEDGES,
            project(project), rw, addOns, bonusAmount = 3.0, shippingRule = shippingRule
        )

        assertEquals(pledgeData1.checkoutTotalAmount(), 98.0)
    }

    fun `test checkoutTotalAmount for reward Not Shipping with AddOns and bonus support on crowdfund`() {
        val project = ProjectFactory.project()
        val shippingRule = ShippingRuleFactory.canadaShippingRule()
        val rw = RewardFactory.digitalReward().toBuilder()
            .shippingRules(listOf(ShippingRuleFactory.canadaShippingRule()))
            .latePledgeAmount(8.0).pledgeAmount(2.0).build()
        val addOn1 = RewardFactory.addOn().toBuilder().id(1L).quantity(3).latePledgeAmount(5.0).pledgeAmount(3.0).build()
        val addOn2 = RewardFactory.addOn().toBuilder().id(2L).quantity(2).latePledgeAmount(6.0).pledgeAmount(2.0).build()
        val addOns = listOf(addOn1, addOn2)

        val pledgeData1 = with(
            PledgeFlowContext.NEW_PLEDGE,
            project(project), rw, addOns, bonusAmount = 4.0, shippingRule = shippingRule
        )

        assertEquals(pledgeData1.checkoutTotalAmount(), 19.0)
    }

    fun `test checkoutTotalAmount for reward shipping with AddOns and bonus support on crowdfund`() {
        val project = ProjectFactory.project()
        val shippingRule = ShippingRuleFactory.canadaShippingRule()
        val rw = RewardFactory.rewardWithShipping().toBuilder()
            .shippingRules(listOf(shippingRule))
            .latePledgeAmount(8.0)
            .pledgeAmount(2.0)
            .build()
        val addOn1 = RewardFactory.addOn().toBuilder().id(1L).quantity(3).latePledgeAmount(5.0).pledgeAmount(3.0).build()
        val addOn2 = RewardFactory.addOn().toBuilder().id(2L).quantity(2).latePledgeAmount(6.0).pledgeAmount(2.0).build()
        val addOns = listOf(addOn1, addOn2)

        val pledgeData1 = with(
            PledgeFlowContext.NEW_PLEDGE,
            project(project), rw, addOns, bonusAmount = 4.0, shippingRule = shippingRule
        )

        assertEquals(pledgeData1.checkoutTotalAmount(), 29.0)
    }

    fun `test when the selected reward has shipping test shippingCostIfShipping`() {
        val rw = RewardFactory.rewardWithShipping().toBuilder()
            .shippingRules(listOf(ShippingRuleFactory.canadaShippingRule()))
            .build()
        val project = ProjectFactory.project()
        val shippingRule = ShippingRuleFactory.canadaShippingRule()

        val pledgeData = with(
            PledgeFlowContext.NEW_PLEDGE,
            project(project), rw, shippingRule = shippingRule
        )

        assertEquals(pledgeData.shippingCostIfShipping(), shippingRule.cost())
    }

    fun `test when digital reward test shippingCostIfShipping is 0`() {
        val digital = RewardFactory.digitalReward()
        val project = ProjectFactory.project()
        val shippingRule = ShippingRuleFactory.canadaShippingRule()

        val pledgeData2 = with(
            PledgeFlowContext.NEW_PLEDGE,
            project(project), digital, shippingRule = shippingRule
        )
        assertEquals(pledgeData2.shippingCostIfShipping(), 0.0)
    }

    fun `test pledgeTotalAmount with AddOns on late pledges`() {
        val project = ProjectFactory.project()
        val rw = RewardFactory.reward().toBuilder().latePledgeAmount(8.0).pledgeAmount(2.0).build()
        val addOn1 = RewardFactory.addOn().toBuilder().id(1L).quantity(3).latePledgeAmount(5.0).pledgeAmount(3.0).build()
        val addOn2 = RewardFactory.addOn().toBuilder().id(2L).quantity(2).latePledgeAmount(6.0).pledgeAmount(2.0).build()
        val addOns = listOf(addOn1, addOn2)

        val pledgeData = with(
            PledgeFlowContext.LATE_PLEDGES,
            project(project), rw, addOns
        )
        assertEquals(pledgeData.pledgeAmountTotal(), 35.0)
    }

    fun `test pledgeTotalAmountPlusBonus with AddOns on late pledges`() {
        val project = ProjectFactory.project()
        val rw = RewardFactory.reward().toBuilder().latePledgeAmount(8.0).pledgeAmount(2.0).build()
        val addOn1 = RewardFactory.addOn().toBuilder().id(1L).quantity(3).latePledgeAmount(5.0).pledgeAmount(3.0).build()
        val addOn2 = RewardFactory.addOn().toBuilder().id(2L).quantity(2).latePledgeAmount(6.0).pledgeAmount(2.0).build()
        val addOns = listOf(addOn1, addOn2)

        val pledgeData1 = with(
            PledgeFlowContext.LATE_PLEDGES,
            project(project), rw, addOns, bonusAmount = 0.0
        )
        assertEquals(pledgeData1.pledgeAmountTotalPlusBonus(), 35.0)

        val pledgeData2 = with(
            PledgeFlowContext.LATE_PLEDGES,
            project(project), rw, addOns, bonusAmount = 7.0
        )
        assertEquals(pledgeData2.pledgeAmountTotalPlusBonus(), 42.0)
    }

    fun `test pledgeTotalAmountPlusBonus with AddOns on crowdfund`() {
        val project = ProjectFactory.project()
        val rw = RewardFactory.reward().toBuilder().latePledgeAmount(8.0).pledgeAmount(2.0).build()
        val addOn1 = RewardFactory.addOn().toBuilder().id(1L).quantity(3).latePledgeAmount(5.0).pledgeAmount(3.0).build()
        val addOn2 = RewardFactory.addOn().toBuilder().id(2L).quantity(2).latePledgeAmount(6.0).pledgeAmount(2.0).build()
        val addOns = listOf(addOn1, addOn2)

        val pledgeData1 = with(
            PledgeFlowContext.NEW_PLEDGE,
            project(project), rw, addOns, bonusAmount = 0.0
        )
        assertEquals(pledgeData1.pledgeAmountTotalPlusBonus(), 15.0)

        val pledgeData2 = with(
            PledgeFlowContext.NEW_PLEDGE,
            project(project), rw, addOns, bonusAmount = 7.0
        )
        assertEquals(pledgeData2.pledgeAmountTotalPlusBonus(), 22.0)
    }

    fun `test pledgeTotalAmount with AddOns on crowdfund`() {
        val project = ProjectFactory.project()
        val rw = RewardFactory.reward().toBuilder().latePledgeAmount(8.0).pledgeAmount(2.0).build()
        val addOn1 = RewardFactory.addOn().toBuilder().id(1L).quantity(3).latePledgeAmount(5.0).pledgeAmount(3.0).build()
        val addOn2 = RewardFactory.addOn().toBuilder().id(2L).quantity(2).latePledgeAmount(6.0).pledgeAmount(2.0).build()
        val addOns = listOf(addOn1, addOn2)

        val pledgeData = with(
            PledgeFlowContext.NEW_PLEDGE,
            project(project), rw, addOns
        )
        assertEquals(pledgeData.pledgeAmountTotal(), 15.0)
    }

    fun testAddOnsCountTotalEmpty() {
        val project = ProjectFactory.project()
        val rw = RewardFactory.reward()
        val addOns = emptyList<Reward>()
        val pledgeData = with(
            PledgeFlowContext.NEW_PLEDGE,
            project(project), rw, addOns, null
        )

        assertEquals(pledgeData.totalCountUnique(), 0)
        assertEquals(pledgeData.totalQuantity(), 0)
    }

    fun testAddOnsCountTotal_whenMultipleQuantity_singleAddOn() {
        val project = ProjectFactory.project()
        val rw = RewardFactory.rewardHasAddOns()
        val addOn = RewardFactory.addOnSingle()
        val addOns = listOf(addOn, addOn)
        val pledgeData = with(
            PledgeFlowContext.NEW_PLEDGE,
            project(project), rw, addOns, null
        )

        assertEquals(pledgeData.totalCountUnique(), addOns.size)
        assertEquals(pledgeData.totalQuantity(), 2)
    }

    fun testAddOnsCountTotal_whenMultipleQuantity_multipleAddOns() {
        val project = ProjectFactory.project()
        val rw = RewardFactory.rewardHasAddOns()
        val addOn = RewardFactory.addOnMultiple()
        val addOns = listOf(addOn, addOn)
        val pledgeData = with(
            PledgeFlowContext.NEW_PLEDGE,
            project(project), rw, addOns, null
        )

        assertEquals(pledgeData.totalCountUnique(), addOns.size)
        assertEquals(pledgeData.totalQuantity(), 10)
    }

    fun testAddOnsCountTotal_whenSingleAndMultiple_total() {
        val project = ProjectFactory.project()
        val rw = RewardFactory.rewardHasAddOns()
        val addOnSingle = RewardFactory.addOnSingle()
        val addOnMultiple = RewardFactory.addOnMultiple()
        val addOns = listOf(addOnSingle, addOnMultiple)
        val pledgeData = with(
            PledgeFlowContext.NEW_PLEDGE,
            project(project), rw, addOns, null
        )

        assertEquals(pledgeData.totalCountUnique(), addOns.size)
        assertEquals(pledgeData.totalQuantity(), 6)
    }

    fun testAddOnsCost_whenUSDProject() {
        val project = ProjectFactory.project()
        val rw = RewardFactory.rewardHasAddOns()
        val addOnSingle = RewardFactory.addOnSingle().toBuilder().minimum(30.0).build()
        val addOnMultiple = RewardFactory.addOnMultiple().toBuilder().minimum(5.0).build()
        val addOns = listOf(addOnSingle, addOnMultiple)
        val pledgeData = with(
            PledgeFlowContext.NEW_PLEDGE,
            project(project), rw, addOns, null
        )

        val expectedValue = 30.0 + (5.0 * 5)
        assertEquals(pledgeData.addOnsCost(project.staticUsdRate()), expectedValue)
    }

    fun testAddOnsCost_whenCAProject() {
        val project = ProjectFactory.caProject()
        val rw = RewardFactory.rewardHasAddOns()
        // - quantity = 1 on addOnSingle
        val addOnSingle = RewardFactory.addOnSingle().toBuilder().minimum(30.0).build()
        // - quantity = 5 on addOnMultiple
        val addOnMultiple = RewardFactory.addOnMultiple().toBuilder().minimum(5.0).build()
        val addOns = listOf(addOnSingle, addOnMultiple)
        val pledgeData = with(
            PledgeFlowContext.NEW_PLEDGE,
            project(project), rw, addOns, null
        )

        val expectedValue = (30.0 + (5.0 * 5)) * project.staticUsdRate()
        assertEquals(pledgeData.addOnsCost(project.staticUsdRate()), expectedValue)
    }

    fun testRewardCost_whenUSDProject() {
        val project = ProjectFactory.project()
        val rw = RewardFactory.rewardHasAddOns()
        val addOns = emptyList<Reward>()
        val pledgeData = with(
            PledgeFlowContext.NEW_PLEDGE,
            project(project), rw, addOns, null
        )

        assertEquals(pledgeData.rewardCost(project.staticUsdRate()), rw.minimum())
    }

    fun testRewardCost_whenCAProject() {
        val project = ProjectFactory.caProject()
        val rw = RewardFactory.rewardHasAddOns()
        val addOns = emptyList<Reward>()
        val pledgeData = with(
            PledgeFlowContext.NEW_PLEDGE,
            project(project), rw, addOns, null
        )

        val expectedValue = rw.minimum() * project.staticUsdRate()
        assertEquals(pledgeData.rewardCost(project.staticUsdRate()), expectedValue)
    }
}
