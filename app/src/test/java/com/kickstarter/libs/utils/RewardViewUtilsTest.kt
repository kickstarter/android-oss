package com.kickstarter.libs.utils

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Project
import org.junit.Test

class RewardViewUtilsTest : KSRobolectricTestCase() {

    @Test
    fun testCheckBackgroundDrawable() {
        assertEquals(R.drawable.circle_blue_alpha_6, RewardViewUtils.checkBackgroundDrawable(ProjectFactory.project()))
        assertEquals(R.drawable.circle_grey_300, RewardViewUtils.checkBackgroundDrawable(ProjectFactory.successfulProject()))
    }

    @Test
    fun testPledgeButtonColor() {
        assertEquals(R.color.button_pledge_live, RewardViewUtils.pledgeButtonColor(ProjectFactory.project(), RewardFactory.reward()))
        val backedProject = ProjectFactory.backedProject()
        val backedReward = backedProject.backing()?.reward()?: RewardFactory.reward()
        assertEquals(R.color.button_pledge_manage, RewardViewUtils.pledgeButtonColor(backedProject, backedReward))
        val backedSuccessfulProject = ProjectFactory.backedProject().toBuilder().state(Project.STATE_SUCCESSFUL).build()
        val backedSuccessfulReward = backedProject.backing()?.reward()?: RewardFactory.reward()
        assertEquals(R.color.button_pledge_ended, RewardViewUtils.pledgeButtonColor(backedSuccessfulProject, backedSuccessfulReward))
        assertEquals(R.color.button_pledge_ended, RewardViewUtils.pledgeButtonColor(ProjectFactory.successfulProject(), RewardFactory.reward()))
    }

    @Test
    fun testPledgeButtonAlternateText() {
        assertEquals(R.string.No_longer_available, RewardViewUtils.pledgeButtonAlternateText(ProjectFactory.project(), RewardFactory.ended()))
        assertEquals(R.string.No_longer_available, RewardViewUtils.pledgeButtonAlternateText(ProjectFactory.project(), RewardFactory.limitReached()))
        val backedProject = ProjectFactory.backedProject()
        val backedReward = backedProject.backing()?.reward()?: RewardFactory.reward()
        assertEquals(R.string.Manage_your_pledge, RewardViewUtils.pledgeButtonAlternateText(backedProject, backedReward))
        assertEquals(R.string.Select_this_instead, RewardViewUtils.pledgeButtonAlternateText(backedProject, RewardFactory.reward()))
        val backedSuccessfulProject = ProjectFactory.backedProject().toBuilder().state(Project.STATE_SUCCESSFUL).build()
        val backedSuccessfulReward = backedSuccessfulProject.backing()?.reward()?: RewardFactory.reward()
        assertEquals(R.string.View_your_pledge, RewardViewUtils.pledgeButtonAlternateText(backedSuccessfulProject, backedSuccessfulReward))
    }

}
