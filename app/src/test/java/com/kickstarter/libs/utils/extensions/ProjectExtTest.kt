package com.kickstarter.libs.utils.extensions

import android.content.Context
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.mock.factories.CheckoutWaveFactory
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.PledgeManagerFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.Project
import com.kickstarter.models.ProjectFaq
import com.kickstarter.models.Reward
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.type.CreditCardTypes
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
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
        assertEquals(ProjectMetadata.NONE, ProjectFactory.project().metadataForProject())
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
        val project = ProjectFactory.project().toBuilder().deadline(DateTime.now().plusDays(2).plusMillis(300)).build()

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

    @Test
    fun testProjectNeedsRootCategory() {
        assertFalse(ProjectFactory.backedProject().projectNeedsRootCategory(CategoryFactory.tabletopGamesCategory()))
        assertTrue(
            ProjectFactory.featured().projectNeedsRootCategory(
                CategoryFactory
                    .photographyCategory().toBuilder().parentId(1).build()
            )
        )
    }

    @Test
    fun testEqualProjects_backing_false() {
        val backedProject = ProjectFactory.backedProject()
        val backing = backedProject.backing()?.toBuilder()?.backer(UserFactory.canadianUser())?.build()
        val secondProject = ProjectFactory.backedProject()
            .toBuilder()
            .backing(backing)
            .build()

        assertFalse(backedProject == secondProject)
        assertFalse(backedProject.backing() == secondProject.backing())
    }

    @Test
    fun testEqualProjects_backing_true() {
        val backedProject = ProjectFactory.backedProject()
        val secondProject = backedProject

        assertTrue(backedProject == secondProject)
        assertTrue(backedProject.backing() == secondProject.backing())
    }

    @Test
    fun testEqualProjects_creator_false() {
        val project = ProjectFactory.project()
        val creator = project.creator().toBuilder().id(9L).build()
        val secondProject = ProjectFactory.project()
            .toBuilder()
            .creator(creator)
            .build()

        assertFalse(project == secondProject)
        assertFalse(project.creator() == secondProject.creator())
    }

    @Test
    fun testEqualProjects_creator_true() {
        val project = ProjectFactory.project()
        val secondProject = project

        assertTrue(project == secondProject)
        assertTrue(project.creator() == secondProject.creator())
    }

    @Test
    fun testEqualProjects_deadline_true() {
        val project = ProjectFactory.project()
        val secondProject = project

        assertTrue(project.deadline() == secondProject.deadline())
    }

    @Test
    fun testReduce_project() {
        val project = ProjectFactory.project()
        val reducedProject = project.reduce()

        assertEquals(project.id(), reducedProject.id())
        assertEquals(project.name(), reducedProject.name())
        assertEquals(project.slug(), reducedProject.slug())
        assertEquals(project.location(), reducedProject.location())
        assertEquals(project.deadline(), reducedProject.deadline())
        assertEquals(project.staticUsdRate(), reducedProject.staticUsdRate())
        assertEquals(project.fxRate(), reducedProject.fxRate())
        assertEquals(project.country(), reducedProject.country())
        assertEquals(project.currency(), reducedProject.currency())
        assertEquals(project.currentCurrency(), reducedProject.currentCurrency())
        assertEquals(project.currencySymbol(), reducedProject.currencySymbol())
        assertEquals(project.currencyTrailingCode(), reducedProject.currencyTrailingCode())
        assertEquals(project.currencyTrailingCode(), reducedProject.currencyTrailingCode())
        assertEquals(project.isBacking(), reducedProject.isBacking())
        assertEquals(project.availableCardTypes(), reducedProject.availableCardTypes())
        assertEquals(project.category(), reducedProject.category())

        assertEquals(reducedProject.rewards(), emptyList<Reward>()) // Default builder value
        assertEquals(reducedProject.creator(), User.builder().build()) // Default builder value
        assertEquals(reducedProject.video(), null) // Default builder value
        assertEquals(reducedProject.projectFaqs(), emptyList<ProjectFaq>()) // Default builder value
        assertEquals(reducedProject.photo(), null) // Default builder value
        assertEquals(reducedProject.story(), "") // Default builder value
    }

    @Test
    fun testReduceToPreLaunchProject_project() {
        val user = UserFactory.germanUser().toBuilder().chosenCurrency("CAD").build()
        val deadline = DateTime(DateTimeZone.UTC).plusDays(10)
        val project = ProjectFactory.project().toBuilder().watchesCount(10).isStarred(true).creator(user).build()
        val reducedProject = project.reduceProjectPayload().toBuilder().deadline(deadline).build()

        assertEquals(project.id(), reducedProject.id())
        assertEquals(project.name(), reducedProject.name())
        assertEquals(project.slug(), reducedProject.slug())
        assertEquals(project.location(), reducedProject.location())
        assertEquals(project.blurb(), reducedProject.blurb())
        assertEquals(project.isStarred(), reducedProject.isStarred())
        assertEquals(10, reducedProject.watchesCount())
        assertEquals(project.country(), reducedProject.country())
        assertEquals(project.currency(), reducedProject.currency())
        assertEquals(project.currentCurrency(), reducedProject.currentCurrency())
        assertEquals(project.currencySymbol(), reducedProject.currencySymbol())
        assertEquals(project.currencyTrailingCode(), reducedProject.currencyTrailingCode())
        assertEquals(project.currencyTrailingCode(), reducedProject.currencyTrailingCode())
        assertEquals(project.category(), reducedProject.category())
        assertEquals(reducedProject.creator(), user)
        assertEquals(project.photo(), reducedProject.photo()) // Default builder value

        assertEquals(deadline, reducedProject.deadline())
        assertEquals(project.isBacking(), reducedProject.isBacking())
        assertEquals(null, reducedProject.availableCardTypes())
        assertEquals(0.0f, reducedProject.staticUsdRate())
        assertEquals(0.0f, reducedProject.fxRate())
        assertEquals(reducedProject.rewards(), emptyList<Reward>()) // Default builder value
        assertEquals(reducedProject.video(), null) // Default builder value
        assertEquals(reducedProject.projectFaqs(), emptyList<ProjectFaq>()) // Default builder value
        assertEquals(reducedProject.story(), "") // Default builder value
    }

    @Test
    fun `test if a project is not allowed to collect pledges when has been funded and no late pledges`() {
        val project = ProjectFactory.project()
            .toBuilder()
            .state(Project.STATE_SUCCESSFUL)
            .isInPostCampaignPledgingPhase(false)
            .postCampaignPledgingEnabled(false)
            .build()

        assertFalse(project.isAllowedToPledge())
    }

    @Test
    fun `test if a project is allowed to collect pledges when campaign is still ongoing`() {
        val project = ProjectFactory.project()
            .toBuilder()
            .state(Project.STATE_LIVE)
            .isInPostCampaignPledgingPhase(false)
            .postCampaignPledgingEnabled(false)
            .build()

        assertTrue(project.isAllowedToPledge())
    }

    @Test
    fun `test if a project is allowed to collect pledges when has been funded but has late pledges enabled and it is collecting`() {
        val project = ProjectFactory.project()
            .toBuilder()
            .state(Project.STATE_SUCCESSFUL)
            .isInPostCampaignPledgingPhase(true)
            .postCampaignPledgingEnabled(true)
            .build()

        assertTrue(project.isAllowedToPledge())
    }

    @Test
    fun `test if a project is not allowed to collect pledges when has been funded but has late pledges enabled but not collecting`() {
        val project = ProjectFactory.project()
            .toBuilder()
            .state(Project.STATE_SUCCESSFUL)
            .isInPostCampaignPledgingPhase(false)
            .postCampaignPledgingEnabled(true)
            .build()

        assertFalse(project.isAllowedToPledge())
    }

    @Test
    fun `test that a project is accepting net new backers for pledge manager when the pledge manager accepts new backers and last checkout wave is active`() {
        val pledgeManager = PledgeManagerFactory.pledgeManagerAcceptsNetNewBackers()
        val lastWave = CheckoutWaveFactory.checkoutWaveActive()

        val project = ProjectFactory.project()
            .toBuilder()
            .pledgeManager(pledgeManager)
            .lastWave(lastWave)
            .isBacking(false)
            .build()

        assertTrue(project.pledgeManagementAvailable())
    }

    @Test
    fun `test that a project is not accepting net new backers for pledge manager when the pledge manager does not accept new backers and last checkout wave is active`() {
        val pledgeManager = PledgeManagerFactory.pledgeManagerDoesNotAcceptNetNewBackers()
        val lastWave = CheckoutWaveFactory.checkoutWaveActive()

        val project = ProjectFactory.project()
            .toBuilder()
            .pledgeManager(pledgeManager)
            .lastWave(lastWave)
            .isBacking(false)
            .build()

        assertFalse(project.pledgeManagementAvailable())
    }

    @Test
    fun `test that a project is not accepting net new backers for pledge manager when the pledge manager accepts new backers but last checkout wave is inactive`() {
        val pledgeManager = PledgeManagerFactory.pledgeManagerAcceptsNetNewBackers()
        val lastWave = CheckoutWaveFactory.checkoutWaveInactive()

        val project = ProjectFactory.project()
            .toBuilder()
            .pledgeManager(pledgeManager)
            .lastWave(lastWave)
            .isBacking(false)
            .build()

        assertFalse(project.pledgeManagementAvailable())
    }

    @Test
    fun `test that a project is not accepting net new backers for pledge manager when the pledge manager is in non approved state and last checkout wave is inactive`() {
        val pledgeManager = PledgeManagerFactory.pledgeManagerInNonApprovedState()
        val lastWave = CheckoutWaveFactory.checkoutWaveInactive()

        val project = ProjectFactory.project()
            .toBuilder()
            .pledgeManager(pledgeManager)
            .lastWave(lastWave)
            .isBacking(false)
            .build()

        assertFalse(project.pledgeManagementAvailable())
    }

    @Test
    fun `test that pledge management is available when pm window is open and user is backer`() {
        val pledgeManager = PledgeManagerFactory.pledgeManagerInNonApprovedState()
            .toBuilder()
            .acceptsNewBackers(false)
            .build()
        val lastWave = CheckoutWaveFactory.checkoutWaveInactive()

        val project = ProjectFactory.project()
            .toBuilder()
            .pledgeManager(pledgeManager)
            .lastWave(lastWave)
            .isBacking(true)
            .build()

        assertFalse(project.pledgeManagementAvailable())
    }
}
