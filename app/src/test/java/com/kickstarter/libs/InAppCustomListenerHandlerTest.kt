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

class InAppCustomListenerHandlerTest : KSRobolectricTestCase() {

    lateinit var build: Build

    override fun setUp() {
        super.setUp()
        build = environment().build()
    }

    @Test
    fun testMessageShouldShow_True() {
        val user = UserFactory.user()
        val mockUser = MockCurrentUser(user)
        val mockConfig = MockCurrentConfig().apply {
            val config = ConfigFactory.configWithFeatureEnabled(ConfigFeatureName.BRAZE_ENABLED.configFeatureName)
            config(config)
        }
        val handler = InAppCustomListenerHandler(mockUser, mockConfig)

        Thread.sleep(100) // wait a bit until InAppCustomListenerHandler.init block executed
        assertTrue(handler.shouldShowMessage())
    }

    @Test
    fun testMessageShouldShow_False() {
        val mockUser = MockCurrentUser()
        val mockConfig = MockCurrentConfig().apply {
            val config = ConfigFactory.configWithFeatureDisabled(ConfigFeatureName.BRAZE_ENABLED.configFeatureName)
            config(config)
        }
        val handler = InAppCustomListenerHandler(mockUser, mockConfig)

        Thread.sleep(100) // wait a bit until InAppCustomListenerHandler.init block executed
        assertFalse(handler.shouldShowMessage())
    }

    @Test
    fun testInAppCustomListener_DisplayNow() {
        val user = UserFactory.user()
        val mockUser = MockCurrentUser(user)
        val mockConfig = MockCurrentConfig().apply {
            val config = ConfigFactory.configWithFeatureEnabled(ConfigFeatureName.BRAZE_ENABLED.configFeatureName)
            config(config)
        }
        val listener = InAppCustomListener(mockUser, mockConfig, build)

        Thread.sleep(100) // wait a bit until InAppCustomListener.init block executed
        assertTrue(listener.beforeInAppMessageDisplayed(null) == InAppMessageOperation.DISPLAY_NOW)
    }

    @Test
    fun testInAppCustomListener_Discard() {
        val user = UserFactory.user()
        val mockUser = MockCurrentUser(user)
        val mockConfig = MockCurrentConfig().apply {
            val config = ConfigFactory.configWithFeatureDisabled(ConfigFeatureName.BRAZE_ENABLED.configFeatureName)
            config(config)
        }
        val listener = InAppCustomListener(mockUser, mockConfig, build)

        Thread.sleep(100) // wait a bit until InAppCustomListener.init block executed
        assertTrue(listener.beforeInAppMessageDisplayed(null) == InAppMessageOperation.DISCARD)
    }
}
