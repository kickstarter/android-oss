package com.kickstarter.libs.utils

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.ApiEndpoint
import com.kickstarter.libs.RefTag
import com.kickstarter.mock.factories.UserFactory
import org.junit.Test

class ExperimentUtilsTest : KSRobolectricTestCase() {

    @Test
    fun attributes_loggedInUser_notProd() {
        val user = UserFactory.user()
                .toBuilder()
                .backedProjectsCount(10)
                .build()
        val attributes = ExperimentUtils.attributes(user, RefTag.discovery(), "10", ApiEndpoint.STAGING)
        assertEquals(10, attributes["user_backed_projects_count"])
        assertEquals("US", attributes["user_country"])
        assertEquals("Android 10", attributes["session_os_version"])
        assertEquals("discovery", attributes["session_ref_tag"])
        assertEquals(true, attributes["session_user_is_logged_in"])
        assertNotNull(attributes["distinct_id"])
    }

    @Test
    fun attributes_loggedInUser_prod() {
        val user = UserFactory.user()
                .toBuilder()
                .backedProjectsCount(10)
                .build()
        val attributes = ExperimentUtils.attributes(user, RefTag.discovery(), "10", ApiEndpoint.PRODUCTION)
        assertEquals(10, attributes["user_backed_projects_count"])
        assertEquals("US", attributes["user_country"])
        assertEquals("Android 10", attributes["session_os_version"])
        assertEquals("discovery", attributes["session_ref_tag"])
        assertEquals(true, attributes["session_user_is_logged_in"])
        assertNull(attributes["distinct_id"])
    }

    @Test
    fun attributes_loggedOutUser_notProd() {
        val attributes = ExperimentUtils.attributes(null, null, "9", ApiEndpoint.STAGING)
        assertEquals(0, attributes["user_backed_projects_count"])
        assertEquals("US", attributes["user_country"])
        assertEquals("Android 9", attributes["session_os_version"])
        assertNull(attributes["session_ref_tag"])
        assertEquals(false, attributes["session_user_is_logged_in"])
        assertNotNull(attributes["distinct_id"])
    }

    @Test
    fun attributes_loggedOutUser_prod() {
        val attributes = ExperimentUtils.attributes(null, null, "9", ApiEndpoint.PRODUCTION)
        assertEquals(0, attributes["user_backed_projects_count"])
        assertEquals("US", attributes["user_country"])
        assertEquals("Android 9", attributes["session_os_version"])
        assertNull(attributes["session_ref_tag"])
        assertEquals(false, attributes["session_user_is_logged_in"])
        assertNull(attributes["distinct_id"])
    }
}
