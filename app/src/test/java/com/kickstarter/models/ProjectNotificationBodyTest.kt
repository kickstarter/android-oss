package com.kickstarter.models

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.services.apirequests.ProjectNotificationBody
import org.junit.Test

class ProjectNotificationBodyTest : KSRobolectricTestCase() {

    @Test
    fun testDefaultInit() {
        val projectNotificationBody = ProjectNotificationBody.builder()
            .email(true)
            .mobile(true)
            .build()

        assertTrue(projectNotificationBody.email())
        assertTrue(projectNotificationBody.mobile())
    }

    @Test
    fun testDefaultToBuilder() {
        val projectNotificationBody = ProjectNotificationBody.builder().build().toBuilder().email(true).build()
        assertTrue(projectNotificationBody.email())
    }

    @Test
    fun testProjectNotificationBody_equalFalse() {
        val projectNotificationBody = ProjectNotificationBody.builder().build()
        val projectNotificationBody2 = ProjectNotificationBody.builder()
            .email(true).build()
        val projectNotificationBody3 = ProjectNotificationBody.builder()
            .mobile(true)
            .build()
        val projectNotificationBody4 = ProjectNotificationBody.builder()
            .email(true)
            .mobile(true)
            .build()

        assertFalse(projectNotificationBody == projectNotificationBody2)
        assertFalse(projectNotificationBody == projectNotificationBody3)
        assertFalse(projectNotificationBody == projectNotificationBody4)

        assertFalse(projectNotificationBody3 == projectNotificationBody2)
        assertFalse(projectNotificationBody3 == projectNotificationBody4)
    }

    @Test
    fun testProjectNotificationBody_equalTrue() {
        val projectNotificationBody1 = ProjectNotificationBody.builder().build()
        val projectNotificationBody2 = ProjectNotificationBody.builder().build()

        assertEquals(projectNotificationBody1, projectNotificationBody2)
    }
}
