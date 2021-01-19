package com.kickstarter.libs

import android.content.Context
import com.kickstarter.libs.qualifiers.ApplicationContext
import com.segment.analytics.Analytics
import java.lang.RuntimeException

class SegmentAnalyticsClient(@param:ApplicationContext private val context: Context,
                             currentUser: CurrentUserType,
                             build: Build,
                             currentConfig: CurrentConfigType,
                             optimizely: ExperimentsClientType,
                             segmentAnalytics: Analytics?) : TrackingClient(context, currentUser, build, currentConfig, optimizely, segmentAnalytics)  {


    // - TrackingData method takes the properties specific for each client and joins them into and String
    // the segment analytics specification cannot override this behaviour from our previous clients
    @Throws(RuntimeException::class)
    override fun trackingData(eventName: String, newProperties: Map<String, Any?>): String {
        return "";
    }

    override fun type() = Type.SEGMENT
}