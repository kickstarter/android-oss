package com.kickstarter.libs.utils.extensions

import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import junit.framework.TestCase
import org.junit.Test

class ProjectExtTest : TestCase() {

    @Test
    fun testBritishProject_WhenNoUserAndCanadaConfig() {
        val user = null
        val config = ConfigFactory.configForCA()
        val project = ProjectFactory.britishProject()

        val updatedProject = project.updateProjectWith(config, user = user)

        assertFalse(updatedProject.currentCurrency() == "GBP")
        assertFalse(updatedProject.currencySymbol() == "£")
        assertTrue(updatedProject.currentCurrency() == "CAD")
        assertTrue(updatedProject.currencyTrailingCode())
        assertTrue(updatedProject.currencySymbol() == "$")
    }

    @Test
    fun testBritishProject_WhenUserCanadaCurrencyAndCanadaConfig() {
        val user = UserFactory.germanUser().toBuilder().chosenCurrency("CAD").build()
        val config = ConfigFactory.configForCA()
        val project = ProjectFactory.britishProject()

        val updatedProject = project.updateProjectWith(config, user = user)

        assertFalse(updatedProject.currentCurrency() == "GBP")
        assertFalse(updatedProject.currencySymbol() == "£")
        assertTrue(updatedProject.currentCurrency() == "CAD")
        assertTrue(updatedProject.currencyTrailingCode())
        assertTrue(updatedProject.currencySymbol() == "$")
    }

    @Test
    fun testBritishProject_WhenUserJapaneseCurrencyAndCanadaConfig() {
        val user = UserFactory.germanUser().toBuilder().chosenCurrency("JPY").build()
        val config = ConfigFactory.configForCA()
        val project = ProjectFactory.britishProject()

        val updatedProject = project.updateProjectWith(config, user = user)

        assertFalse(updatedProject.currentCurrency() == "GBP")
        assertFalse(updatedProject.currencySymbol() == "£")
        assertTrue(updatedProject.currentCurrency() == "JPY")
        assertFalse(updatedProject.currencyTrailingCode())
        assertTrue(updatedProject.currencySymbol() == "¥")
    }

    @Test
    fun testUserCanUpdateRewardFulfilled() {
        val projectReady = ProjectFactory.backedProject().toBuilder().state("successful").build()
        assertTrue(projectReady.canUpdateFulfillment())

        val project = ProjectFactory.project()
        assertFalse(project.canUpdateFulfillment())
    }
}
