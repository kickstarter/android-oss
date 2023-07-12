package com.kickstarter.viewmodels.usecases

import android.content.SharedPreferences
import android.util.Pair
import com.facebook.appevents.cloudbridge.ConversionsAPIEventName
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.FirebaseHelper
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.extensions.toHashedSHAEmail
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.transformers.encodeRelayId
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import io.reactivex.Observable
import type.AppDataInput
import type.CustomDataInput
import type.ThirdPartyEventItemInput
import type.TriggerCapiEventInput
import type.TriggerThirdPartyEventInput

class SendThirdPartyEventUseCaseV2(
    sharedPreferences: SharedPreferences,
    ffClient: FeatureFlagClientType,
) {
    private val canSendEventFlag = (
        ffClient.getBoolean(FlagKey.ANDROID_CONSENT_MANAGEMENT) &&
            sharedPreferences.getBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false) &&
            (ffClient.getBoolean(FlagKey.ANDROID_CAPI_INTEGRATION) || ffClient.getBoolean(FlagKey.ANDROID_GOOGLE_ANALYTICS))
        )

    fun sendCAPIEvent(
        project: Observable<Project>,
        currentUser: CurrentUserTypeV2,
        apolloClient: ApolloClientTypeV2,
        eventName: ConversionsAPIEventName,
        pledgeAmountAndCurrency: Observable<Pair<String?, String?>> = Observable.just(Pair(null, null)),
    ): Observable<Pair<TriggerCapiEventMutation.Data, TriggerCapiEventInput>> {
        val androidApp = "a2"

        return project
            .filter {
                it.sendMetaCapiEvents() ?: false
            }
            .filter { canSendEventFlag }
            .compose(Transformers.combineLatestPair<Project, KsOptional<User>>(currentUser.observable()))
            .compose(Transformers.combineLatestPair(project))
            .compose(Transformers.combineLatestPair(pledgeAmountAndCurrency))
            .map {
                val userEmail = it.first.first.second?.getValue()?.email() ?: ""
                val hashedEmail = if (it.first.first.second == null || userEmail.isNullOrEmpty()) {
                    userEmail.orEmpty()
                } else {
                    userEmail.toHashedSHAEmail()
                }

                TriggerCapiEventInput.builder()
                    .appData(AppDataInput.builder().extinfo(listOf(androidApp)).build())
                    .eventName(eventName.rawValue)
                    .projectId(encodeRelayId(it.first.second))
                    .externalId(FirebaseHelper.identifier)
                    .userEmail(hashedEmail)
                    .customData(
                        CustomDataInput.builder().currency(it.second.second)
                            .value(it.second.first).build(),
                    )
                    .build()
            }
            .switchMap { input ->
                apolloClient.triggerCapiEvent(
                    input,
                ).map { Pair(it, input) }
                    .compose(Transformers.neverErrorV2()).share()
            }
    }

    fun sendThirdPartyEvent(
        project: Observable<Project>,
        apolloClient: ApolloClientTypeV2,
        checkoutAndPledgeData: Observable<Pair<CheckoutData, PledgeData>?> = Observable.just(Pair(null, null)),
        currentUser: CurrentUserTypeV2,
        eventName: ThirdPartyEventValues.EventName,
        firebaseScreen: String = "",
        firebasePreviousScreen: String = "",
    ): Observable<Pair<Boolean, String>> {

        return project
            .filter { it.sendThirdPartyEvents() ?: false }
            .filter { canSendEventFlag }
            .compose(Transformers.combineLatestPair(currentUser.observable()))
            .compose(Transformers.combineLatestPair(checkoutAndPledgeData))
            .map {
                val eventInput = TriggerThirdPartyEventInput.builder()
                    .eventName(eventName.value)
                    .userId(it.first.second?.getValue()?.id().toString())
                    .deviceId(FirebaseHelper.identifier)
                    .projectId(encodeRelayId(it.first.first))

                it.second?.second?.let { pledgeData ->
                    val rewardsAndAddons = mutableListOf(pledgeData.reward())
                    pledgeData.addOns()?.forEach { addon ->
                        rewardsAndAddons.add(addon)
                    }

                    eventInput.items(
                        rewardsAndAddons.map { rewards ->
                            ThirdPartyEventItemInput.builder()
                                .itemId(rewards.id().toString())
                                .itemName(rewards.title().toString())
                                .price(rewards.minimum() * (rewards.quantity() ?: 1))
                                .build()
                        }
                    )
                }

                it.second?.first?.let { checkoutData ->
                    eventInput.apply {
                        pledgeAmount(checkoutData.amount())
                        shipping(checkoutData.shippingAmount())
                        transactionId(checkoutData.id().toString())
                    }
                }

                firebaseScreen?.let { screen -> eventInput.firebaseScreen(screen) }
                firebasePreviousScreen.let { previousScreen -> eventInput.firebasePreviousScreen(previousScreen) }
                eventInput.build()
            }
            .switchMap { input ->
                apolloClient.triggerThirdPartyEvent(
                    input,
                )
                    .compose(Transformers.neverErrorV2()).share()
            }
    }
}
