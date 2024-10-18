package com.kickstarter.viewmodels.usecases

import com.kickstarter.models.UserPrivacy
import com.kickstarter.services.ApolloClientTypeV2

class GetUserPrivacyUseCaseV2(
    private val apolloClient: ApolloClientTypeV2
) {
    fun getUserPrivacy(): io.reactivex.Observable<UserPrivacy> {
        return apolloClient.userPrivacy()
    }
}
