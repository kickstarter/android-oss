package com.kickstarter.libs

import android.content.Context
import com.segment.analytics.Analytics
import com.segment.analytics.Properties

class SegmentTrackingClient(
        build: Build,
        context: Context,
        currentConfig: CurrentConfigType,
        currentUser: CurrentUserType,
        optimizely: ExperimentsClientType,
        private val segmentAnalytics: Analytics?) : TrackingClient(context, currentUser, build, currentConfig, optimizely) {

    /**
     * Perform the request to the Segment third party library
     * see https://segment.com/docs/connections/sources/catalog/libraries/mobile/android/#track
     */
    override fun trackingData(eventName: String, newProperties: Map<String, Any?>): String {
        segmentAnalytics?.let { segment ->
            segment.track(eventName, this.getProperties(newProperties))
        }
        return ""
    }

    /**
     * In order to send custom properties to segment we need to use
     * the method Properties() from the Segment SDK
     * see https://segment.com/docs/connections/sources/catalog/libraries/mobile/android/#track
     */
    private fun getProperties(newProperties: Map<String, Any?>) = Properties().apply {
        newProperties.forEach { (key, value) ->
            this[key] = value
        }
    }

    override fun type() = Type.SEGMENT
}