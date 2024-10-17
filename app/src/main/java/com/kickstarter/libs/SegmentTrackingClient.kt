package com.kickstarter.libs

import android.content.Context
import android.content.SharedPreferences
import com.kickstarter.libs.braze.BrazeClient
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.extensions.isKSApplication
import com.kickstarter.models.User
import com.kickstarter.models.extensions.NAME
import com.kickstarter.models.extensions.getTraits
import com.kickstarter.models.extensions.getUniqueTraits
import com.kickstarter.models.extensions.persistTraits
import com.segment.analytics.Analytics
import com.segment.analytics.Middleware
import com.segment.analytics.Properties
import com.segment.analytics.Traits
import com.segment.analytics.android.integrations.appboy.AppboyIntegration
import com.segment.analytics.integrations.BasePayload
import com.segment.analytics.integrations.IdentifyPayload
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

open class SegmentTrackingClient(
    build: Build,
    private val context: Context,
    currentConfig: CurrentConfigTypeV2,
    currentUser: CurrentUserTypeV2,
    ffClient: FeatureFlagClientType,
    preference: SharedPreferences
) : TrackingClient(context, currentUser, build, currentConfig, ffClient, preference) {

    override var isInitialized = false
    override var loggedInUser: User? = null
    override var config: Config? = null

    private var calledFromOnCreate = false
    private var prefStorage = preference

    init {

        this.currentConfig.observable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                this.config = it
                if (calledFromOnCreate) {
                    privateInitializer()

                    if (build.isDebug) {
                        Timber.d("${type().tag} isCalledFromOnCreate:$calledFromOnCreate withConfig:$config")
                        Timber.d("${type().tag} currentThread: ${Thread.currentThread()}")
                    }
                }
            }
            .subscribe()

        this.currentUser.observable()
            .filter { it.isPresent() }
            .map { requireNotNull(it.getValue()) }
            .map {
                this.loggedInUser = it
                identify(it)
            }
            .subscribe()
    }

    override fun initialize() {
        calledFromOnCreate = true
        if (build.isDebug) {
            Timber.d("${type().tag} initialize called from currentThread: ${Thread.currentThread()}")
        }
    }

    private fun privateInitializer() {
        if (build.isDebug) {
            Timber.d("${type().tag} isEnabled: ${this.isEnabled()}")
            Timber.d("${type().tag} currentThread: ${Thread.currentThread()}")
        }

        if (this.context.isKSApplication() && !this.isInitialized && this.isEnabled()) {
            var apiKey = ""
            var logLevel = Analytics.LogLevel.NONE
            var segmentClient: Analytics

            if (build.isRelease && Build.isExternal()) {
                apiKey = Secrets.Segment.PRODUCTION
            }

            if (build.isDebug || Build.isInternal()) {
                apiKey = Secrets.Segment.STAGING
                logLevel = Analytics.LogLevel.VERBOSE

                segmentClient = Analytics.Builder(context, apiKey)
                    // - This flag will activate sending information to Braze
                    .use(AppboyIntegration.FACTORY)
                    .trackApplicationLifecycleEvents()
                    .flushQueueSize(1)
                    .logLevel(logLevel)
                    // - Set middleware for Braze destination
                    .useDestinationMiddleware(AppboyIntegration.FACTORY.key(), getMiddleware())
                    .build()
            } else {
                segmentClient = Analytics.Builder(context, apiKey)
                    // - This flag will activate sending information to Braze
                    .use(AppboyIntegration.FACTORY)
                    .trackApplicationLifecycleEvents()
                    .logLevel(logLevel)
                    // - Set middleware for Braze destination
                    .useDestinationMiddleware(AppboyIntegration.FACTORY.key(), getMiddleware())
                    .build()
            }

            Analytics.setSingletonInstance(segmentClient)

            // - onIntegrationReady Callback will be called once Segment has finalized the integration with Braze
            // - moment when we will set App the Listener for InAppMessages
            Analytics.with(context).onIntegrationReady(
                AppboyIntegration.FACTORY.key(),
                Analytics.Callback<Any?> {
                    if (build.isDebug) Timber.d("${type().tag} Integration with ${AppboyIntegration.FACTORY} finalized")
                    BrazeClient.setInAppCustomListener(this.currentUser, this.build)
                }
            )

            this.isInitialized = true

            if (build.isDebug) {
                Timber.d("${type().tag} client:$segmentClient isInitialized:$isInitialized")
                Timber.d("${type().tag} currentThread: ${Thread.currentThread()}")
            }
        }
    }

    private fun getMiddleware(): Middleware {
        return Middleware { chain ->
            chain.proceed(getPayload(chain.payload()))
            // - persist traits once the payload has been modified
            this.loggedInUser?.persistTraits(prefStorage)
        }
    }

    /**
     * Returns a modified Payload if necessary
     *  - get the UniqueTraits from the logged in user
     *  - Update the payload for braze source with the unique traits (only the ones that changed)
     *
     * By doing so we send to the braze source only the traits that has changed.
     */
    private fun getPayload(payload: BasePayload): BasePayload {
        if (payload.type() == BasePayload.Type.identify) {
            if (payload is IdentifyPayload) {
                this.loggedInUser?.getUniqueTraits(prefStorage)?.let { uniqueTraits ->
                    val modifiedPayload = payload.toBuilder()
                        .traits(uniqueTraits)
                        .build()
                    if (build.isDebug) Timber.d("${type().tag} Identify payload intercepted and modified: ${modifiedPayload.toJsonObject()}")
                    return modifiedPayload
                }
            }
        }

        return payload
    }

    /**
     * Perform the request to the Segment third party library
     * see https://segment.com/docs/connections/sources/catalog/libraries/mobile/android/#track
     */
    override fun trackingData(eventName: String, newProperties: Map<String, Any?>) {
        if (isInitialized) {
            Timber.d("Queued ${type().tag} Track eventName: $eventName properties: $newProperties")
            Analytics.with(context).track(eventName, this.getProperties(newProperties))
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
        if (isInitialized) {
            if (this.build.isDebug && type() == Type.SEGMENT) {
                user.apply {
                    Timber.d("Queued ${type().tag} Identify userName: ${this.name()} userId: ${this.id()} traits: ${getTraits(user)}")
                }
            }
            Analytics.with(context).identify(user.id().toString(), getTraits(user), null)
        }
    }

    /**
     * clears the internal stores on Segment SDK for the current user and group
     * https://segment.com/docs/connections/sources/catalog/libraries/mobile/android/#reset
     */
    override fun reset() {
        super.reset()

        if (isInitialized) {
            if (this.build.isDebug) {
                Timber.d("Queued ${type().tag} Reset user after logout")
            }
            Analytics.with(context).reset()
        }
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
