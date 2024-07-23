package com.kickstarter.libs

import android.content.SharedPreferences
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.models.User
import com.kickstarter.ui.SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE

interface FirebaseAnalyticsClientType {
    fun isEnabled(): Boolean

    fun trackEvent(eventName: String, parameters: Bundle)

    fun sendUserId(user: User)
}

open class FirebaseAnalyticsClient(
    private var ffClient: FeatureFlagClientType,
    private var preference: SharedPreferences,
    private val firebaseAnalytics: FirebaseAnalytics?,
) : FirebaseAnalyticsClientType {

    override fun isEnabled() = preference.getBoolean(CONSENT_MANAGEMENT_PREFERENCE, false) && ffClient.getBoolean(FlagKey.ANDROID_CONSENT_MANAGEMENT)

    override fun trackEvent(eventName: String, parameters: Bundle) {
        firebaseAnalytics?.let {
            if (isEnabled()) {
                firebaseAnalytics.logEvent(eventName, parameters)
            }
        }
    }

    override fun sendUserId(user: User) {
        firebaseAnalytics?.let {
            if (isEnabled()) {
                firebaseAnalytics.setUserId(user.id().toString())
            }
        }
    }
}
