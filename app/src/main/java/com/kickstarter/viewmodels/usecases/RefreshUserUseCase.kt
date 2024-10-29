package com.kickstarter.viewmodels.usecases

import com.kickstarter.libs.Environment
import com.kickstarter.models.User

class RefreshUserUseCase(environment: Environment) {
    private val currentUserV2 = requireNotNull(environment.currentUserV2())

    fun refresh(newUser: User) {
        currentUserV2.refresh(newUser)
    }
}
