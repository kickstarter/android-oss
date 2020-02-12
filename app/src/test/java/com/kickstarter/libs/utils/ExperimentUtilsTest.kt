package com.kickstarter.libs.utils

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.RefTag
import com.kickstarter.mock.factories.UserFactory
import org.junit.Test

class ExperimentUtilsTest : KSRobolectricTestCase() {

    @Test
    fun attributes_loggedInUser() {
        val user = UserFactory.user()
                .toBuilder()
                .backedProjectsCount(10)
                .build()
        val attributes = ExperimentUtils.attributes(user, RefTag.discovery(), "10")
        assertEquals(10, attributes["user_backed_projects_count"])
        assertEquals("US", attributes["user_country"])
        assertEquals("Android 10", attributes["session_os_version"])
        assertEquals("discovery", attributes["session_ref_tag"])
        assertEquals(true, attributes["session_user_is_logged_in"])
    }

    @Test
    fun attributes_loggedOutUser() {
        val attributes = ExperimentUtils.attributes(null, null, "9")
        assertEquals(0, attributes["user_backed_projects_count"])
        assertEquals("US", attributes["user_country"])
        assertEquals("Android 9", attributes["session_os_version"])
        assertNull(attributes["session_ref_tag"])
        assertEquals(false, attributes["session_user_is_logged_in"])
    }
}
