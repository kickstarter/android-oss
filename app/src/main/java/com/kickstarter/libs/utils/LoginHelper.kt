package com.kickstarter.libs.utils

import com.kickstarter.libs.Config
import com.kickstarter.models.User
import com.kickstarter.models.extensions.isUserEmailVerified
import rx.Observable

object LoginHelper {
    fun hasCurrentUserVerifiedEmail(user: Observable<User>, config: Observable<Config>): Observable<Boolean> =
            Observable.combineLatest(user, config) {
                cUser, cConfig -> cUser.isUserEmailVerified() // TODO: check in here feature flag
            }
}