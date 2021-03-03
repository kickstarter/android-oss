package com.kickstarter.libs.utils.extensions

import com.kickstarter.mock.factories.ProjectDataFactory.Companion.project
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Reward
import com.kickstarter.ui.data.PledgeData.Companion.with
import com.kickstarter.ui.data.PledgeFlowContext
import junit.framework.TestCase

class PledgeDataExtTest : TestCase() {

    fun testAddOnsCountTotalEmpty() {
        val project = ProjectFactory.project()
        val rw = RewardFactory.reward()
        val addOns = emptyList<Reward>() as java.util.List<Reward>
        val pledgeData = with(PledgeFlowContext.NEW_PLEDGE,
                project(project), rw, addOns, null)

        assertEquals(pledgeData.totalCountUnique(), 0)
        assertEquals(pledgeData.totalQuantity(), 0)
    }

    fun testAddOnsCountTotal_whenMultipleQuantity_singleAddOn() {
        val project = ProjectFactory.project()
        val rw = RewardFactory.rewardHasAddOns()
        val addOn = RewardFactory.addOnSingle()
        val addOns = listOf(addOn, addOn) as java.util.List<Reward>
        val pledgeData = with(PledgeFlowContext.NEW_PLEDGE,
                project(project), rw, addOns, null)

        assertEquals(pledgeData.totalCountUnique(), addOns.size)
        assertEquals(pledgeData.totalQuantity(), 2)
    }

    fun testAddOnsCountTotal_whenMultipleQuantity_multipleAddOns() {
        val project = ProjectFactory.project()
        val rw = RewardFactory.rewardHasAddOns()
        val addOn = RewardFactory.addOnMultiple()
        val addOns = listOf(addOn, addOn) as java.util.List<Reward>
        val pledgeData = with(PledgeFlowContext.NEW_PLEDGE,
                project(project), rw, addOns, null)

        assertEquals(pledgeData.totalCountUnique(), addOns.size)
        assertEquals(pledgeData.totalQuantity(), 10)
    }

    fun testAddOnsCountTotal_whenSingleAndMultiple_total() {
        val project = ProjectFactory.project()
        val rw = RewardFactory.rewardHasAddOns()
        val addOnSingle = RewardFactory.addOnSingle()
        val addOnMultiple = RewardFactory.addOnMultiple()
        val addOns = listOf(addOnSingle, addOnMultiple) as java.util.List<Reward>
        val pledgeData = with(PledgeFlowContext.NEW_PLEDGE,
                project(project), rw, addOns, null)

        assertEquals(pledgeData.totalCountUnique(), addOns.size)
        assertEquals(pledgeData.totalQuantity(), 6)
    }
}