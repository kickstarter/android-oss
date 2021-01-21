package com.kickstarter.libs

import android.content.Context
import com.kickstarter.libs.qualifiers.ApplicationContext
import com.segment.analytics.Analytics
import com.segment.analytics.Properties

class SegmentAnalyticsClient(@param:ApplicationContext private val context: Context,
                             currentUser: CurrentUserType,
                             build: Build,
                             currentConfig: CurrentConfigType,
                             optimizely: ExperimentsClientType,
                             private val segmentAnalytics: Analytics?) : TrackingClient(context, currentUser, build, currentConfig, optimizely, segmentAnalytics)  {

    /**
     * Perform the request to the Segment third party library
     */
    override fun trackingData(eventName: String, newProperties: Map<String, Any?>): String {
        segmentAnalytics?.let { segment ->
            segment.track(eventName, this.getProperties(newProperties))
        }
        return "";
    }

    private fun getProperties(newProperties: Map<String, Any?>) = Properties().apply {
        newProperties.forEach { (key, value) ->
            this[key] = value
        }
    }


    override fun type() = Type.SEGMENT
}