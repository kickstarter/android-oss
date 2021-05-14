package com.kickstarter.libs.braze

import com.appboy.models.IInAppMessage
import com.appboy.ui.inappmessage.InAppMessageOperation
import com.appboy.ui.inappmessage.listeners.AppboyDefaultInAppMessageManagerListener
import com.kickstarter.libs.Build
import com.kickstarter.libs.CurrentConfigType
import com.kickstarter.libs.CurrentUserType
import timber.log.Timber

/**
 * Listener for the Braze InAppMessages
 * All business logic will be delegated to the `InAppCustomListenerHandler`
 * this class is meant to override the necessary methods for InAppMessages
 * for now we just need `beforeInAppMessageDisplayed`.
 */
class InAppCustomListener(
    loggedInUser: CurrentUserType,
    config: CurrentConfigType,
    private val build: Build
) : AppboyDefaultInAppMessageManagerListener() {

    private var handler: InAppCustomListenerHandler

    init {
        if (build.isDebug) Timber.d("${this.javaClass.canonicalName} Init block custom listener")
        handler = InAppCustomListenerHandler(loggedInUser, config)
    }

    /**
     * Callback method call everytime the app receives and InAppMessage from Braze before displaying it on the screen:
     * In case the user is logged in, and the
     * feature flag is active
     * @return InAppMessageOperation.DISPLAY_NOW
     *
     * In case no user logged in or the feature flag not active
     * feature
     * @return InAppMessageOperation.DISCARD
     */
    override fun beforeInAppMessageDisplayed(inAppMessage: IInAppMessage?): InAppMessageOperation {
        if (build.isDebug) Timber.d("${this.javaClass.canonicalName} beforeInAppMessageDisplayed: ${inAppMessage?.toString()}")

        val shouldShowMessage = if (handler.shouldShowMessage())
            InAppMessageOperation.DISPLAY_NOW
        else InAppMessageOperation.DISCARD

        if (build.isDebug) Timber.d("${this.javaClass.canonicalName} beforeInAppMessageDisplayed: $shouldShowMessage")
        return shouldShowMessage
    }
}
