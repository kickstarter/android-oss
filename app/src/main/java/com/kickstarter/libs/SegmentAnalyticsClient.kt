package com.kickstarter.libs

import com.kickstarter.models.User
import com.segment.analytics.Analytics
import com.segment.analytics.Properties
import org.json.JSONArray

class SegmentAnalyticsClient(private val segment: Analytics?) : TrackingClientType() {

    override fun track(eventName: String?, additionalProperties: MutableMap<String, Any>?) {
        if (eventName != null && segment != null) {
            val combined = additionalProperties?.let { props -> this.combinedProperties(props) }

            segment.track(eventName, additionalProperties.let {
                // TODO: Sending for now just the first of the combines properties
                Properties().putValue(combined?.entries?.first()?.key, combined?.entries?.first()?.value)
            })
        }
    }

    override fun type() = Type.SEGMENT

    override fun optimizely(): ExperimentsClientType {
        TODO("Not yet implemented")
    }

    override fun brand(): String {
        TODO("Not yet implemented")
    }

    override fun buildNumber(): Int {
        TODO("Not yet implemented")
    }

    override fun currentVariants(): JSONArray {
        TODO("Not yet implemented")
    }

    override fun deviceDistinctId(): String {
        TODO("Not yet implemented")
    }

    override fun deviceFormat(): String {
        TODO("Not yet implemented")
    }

    override fun deviceOrientation(): String {
        TODO("Not yet implemented")
    }

    override fun enabledFeatureFlags(): JSONArray {
        TODO("Not yet implemented")
    }

    override fun isGooglePlayServicesAvailable(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isTalkBackOn(): Boolean {
        TODO("Not yet implemented")
    }

    override fun loggedInUser(): User {
        TODO("Not yet implemented")
    }

    override fun manufacturer(): String {
        TODO("Not yet implemented")
    }

    override fun model(): String {
        TODO("Not yet implemented")
    }

    override fun OSVersion(): String {
        TODO("Not yet implemented")
    }

    override fun time(): Long {
        TODO("Not yet implemented")
    }

    override fun userAgent(): String {
        TODO("Not yet implemented")
    }

    override fun userCountry(user: User?): String {
        TODO("Not yet implemented")
    }

    override fun versionName(): String {
        TODO("Not yet implemented")
    }

    override fun wifiConnection(): Boolean {
        TODO("Not yet implemented")
    }
}