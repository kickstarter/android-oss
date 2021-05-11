package com.kickstarter.libs

import com.appboy.ui.inappmessage.InAppMessageOperation
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.braze.InAppCustomListener
import com.kickstarter.libs.braze.InAppCustomListenerHandler
import com.kickstarter.libs.utils.ConfigFeatureName
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.UserFactory
import org.junit.Test
import org.mockito.Mockito

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

    @Test
    fun testInAppCustomListener_DisplayNow() {
        val build = Mockito.mock(Build::class.java)
        Mockito.`when`(build.isDebug).thenReturn(true)

        val user = UserFactory.user()
        val mockUser = MockCurrentUser(user)
        val mockConfig = MockCurrentConfig().apply {
            val config = ConfigFactory.configWithFeatureEnabled(ConfigFeatureName.BRAZE_ENABLED.configFeatureName)
            config(config)
        }
        val listener = InAppCustomListener(mockUser, mockConfig, build)

        assertTrue(listener.beforeInAppMessageDisplayed(null) == InAppMessageOperation.DISPLAY_NOW)
    }

    @Test
    fun testInAppCustomListener_Discard() {
        val build = Mockito.mock(Build::class.java)
        Mockito.`when`(build.isDebug).thenReturn(true)

        val user = UserFactory.user()
        val mockUser = MockCurrentUser(user)
        val mockConfig = MockCurrentConfig().apply {
            val config = ConfigFactory.configWithFeatureDisabled(ConfigFeatureName.BRAZE_ENABLED.configFeatureName)
            config(config)
        }
        val listener = InAppCustomListener(mockUser, mockConfig, build)

        assertTrue(listener.beforeInAppMessageDisplayed(null) == InAppMessageOperation.DISCARD)
    }
}
