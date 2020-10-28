package com.kickstarter.libs.utils

import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Project
import org.junit.Test

class RewardViewUtilsTest : KSRobolectricTestCase() {

    @Test
    fun testPledgeButtonText() {

        // - Selected reward with addOns if choosing same reward always available unless project ended
        val project = ProjectFactory.backedProjectWithRewardAndAddOnsLimitReached()
        val rw = project.backing()?.reward() ?: RewardFactory.noReward()
        assertEquals(R.string.Continue, RewardViewUtils.pledgeButtonText(project, rw))

        assertEquals(R.string.Select, RewardViewUtils.pledgeButtonText(ProjectFactory.project(), RewardFactory.reward()))
        assertEquals(R.string.No_longer_available, RewardViewUtils.pledgeButtonText(ProjectFactory.project(), RewardFactory.ended()))
        assertEquals(R.string.No_longer_available, RewardViewUtils.pledgeButtonText(ProjectFactory.project(), RewardFactory.limitReached()))
        val backedProject = ProjectFactory.backedProject()
        val backedReward = backedProject.backing()?.reward()?: RewardFactory.reward()
        assertEquals(R.string.Selected, RewardViewUtils.pledgeButtonText(backedProject, backedReward))
        assertEquals(R.string.Select, RewardViewUtils.pledgeButtonText(backedProject, RewardFactory.reward()))
        val backedSuccessfulProject = ProjectFactory.backedProject().toBuilder().state(Project.STATE_SUCCESSFUL).build()
        val backedSuccessfulReward = backedSuccessfulProject.backing()?.reward()?: RewardFactory.reward()
        assertEquals(R.string.Selected, RewardViewUtils.pledgeButtonText(backedSuccessfulProject, backedSuccessfulReward))
    }

    /**
     * Given a backedProject the backed reward has available addOns
     * when not backed addOns
     * Then the text for the button should be R.string.Continue
     */
    @Test
    fun rewardBackedHasAvailableAddOns_whenNotBackedAddOns_textContinue() {
        val backedProjectRwHasAvailableNotBackedAddOns = ProjectFactory.backedProjectRewardAvailableAddOnsNotBackedAddOns()
        val backedRwAvailableAddOns = requireNotNull(backedProjectRwHasAvailableNotBackedAddOns.backing()?.reward())
        assertEquals(R.string.Continue, RewardViewUtils.pledgeButtonText(backedProjectRwHasAvailableNotBackedAddOns, backedRwAvailableAddOns))
    }

    @Test
    fun testShippingSummary() {
        val ksString = ksString()
        assertEquals("Ships worldwide", RewardViewUtils.shippingSummary(context(), ksString, Pair(R.string.Ships_worldwide, null)))
        assertEquals("Limited shipping", RewardViewUtils.shippingSummary(context(), ksString, Pair(R.string.Limited_shipping, null)))
        assertEquals("Limited shipping", RewardViewUtils.shippingSummary(context(), ksString, Pair(R.string.location_name_only, null)))
        assertEquals("Nigeria only", RewardViewUtils.shippingSummary(context(), ksString, Pair(R.string.location_name_only, "Nigeria")))
    }

}
