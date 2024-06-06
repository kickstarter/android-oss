package com.kickstarter.libs

import android.content.Context
import android.content.SharedPreferences
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.extensions.isKSApplication
import com.kickstarter.libs.utils.extensions.registerActivityLifecycleCallbacks
import com.kickstarter.models.User
import com.kickstarter.models.extensions.getTraits
import com.segment.analytics.kotlin.android.Analytics
import com.segment.analytics.kotlin.core.Analytics
import com.segment.analytics.kotlin.destinations.braze.BrazeDestination
import timber.log.Timber

open class SegmentTrackingClient(
    build: Build,
    private val context: Context,
    currentConfig: CurrentConfigType,
    currentUser: CurrentUserTypeV2,
    ffClient: FeatureFlagClientType,
    preference: SharedPreferences
) : TrackingClient(context, currentUser, build, currentConfig, ffClient, preference) {

    override var isInitialized = false
    override var loggedInUser: User? = null
    override var config: Config? = null

    private var calledFromOnCreate = false
    private var prefStorage = preference
    private lateinit var segmentClient: Analytics
    init {
        privateInitializer()

        this.currentUser.observable()
            .filter { it.isPresent() }
            .map { requireNotNull(it.getValue()) }
            .subscribe {
                this.loggedInUser = it
                identify(it)
            }.dispose()
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

            if (build.isRelease && Build.isExternal()) {
                apiKey = Secrets.Segment.PRODUCTION
            }

            if (build.isDebug || Build.isInternal()) {
                apiKey = Secrets.Segment.STAGING

                segmentClient = Analytics(apiKey, context) {
                    this.collectDeviceId = true
                    this.trackApplicationLifecycleEvents = true
                    this.trackDeepLinks = true
                    flushAt = 1
                }
//                segmentClient = Analytics.Builder(context, apiKey)
//                    // - This flag will activate sending information to Braze
//                    .use(AppboyIntegration.FACTORY)
//                    .trackApplicationLifecycleEvents()
//                    .flushQueueSize(1)
//                    .logLevel(logLevel)
//                    // - Set middleware for Braze destination
//                    .useDestinationMiddleware(AppboyIntegration.FACTORY.key(), getMiddleware())
//                    .build()
            } else {
                segmentClient = Analytics(apiKey, context) {
                    this.collectDeviceId = true
                    this.trackApplicationLifecycleEvents = true
                    this.trackDeepLinks = true
                }
//                segmentClient = Analytics.Builder(context, apiKey)
//                    // - This flag will activate sending information to Braze
//                    .use(AppboyIntegration.FACTORY)
//                    .trackApplicationLifecycleEvents()
//                    .logLevel(logLevel)
//                    // - Set middleware for Braze destination
//                    .useDestinationMiddleware(AppboyIntegration.FACTORY.key(), getMiddleware())
//                    .build()
            }

            segmentClient.add(plugin = BrazeDestination(context))
            // Analytics.setSingletonInstance(segmentClient)

            // - onIntegrationReady Callback will be called once Segment has finalized the integration with Braze
            // - moment when we will set App the Listener for InAppMessages
//            Analytics.with(context).onIntegrationReady(
//                AppboyIntegration.FACTORY.key(),
//                Analytics.Callback<Any?> {
//                    if (build.isDebug) Timber.d("${type().tag} Integration with ${AppboyIntegration.FACTORY} finalized")
//                    BrazeClient.setInAppCustomListener(this.currentUser, this.build)
//                }
//            )

            this.isInitialized = true

            if (build.isDebug) {
                Timber.d("${type().tag} client:$segmentClient isInitialized:$isInitialized")
                Timber.d("${type().tag} currentThread: ${Thread.currentThread()}")
            }
        }
    }

//    private fun getMiddleware(): Middleware {
//        return Middleware { chain ->
//            chain.proceed(getPayload(chain.payload()))
//            // - persist traits once the payload has been modified
//            this.loggedInUser?.persistTraits(prefStorage)
//        }
//    }

    /**
     * Returns a modified Payload if necessary
     *  - get the UniqueTraits from the logged in user
     *  - Update the payload for braze source with the unique traits (only the ones that changed)
     *
     * By doing so we send to the braze source only the traits that has changed.
     */
//    private fun getPayload(payload: BasePayload): BasePayload {
//        if (payload.type() == BasePayload.Type.identify) {
//            if (payload is IdentifyPayload) {
//                this.loggedInUser?.getUniqueTraits(prefStorage)?.let { uniqueTraits ->
//                    val modifiedPayload = payload.toBuilder()
//                        .traits(uniqueTraits)
//                        .build()
//                    if (build.isDebug) Timber.d("${type().tag} Identify payload intercepted and modified: ${modifiedPayload.toJsonObject()}")
//                    return modifiedPayload
//                }
//            }
//        }
//
//        return payload
//    }

    /**
     * Perform the request to the Segment third party library
     * see https://segment.com/docs/connections/sources/catalog/libraries/mobile/kotlin-android/implementation/#track
     */
    override fun trackingData(eventName: String, newProperties: Map<String, Any?>) {
        if (isInitialized) {
            Timber.d("Queued ${type().tag} Track eventName: $eventName properties: $newProperties")
            segmentClient.track(eventName, newProperties)
        }
    }

    override fun type() = Type.SEGMENT

    /**
     * Perform the request to the Segment third party library
     * see https://segment.com/docs/connections/sources/catalog/libraries/mobile/kotlin-android/implementation/#identify
     */
    override fun identify(user: User) {
        super.identify(user)
        if (isInitialized) {
            if (this.build.isDebug && type() == Type.SEGMENT) {
                user.apply {
                    Timber.d("Queued ${type().tag} Identify userName: ${this.name()} userId: ${this.id()} traits: ${user.getTraits()}")
                }
            }
            segmentClient.identify(user.id().toString(), user.getTraits(), null)
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
            segmentClient.reset()
        }
    }
}
