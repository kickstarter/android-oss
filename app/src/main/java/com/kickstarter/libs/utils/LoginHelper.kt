package com.kickstarter.libs.utils

import com.kickstarter.libs.Config
import com.kickstarter.libs.utils.extensions.EMAIL_VERIFICATION_FLOW
import com.kickstarter.libs.utils.extensions.isFeatureFlagEnabled
import com.kickstarter.models.User
import com.kickstarter.models.extensions.isUserEmailVerified
import rx.Observable

object LoginHelper {

    /**
     * Takes the current User and configuration observables:
     * @param user currentUser Observable
     * @param config current configuration Observable
     *
     * @return Observable<Boolean?>
     *     True in case feature flag active and verified
     *     False in case feature flag active and not verified
     *     True in case not active feature flag
     *     Null in case no current User (not logged)
     *
     */
    fun hasCurrentUserVerifiedEmail(user: Observable<User?>, config: Observable<Config>): Observable<Boolean?> =
            Observable.combineLatest(user, config) {
                cUser, cConfig ->
                if (cUser == null) {
                    return@combineLatest null
                }

                return@combineLatest if (cConfig.isFeatureFlagEnabled(EMAIL_VERIFICATION_FLOW)) {
                    cUser.isUserEmailVerified()
                } else true
            }
                    .distinctUntilChanged()

}