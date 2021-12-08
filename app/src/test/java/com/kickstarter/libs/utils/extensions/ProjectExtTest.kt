package com.kickstarter.libs.utils.extensions

import android.content.Context
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import org.joda.time.DateTime
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import type.CreditCardTypes
import java.util.Locale

class ProjectExtTest : KSRobolectricTestCase() {

    var context: Context = mock(Context::class.java)

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

    @Test
    fun testAcceptedCardTypes_whenCardNotInList_shouldReturnFalse() {
        val acceptedCardTypes: List<String> = listOf(CreditCardTypes.AMEX.toString(), CreditCardTypes.DISCOVER.toString())
        val project: Project = ProjectFactory.project().toBuilder().availableCardTypes(acceptedCardTypes).build()
        assertFalse(project.acceptedCardType(CreditCardTypes.MASTERCARD))
    }

    @Test
    fun testAcceptedCardTypes_whenCardListNull_shouldReturnFalse() {
        val project: Project = ProjectFactory.project().toBuilder().availableCardTypes(null).build()
        assertFalse(project.acceptedCardType(CreditCardTypes.MASTERCARD))
    }

    @Test
    fun testAcceptedCardTypes_whenCardInList_shouldReturnTrue() {
        val acceptedCardTypes: List<String> = listOf(CreditCardTypes.AMEX.toString(), CreditCardTypes.DISCOVER.toString())
        val project: Project = ProjectFactory.project().toBuilder().availableCardTypes(acceptedCardTypes).build()
        assertTrue(project.acceptedCardType(CreditCardTypes.AMEX))
    }

    @Test
    fun testCombineProjectsAndParams_shouldReturnCorrectList() {
        val projectList: List<Project> = listOf(ProjectFactory.project().toBuilder().name("First").build(), ProjectFactory.project().toBuilder().name("Second").build())
        val discoveryParams = DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build()

        val projectsAndParams = combineProjectsAndParams(projectList, discoveryParams)
        assertEquals(projectsAndParams.size, 2)
        assertEquals("First", projectsAndParams[0].first.name())
        assertEquals(projectsAndParams[0].second.sort(), DiscoveryParams.Sort.MAGIC)
        assertEquals(projectsAndParams[1].first.name(), "Second")
        assertEquals(projectsAndParams[1].second.sort(), DiscoveryParams.Sort.MAGIC)
    }

    @Test
    fun testDeadlineCountdown_shouldReturnCorrectString() {
        `when`(context.getString(R.string.discovery_baseball_card_deadline_units_secs)).thenReturn("secs")
        `when`(context.getString(R.string.discovery_baseball_card_deadline_units_mins)).thenReturn("mins")
        `when`(context.getString(R.string.discovery_baseball_card_deadline_units_hours)).thenReturn("hours")
        `when`(context.getString(R.string.discovery_baseball_card_deadline_units_days)).thenReturn("days")

        // - Added milliseconds to allow processing times
        val project: Project = ProjectFactory.project().toBuilder().deadline(DateTime.now().plusDays(2).plusMillis(300)).build()

        assertEquals("48 hours", project.deadlineCountdown(context))
    }

    @Test
    fun testDeadlineCountdownUnit_testAllCases_shouldReturnCorrectUnitOfTime() {
        `when`(context.getString(R.string.discovery_baseball_card_deadline_units_secs)).thenReturn("secs")
        `when`(context.getString(R.string.discovery_baseball_card_deadline_units_mins)).thenReturn("mins")
        `when`(context.getString(R.string.discovery_baseball_card_deadline_units_hours)).thenReturn("hours")
        `when`(context.getString(R.string.discovery_baseball_card_deadline_units_days)).thenReturn("days")

        var project: Project = ProjectFactory.project().toBuilder().deadline(DateTime.now().plusDays(1)).build()
        assertEquals("hours", project.deadlineCountdownUnit(context))

        project = project.toBuilder().deadline(DateTime.now().plusMinutes(10)).build()
        assertEquals("mins", project.deadlineCountdownUnit(context))

        project = project.toBuilder().deadline(DateTime.now().plusSeconds(25)).build()
        assertEquals("secs", project.deadlineCountdownUnit(context))

        project = project.toBuilder().deadline(DateTime.now().plusDays(10)).build()
        assertEquals("days", project.deadlineCountdownUnit(context))
    }

    @Test
    fun testDeadlineCountdownValue_testAllCases_shouldReturnCorrectValueOfTime() {
        // - Added milliseconds to allow processing times
        var project: Project = ProjectFactory.project().toBuilder().deadline(DateTime.now().plusDays(2).plusMillis(300)).build()
        assertEquals(48, project.deadlineCountdownValue())

        // - Added milliseconds to allow processing times
        project = project.toBuilder().deadline(DateTime.now().plusMinutes(10).plusMillis(300)).build()
        assertEquals(10, project.deadlineCountdownValue())

        // - Added milliseconds to allow processing times
        project = project.toBuilder().deadline(DateTime.now().plusSeconds(25).plusMillis(300)).build()
        assertEquals(25, project.deadlineCountdownValue())

        // - Added milliseconds to allow processing times
        project = project.toBuilder().deadline(DateTime.now().plusDays(10).plusMillis(300)).build()
        assertEquals(10, project.deadlineCountdownValue())
    }

