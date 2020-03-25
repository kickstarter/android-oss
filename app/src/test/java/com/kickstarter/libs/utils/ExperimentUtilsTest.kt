package com.kickstarter.libs.utils

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.ApiEndpoint
import com.kickstarter.libs.RefTag
import com.kickstarter.mock.factories.*
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import org.junit.Test

class ExperimentUtilsTest : KSRobolectricTestCase() {

    @Test
    fun attributes_loggedInUser_notProd() {
        val user = UserFactory.user()
                .toBuilder()
                .backedProjectsCount(10)
                .build()
        val experimentData = ExperimentData(user, RefTag.discovery(), RefTag.search())
        val attributes = ExperimentUtils.attributes(experimentData, "9.9.9", "10", ApiEndpoint.STAGING)
        assertNotNull(attributes["distinct_id"])
        assertEquals("9.9.9", attributes["session_app_release_version"])
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
                .backedProjectsCount(10)
                .build()
        val experimentData = ExperimentData(user, RefTag.discovery(), RefTag.search())
        val attributes = ExperimentUtils.attributes(experimentData, "9.9.9", "10", ApiEndpoint.PRODUCTION)
        assertNull(attributes["distinct_id"])
        assertEquals("9.9.9", attributes["session_app_release_version"])
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
        val attributes = ExperimentUtils.attributes(experimentData, "9.9.9", "9", ApiEndpoint.STAGING)
        assertNotNull(attributes["distinct_id"])
        assertEquals("9.9.9", attributes["session_app_release_version"])
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
        val attributes = ExperimentUtils.attributes(experimentData, "9.9.9", "9", ApiEndpoint.PRODUCTION)
        assertNull(attributes["distinct_id"])
        assertEquals("9.9.9", attributes["session_app_release_version"])
        assertEquals("Android 9", attributes["session_os_version"])
        assertEquals("discovery", attributes["session_ref_tag"])
        assertEquals("search", attributes["session_referrer_credit"])
        assertEquals(false, attributes["session_user_is_logged_in"])
        assertEquals(0, attributes["user_backed_projects_count"])
        assertEquals("US", attributes["user_country"])
    }

    @Test
    fun checkoutTags_USProject() {
        val experimentData = ExperimentData(UserFactory.user(), RefTag.discovery(), RefTag.search())
        val pledgeData = PledgeData.with(PledgeFlowContext.NEW_PLEDGE, ProjectDataFactory.project(ProjectFactory.project()), RewardFactory.reward())
        val checkoutData = CheckoutDataFactory.checkoutData(10.0, 20.0)
        val experimentRevenueData = ExperimentRevenueData(experimentData, checkoutData, pledgeData)
        val checkoutTags = ExperimentUtils.checkoutTags(experimentRevenueData)
        assertEquals(20.0, checkoutTags["checkout_amount"])
        assertEquals("CREDIT_CARD", checkoutTags["checkout_payment_type"])
        assertEquals(2000, checkoutTags["checkout_revenue_in_usd_cents"])
        assertEquals("USD", checkoutTags["currency"])
    }

    @Test
    fun checkoutTags_CAProject() {
        val experimentData = ExperimentData(UserFactory.user(), RefTag.discovery(), RefTag.search())
        val pledgeData = PledgeData.with(PledgeFlowContext.NEW_PLEDGE, ProjectDataFactory.project(ProjectFactory.caProject()), RewardFactory.reward())
        val checkoutData = CheckoutDataFactory.checkoutData(10.0, 20.0)
        val experimentRevenueData = ExperimentRevenueData(experimentData, checkoutData, pledgeData)
        val checkoutTags = ExperimentUtils.checkoutTags(experimentRevenueData)
        assertEquals(20.0, checkoutTags["checkout_amount"])
        assertEquals("CREDIT_CARD", checkoutTags["checkout_payment_type"])
        assertEquals(1500, checkoutTags["checkout_revenue_in_usd_cents"])
        assertEquals("CAD", checkoutTags["currency"])
    }
}
