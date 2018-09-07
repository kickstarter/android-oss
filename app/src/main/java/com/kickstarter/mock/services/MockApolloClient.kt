package com.kickstarter.mock.services

import UserPrivacyQuery
import com.kickstarter.services.ApolloClientType
import rx.Observable

open class MockApolloClient : ApolloClientType {
    override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
        return Observable.just(UserPrivacyQuery.Data(UserPrivacyQuery.Me("", "Some Name", "some@email.com")))
    }
}
