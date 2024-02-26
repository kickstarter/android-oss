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

    fun logout() {
        currentUser.logout()
        currentUserV2.logout()
    }

    fun setToken(accessToken: String) {
        currentUser.setToken(accessToken)
        currentUserV2.setToken(accessToken)
    }

    fun setUser(user: User) {
        currentUser.login(user)
        currentUserV2.login(user)
    }

    fun loginAndUpdateUserPrivacy(newUser: User, accessToken: String): Observable<User> {
        currentUser.setToken(accessToken)
        return GetUserPrivacyUseCase(apolloClient).getUserPrivacy()
            .compose(Transformers.neverError())
            .map {
                val user = newUser.toBuilder()
                    .email(it.me()?.email())
                    .isCreator(it.me()?.isCreator)
                    .isDeliverable(it.me()?.isDeliverable)
                    .isEmailVerified(it.me()?.isEmailVerified)
                    .hasPassword(it.me()?.hasPassword()).build()
                currentUser.login(user)
                return@map user
            }
    }

    fun loginAndUpdateUserPrivacyV2(newUser: User, accessToken: String): io.reactivex.Observable<User> {
        currentUserV2.setToken(accessToken)
        return GetUserPrivacyUseCaseV2(apolloClientV2).getUserPrivacy()
            .compose(Transformers.neverErrorV2())
            .map {
                newUser.toBuilder()
                    .email(it.email)
                    .isCreator(it.isCreator)
                    .isDeliverable(it.isDeliverable)
                    .isEmailVerified(it.isEmailVerified)
                    .hasPassword(it.hasPassword).build()
                currentUserV2.login(newUser)
                return@map newUser
            }
    }
}
