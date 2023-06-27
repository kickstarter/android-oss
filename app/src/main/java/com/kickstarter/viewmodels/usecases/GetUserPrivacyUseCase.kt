package com.kickstarter.viewmodels.usecases

import com.kickstarter.models.UserPrivacy
import com.kickstarter.services.ApolloClientType
import com.kickstarter.services.ApolloClientTypeV2
import rx.Observable

class GetUserPrivacyUseCase(
    private val apolloClient: ApolloClientType
) {
    fun getUserPrivacy(): Observable<UserPrivacyQuery.Data> {
        return apolloClient.userPrivacy()
    }
}

class GetUserPrivacyUseCaseV2(
    private val apolloClient: ApolloClientTypeV2
) {
    fun getUserPrivacy(): io.reactivex.Observable<UserPrivacy> {
        return apolloClient.userPrivacy()
    }
}
