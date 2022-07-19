package com.kickstarter.libs.utils

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.models.OptimizelyEnvironment
import com.kickstarter.mock.factories.UserFactory
import org.junit.Test

class ExperimentUtilsTest : KSRobolectricTestCase() {

    @Test
    fun attributes_loggedInUser_notProd() {
        val user = UserFactory.user()
            .toBuilder()
            .id(27)
            .backedProjectsCount(10)
            .build()
        val experimentData = ExperimentData(user, RefTag.discovery(), RefTag.search())
        val attributes = ExperimentUtils.attributes(experimentData, "9.9.9", "10", 99999, OptimizelyEnvironment.DEVELOPMENT)
        assertNotNull(attributes["distinct_id"])
        assertEquals("9.9.9", attributes["session_app_release_version"])
        assertEquals(27, attributes["user_id"])
        assertEquals(999, attributes["session_app_release_version_number"])
        assertEquals(99999, attributes["app_build_number"])
        assertEquals("Android 10", attributes["session_os_version"])
        assertEquals("discovery", attributes["session_ref_tag"])
        assertEquals("search", attributes["session_referrer_credit"])
        assertEquals(true, attributes["session_user_is_logged_in"])
        assertEquals(10, attributes["user_backed_projects_count"])
        assertEquals("US", attributes["user_country"])
    }

    @Test
    fun attributes_loggedInUser_prod() {
        val user = UserFactory.user()
            .toBuilder()
            .id(27)
            .backedProjectsCount(10)
            .build()
        val experimentData = ExperimentData(user, RefTag.discovery(), RefTag.search())
        val attributes = ExperimentUtils.attributes(experimentData, "9.9.9", "10", 99999, OptimizelyEnvironment.PRODUCTION)
        assertNotNull(attributes["distinct_id"])
        assertEquals("9.9.9", attributes["session_app_release_version"])
        assertEquals(999, attributes["session_app_release_version_number"])
        assertEquals(99999, attributes["app_build_number"])
        assertEquals(27, attributes["user_id"])
        assertEquals("Android 10", attributes["session_os_version"])
        assertEquals("discovery", attributes["session_ref_tag"])
        assertEquals("search", attributes["session_referrer_credit"])
        assertEquals(true, attributes["session_user_is_logged_in"])
        assertEquals(10, attributes["user_backed_projects_count"])
        assertEquals("US", attributes["user_country"])
    }

    @Test
    fun attributes_loggedOutUser_notProd() {
        val experimentData = ExperimentData(null, RefTag.discovery(), RefTag.search())
        val attributes = ExperimentUtils.attributes(experimentData, "9.9.9", "9", 99999, OptimizelyEnvironment.DEVELOPMENT)
        assertNotNull(attributes["distinct_id"])
        assertEquals("9.9.9", attributes["session_app_release_version"])
        assertEquals(999, attributes["session_app_release_version_number"])
        assertEquals(99999, attributes["app_build_number"])
        assertEquals("Android 9", attributes["session_os_version"])
        assertEquals("discovery", attributes["session_ref_tag"])
        assertEquals("search", attributes["session_referrer_credit"])
        assertEquals(false, attributes["session_user_is_logged_in"])
        assertEquals(0, attributes["user_backed_projects_count"])
        assertEquals("US", attributes["user_country"])
    }

    @Test
    fun attributes_loggedOutUser_prod() {
        val experimentData = ExperimentData(null, RefTag.discovery(), RefTag.search())
        val attributes = ExperimentUtils.attributes(experimentData, "9.9.9", "9", 99999, OptimizelyEnvironment.PRODUCTION)
        assertNotNull(attributes["distinct_id"])
        assertEquals("9.9.9", attributes["session_app_release_version"])
        assertEquals(999, attributes["session_app_release_version_number"])
        assertEquals(99999, attributes["app_build_number"])
        assertEquals("Android 9", attributes["session_os_version"])
        assertEquals("discovery", attributes["session_ref_tag"])
        assertEquals("search", attributes["session_referrer_credit"])
        assertEquals(false, attributes["session_user_is_logged_in"])
        assertEquals(0, attributes["user_backed_projects_count"])
        assertEquals("US", attributes["user_country"])
    }
}
