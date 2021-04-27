package com.kickstarter.libs

import android.content.Context
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.extensions.isKSApplication
import com.kickstarter.models.User
import com.kickstarter.models.extensions.NAME
import com.kickstarter.models.extensions.getTraits
import com.segment.analytics.Analytics
import com.segment.analytics.Properties
import com.segment.analytics.Traits
import com.segment.analytics.android.integrations.appboy.AppboyIntegration
import timber.log.Timber

class SegmentTrackingClient(
    build: Build,
    private val context: Context,
    currentConfig: CurrentConfigType,
    currentUser: CurrentUserType,
    optimizely: ExperimentsClientType,
) : TrackingClient(context, currentUser, build, currentConfig, optimizely) {

    private var segmentClient: Analytics? = null
    override var isInitialized = false
    override var loggedInUser: User? = null
    override var config: Config? = null

    init {
        this.currentConfig.observable()
            .distinctUntilChanged()
            .subscribe {
                // - Check the feature flag active, and initialize Segment client
                this.config = it
                if (this.isEnabled() && !isInitialized) {
                    initialize()
                }
            }

        this.currentUser.observable()
                .distinctUntilChanged { prevUser, newUser ->
                    updateUserAndCheckTraits(prevUser, newUser)
                }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }
                .subscribe {
                    identify(it)
                }
    }

    /**
     * Takes the new user and the previous user emitted to the observable.
     * - updates the current logged in user with the new user
     * - and compares just the traits we send with the identify call, not the entire user object
     *
     * @param prevUser
     * @param newUser
     *
     * @return true in case traits remain the same
     *         false in case traits changed
     */
    private fun updateUserAndCheckTraits(prevUser: User?, newUser: User?) :Boolean {
        this.loggedInUser = newUser
        return prevUser?.getTraits() == newUser?.getTraits()
    }

    private fun initialize() {
        if (this.context.isKSApplication()) {
            var apiKey = ""
            var logLevel = Analytics.LogLevel.NONE

            if (build.isRelease && Build.isExternal()) {
                apiKey = Secrets.Segment.PRODUCTION
            }
            if (build.isDebug || Build.isInternal()) {
                apiKey = Secrets.Segment.STAGING
                logLevel = Analytics.LogLevel.VERBOSE
            }

            this.segmentClient = Analytics.Builder(context, apiKey)
                // - This flag will activate sending information to Braze
                .use(AppboyIntegration.FACTORY)
                .trackApplicationLifecycleEvents()
                .logLevel(logLevel)
                .build()
            Analytics.setSingletonInstance(segmentClient)
        }
        isInitialized = true
    }

    /**
     * Perform the request to the Segment third party library
     * see https://segment.com/docs/connections/sources/catalog/libraries/mobile/android/#track
     */
    override fun trackingData(eventName: String, newProperties: Map<String, Any?>) {
        this.segmentClient?.let { segment ->
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
        this.segmentClient?.let { segment ->

            if (this.build.isDebug && type() == Type.SEGMENT) {
                user.apply {
                    Timber.d("Queued ${type().tag} Identify userName: ${this.name()} userId: ${this.id()} traits: ${getTraits(user)}")
                }
            }
            segment.identify(user.id().toString(), getTraits(user), null)
        }
    }

    /**
     * clears the internal stores on Segment SDK for the current user and group
     * https://segment.com/docs/connections/sources/catalog/libraries/mobile/android/#reset
     */
    override fun reset() {
        super.reset()
        if (this.build.isDebug) {
            Timber.d("Queued ${type().tag} Reset user after logout")
        }
        this.segmentClient?.reset()
    }

    /**
     * In order to send custom properties to segment for the Identify method we need to use
     * the method Traits() from the Segment SDK
     * see https://segment.com/docs/connections/sources/catalog/libraries/mobile/android/#identify
     *
     * Added as trait the user name
     * Added as traits the user preferences for Email and Push Notifications Subscriptions
     * see User.getTraits()
     */
    private fun getTraits(user: User) = Traits().apply {
        user.getTraits().map { entry ->
            if (entry.key == NAME) this.putName(user.name())
            else {
                this[entry.key] = entry.value
            }
        }
    }
}
