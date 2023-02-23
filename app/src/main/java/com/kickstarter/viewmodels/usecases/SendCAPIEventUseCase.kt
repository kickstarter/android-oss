package com.kickstarter.viewmodels.usecases

import android.content.SharedPreferences
import android.util.Pair
import com.braze.support.emptyToNull
import com.facebook.appevents.cloudbridge.ConversionsAPIEventName
import com.kickstarter.libs.ExperimentsClientType
import com.kickstarter.libs.FirebaseHelper
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Project
import com.kickstarter.services.ApolloClientType
import com.kickstarter.services.transformers.encodeRelayId
import com.kickstarter.ui.SharedPreferenceKey
import rx.Notification
import rx.Observable
import type.AppDataInput
import type.CustomDataInput
import type.TriggerCapiEventInput
import java.security.MessageDigest

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
        apolloClient: ApolloClientType,
        eventName: ConversionsAPIEventName,
        pledgeAmountAndCurrency: Observable<Pair<String?, String?>> = Observable.just(Pair(null, null))
    ): Observable<Notification<TriggerCapiEventMutation.Data>> {
        val androidApp = "a2"

        return project
            .filter {
                it.sendMetaCapiEvents()
            }
            // .filter { canSendCAPIEventFlag }
            .switchMap {
                GetUserPrivacyUseCase(apolloClient)
                    .getUserPrivacy()
                    .compose(Transformers.neverError())
                    .filter { ObjectUtils.isNotNull(it) }
                    .map { it.me()?.email() }
                    .map {
                        if (it?.isNotEmpty() == true) {
                            MessageDigest
                                .getInstance("SHA-256")
                                .digest(it.toByteArray())
                                .fold("") { str, it -> str + "%02x".format(it) }
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
            .switchMap {
                apolloClient.triggerCapiEvent(
                    it
                )
                    .compose(Transformers.neverError()).materialize()
            }.share()
    }
}
