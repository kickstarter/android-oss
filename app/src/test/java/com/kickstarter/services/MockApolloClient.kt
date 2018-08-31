package com.kickstarter.services

import UserPrivacyQuery
import rx.Single

class MockApolloClient : ApolloClientType {
    override fun userPrivacy(): Single<UserPrivacyQuery.Data> {
        return Single.just(null)
    }
}
