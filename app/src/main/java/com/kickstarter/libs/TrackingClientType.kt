package com.kickstarter.libs

import com.kickstarter.libs.utils.KoalaUtils
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

    // TODO: Will add method Screen those two are specifics to Segment, the implementation on Lake will be empty
    abstract fun track(eventName: String, additionalProperties: Map<String, Any>)
    // - Specific to segment
    abstract fun identify()

    fun track(eventName: String) {
        track(eventName, HashMap())
    }

    private fun lakeProperties(): Map<String, Any> {
        val hashMap = hashMapOf<String, Any>()
        loggedInUser()?.let {
            hashMap.putAll(KoalaUtils.userProperties(it))
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
            this["client_platform"] = "android"
            this["client_type"] = "native"
            this["current_variants"] = currentVariants() ?: ""
            this["device_distinct_id"] = deviceDistinctId()
            this["device_format"] = deviceFormat()
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

    private fun koalaProperties(): Map<String, Any> {
        val properties = hashMapOf<String, Any>()

        loggedInUser()?.let {
            properties.putAll(KoalaUtils.userProperties(it))
            properties["user_logged_in"] = true
        }

        properties.apply {
            this["app_version"] = versionName()
            this["brand"] = brand()
            this["client_platform"] = "android"
            this["client_type"] = "native"
            this["device_fingerprint"] = deviceDistinctId()
            this["device_format"] = deviceFormat()
            this["device_orientation"] = deviceOrientation()
            this["distinct_id"] = deviceDistinctId()
            this["enabled_feature_flags"] = enabledFeatureFlags()
            this["google_play_services"] = if (isGooglePlayServicesAvailable) "available" else "unavailable"
            this["is_vo_on"] = isTalkBackOn
            this["koala_lib"] = "kickstarter_android"
            this["manufacturer"] = manufacturer()
            this["model"] = model()
            this["mp_lib"] = "android"
            this["os"] = "Android"
            this["os_version"] = OSVersion()
            this["time"] = time()
        }

        return properties
    }

    fun combinedProperties(additionalProperties: Map<String, Any>): Map<String, Any> {
        val combinedProperties = HashMap(additionalProperties)
        if (type() == Type.LAKE || type() == Type.SEGMENT) {
            combinedProperties.putAll(lakeProperties())
        }
        return combinedProperties
    }
}