package com.kickstarter.models

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.RefTag
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.ui.data.ProjectData
import org.junit.Test

class ProjectDataTest : KSRobolectricTestCase() {
    @Test
    fun testDefaultInit() {
        val user = User.builder().build()
        val project = ProjectFactory.project()
        val intentRefTag = RefTag.discovery()
        val cookieRefTag = RefTag.recommended()

        val backing = BackingFactory.backing().toBuilder()
            .project(project)
            .bonusAmount(35.0)
            .shippingAmount(20f)
            .location(LocationFactory.germany())
            .locationId(LocationFactory.germany().id())
            .build()

        val projectData = ProjectData.builder()
            .project(project)
            .user(user)
            .refTagFromIntent(intentRefTag)
            .refTagFromCookie(cookieRefTag)
            .backing(backing)
            .build()

        assertEquals(projectData.project(), project)
        assertEquals(projectData.user(), user)
        assertEquals(projectData.refTagFromIntent(), intentRefTag)
        assertEquals(projectData.refTagFromCookie(), cookieRefTag)
        assertEquals(projectData.backing(), backing)
    }

    @Test
    fun testProjectData_equalFalse() {
        val user = User.builder().build()
        val project = ProjectFactory.project()
        val intentRefTag = RefTag.discovery()
        val cookieRefTag = RefTag.recommended()

        val projectData = ProjectData.builder().build()
        val projectData2 = ProjectDataFactory.project(project)
        val projectData3 = ProjectDataFactory.project(project, intentRefTag, cookieRefTag)
        val projectData4 = ProjectData.builder().project(project).user(user)
            .refTagFromIntent(intentRefTag)
            .refTagFromCookie(cookieRefTag)
            .build()

        assertFalse(projectData == projectData2)
        assertFalse(projectData == projectData3)
        assertFalse(projectData == projectData4)

        assertFalse(projectData3 == projectData2)
        assertFalse(projectData3 == projectData4)
    }

    @Test
    fun testProjectData_equalTrue() {
        val projectData1 = ProjectData.builder().build()
        val projectData2 = ProjectData.builder().build()

        assertEquals(projectData1, projectData2)
    }

    @Test
    fun testProjectDataToBuilder() {
        val user = User.builder().build()
        val projectData = ProjectData.builder().build().toBuilder()
            .user(user).build()

        assertEquals(projectData.user(), user)
    }
}
