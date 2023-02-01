package com.kickstarter.viewmodels.usecases

import com.kickstarter.libs.Environment
import com.kickstarter.models.User

class LoginUseCase(
    private val environment: Environment
) {
    private val currentUser = requireNotNull(environment.currentUser())
    private val currentUserV2 = requireNotNull(environment.currentUserV2())

    fun login(newUser: User, accessToken: String) {
        currentUser.login(newUser, accessToken)
        currentUserV2.login(newUser, accessToken)
    }
}
