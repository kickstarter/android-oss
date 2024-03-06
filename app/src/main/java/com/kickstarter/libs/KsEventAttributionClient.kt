package com.kickstarter.libs

import android.content.Context
import android.content.SharedPreferences
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.models.User
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.mutations.CreateAttributionEventData
import timber.log.Timber

open class KsEventAttributionClient (
    build: Build,
    context: Context,
    currentConfig: CurrentConfigType,
    currentUser: CurrentUserType,
    ffClient: FeatureFlagClientType,
    preference: SharedPreferences,
    val apolloClient: ApolloClientTypeV2
) : TrackingClient(context, currentUser, build, currentConfig, ffClient, preference) {

    override var isInitialized = false
    override var loggedInUser: User? = null
    override fun type() = Type.KS_EVENT_ATTRIBUTION

    override fun initialize() {
        TODO("Not yet implemented")
    }

    // Attribution events sent to backend are not subject to consent management
    override fun isEnabled(): Boolean = true
    override fun trackingData(eventName: String, eventProperties: Map<String, Any?>) {
        val attributionEventData = CreateAttributionEventData(
            eventName = "Project Page Viewed",
            eventProperties =  eventProperties,
            clientMutationId = "TBD" //TODO
        )
        createAttributionEvent(attributionEventData)
            .subscribe {
                Timber.tag("YC").d("YC project page viewed sent to backend: "+it.toString())
            }
    }

    private fun createAttributionEvent(eventInput: CreateAttributionEventData) =
        this.apolloClient.createAttributionEvent(eventInput)
            .doOnSubscribe {
                //TODO
            }
            .doAfterTerminate {
                //TODO
            }
            .materialize()
            .share()

    override var config: Config? = null
}