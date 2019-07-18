package com.kickstarter.libs.utils

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.Project
import org.junit.Test

class ProjectViewUtilsTest : KSRobolectricTestCase() {

    @Test
    fun testRewardsButtonColor() {
        assertEquals(R.color.button_pledge_live, ProjectViewUtils.rewardsButtonColor(ProjectFactory.project()))
        assertEquals(R.color.button_pledge_manage, ProjectViewUtils.rewardsButtonColor(ProjectFactory.backedProject()))
        assertEquals(R.color.button_pledge_ended, ProjectViewUtils.rewardsButtonColor(ProjectFactory.successfulProject()))
        val backedSuccessfulProject = ProjectFactory.backedProject()
                .toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        assertEquals(R.color.button_pledge_ended, ProjectViewUtils.rewardsButtonColor(backedSuccessfulProject))
    }

    @Test
    fun testRewardsButtonText() {
        assertEquals(R.string.Back_this_project, ProjectViewUtils.rewardsButtonText(ProjectFactory.project()))
        assertEquals(R.string.Manage, ProjectViewUtils.rewardsButtonText(ProjectFactory.backedProject()))
        assertEquals(R.string.View_rewards, ProjectViewUtils.rewardsButtonText(ProjectFactory.successfulProject()))
        val backedSuccessfulProject = ProjectFactory.backedProject()
                .toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        assertEquals(R.string.View_your_pledge, ProjectViewUtils.rewardsButtonText(backedSuccessfulProject))
    }

    @Test
    fun testRewardsToolbarTitle() {
        assertEquals(R.string.Back_this_project, ProjectViewUtils.rewardsToolbarTitle(ProjectFactory.project()))
        assertEquals(R.string.Manage_your_pledge, ProjectViewUtils.rewardsToolbarTitle(ProjectFactory.backedProject()))
        assertEquals(R.string.View_rewards, ProjectViewUtils.rewardsToolbarTitle(ProjectFactory.successfulProject()))
        val backedSuccessfulProject = ProjectFactory.backedProject()
                .toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        assertEquals(R.string.View_your_pledge, ProjectViewUtils.rewardsToolbarTitle(backedSuccessfulProject))
    }
}
