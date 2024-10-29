package com.kickstarter.viewmodels.usecases

import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.models.User

class LoginUseCase(environment: Environment) {
    private val currentUserV2 = requireNotNull(environment.currentUserV2())
    private val apolloClientV2 = requireNotNull(environment.apolloClientV2())

    fun logout() {
        currentUserV2.logout()
    }

    fun setToken(accessToken: String) {
        currentUserV2.setToken(accessToken)
    }

    fun setUser(user: User) {
        currentUserV2.login(user)
    }

    fun loginAndUpdateUserPrivacy(newUser: User, accessToken: String): io.reactivex.Observable<User> {
        currentUserV2.setToken(accessToken)
        return GetUserPrivacyUseCaseV2(apolloClientV2).getUserPrivacy()
            .compose(Transformers.neverErrorV2())
            .map {
                val updated = newUser.toBuilder()
                    .email(it.email)
                    .isCreator(it.isCreator)
                    .isDeliverable(it.isDeliverable)
                    .isEmailVerified(it.isEmailVerified)
                    .hasPassword(it.hasPassword).build()
                currentUserV2.login(updated)
                return@map updated
            }
    }
}
