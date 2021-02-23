package com.kickstarter.libs

import com.kickstarter.libs.utils.AnalyticEventsUtils.userProperties
import com.kickstarter.libs.utils.MapUtils
import com.kickstarter.models.User
import org.json.JSONArray
import java.util.*

abstract class TrackingClientType {
    enum class Type(val tag: String) {
        LAKE("💧 Lake"),
        SEGMENT("\uD83C\uDF81 Segment");
    }

    protected abstract val isGooglePlayServicesAvailable: Boolean
    protected abstract val isTalkBackOn: Boolean

    abstract fun optimizely(): ExperimentsClientType?
    abstract fun loggedInUser(): User?

    protected abstract fun brand(): String
    protected abstract fun buildNumber(): Int
    protected abstract fun currentVariants(): JSONArray?
    protected abstract fun deviceDistinctId(): String
    protected abstract fun deviceFormat(): String
    protected abstract fun deviceOrientation(): String
    protected abstract fun enabledFeatureFlags(): JSONArray
    protected abstract fun manufacturer(): String
    protected abstract fun model(): String
    protected abstract fun OSVersion(): String
    protected abstract fun time(): Long
    abstract fun type(): Type
    protected abstract fun userAgent(): String?
    protected abstract fun userCountry(user: User): String
    protected abstract fun versionName(): String
    protected abstract fun wifiConnection(): Boolean

    abstract fun track(eventName: String, additionalProperties: Map<String, Any>)
    abstract fun identify(u: User)
    abstract fun reset()

    /**
     * Will determine if a concrete TrackingClient
     * is enabled to send data
     */
    abstract fun isEnabled(): Boolean

    fun track(eventName: String) {
        track(eventName, HashMap())
    }

    private fun genericProperties(): Map<String, Any> {
        val hashMap = hashMapOf<String, Any>()
        loggedInUser()?.let {
            hashMap.putAll(userProperties(it))
            hashMap["user_country"] = userCountry(it)
        }
        hashMap.putAll(sessionProperties(loggedInUser() != null))
        hashMap.putAll(contextProperties())
        return hashMap
    }

    private fun contextProperties(): Map<String, Any> {
        val properties = hashMapOf<String, Any>()
        properties["timestamp"] = time()
        return MapUtils.prefixKeys(properties, "context_")
    }

    private fun sessionProperties(userIsLoggedIn: Boolean): Map<String, Any> {
        val properties = hashMapOf<String, Any>()
        properties.apply {
            this["app_build_number"] = buildNumber()
            this["app_release_version"] = versionName()
            this["platform"] = "android"
            this["client"] = "native"
            this["current_variants"] = currentVariants() ?: ""
            this["device_distinct_id"] = deviceDistinctId()
            this["device_type"] = deviceFormat()
            this["device_manufacturer"] = manufacturer()
            this["device_model"] = model()
            this["device_orientation"] = deviceOrientation()
            this["display_language"] = Locale.getDefault().language
            this["enabled_features"] = enabledFeatureFlags()
            this["is_voiceover_running"] = isTalkBackOn
            this["mp_lib"] = "kickstarter_android"
            this["os"] = "Android"
            this["os_version"] = OSVersion()
            this["user_agent"] = userAgent() ?: ""
            this["user_is_logged_in"] = userIsLoggedIn
            this["wifi_connection"] = wifiConnection()
        }

        return MapUtils.prefixKeys(properties, "session_")
    }

    /**
     * We use the same properties for Segment and DataLake
     */
    fun combinedProperties(additionalProperties: Map<String, Any>): Map<String, Any> {
        return HashMap(additionalProperties).apply {
            putAll(genericProperties())
        }
    }
}