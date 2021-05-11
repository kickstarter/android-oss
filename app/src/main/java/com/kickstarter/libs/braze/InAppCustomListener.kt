package com.kickstarter.libs.braze

import com.appboy.models.IInAppMessage
import com.appboy.ui.inappmessage.InAppMessageOperation
import com.appboy.ui.inappmessage.listeners.AppboyDefaultInAppMessageManagerListener
import com.kickstarter.libs.Build
import com.kickstarter.libs.CurrentConfigType
import com.kickstarter.libs.CurrentUserType
import timber.log.Timber

class InAppCustomListener(
    private val loggedInUser: CurrentUserType,
    private val config: CurrentConfigType,
    private val build: Build
) : AppboyDefaultInAppMessageManagerListener() {

    private var handler: InAppCustomListenerHandler

    init {
        if (build.isDebug) Timber.d("${this.javaClass.canonicalName} Init block custom listener")
        handler = InAppCustomListenerHandler(loggedInUser, config)
    }

    override fun beforeInAppMessageDisplayed(inAppMessage: IInAppMessage?): InAppMessageOperation {
        if (build.isDebug) Timber.d("${this.javaClass.canonicalName} beforeInAppMessageDisplayed: ${inAppMessage?.toString()}")

        val shouldShowMessage = if (handler.shouldShowMessage())
            InAppMessageOperation.DISPLAY_NOW
        else InAppMessageOperation.DISCARD

        if (build.isDebug) Timber.d("${this.javaClass.canonicalName} beforeInAppMessageDisplayed: $shouldShowMessage")
        return shouldShowMessage
    }
}
