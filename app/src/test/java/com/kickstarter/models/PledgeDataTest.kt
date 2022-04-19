package com.kickstarter.models

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import org.junit.Test

class PledgeDataTest : KSRobolectricTestCase() {
    @Test
    fun testDefaultInit() {
        val project = ProjectFactory.project()
        val projectData = ProjectDataFactory.project(project)
        val reward = Reward.builder().build()
        val addOns = listOf(reward)

        val pledgeData = PledgeData.builder()
            .pledgeFlowContext(PledgeFlowContext.MANAGE_REWARD)
            .projectData(projectData)
            .reward(reward)
            .addOns(addOns)
            .build()

        assertEquals(pledgeData.pledgeFlowContext(), PledgeFlowContext.MANAGE_REWARD)
        assertEquals(pledgeData.reward(), reward)
        assertEquals(pledgeData.projectData(), projectData)
        assertEquals(pledgeData.shippingRule(), null)
        assertEquals(pledgeData.addOns(), addOns)
    }

    @Test
    fun testPledgeData_equalFalse() {
        val project = ProjectFactory.project()
        val projectData = ProjectDataFactory.project(project)
        val reward: Reward = Reward.builder().build()

        val pledgeData = PledgeData.builder().build()
        val pledgeData2 = PledgeData.builder()
            .pledgeFlowContext(PledgeFlowContext.MANAGE_REWARD)
            .projectData(projectData)
            .reward(reward)
            .addOns(listOf(reward))
            .build()
        val pledgeData3 = PledgeData.builder()
            .pledgeFlowContext(PledgeFlowContext.FIX_ERRORED_PLEDGE)
            .projectData(projectData)
            .build()
        
        val pledgeData4 = PledgeData.builder()
            .pledgeFlowContext(PledgeFlowContext.MANAGE_REWARD)
            .projectData(projectData)
            .reward(reward)
            .build()
        assertFalse(pledgeData == pledgeData2)
        assertFalse(pledgeData == pledgeData3)
        assertFalse(pledgeData == pledgeData4)

        assertFalse(pledgeData3 == pledgeData2)
        assertFalse(pledgeData3 == pledgeData4)
    }

    @Test
    fun testPledgeData_equalTrue() {
        val pledgeData1 = PledgeData.builder().build()
        val pledgeData2 = PledgeData.builder().build()

        assertEquals(pledgeData1, pledgeData2)
    }

    @Test
    fun testPledgeDataToBuilder() {
        val reward: Reward = Reward.builder().build()
        val pledgeData = PledgeData.builder().build().toBuilder()
            .reward(reward).build()

        assertEquals(pledgeData.reward(), reward)
    }
    @Test
    fun testPledgeDataWith() {
        val project = ProjectFactory.project()
        val projectData = ProjectDataFactory.project(project)
        val reward = Reward.builder().build()
        val addOns = listOf(reward)

        val pledgeData = PledgeData.with(
            pledgeFlowContext = PledgeFlowContext.MANAGE_REWARD,
            projectData = projectData,
            reward = reward,
            addOns = addOns
        ) 

        assertEquals(pledgeData.pledgeFlowContext(), PledgeFlowContext.MANAGE_REWARD)
        assertEquals(pledgeData.reward(), reward)
        assertEquals(pledgeData.projectData(), projectData)
        assertEquals(pledgeData.shippingRule(), null)
        assertEquals(pledgeData.addOns(), addOns)
    }
}
