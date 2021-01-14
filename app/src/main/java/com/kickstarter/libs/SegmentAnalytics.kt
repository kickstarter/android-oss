package com.kickstarter.libs

import android.util.Pair
import com.segment.analytics.Analytics
import com.segment.analytics.Properties

class SegmentAnalytics(private val segment: Analytics) : SegmentClientType {

    override fun track(name: String, properties: Pair<String, Any>?) {
        segment.track(name, properties?.let { Properties().putValue(it.first, it.second) })
    }
}