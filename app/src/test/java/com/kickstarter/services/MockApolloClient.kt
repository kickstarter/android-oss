package com.kickstarter.services

import UserPrivacyQuery
import rx.Single

open class MockApolloClient : ApolloClientType {
    override fun userPrivacy(): Single<UserPrivacyQuery.Data> {
        return Single.just(UserPrivacyQuery.Data(UserPrivacyQuery.Me("", "", "")))
    }
}
