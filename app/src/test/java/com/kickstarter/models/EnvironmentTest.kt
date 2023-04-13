package com.kickstarter.models

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.mock.factories.UserFactory
import org.junit.Test

class EnvironmentTest : KSRobolectricTestCase() {

    @Test
    fun testDefaultInit() {
        val environment = environment()

        assertNotNull(environment.activitySamplePreference())
        assertNotNull(environment.apiClient())
        assertNotNull(environment.apolloClient())
        assertNotNull(environment.build())
        assertNotNull(environment.buildCheck())
        assertNotNull(environment.cookieManager())
        assertNotNull(environment.currentConfig())
        assertNotNull(environment.currentUser())
        assertNotNull(environment.firstSessionPreference())
        assertNotNull(environment.gson())
        assertNotNull(environment.hasSeenAppRatingPreference())
        assertNotNull(environment.hasSeenGamesNewsletterPreference())
        assertNotNull(environment.internalTools())
        assertNotNull(environment.ksCurrency())
        assertNotNull(environment.ksString())
        assertNotNull(environment.analytics())
        assertNotNull(environment.logout())
        assertNotNull(environment.playServicesCapability())
        assertNotNull(environment.scheduler())
        assertNotNull(environment.sharedPreferences())
        assertNotNull(environment.stripe())
        assertNotNull(environment.webClient())
        assertNotNull(environment.webEndpoint())
    }

    @Test
    fun testToBuilderInit() {
        val collaborator = MockCurrentUser(UserFactory.collaborator())
        val environment = environment().toBuilder().currentUser(collaborator).build()
        assertEquals(environment.currentUser(), collaborator)
    }
}
