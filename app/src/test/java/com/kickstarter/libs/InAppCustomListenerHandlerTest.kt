package com.kickstarter.libs

import android.net.Uri
import com.braze.enums.inappmessage.ClickAction
import com.braze.enums.inappmessage.CropType
import com.braze.enums.inappmessage.DismissType
import com.braze.enums.inappmessage.InAppMessageFailureType
import com.braze.enums.inappmessage.MessageType
import com.braze.enums.inappmessage.Orientation
import com.braze.enums.inappmessage.TextAlign
import com.braze.models.inappmessage.IInAppMessage
import com.braze.ui.inappmessage.InAppMessageOperation
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.braze.InAppCustomListener
import com.kickstarter.libs.braze.InAppCustomListenerHandler
import com.kickstarter.mock.factories.UserFactory
import org.json.JSONObject
import org.junit.Test

class InAppCustomListenerHandlerTest : KSRobolectricTestCase() {

    lateinit var build: Build

    private val mockInAppMessage = object : IInAppMessage {
        override var animateIn: Boolean
            get() = TODO("Not yet implemented")
            set(value) {}
        override var animateOut: Boolean
            get() = TODO("Not yet implemented")
            set(value) {}
        override var backgroundColor: Int
            get() = TODO("Not yet implemented")
            set(value) {}
        override val clickAction: ClickAction
            get() = TODO("Not yet implemented")
        override var cropType: CropType
            get() = TODO("Not yet implemented")
            set(value) {}
        override var dismissType: DismissType
            get() = TODO("Not yet implemented")
            set(value) {}
        override var durationInMilliseconds: Int
            get() = TODO("Not yet implemented")
            set(value) {}
        override var expirationTimestamp: Long
            get() = TODO("Not yet implemented")
            set(value) {}
        override var extras: Map<String, String>
            get() = TODO("Not yet implemented")
            set(value) {}
        override var icon: String?
            get() = TODO("Not yet implemented")
            set(value) {}
        override var iconBackgroundColor: Int
            get() = TODO("Not yet implemented")
            set(value) {}
        override var iconColor: Int
            get() = TODO("Not yet implemented")
            set(value) {}
        override val isControl: Boolean
            get() = TODO("Not yet implemented")
        override var message: String?
            get() = TODO("Not yet implemented")
            set(value) {}
        override var messageTextAlign: TextAlign
            get() = TODO("Not yet implemented")
            set(value) {}
        override var messageTextColor: Int
            get() = TODO("Not yet implemented")
            set(value) {}
        override val messageType: MessageType
            get() = TODO("Not yet implemented")
        override var openUriInWebView: Boolean
            get() = TODO("Not yet implemented")
            set(value) {}
        override var orientation: Orientation
            get() = TODO("Not yet implemented")
            set(value) {}
        override val uri: Uri?
            get() = TODO("Not yet implemented")

        override fun forJsonPut(): JSONObject {
            TODO("Not yet implemented")
        }

        override fun getRemoteAssetPathsForPrefetch(): List<String> {
            TODO("Not yet implemented")
        }

        override fun logClick(): Boolean {
            TODO("Not yet implemented")
        }

        override fun logDisplayFailure(failureType: InAppMessageFailureType): Boolean {
            TODO("Not yet implemented")
        }

        override fun logImpression(): Boolean {
            TODO("Not yet implemented")
        }

        override fun onAfterClosed() {
            TODO("Not yet implemented")
        }

        override fun setClickBehavior(clickAction: ClickAction) {
            TODO("Not yet implemented")
        }

        override fun setClickBehavior(clickAction: ClickAction, uri: Uri?) {
            TODO("Not yet implemented")
        }

        override fun setLocalPrefetchedAssetPaths(remotePathToLocalAssetMap: Map<String, String>) {
            TODO("Not yet implemented")
        }
    }

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
        assertTrue(listener.beforeInAppMessageDisplayed(mockInAppMessage) == InAppMessageOperation.DISPLAY_NOW)
    }

    @Test
    fun testInAppCustomListener_Discard() {
        val mockUser = MockCurrentUser() // - no user logged in
        val listener = InAppCustomListener(mockUser, build)

        Thread.sleep(100) // wait a bit until InAppCustomListener.init block executed
        assertTrue(listener.beforeInAppMessageDisplayed(mockInAppMessage) == InAppMessageOperation.DISCARD)
    }
}
