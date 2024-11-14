package com.kickstarter.models

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.mock.factories.UserFactory
import org.junit.Test

class EnvironmentTest : KSRobolectricTestCase() {

    @Test
    fun testDefaultInit() {
        val environment = environment()

        assertNotNull(environment.activitySamplePreference())
        assertNotNull(environment.build())
        assertNotNull(environment.cookieManager())
        assertNotNull(environment.currentUserV2())
        assertNotNull(environment.firstSessionPreference())
        assertNotNull(environment.gson())
        assertNotNull(environment.hasSeenAppRatingPreference())
        assertNotNull(environment.hasSeenGamesNewsletterPreference())
        assertNotNull(environment.internalTools())
        assertNotNull(environment.ksCurrency())
        assertNotNull(environment.ksString())
        assertNotNull(environment.analytics())
        assertNotNull(environment.attributionEvents())
        assertNotNull(environment.logout())
        assertNotNull(environment.playServicesCapability())
        assertNotNull(environment.schedulerV2())
        assertNotNull(environment.sharedPreferences())
        assertNotNull(environment.stripe())
        assertNotNull(environment.webEndpoint())
    }

    @Test
    fun testToBuilderInit() {
        val collaborator = MockCurrentUserV2(UserFactory.collaborator())
        val environment = environment().toBuilder().currentUserV2(collaborator).build()
        assertEquals(environment.currentUserV2(), collaborator)
    }
}
