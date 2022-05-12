package com.kickstarter.libs

import com.braze.ui.inappmessage.InAppMessageOperation
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.braze.InAppCustomListener
import com.kickstarter.libs.braze.InAppCustomListenerHandler
import com.kickstarter.mock.factories.UserFactory
import org.junit.Test

class InAppCustomListenerHandlerTest : KSRobolectricTestCase() {

    lateinit var build: Build

    override fun setUp() {
        super.setUp()
        build = requireNotNull(environment().build())
    }

    @Test
    fun testMessageShouldShow_True() {
        val user = UserFactory.user()
        val mockUser = MockCurrentUser(user)
        val handler = InAppCustomListenerHandler(mockUser)

        Thread.sleep(100) // wait a bit until InAppCustomListenerHandler.init block executed
        assertTrue(handler.shouldShowMessage())
    }

    @Test
    fun testMessageShouldShow_False() {
        val mockUser = MockCurrentUser() // - no user logged in
        val handler = InAppCustomListenerHandler(mockUser)

        Thread.sleep(100) // wait a bit until InAppCustomListenerHandler.init block executed
        assertFalse(handler.shouldShowMessage())
    }

    @Test
    fun testInAppCustomListener_DisplayNow() {
        val user = UserFactory.user()
        val mockUser = MockCurrentUser(user)
        val listener = InAppCustomListener(mockUser, build)

        Thread.sleep(100) // wait a bit until InAppCustomListener.init block executed
        assertTrue(listener.beforeInAppMessageDisplayed(null) == InAppMessageOperation.DISPLAY_NOW)
    }

    @Test
    fun testInAppCustomListener_Discard() {
        val mockUser = MockCurrentUser() // - no user logged in
        val listener = InAppCustomListener(mockUser, build)

        Thread.sleep(100) // wait a bit until InAppCustomListener.init block executed
        assertTrue(listener.beforeInAppMessageDisplayed(null) == InAppMessageOperation.DISCARD)
    }
}
