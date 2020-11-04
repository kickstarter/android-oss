package com.kickstarter.libs.utils

import com.kickstarter.libs.Config
import com.kickstarter.libs.utils.extensions.EMAIL_VERIFICATION_FLOW
import com.kickstarter.libs.utils.extensions.isFeatureFlagEnabled
import com.kickstarter.models.User
import com.kickstarter.models.extensions.isUserEmailVerified
import rx.Observable

object LoginHelper {

    /**
     * Takes and user and a configuration:
     * @param user User
     * @param config Config
     *
     * @return Observable<Boolean?>
     *     True in case feature flag active and verified
     *     False in case feature flag active and not verified
     *     True in case not active feature flag
     *     Null in case no current User (not logged)
     */
    fun hasCurrentUserVerifiedEmail(user: User?, config: Config): Observable<Boolean?> {
        if (user == null) {
            return Observable.just(null)
        }

        return if (config.isFeatureFlagEnabled(EMAIL_VERIFICATION_FLOW)) {
            Observable.just(user.isUserEmailVerified())
        } else Observable.just(true)
    }
}