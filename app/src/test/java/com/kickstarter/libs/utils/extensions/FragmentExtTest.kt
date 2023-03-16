package com.kickstarter.libs.utils.extensions

import androidx.fragment.app.Fragment
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.Reward
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.fragments.PledgeFragment
import org.junit.Test

class FragmentExtTest : KSRobolectricTestCase() {

    @Test
    fun testFragment_whenData_Null() {
        val fragment = Fragment().withData(null, null)
        assertNull(fragment.arguments?.get(ArgumentsKey.PLEDGE_PLEDGE_DATA))
        assertNull(fragment.arguments?.get(ArgumentsKey.PLEDGE_PLEDGE_REASON))
    }

    @Test
    fun testFragment_whenData_HaveData() {
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

        val fragment = Fragment().withData(pledgeData, PledgeReason.PLEDGE)

        val arg1 = fragment.arguments?.get(ArgumentsKey.PLEDGE_PLEDGE_DATA) as? PledgeData
        val arg2 = fragment.arguments?.get(ArgumentsKey.PLEDGE_PLEDGE_REASON)

        assertEquals(arg1, pledgeData)
        assertEquals(arg2, PledgeReason.PLEDGE)
    }

    @Test
    fun testPledgeFragmentInstance_whenFeatureFlag_Active() {
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

        val fragment = Fragment().selectPledgeFragment(pledgeData, PledgeReason.PLEDGE, true)

        assertTrue(fragment is PledgeFragment)
        assertFalse(fragment is PledgeFragmentLegacy)

        val arg1 = fragment.arguments?.get(ArgumentsKey.PLEDGE_PLEDGE_DATA) as? PledgeData
        val arg2 = fragment.arguments?.get(ArgumentsKey.PLEDGE_PLEDGE_REASON)

        assertEquals(arg1, pledgeData)
        assertEquals(arg2, PledgeReason.PLEDGE)
    }

    @Test
    fun testPledgeFragmentInstance_whenFeatureFlag_Deactivated() {
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

        val fragment = Fragment().selectPledgeFragment(pledgeData, PledgeReason.PLEDGE, false)

        assertFalse(fragment is PledgeFragment)
        assertTrue(fragment is PledgeFragmentLegacy)

        val arg1 = fragment.arguments?.get(ArgumentsKey.PLEDGE_PLEDGE_DATA) as? PledgeData
        val arg2 = fragment.arguments?.get(ArgumentsKey.PLEDGE_PLEDGE_REASON)

        assertEquals(arg1, pledgeData)
        assertEquals(arg2, PledgeReason.PLEDGE)
    }
}
