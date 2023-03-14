package com.kickstarter.libs

import android.content.SharedPreferences
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.ui.SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE

open class FirebaseAnalyticsClient(
    private var optimizely: ExperimentsClientType,
    preference: SharedPreferences,
) {
    private var userConsent = false
    private var firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, _ ->
        this.userConsent = sharedPreferences.getBoolean(CONSENT_MANAGEMENT_PREFERENCE, false)
    }

    init {
        preference.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    open fun trackEvent(eventName: String, parameters: Bundle) {
        if(userConsent && optimizely.isFeatureEnabled(OptimizelyFeature.Key.ANDROID_CONSENT_MANAGEMENT)) {
            firebaseAnalytics.logEvent(eventName, parameters)
        }
    }
}