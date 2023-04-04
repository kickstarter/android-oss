package com.kickstarter.libs.featureflag

import android.app.Activity
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.kickstarter.libs.Build
import com.kickstarter.libs.Build.isInternal
import timber.log.Timber

interface FeatureFlagClientType {
    /**
     * Indicates if the FeatureFlag client is currently enabled,
     * meaning it has been initialized properly, and fetched already some data from the backend.
     */
    var isEnabled: Boolean

    /**
     * Indicates if the FeatureFlag client has currently activated the latest values fetched from the backend
     */
    var isActive: Boolean

    /**
     * Will received a callback, that callback will usually
     * initialize the external library
     */
    fun initialize()

    /**
     * Will connect to the backend and fetch available values
     */
    fun fetch(context: Activity)

    /**
     * Will activate the latest values fetched from the backend
     */
    fun activate(context: Activity)

    /**
     * Will perform fetch and activate at the same time
     */
    fun fetchAndActivate(context: Activity)

    /**
     * Will return the active status for a boolean feature flag
     */
    fun getBoolean(FFKey: FFKey): Boolean

    /**
     * Will return the active value for a Double feature flag
     */
    fun getDouble(FFKey: FFKey): Double

    /**
     * Will return the active value for a Long feature flag
     */
    fun getLong(FFKey: FFKey): Long

    /**
     * Will return the active value for a String feature flag
     */
    fun getString(FFKey: FFKey): String
}

enum class FFKey(val key: String) {
    ANDROID_FACEBOOK_LOGIN_REMOVE("android_facebook_login_remove"),
    ANDROID_HIDE_APP_RATING_DIALOG("android_hide_app_rating_dialog"),
    ANDROID_CONSENT_MANAGEMENT("android_consent_management"),
    ANDROID_CAPI_INTEGRATION("android_capi_integration"),
    ANDROID_GOOGLE_ANALYTICS("android_google_analytics"),
    ANDROID_PRE_LAUNCH_SCREEN("android_pre_launch_screen"),
}

fun FeatureFlagClient.getFetchInterval(): Long =
    if (this.build.isDebug || isInternal()) 0
    else 3600

class FeatureFlagClient(internal val build: Build) : FeatureFlagClientType {
    override var isEnabled: Boolean = false
    override var isActive: Boolean = false

    override fun initialize() {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = getFetchInterval()
        }

        // - For the MVP no in-app defaults, will add them later on
        Firebase.remoteConfig.setConfigSettingsAsync(configSettings)

        if (build.isDebug) {
            Timber.d("${this.javaClass} initialized with interval: ${this.getFetchInterval()}")
        }
    }

    override fun fetch(context: Activity) {
        Firebase.remoteConfig.fetch()
            .addOnCompleteListener(context) { task ->
                isEnabled = task.isSuccessful
                if (build.isDebug) {
                    Timber.d("${this.javaClass} fetch: $isEnabled")
                }
            }
    }

    override fun activate(context: Activity) {
        Firebase.remoteConfig.activate()
            .addOnCompleteListener(context) { task ->
                isActive = task.isSuccessful
                if (build.isDebug) {
                    Timber.d("${this.javaClass} activated: $isActive")
                }
            }
    }

    override fun fetchAndActivate(context: Activity) {
        Firebase.remoteConfig.fetchAndActivate()
            .addOnCompleteListener(context) { task ->
                isEnabled = task.isSuccessful
                isActive = task.isSuccessful

                if (build.isDebug) {
                    Timber.d("${this.javaClass} fetchAndActivated: $isEnabled $isActive")
                }
            }
    }

    private fun log(key: FFKey, value: Any) =
        if (build.isDebug) Timber.d("${this.javaClass} feature flag ${key.key}: $value")
        else {}

    override fun getBoolean(key: FFKey): Boolean =
        if (isActive && isEnabled) {
            val value = Firebase.remoteConfig.getBoolean(key.key)
            log(key, value)
            value
        } else false

    override fun getDouble(key: FFKey): Double =
        if (isActive && isEnabled) {
            val value = Firebase.remoteConfig.getDouble(key.key)
            log(key, value)
            value
        } else 0.0

    override fun getLong(key: FFKey): Long =
        if (isActive && isEnabled) {
            val value = Firebase.remoteConfig.getLong(key.key)
            log(key, value)
            value
        } else 0L

    override fun getString(key: FFKey): String =
        if (isActive && isEnabled) {
            val value = Firebase.remoteConfig.getString(key.key)
            log(key, value)
            value
        } else ""
}
