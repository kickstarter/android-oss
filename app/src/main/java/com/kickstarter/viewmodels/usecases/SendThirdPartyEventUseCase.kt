package com.kickstarter.viewmodels.usecases

import android.content.SharedPreferences
import android.util.Pair
import com.facebook.appevents.cloudbridge.ConversionsAPIEventName
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.FirebaseHelper
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.extensions.toHashedSHAEmail
import com.kickstarter.models.Project
import com.kickstarter.services.ApolloClientType
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.transformers.encodeRelayId
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import rx.Observable
import type.AppDataInput
import type.CustomDataInput
import type.ThirdPartyEventItemInput
import type.TriggerCapiEventInput
import type.TriggerThirdPartyEventInput

class SendThirdPartyEventUseCase(
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
        currentUser: CurrentUserType,
        apolloClient: ApolloClientType,
        eventName: ConversionsAPIEventName,
        pledgeAmountAndCurrency: Observable<Pair<String?, String?>> = Observable.just(Pair(null, null)),
    ): Observable<Pair<TriggerCapiEventMutation.Data, TriggerCapiEventInput>> {
        val androidApp = "a2"

        return project
            .filter {
                it.sendMetaCapiEvents()
            }
            .filter { canSendEventFlag }
            .compose(Transformers.combineLatestPair(currentUser.observable()))
            .compose(Transformers.combineLatestPair(project))
            .compose(Transformers.combineLatestPair(pledgeAmountAndCurrency))
            .map {
                val userEmail = it.first.first.second?.email()
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
                    .compose(Transformers.neverError()).share()
            }
    }

    fun sendThirdPartyEvent(
        project: Observable<Project>,
        apolloClient: ApolloClientType,
        currentUser: CurrentUserType,
        eventName: ThirdPartyEventValues.EventName,
        firebaseScreen: ThirdPartyEventValues.ScreenName? = null,
        firebasePreviousScreen: Observable<String?> = Observable.just(""),
        checkoutAndPledgeData: Observable<Pair<CheckoutData, PledgeData>?> = Observable.just(Pair(null, null)),
    ): Observable<Pair<TriggerThirdPartyEventMutation.Data, TriggerThirdPartyEventInput>> {

        return project
            .filter { it.sendThirdPartyEvents() }
            .filter { canSendEventFlag }
            .compose(Transformers.combineLatestPair(currentUser.observable()))
            .compose(Transformers.combineLatestPair(checkoutAndPledgeData))
            .compose(Transformers.combineLatestPair(firebasePreviousScreen))
            .map {
                val eventInput = TriggerThirdPartyEventInput.builder()
                    .eventName(eventName.value)
                    .userId(it.first.first.second.id().toString())
                    .deviceId(FirebaseHelper.identifier)
                    .projectId(encodeRelayId(it.first.first.first))

                it.first.second?.second?.let { pledgeData ->
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

                it.first.second?.first?.let { checkoutData ->
                    eventInput.apply {
                        pledgeAmount(checkoutData.amount() - checkoutData.shippingAmount())
                        shipping(checkoutData.shippingAmount())
                        transactionId(checkoutData.id().toString())
                    }
                }

                firebaseScreen?.let { screen -> eventInput.firebaseScreen(screen.value) }
                it.second?.let { previousScreenName ->
                    eventInput.firebasePreviousScreen(previousScreenName)
                }
                eventInput.build()
            }
            .switchMap { input ->
                apolloClient.triggerThirdPartyEvent(
                    input,
                ).map { Pair(it, input) }
                    .compose(Transformers.neverError()).share()
            }
    }

    fun sendThirdPartyEventV2(
        project: io.reactivex.Observable<Project>,
        apolloClient: ApolloClientTypeV2,
        currentUser: CurrentUserTypeV2,
        eventName: ThirdPartyEventValues.EventName,
        firebaseScreen: ThirdPartyEventValues.ScreenName? = null,
        firebasePreviousScreen: io.reactivex.Observable<KsOptional<String>> = io.reactivex.Observable.just(KsOptional.empty()),
        checkoutAndPledgeData: io.reactivex.Observable<KsOptional<Pair<CheckoutData, PledgeData>>> = io.reactivex.Observable.just(KsOptional.empty()),
    ): io.reactivex.Observable<Pair<TriggerThirdPartyEventMutation.Data, TriggerThirdPartyEventInput>> {

        return project
            .filter { it.sendThirdPartyEvents() ?: false }
            .filter { canSendEventFlag }
            .compose(Transformers.combineLatestPair(currentUser.observable()))
            .compose(Transformers.combineLatestPair(checkoutAndPledgeData))
            .compose(Transformers.combineLatestPair(firebasePreviousScreen))
            .map {
                val eventInput = TriggerThirdPartyEventInput.builder()
                    .eventName(eventName.value)
                    .userId(it.first.first.second.getValue()?.id().toString())
                    .deviceId(FirebaseHelper.identifier)
                    .projectId(encodeRelayId(it.first.first.first))

                it.first.second.getValue()?.let { checkoutAndPledgeData ->
                    val checkoutData = checkoutAndPledgeData.first
                    val pledgeData = checkoutAndPledgeData.second
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

                    eventInput.apply {
                        pledgeAmount(checkoutData.amount())
                        shipping(checkoutData.shippingAmount())
                        transactionId(checkoutData.id().toString())
                    }
                }

                firebaseScreen?.let { screen -> eventInput.firebaseScreen(screen.value) }
                it.second.getValue()?.let { previousScreenName ->
                    eventInput.firebasePreviousScreen(previousScreenName)
                }
                eventInput.build()
            }
            .switchMap { input ->
                apolloClient.triggerThirdPartyEvent(
                    input,
                ).map { Pair(it, input) }
                    .compose(Transformers.neverErrorV2())
            }
    }
}
