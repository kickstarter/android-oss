package com.kickstarter.libs

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.ui.SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE

open class FirebaseAnalyticsTrackingClient(
    context: Context,
    optimizely: ExperimentsClientType,
    preference: SharedPreferences,
) {
    private var userConsent = false
    private var firebaseAnalytics: FirebaseAnalytics = Firebase.analytics
    private var optimizely = optimizely

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, _ ->
        userConsent = sharedPreferences.getBoolean(CONSENT_MANAGEMENT_PREFERENCE, false)
    }

    init {
        preference.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    fun trackEvent(eventName: String, newProperties: Map<String, Any?>) {
        if(userConsent && optimizely.isFeatureEnabled(OptimizelyFeature.Key.ANDROID_CONSENT_MANAGEMENT)) {
            firebaseAnalytics.logEvent(eventName) {
                newProperties.forEach { (key, value) ->
                    param(key, value)
                }
            }
        }
    }

//    private fun getProperties(newProperties: Map<String, Any?>);
//        bundleOf(newProperties)
//        Bundle().apply {
//        newProperties.forEach{(key, value) ->
//            this.putString(key, value)
//        }
//
//    }
}