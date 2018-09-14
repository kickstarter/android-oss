package com.kickstarter.mock.services

import UpdateUserEmailMutation
import UserPrivacyQuery
import com.kickstarter.services.ApolloClientType
import rx.Observable

open class MockApolloClient : ApolloClientType {
    override fun updateUserEmail(email: String, currentPassword: String): Observable<UpdateUserEmailMutation.Data> {
        return Observable.empty()
    }

    override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
        return Observable.just(UserPrivacyQuery.Data(UserPrivacyQuery.Me("", "Some Name", "some@email.com")))
    }
}
