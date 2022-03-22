package com.kickstarter.models

import com.kickstarter.mock.factories.ProjectNotificationFactory
import junit.framework.TestCase
import org.junit.Test

class ProjectNotificationTest : TestCase() {

    @Test
    fun testDefaultInit() {
        val projectNotification = ProjectNotification.builder().build()

        assertEquals(projectNotification.email(), false)
        assertEquals(projectNotification.project().id(), 0L)
        assertEquals(projectNotification.project().name(), "")
        assertEquals(projectNotification.id(), 0L)
        assertEquals(projectNotification.mobile(), false)
        assertEquals(projectNotification.urls().api().notification(), "")
    }

    @Test
    fun testProjectNotification_equalsFalse() {
        val projectNotification = ProjectNotification.builder().build()
        val projectNotification1 = ProjectNotificationFactory.enabled()

        assertFalse(projectNotification == projectNotification1)
        assertNotSame(projectNotification.email(), projectNotification1.email())
        assertNotSame(projectNotification.project().id(), projectNotification1.id())
        assertNotSame(projectNotification.project().name(), projectNotification1.project().name())
        assertNotSame(projectNotification.id(), projectNotification1.id())
        assertNotSame(projectNotification.mobile(), projectNotification1.mobile())
        assertNotSame(projectNotification.urls().api().notification(), projectNotification1.urls().api().notification())
    }

    @Test
    fun testProjectNotification_equalsTrue() {
        val projectNotification = ProjectNotificationFactory.enabled().toBuilder().id(1).build()
        val projectNotification1 = projectNotification

        assertTrue(projectNotification == projectNotification1)
        assertSame(projectNotification.email(), projectNotification1.email())
        assertSame(projectNotification.project().id(), projectNotification1.id())
        assertSame(projectNotification.project().name(), projectNotification1.project().name())
        assertSame(projectNotification.id(), projectNotification1.id())
        assertSame(projectNotification.mobile(), projectNotification1.mobile())
        assertSame(projectNotification.urls().api().notification(), projectNotification1.urls().api().notification())
    }

    @Test
    fun testProjectNotificationToBuilder() {
        val projectNotification = ProjectNotificationFactory.enabled()
        val project = ProjectNotification.Project.builder().build()
        val urls = ProjectNotification.Urls.builder().build()
        val projectNotification1 = projectNotification.toBuilder()
            .email(false)
            .mobile(false)
            .project(project)
            .urls(urls)
            .build()

        assertFalse(projectNotification == projectNotification1)
        assertNotSame(projectNotification.email(), projectNotification1.email())
        assertNotSame(projectNotification.project(), projectNotification1.project())
        // assertSame(projectNotification.id(), projectNotification1.id())
        // assertNotSame(projectNotification.mobile(), projectNotification1.mobile())
        // assertNotSame(projectNotification.urls(), projectNotification1.urls())
    }
}
