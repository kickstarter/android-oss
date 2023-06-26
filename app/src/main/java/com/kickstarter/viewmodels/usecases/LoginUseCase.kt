package com.kickstarter.viewmodels.usecases

import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.models.User
import rx.Observable

class LoginUseCase(environment: Environment) {
    private val currentUser = requireNotNull(environment.currentUser())
    private val currentUserV2 = requireNotNull(environment.currentUserV2())
    private val apolloClient = requireNotNull(environment.apolloClient())
    private val apolloClientV2 = requireNotNull(environment.apolloClientV2())

    fun login(newUser: User, accessToken: String) {
        currentUser.login(newUser, accessToken)
        currentUserV2.login(newUser, accessToken)
    }

    fun loginAndUpdateUserPrivacy(newUser: User, accessToken: String): Observable<User> {
        login(newUser, accessToken)
        return GetUserPrivacyUseCase(apolloClient).getUserPrivacy()
            .compose(Transformers.neverError())
            .map {
                newUser.toBuilder()
                    .email(it.me()?.email())
                    .isCreator(it.me()?.isCreator)
                    .isDeliverable(it.me()?.isDeliverable)
                    .isEmailVerified(it.me()?.isEmailVerified)
                    .hasPassword(it.me()?.hasPassword()).build()
            }
    }

    fun loginAndUpdateUserPrivacyV2(newUser: User, accessToken: String): io.reactivex.Observable<User> {
        login(newUser, accessToken)
        return GetUserPrivacyUseCaseV2(apolloClientV2).getUserPrivacy()
            .compose(Transformers.neverErrorV2())
            .map {
                newUser.toBuilder()
                    .email(it.email)
                    .isCreator(it.isCreator)
                    .isDeliverable(it.isDeliverable)
                    .isEmailVerified(it.isEmailVerified)
                    .hasPassword(it.hasPassword).build()
            }
    }
}
