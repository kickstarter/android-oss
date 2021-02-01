package com.kickstarter.libs

import android.content.Context
import com.kickstarter.libs.utils.ObjectUtils
import com.segment.analytics.Analytics
import com.segment.analytics.Properties
import com.segment.analytics.Traits
import rx.Observable

class SegmentTrackingClient(
        build: Build,
        context: Context,
        currentConfig: CurrentConfigType,
        currentUser: CurrentUserType,
        optimizely: ExperimentsClientType,
        private val segmentAnalytics: Analytics?) : TrackingClient(context, currentUser, build, currentConfig, optimizely) {

    init {

        Observable
                .combineLatest(this.currentUser.isLoggedIn, this.currentUser.observable() ) { isLoggedIn, currentUser ->
                    if (isLoggedIn) return@combineLatest currentUser
                    else return@combineLatest null
                }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }
                .distinctUntilChanged()
                .subscribe { user ->
                    val userId = user.id().toString()
                    this.identify(userId, null)
                }
    }

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

    /**
     * Perform the request to the Segment third party library
     * see https://segment.com/docs/connections/sources/catalog/libraries/mobile/android/#identify
     */
    override fun identify(userId: String, additionalTraits: Map<String, String>?) {
        super.identify(userId, additionalTraits)

        val traits = additionalTraits?.let { this.getTraits(it) }
        segmentAnalytics?.let { segment ->
            traits?.let { traits ->
                segment.identify(userId, traits, null)
            } ?: segment.identify(userId)
        }
    }

    private fun getTraits(additionalTraits: Map<String, String>) = Traits().apply {
        this.putName(additionalTraits["name"])
    }
}