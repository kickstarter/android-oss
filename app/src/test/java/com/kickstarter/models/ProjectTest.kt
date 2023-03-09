package com.kickstarter.models

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.ProjectFactory.allTheWayProject
import com.kickstarter.mock.factories.ProjectFactory.doubledGoalProject
import com.kickstarter.mock.factories.ProjectFactory.halfWayProject
import com.kickstarter.mock.factories.ProjectFactory.project
import org.joda.time.DateTime
import org.junit.Test

class ProjectTest : KSRobolectricTestCase() {

    fun projectWithSecureUrl(): Project {
        val projectUrl = "https://www.kickstarter.com/projects/foo/bar"
        val urls = Urls.builder()
            .web(Web.builder().project(projectUrl).rewards("$projectUrl/rewards").build())
            .build()
        return project().toBuilder().urls(urls).build()
    }

    @Test
    fun testSecureWebProjectUrl() {
        val projectUrl = "http://www.kickstarter.com/projects/foo/bar"
        val urls = Urls.builder()
            .web(Web.builder().project(projectUrl).rewards("$projectUrl/rewards").build())
            .build()
        val project = project().toBuilder().urls(urls).build()
        assertEquals("https://www.kickstarter.com/projects/foo/bar", project.secureWebProjectUrl())
    }

    @Test
    fun testNewPledgeUrl() {
        assertEquals(
            "https://www.kickstarter.com/projects/foo/bar/pledge/new",
            projectWithSecureUrl().newPledgeUrl()
        )
    }

    @Test
    fun testEditPledgeUrl() {
        assertEquals(
            "https://www.kickstarter.com/projects/foo/bar/pledge/edit",
            projectWithSecureUrl().editPledgeUrl()
        )
    }

    @Test
    fun testPercentageFunded() {
        assertEquals(50.0f, halfWayProject().percentageFunded())
        assertEquals(100.0f, allTheWayProject().percentageFunded())
        assertEquals(200.0f, doubledGoalProject().percentageFunded())
    }

    @Test
    fun testIsApproachingDeadline() {
        val projectApproachingDeadline = project().toBuilder()
            .deadline(DateTime().plusDays(1)).build()
        val projectNotApproachingDeadline = project().toBuilder()
            .deadline(DateTime().plusDays(3)).build()
        assertTrue(projectApproachingDeadline.isApproachingDeadline)
        assertFalse(projectNotApproachingDeadline.isApproachingDeadline)
    }

    fun testEquals_whenProjectsEquals_returnTrue() {
        val projectA = project().toBuilder().id(1).build()
        val projectB = project().toBuilder().id(1).build()

        assertTrue(projectA == projectB)
    }
}
