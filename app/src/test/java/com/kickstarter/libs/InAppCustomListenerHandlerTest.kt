package com.kickstarter.libs

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.braze.InAppCustomListenerHandler
import com.kickstarter.libs.utils.ConfigFeatureName
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.UserFactory
import org.junit.Test

class InAppCustomListenerHandlerTest : KSRobolectricTestCase() {

    @Test
    fun testInitialize_MessageShouldShow_True() {
        val user = UserFactory.user()
        val mockUser = MockCurrentUser(user)
        val mockConfig = MockCurrentConfig().apply {
            val config = ConfigFactory.configWithFeatureEnabled(ConfigFeatureName.BRAZE_ENABLED.configFeatureName)
            config(config)
        }
        val handler = InAppCustomListenerHandler(mockUser, mockConfig)
        assertTrue(handler.shouldShowMessage())
    }

    @Test
    fun testInitialize_MessageShouldShow_False() {
        val mockUser = MockCurrentUser()
        val mockConfig = MockCurrentConfig().apply {
            val config = ConfigFactory.configWithFeatureDisabled(ConfigFeatureName.BRAZE_ENABLED.configFeatureName)
            config(config)
        }
        val handler = InAppCustomListenerHandler(mockUser, mockConfig)
        assertFalse(handler.shouldShowMessage())
    }
}
