package com.kickstarter.libs

import android.os.Bundle

class FirebaseAnalyticsEvents(private val firebaseAnalyticsClient: FirebaseAnalyticsClient) {

    fun track(eventName: String, parameters: Bundle) {
        firebaseAnalyticsClient.trackEvent(eventName, parameters)
    }
}