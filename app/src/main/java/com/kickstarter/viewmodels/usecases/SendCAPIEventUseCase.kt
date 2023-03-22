package com.kickstarter.viewmodels.usecases

import android.content.SharedPreferences
import android.util.Pair
import com.braze.support.emptyToNull
import com.facebook.appevents.cloudbridge.ConversionsAPIEventName
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.ExperimentsClientType
import com.kickstarter.libs.FirebaseHelper
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.toHashedSHAEmail
import com.kickstarter.models.Project
import com.kickstarter.services.ApolloClientType
import com.kickstarter.services.transformers.encodeRelayId
import com.kickstarter.ui.SharedPreferenceKey
import rx.Observable
import type.AppDataInput
import type.CustomDataInput
import type.TriggerCapiEventInput

class SendCAPIEventUseCase(
    optimizely: ExperimentsClientType,
    sharedPreferences: SharedPreferences
) {
    private val canSendCAPIEventFlag = (
        optimizely.isFeatureEnabled(OptimizelyFeature.Key.ANDROID_CONSENT_MANAGEMENT) &&
            sharedPreferences.getBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false) &&
            optimizely.isFeatureEnabled(OptimizelyFeature.Key.ANDROID_CAPI_INTEGRATION)
        )

    fun sendCAPIEvent(
        project: Observable<Project>,
        currentUser: CurrentUserType,
        apolloClient: ApolloClientType,
        eventName: ConversionsAPIEventName,
        pledgeAmountAndCurrency: Observable<Pair<String?, String?>> = Observable.just(Pair(null, null))
    ): Observable<Pair<TriggerCapiEventMutation.Data, TriggerCapiEventInput>> {
        val androidApp = "a2"

        return project
            .filter {
                it.sendMetaCapiEvents()
            }
            .filter { canSendCAPIEventFlag }
            .compose(Transformers.combineLatestPair(currentUser.observable()))
            .compose(Transformers.combineLatestPair(project))
            .compose(Transformers.combineLatestPair(pledgeAmountAndCurrency))
            .map {
                val userEmail = it.first.first.second?.email()
                val hashedEmail = if (userEmail?.isNotEmpty() == true) {
                    userEmail?.toHashedSHAEmail()
                } else {
                    userEmail?.emptyToNull()
                }

                TriggerCapiEventInput.builder()
                    .appData(AppDataInput.builder().extinfo(listOf(androidApp)).build())
                    .eventName(eventName.rawValue)
                    .projectId(encodeRelayId(it.first.second))
                    .externalId(FirebaseHelper.identifier)
                    .userEmail(hashedEmail)
                    .customData(
                        CustomDataInput.builder().currency(it.second.second)
                            .value(it.second.first).build()
                    )
                    .build()
            }
            .switchMap { input ->
                apolloClient.triggerCapiEvent(
                    input
                ).map { Pair(it, input) }
                    .compose(Transformers.neverError()).share()
            }
    }
}
