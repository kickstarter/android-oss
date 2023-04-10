package com.kickstarter.viewmodels.usecases

import android.content.SharedPreferences
import android.util.Pair
import com.braze.support.emptyToNull
import com.facebook.appevents.cloudbridge.ConversionsAPIEventName
import com.kickstarter.libs.ExperimentsClientType
import com.kickstarter.libs.FirebaseHelper
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
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
    sharedPreferences: SharedPreferences,
    ffClient: FeatureFlagClientType
) {
    private val canSendCAPIEventFlag = (
        ffClient.getBoolean(FlagKey.ANDROID_CONSENT_MANAGEMENT) &&
            sharedPreferences.getBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false) &&
            optimizely.isFeatureEnabled(OptimizelyFeature.Key.ANDROID_CAPI_INTEGRATION)
        )

    fun sendCAPIEvent(
        project: Observable<Project>,
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
            .switchMap {
                GetUserPrivacyUseCase(apolloClient)
                    .getUserPrivacy()
                    .compose(Transformers.neverError())
                    .filter { ObjectUtils.isNotNull(it) }
                    .map { it.me()?.email() }
                    .map {
                        if (it?.isNotEmpty() == true) {
                            it.toHashedSHAEmail()
                        } else {
                            it?.emptyToNull()
                        }
                    }
            }
            .compose(Transformers.combineLatestPair(project))
            .compose(Transformers.combineLatestPair(pledgeAmountAndCurrency))
            .map {
                TriggerCapiEventInput.builder()
                    .appData(AppDataInput.builder().extinfo(listOf(androidApp)).build())
                    .eventName(eventName.rawValue)
                    .projectId(encodeRelayId(it.first.second))
                    .externalId(FirebaseHelper.identifier)
                    .userEmail(it.first.first)
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
