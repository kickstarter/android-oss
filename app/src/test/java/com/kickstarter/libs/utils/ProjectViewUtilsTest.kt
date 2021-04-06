package com.kickstarter.libs.utils

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.models.OptimizelyExperiment
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.Project
import org.junit.Test

class ProjectViewUtilsTest : KSRobolectricTestCase() {

    @Test
    fun testPledgeActionButtonColor() {
        assertEquals(R.color.button_pledge_live, ProjectViewUtils.pledgeActionButtonColor(ProjectFactory.project(), UserFactory.user()))
        assertEquals(R.color.button_pledge_live, ProjectViewUtils.pledgeActionButtonColor(ProjectFactory.project(), null))

        assertEquals(R.color.button_pledge_manage, ProjectViewUtils.pledgeActionButtonColor(ProjectFactory.backedProject(), UserFactory.user()))
        assertEquals(R.color.button_pledge_manage, ProjectViewUtils.pledgeActionButtonColor(ProjectFactory.backedProject(), null))

        assertEquals(R.color.button_pledge_ended, ProjectViewUtils.pledgeActionButtonColor(ProjectFactory.successfulProject(), UserFactory.user()))
        assertEquals(R.color.button_pledge_ended, ProjectViewUtils.pledgeActionButtonColor(ProjectFactory.successfulProject(), null))

        val backedSuccessfulProject = ProjectFactory.backedProject()
            .toBuilder()
            .state(Project.STATE_SUCCESSFUL)
            .build()
        assertEquals(R.color.button_pledge_ended, ProjectViewUtils.pledgeActionButtonColor(backedSuccessfulProject, UserFactory.user()))
        assertEquals(R.color.button_pledge_ended, ProjectViewUtils.pledgeActionButtonColor(backedSuccessfulProject, null))

        val creator = UserFactory.creator()
        val creatorProject = ProjectFactory.project()
            .toBuilder()
            .creator(creator)
            .build()
        assertEquals(R.color.button_pledge_ended, ProjectViewUtils.pledgeActionButtonColor(creatorProject, creator))
    }

    @Test
    fun testPledgeActionButtonText() {
        assertEquals(R.string.Back_this_project, ProjectViewUtils.pledgeActionButtonText(ProjectFactory.project(), UserFactory.user()))
        assertEquals(R.string.Back_this_project, ProjectViewUtils.pledgeActionButtonText(ProjectFactory.project(), null))
        assertEquals(R.string.Back_this_project, ProjectViewUtils.pledgeActionButtonText(ProjectFactory.project(), null, null))
        assertEquals(R.string.Back_this_project, ProjectViewUtils.pledgeActionButtonText(ProjectFactory.project(), null, OptimizelyExperiment.Variant.CONTROL))
        assertEquals(R.string.See_the_rewards, ProjectViewUtils.pledgeActionButtonText(ProjectFactory.project(), null, OptimizelyExperiment.Variant.VARIANT_1))
        assertEquals(R.string.View_the_rewards, ProjectViewUtils.pledgeActionButtonText(ProjectFactory.project(), null, OptimizelyExperiment.Variant.VARIANT_2))

        assertEquals(R.string.Manage, ProjectViewUtils.pledgeActionButtonText(ProjectFactory.backedProject(), UserFactory.user()))
        assertEquals(R.string.Manage, ProjectViewUtils.pledgeActionButtonText(ProjectFactory.backedProject(), null))
        assertEquals(R.string.Manage, ProjectViewUtils.pledgeActionButtonText(ProjectFactory.backedProjectWithError(), null))

        assertEquals(R.string.View_rewards, ProjectViewUtils.pledgeActionButtonText(ProjectFactory.successfulProject(), UserFactory.user()))
        assertEquals(R.string.View_rewards, ProjectViewUtils.pledgeActionButtonText(ProjectFactory.successfulProject(), null))
        val backedSuccessfulProject = ProjectFactory.backedProject()
            .toBuilder()
            .state(Project.STATE_SUCCESSFUL)
            .build()
        assertEquals(R.string.View_your_pledge, ProjectViewUtils.pledgeActionButtonText(backedSuccessfulProject, UserFactory.user()))
        assertEquals(R.string.View_your_pledge, ProjectViewUtils.pledgeActionButtonText(backedSuccessfulProject, null))

        val creator = UserFactory.creator()
        val creatorProject = ProjectFactory.project()
            .toBuilder()
            .creator(creator)
            .build()
        assertEquals(R.string.View_your_rewards, ProjectViewUtils.pledgeActionButtonText(creatorProject, creator))
    }

    @Test
    fun testPledgeToolbarTitle() {
        assertEquals(R.string.Back_this_project, ProjectViewUtils.pledgeToolbarTitle(ProjectFactory.project(), UserFactory.user()))
        assertEquals(R.string.Back_this_project, ProjectViewUtils.pledgeToolbarTitle(ProjectFactory.project(), null))

        assertEquals(R.string.Manage_your_pledge, ProjectViewUtils.pledgeToolbarTitle(ProjectFactory.backedProject(), UserFactory.user()))
        assertEquals(R.string.Manage_your_pledge, ProjectViewUtils.pledgeToolbarTitle(ProjectFactory.backedProject(), null))

        assertEquals(R.string.View_rewards, ProjectViewUtils.pledgeToolbarTitle(ProjectFactory.successfulProject(), UserFactory.user()))
        assertEquals(R.string.View_rewards, ProjectViewUtils.pledgeToolbarTitle(ProjectFactory.successfulProject(), null))

        val backedSuccessfulProject = ProjectFactory.backedProject()
            .toBuilder()
            .state(Project.STATE_SUCCESSFUL)
            .build()
        assertEquals(R.string.View_your_pledge, ProjectViewUtils.pledgeToolbarTitle(backedSuccessfulProject, UserFactory.user()))
        assertEquals(R.string.View_your_pledge, ProjectViewUtils.pledgeToolbarTitle(backedSuccessfulProject, null))

        val creator = UserFactory.creator()
        val creatorProject = ProjectFactory.project()
            .toBuilder()
            .creator(creator)
            .build()
        assertEquals(R.string.View_your_rewards, ProjectViewUtils.pledgeToolbarTitle(creatorProject, creator))
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

        assertEquals("US$ 30", ProjectViewUtils.styleCurrency(30.0, ProjectFactory.project(), currency).toString())
        assertEquals("US$ 30.50", ProjectViewUtils.styleCurrency(30.5, ProjectFactory.project(), currency).toString())
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
