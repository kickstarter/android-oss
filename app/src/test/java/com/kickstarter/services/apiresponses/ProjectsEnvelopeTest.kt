package com.kickstarter.services.apiresponses

import com.kickstarter.mock.factories.ProjectFactory
import junit.framework.TestCase
import org.junit.Test

class ProjectsEnvelopeTest : TestCase() {

    @Test
    fun testProjectsEnvelopDefaultInit() {
        val projects = listOf(ProjectFactory.almostCompletedProject(), ProjectFactory.backedProjectWithAddOns())
        val urls = ProjectsEnvelope.UrlsEnvelope.builder().build()
        val projectsEnvelope = ProjectsEnvelope.builder().projects(projects).urls(urls).build()

        assertEquals(projectsEnvelope.urls(), urls)
        assertEquals(projectsEnvelope.projects(), projects)
    }

    @Test
    fun testUrlsEnvelopeDefaultInit() {
        val api = ProjectsEnvelope.UrlsEnvelope.ApiEnvelope
            .builder()
            .moreProjects("more_projects")
            .build()
        val urlsEnvelope = ProjectsEnvelope.UrlsEnvelope.builder()
            .api(api)
            .build()

        assertEquals(urlsEnvelope.api(), api)
    }

    @Test
    fun testApiEnvelopeDefaultInit() {
        val apiEnvelope = ProjectsEnvelope.UrlsEnvelope.ApiEnvelope.builder().moreProjects("more_projects").build()

        assertEquals(apiEnvelope.moreProjects(), "more_projects")
    }

    @Test
    fun testProjectsEnvelopEquals_whenFieldsDontMatch_returnsFalse() {
        val projectsEnvelope1 = ProjectsEnvelope.builder()
            .projects(listOf(ProjectFactory.almostCompletedProject(), ProjectFactory.backedProjectWithAddOns()))
            .urls(ProjectsEnvelope.UrlsEnvelope.builder().api(ProjectsEnvelope.UrlsEnvelope.ApiEnvelope.builder().moreProjects("first_project").build()).build())
            .build()

        val projectsEnvelope2 = projectsEnvelope1.toBuilder()
            .projects(listOf(ProjectFactory.staffPick(), ProjectFactory.backedProjectWithAddOns(), ProjectFactory.projectWithAddOns()))
            .build()

        val projectsEnvelope3 = projectsEnvelope1.toBuilder()
            .urls(ProjectsEnvelope.UrlsEnvelope.builder().api(ProjectsEnvelope.UrlsEnvelope.ApiEnvelope.builder().moreProjects("second_project").build()).build())
            .build()

        assertFalse(projectsEnvelope1 == projectsEnvelope2)
        assertFalse(projectsEnvelope1 == projectsEnvelope3)
        assertFalse(projectsEnvelope2 == projectsEnvelope3)
    }

    @Test
    fun testUrlsEnvelopeEquals_whenFieldsDontMatch_returnsFalse() {
        val urlsEnvelope1 = ProjectsEnvelope.UrlsEnvelope.builder().api(ProjectsEnvelope.UrlsEnvelope.ApiEnvelope.builder().moreProjects("first_project").build()).build()

        val urlsEnvelope2 = urlsEnvelope1.toBuilder()
            .api(ProjectsEnvelope.UrlsEnvelope.ApiEnvelope.builder().moreProjects("second_project").build()).build()

        assertFalse(urlsEnvelope1 == urlsEnvelope2)
    }

    @Test
    fun testApiEnvelopeEquals_whenFieldsDontMatch_returnsFalse() {
        val apiEnvelope1 = ProjectsEnvelope.UrlsEnvelope.ApiEnvelope.builder().moreProjects("first_project").build()

        val apiEnvelope2 = apiEnvelope1.toBuilder()
            .moreProjects("second_project").build()

        assertFalse(apiEnvelope1 == apiEnvelope2)
    }

    @Test
    fun testProjectsEnvelopEquals_whenFieldsMatch_returnsTrue() {
        val updatesEnvelope1 = ProjectsEnvelope.builder()
            .projects(listOf(ProjectFactory.almostCompletedProject(), ProjectFactory.backedProjectWithAddOns()))
            .urls(ProjectsEnvelope.UrlsEnvelope.builder().api(ProjectsEnvelope.UrlsEnvelope.ApiEnvelope.builder().moreProjects("first_project").build()).build())
            .build()

        val updatesEnvelope2 = updatesEnvelope1

        assertTrue(updatesEnvelope1 == updatesEnvelope2)
    }

    @Test
    fun testUrlsEnvelopeEquals_whenFieldsMatch_returnsTrue() {
        val urlsEnvelope1 = ProjectsEnvelope.UrlsEnvelope.builder().api(ProjectsEnvelope.UrlsEnvelope.ApiEnvelope.builder().moreProjects("first_project").build()).build()

        val urlsEnvelope2 = urlsEnvelope1

        assertTrue(urlsEnvelope1 == urlsEnvelope2)
    }

    @Test
    fun testApiEnvelopeEquals_whenFieldsMatch_returnsTrue() {
        val apiEnvelope1 = ProjectsEnvelope.UrlsEnvelope.ApiEnvelope.builder().moreProjects("first_projects").build()

        val apiEnvelope2 = apiEnvelope1

        assertTrue(apiEnvelope1 == apiEnvelope2)
    }

    @Test
    fun testProjectsEnvelopToBuilder() {
        val projects1 = listOf(ProjectFactory.almostCompletedProject(), ProjectFactory.backedProjectWithAddOns())
        val projects2 = listOf(ProjectFactory.staffPick(), ProjectFactory.backedProjectWithAddOns(), ProjectFactory.projectWithAddOns())

        val updatesEnvelope = ProjectsEnvelope.builder().projects(projects1).build().toBuilder().projects(projects2).build()

        assertEquals(updatesEnvelope.projects(), projects2)
    }

    @Test
    fun testUrlsEnvelopeToBuilder() {
        val apiEnvelope1 = ProjectsEnvelope.UrlsEnvelope.ApiEnvelope.builder().moreProjects("first_project").build()
        val apiEnvelope2 = ProjectsEnvelope.UrlsEnvelope.ApiEnvelope.builder().moreProjects("second_project").build()
        val urlsEnvelope =
            ProjectsEnvelope.UrlsEnvelope.builder()
                .api(apiEnvelope1)
                .build()
                .toBuilder()
                .api(apiEnvelope2)
                .build()

        assertEquals(urlsEnvelope.api(), apiEnvelope2)
    }

    @Test
    fun testApiEnvelopeToBuilder() {
        val apiEnvelope =
            ProjectsEnvelope.UrlsEnvelope.ApiEnvelope.builder()
                .moreProjects("first_project")
                .build().toBuilder()
                .moreProjects("second_project")
                .build()

        assertEquals(apiEnvelope.moreProjects(), "second_project")
    }
}
