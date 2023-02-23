package com.kickstarter.viewmodels.usecases

import com.kickstarter.services.ApolloClientType
import rx.Observable

class GetUserPrivacyUseCase(
    private val apolloClient: ApolloClientType
) {

    fun getUserPrivacy(): Observable<UserPrivacyQuery.Data> {
        return apolloClient.userPrivacy()
    }
}
