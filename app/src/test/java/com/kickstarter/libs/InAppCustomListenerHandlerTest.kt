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
        override fun forJsonPut(): JSONObject {
            TODO("Not yet implemented")
        }

        override fun getMessage(): String {
            TODO("Not yet implemented")
        }

        override fun getExtras(): MutableMap<String, String>? {
            TODO("Not yet implemented")
        }

        override fun setExtras(p0: MutableMap<String, String>?) {
            TODO("Not yet implemented")
        }

        override fun getDurationInMilliseconds(): Int {
            TODO("Not yet implemented")
        }

        override fun getBackgroundColor(): Int {
            TODO("Not yet implemented")
        }

        override fun getIconColor(): Int {
            TODO("Not yet implemented")
        }

        override fun getIconBackgroundColor(): Int {
            TODO("Not yet implemented")
        }

        override fun getMessageTextColor(): Int {
            TODO("Not yet implemented")
        }

        override fun getIcon(): String {
            TODO("Not yet implemented")
        }

        override fun getAnimateIn(): Boolean {
            TODO("Not yet implemented")
        }

        override fun getAnimateOut(): Boolean {
            TODO("Not yet implemented")
        }

        override fun getClickAction(): ClickAction {
            TODO("Not yet implemented")
        }

        override fun getUri(): Uri {
            TODO("Not yet implemented")
        }

        override fun getDismissType(): DismissType {
            TODO("Not yet implemented")
        }

        override fun getRemoteAssetPathsForPrefetch(): MutableList<String> {
            TODO("Not yet implemented")
        }

        override fun getOrientation(): Orientation {
            TODO("Not yet implemented")
        }

        override fun getCropType(): CropType {
            TODO("Not yet implemented")
        }

        override fun getMessageTextAlign(): TextAlign {
            TODO("Not yet implemented")
        }

        override fun getExpirationTimestamp(): Long {
            TODO("Not yet implemented")
        }

        override fun getOpenUriInWebView(): Boolean {
            TODO("Not yet implemented")
        }

        override fun getMessageType(): MessageType {
            TODO("Not yet implemented")
        }

        override fun setOpenUriInWebView(p0: Boolean) {
            TODO("Not yet implemented")
        }

        override fun setExpirationTimestamp(p0: Long) {
            TODO("Not yet implemented")
        }

        override fun setMessageTextAlign(p0: TextAlign?) {
            TODO("Not yet implemented")
        }

        override fun setCropType(p0: CropType?) {
            TODO("Not yet implemented")
        }

        override fun setOrientation(p0: Orientation?) {
            TODO("Not yet implemented")
        }

        override fun setMessage(p0: String?) {
            TODO("Not yet implemented")
        }

        override fun setAnimateIn(p0: Boolean) {
            TODO("Not yet implemented")
        }

        override fun setAnimateOut(p0: Boolean) {
            TODO("Not yet implemented")
        }

        override fun setClickAction(p0: ClickAction?): Boolean {
            TODO("Not yet implemented")
        }

        override fun setClickAction(p0: ClickAction?, p1: Uri?): Boolean {
            TODO("Not yet implemented")
        }

        override fun setDismissType(p0: DismissType?) {
            TODO("Not yet implemented")
        }

        override fun setDurationInMilliseconds(p0: Int) {
            TODO("Not yet implemented")
        }

        override fun setBackgroundColor(p0: Int) {
            TODO("Not yet implemented")
        }

        override fun setIconBackgroundColor(p0: Int) {
            TODO("Not yet implemented")
        }

        override fun setIconColor(p0: Int) {
            TODO("Not yet implemented")
        }

        override fun setMessageTextColor(p0: Int) {
            TODO("Not yet implemented")
        }

        override fun setIcon(p0: String?) {
            TODO("Not yet implemented")
        }

        override fun setLocalPrefetchedAssetPaths(p0: MutableMap<String, String>) {
            TODO("Not yet implemented")
        }

        override fun getLocalPrefetchedAssetPaths(): MutableMap<String, String> {
            TODO("Not yet implemented")
        }

        override fun logImpression(): Boolean {
            TODO("Not yet implemented")
        }

        override fun logClick(): Boolean {
            TODO("Not yet implemented")
        }

        override fun logDisplayFailure(p0: InAppMessageFailureType?): Boolean {
            TODO("Not yet implemented")
        }

        override fun onAfterClosed() {
            TODO("Not yet implemented")
        }

        override fun isControl(): Boolean {
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
