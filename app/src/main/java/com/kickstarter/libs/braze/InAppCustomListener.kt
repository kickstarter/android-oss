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

    init {
        Timber.d("Init block custom listener")
    }

    override fun beforeInAppMessageDisplayed(inAppMessage: IInAppMessage?): InAppMessageOperation {
        Timber.d("Display Always")
        return InAppMessageOperation.DISPLAY_NOW
    }

    override fun onInAppMessageButtonClicked(inAppMessage: IInAppMessage?, button: MessageButton?, inAppMessageCloser: InAppMessageCloser?): Boolean {
        Timber.d("Button Clicked")
        return super.onInAppMessageButtonClicked(inAppMessage, button, inAppMessageCloser)
    }
}
