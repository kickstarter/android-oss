package com.kickstarter.libs

import android.content.Context
import com.kickstarter.models.User
import com.segment.analytics.Analytics
import com.segment.analytics.Properties
import com.segment.analytics.Traits

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
    override fun trackingData(eventName: String, newProperties: Map<String, Any?>) {
        segmentAnalytics?.let { segment ->
            segment.track(eventName, this.getProperties(newProperties))
        }
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

    /**
     * Perform the request to the Segment third party library
     * see https://segment.com/docs/connections/sources/catalog/libraries/mobile/android/#identify
     */
    override fun identify(user: User) {
        super.identify(user)
        segmentAnalytics?.let { segment ->
            segment.identify(user.id().toString(), getTraits(user), null)
        }
    }

    /**
     * In order to send custom properties to segment for the Identify method we need to use
     * the method Traits() from the Segment SDK
     * see https://segment.com/docs/connections/sources/catalog/libraries/mobile/android/#identify
     */
    private fun getTraits(user: User) = Traits().apply {
        this.putName(user.name())
        this.putAvatar(user.avatar().toString())
    }
}