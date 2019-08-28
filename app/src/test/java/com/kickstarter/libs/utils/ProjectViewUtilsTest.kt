package com.kickstarter.libs.utils

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.KSCurrency
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.ConfigFactory
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

    @Test
    fun testStyleCurrency_US() {
        val currency = createKSCurrency("US")

        assertEquals("$30", ProjectViewUtils.styleCurrency(30.0, ProjectFactory.project(), currency).toString())
        assertEquals("$30.50", ProjectViewUtils.styleCurrency(30.5, ProjectFactory.project(), currency).toString())
    }

    @Test
    fun testStyleCurrency_nonUS() {
        val currency = createKSCurrency("DE")

        assertEquals("US$ 30", ProjectViewUtils.styleCurrency(30.0, ProjectFactory.project(), currency).toString())
        assertEquals("US$ 30.50", ProjectViewUtils.styleCurrency(30.5, ProjectFactory.project(), currency).toString())
    }

    private fun createKSCurrency(countryCode: String): KSCurrency {
        val config = ConfigFactory.config().toBuilder()
                .countryCode(countryCode)
                .build()

        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        return KSCurrency(currentConfig)
    }
}
