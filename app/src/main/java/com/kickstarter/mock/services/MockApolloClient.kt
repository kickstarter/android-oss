package com.kickstarter.mock.services

import UpdateUserCurrencyMutation
import UpdateUserEmailMutation
import UpdateUserPasswordMutation
import UserPaymentsQuery
import UserPrivacyQuery
import com.kickstarter.services.ApolloClientType
import rx.Observable
import type.CreditCardPaymentType
import type.CreditCardState
import type.CreditCardType
import type.CurrencyCode

open class MockApolloClient : ApolloClientType {
    override fun getStoredCards(): Observable<UserPaymentsQuery.Data> {
        return Observable.just(UserPaymentsQuery.Data(UserPaymentsQuery.Me("",
                UserPaymentsQuery.StoredCards("", List<UserPaymentsQuery.Node>(1
                ) { _ -> UserPaymentsQuery.Node("","4333", "03/12", "1234",
                        CreditCardState.ACTIVE, CreditCardPaymentType.CREDIT_CARD, CreditCardType.VISA )}))))
    }

    override fun updateUserCurrencyPreference(currency: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data> {
        return Observable.just(UpdateUserCurrencyMutation.Data(UpdateUserCurrencyMutation.UpdateUserProfile("",
                UpdateUserCurrencyMutation.User("", "USD"))))
    }

    override fun updateUserPassword(currentPassword: String, newPassword: String, confirmPassword: String): Observable<UpdateUserPasswordMutation.Data> {
        return Observable.just(UpdateUserPasswordMutation.Data(UpdateUserPasswordMutation.UpdateUserAccount("",
                UpdateUserPasswordMutation.User("", "some@email.com"))))
    }

    override fun updateUserEmail(email: String, currentPassword: String): Observable<UpdateUserEmailMutation.Data> {
        return Observable.just(UpdateUserEmailMutation.Data(UpdateUserEmailMutation.UpdateUserAccount("",
                UpdateUserEmailMutation.User("", "Some Name", "some@email.com"))))
    }

    override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
        return Observable.just(UserPrivacyQuery.Data(UserPrivacyQuery.Me("", "Some Name",
                "some@email.com",  "USD")))
    }
}

