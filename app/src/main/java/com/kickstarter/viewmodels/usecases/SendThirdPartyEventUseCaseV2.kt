package com.kickstarter.viewmodels.usecases

import android.content.SharedPreferences
import android.util.Pair
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.models.Project
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import io.reactivex.Observable

class SendThirdPartyEventUseCaseV2(
    sharedPreferences: SharedPreferences,
    ffClient: FeatureFlagClientType,
) : BuildInput {
    private val canSendEventFlag = (
        ffClient.getBoolean(FlagKey.ANDROID_CONSENT_MANAGEMENT) &&
            sharedPreferences.getBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false) &&
            (ffClient.getBoolean(FlagKey.ANDROID_CAPI_INTEGRATION) || ffClient.getBoolean(FlagKey.ANDROID_GOOGLE_ANALYTICS))
        )

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
            .withLatestFrom(currentUser.observable()) { proj, user ->
                Pair(proj, user.getValue())
            }
            .compose(Transformers.combineLatestPair(checkoutAndPledgeData))
            .map {
                this.buildInput(
                    eventName = eventName,
                    canSendEventFlag = canSendEventFlag,
                    firebaseScreen = firebaseScreen,
                    firebasePreviousScreen = firebasePreviousScreen,
                    rawData = it
                )
            }
            .switchMap { input ->
                apolloClient.triggerThirdPartyEvent(
                    input,
                )
                    .compose(Transformers.neverErrorV2()).share()
            }
            .share()
    }
}