    @Test
    fun testIsCompleted_whenStateCompleted_shouldReturnTrue() {
        var project: Project = ProjectFactory.project().toBuilder().state(Project.STATE_CANCELED).build()
        assertEquals(true, project.isCompleted())

        project = ProjectFactory.project().toBuilder().state(Project.STATE_FAILED).build()
        assertEquals(true, project.isCompleted())

        project = ProjectFactory.project().toBuilder().state(Project.STATE_SUCCESSFUL).build()
        assertEquals(true, project.isCompleted())

        project = ProjectFactory.project().toBuilder().state(Project.STATE_PURGED).build()
        assertEquals(true, project.isCompleted())

        project = ProjectFactory.project().toBuilder().state(Project.STATE_SUSPENDED).build()
        assertEquals(true, project.isCompleted())
    }

    @Test
    fun testIsCompleted_whenStateNotCompleted_shouldReturnFalse() {
        var project = ProjectFactory.project().toBuilder().state(Project.STATE_STARTED).build()
        assertEquals(false, project.isCompleted())

        project = ProjectFactory.project().toBuilder().state(Project.STATE_SUBMITTED).build()
        assertEquals(false, project.isCompleted())

        project = ProjectFactory.project().toBuilder().state(Project.STATE_LIVE).build()
        assertEquals(false, project.isCompleted())
    }

    @Test
    fun testIsProjectNamePunctuated_whenProjectEndsWithPunctuation_shouldReturnTrue() {
        assertTrue(isProjectNamePunctuated("Project!"))
    }

    @Test
    fun testIsProjectNamePunctuated_whenProjectDoesNotEndWithPunctuation_shouldReturnFalse() {
        assertFalse(isProjectNamePunctuated("Project"))
    }

    @Test
    fun testMetadataForProject_testAllCases_shouldReturnCorrectMetadata() {
        val backedProject = ProjectFactory.project().toBuilder().isBacking(true).build()
        assertEquals(ProjectMetadata.BACKING, backedProject.metadataForProject())

        val starredProject = ProjectFactory.project().toBuilder().isStarred(true).build()
        assertEquals(ProjectMetadata.SAVING, starredProject.metadataForProject())

        val featuredProject = ProjectFactory.featured()
        assertEquals(ProjectMetadata.CATEGORY_FEATURED, featuredProject.metadataForProject())
    }

    @Test
    fun testMetadataForProject_whenNoMetadata_shouldReturnNull() {
        assertNull(ProjectFactory.project().metadataForProject())
    }

    @Test
    fun testPhotoHeightFromWidthRatio_whenGivenHeight_shouldReturnCorrectHeight() {
        assertEquals(84, photoHeightFromWidthRatio(150))
    }

    @Test
    fun testTimeInSecondsOfDuration_shouldReturnCorrectDuration() {
        val project = ProjectFactory.project().toBuilder().launchedAt(DateTime.now().minusDays(10)).deadline(DateTime.now().plusDays(2)).build()

        assertEquals(1036800, project.timeInSecondsOfDuration())
    }

    @Test
    fun testTimeInDaysOfDuration_shouldReturnCorrectDuration() {
        val project = ProjectFactory.project().toBuilder().launchedAt(DateTime.now().minusDays(10)).deadline(DateTime.now().plusDays(2)).build()

        assertEquals(12, project.timeInDaysOfDuration())
    }

    @Test
    fun testTimeInSecondsUntilDeadline_whenProjectFinished_shouldReturnZero() {
        val project = ProjectFactory.project().toBuilder().deadline(DateTime.now().minusDays(2)).build()

        assertEquals(0, project.timeInSecondsUntilDeadline())
    }

    @Test
    fun testTimeInSecondsUntilDeadline_whenProjectNotFinished_shouldReturnDuration() {
        val project = ProjectFactory.project().toBuilder().deadline(DateTime.now().plusDays(2)).build()

        assertEquals(172800, project.timeInSecondsUntilDeadline())
    }

    @Test
    fun testUserIsCreator_whenUserIsNotCreator_shouldReturnFalse() {
        val project = ProjectFactory.project().toBuilder().id(19).build()
        val user = UserFactory.user().toBuilder().id(13).build()

        assertFalse(project.userIsCreator(user))
    }

    @Test
    fun testUserIsCreator_whenUserNull_shouldReturnFalse() {
        val project = ProjectFactory.project().toBuilder().id(19).build()

        assertFalse(project.userIsCreator(null))
    }

    @Test
    fun testUserIsCreator_whenUserCreator_shouldReturnTrue() {
        val project = ProjectFactory.project().toBuilder().id(19).build()
        val user = UserFactory.user().toBuilder().id(19).build()

        assertFalse(project.userIsCreator(user))
    }

    @Test
    fun testIsUSUserViewingNonUSProject_whenProjectUS_shouldReturnFalse() {
        assertFalse(isUSUserViewingNonUSProject(Locale.US.country, Locale.US.country))
    }

    @Test
    fun testIsUSUserViewingNonUSProject_whenUserUSButProjectNotUS_shouldReturnTrue() {
        assertTrue(isUSUserViewingNonUSProject(Locale.US.country, Locale.CANADA.country))
    }

    @Test
    fun testIsUSUserViewingNonUSProject_whenProjectUSButUserNotUS_shouldReturnFalse() {
        assertFalse(isUSUserViewingNonUSProject(Locale.GERMANY.country, Locale.US.country))
    }
}
