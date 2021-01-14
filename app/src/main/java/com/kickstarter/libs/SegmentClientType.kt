package com.kickstarter.libs

import android.util.Pair

interface SegmentClientType {

    /**
     * Facade method for Segment Analytics track method
     * https://segment.com/docs/connections/sources/catalog/libraries/mobile/android/#track
     */
    fun track(name: String = "", properties: Pair<String, Any>? = null)
}