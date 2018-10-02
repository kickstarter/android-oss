package com.kickstarter.mock.services

import UpdateUserCurrencyMutation
import UpdateUserEmailMutation
import UserPrivacyQuery
import com.kickstarter.services.ApolloClientType
import rx.Observable
import type.CurrencyCode

open class MockApolloClient : ApolloClientType {
    override fun updateUserCurrencyPreference(currency: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data> {
        return Observable.empty()
    }

    override fun updateUserEmail(email: String, currentPassword: String): Observable<UpdateUserEmailMutation.Data> {
        return Observable.just(UpdateUserEmailMutation.Data(UpdateUserEmailMutation.UpdateUserAccount("",
                UpdateUserEmailMutation.User("", "Some Name", "some@email.com"))))
    }

    override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
        return Observable.just(UserPrivacyQuery.Data(UserPrivacyQuery.Me("", "Some Name", "some@email.com", "bio", "USD")))
    }
}
