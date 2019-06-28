package com.kickstarter.libs.utils

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.Project
import org.junit.Test

class ProjectViewUtilsTest : KSRobolectricTestCase() {

    @Test
    fun testPledgeButtonColor() {
        assertEquals(R.color.button_pledge_live, ProjectViewUtils.pledgeButtonColor(ProjectFactory.project()))
        assertEquals(R.color.button_pledge_manage, ProjectViewUtils.pledgeButtonColor(ProjectFactory.backedProject()))
        assertEquals(R.color.button_pledge_live, ProjectViewUtils.pledgeButtonColor(ProjectFactory.successfulProject()))
        val backedSuccessfulProject = ProjectFactory.backedProject()
                .toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        assertEquals(R.color.button_pledge_ended, ProjectViewUtils.pledgeButtonColor(backedSuccessfulProject))
    }
}
