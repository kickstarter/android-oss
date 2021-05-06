package com.kickstarter.libs.braze

import com.appboy.models.IInAppMessage
import com.appboy.models.MessageButton
import com.appboy.ui.inappmessage.InAppMessageCloser
import com.appboy.ui.inappmessage.InAppMessageOperation
import com.appboy.ui.inappmessage.listeners.AppboyDefaultInAppMessageManagerListener
import com.kickstarter.libs.Build
import com.kickstarter.libs.Config
import com.kickstarter.models.User
import timber.log.Timber

class InAppCustomListener(
    private val loggedInUser: User?,
    private val config: Config?,
    private val build: Build
) : AppboyDefaultInAppMessageManagerListener() {

    // TODO: deeplink with this on on button clicked
    //  url staging: https://staging.kickstarter.com/settings/notify_mobile_of_marketing_update/true
    //  url production: https://www.kickstarter.com/settings/notify_mobile_of_marketing_update/true

    private val handler = InAppCustomListenerHandler(this.loggedInUser, this.config)

    init {
        if (build.isDebug) Timber.d("${this.javaClass.canonicalName} Init block custom listener")
    }

    override fun beforeInAppMessageDisplayed(inAppMessage: IInAppMessage?): InAppMessageOperation {
        if (build.isDebug) Timber.d("${this.javaClass.canonicalName} beforeInAppMessageDisplayed: ${inAppMessage?.toString()}")

        val shouldShowMessage = if (handler.shouldShowMessage())
            InAppMessageOperation.DISPLAY_NOW
        else InAppMessageOperation.DISCARD

        if (build.isDebug) Timber.d("${this.javaClass.canonicalName} beforeInAppMessageDisplayed: $shouldShowMessage")
        return shouldShowMessage
    }

    override fun onInAppMessageButtonClicked(inAppMessage: IInAppMessage?, button: MessageButton?, inAppMessageCloser: InAppMessageCloser?): Boolean {
        if (build.isDebug) Timber.d("${this.javaClass.canonicalName} onInAppMessageButtonClicked: Button Clicked")
        button?.let {
            // - Button ID plus the url when it's configured with deeplink on braze
            handler.validateDataWith(it.id, it.uri)
        }
        return super.onInAppMessageButtonClicked(inAppMessage, button, inAppMessageCloser)
    }
}
