package com.kickstarter.services

import UserPrivacyQuery
import rx.Observable

open class MockApolloClient : ApolloClientType {
    override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
        return Observable.just(UserPrivacyQuery.Data(UserPrivacyQuery.Me("", "", "")))
    }
}
