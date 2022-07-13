package com.kickstarter.viewmodels.usecases

import android.util.Pair
import com.kickstarter.libs.ExperimentsClientType
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.models.User
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import rx.Observable

/**
 * Use Case that will provide the required Date to instanciate
 * [PledgeFragment] or [PledgeFragmentLegacy]
 */
class ShowPledgeFragmentUseCase(private val pledgeFragmentData: Observable<Pair<PledgeData, PledgeReason>>) {

    fun data(currentUser: Observable<User?>, optimizely: ExperimentsClientType): Observable<Triple<PledgeData, PledgeReason, Boolean>> {
        return pledgeFragmentData
            .withLatestFrom(currentUser) { data, user ->
                Triple(
                    data.first, data.second,
                    FeatureFlagStateUseCase(
                        optimizely, user,
                        OptimizelyFeature.Key.ANDROID_PAYMENTSHEET
                    ).isActive()
                )
            }
    }
}
