package com.kickstarter.mock.services

import SendEmailVerificationMutation
import UpdateUserCurrencyMutation
import UpdateUserEmailMutation
import UpdateUserPasswordMutation
import UserPrivacyQuery
import com.kickstarter.services.ApolloClientType
import rx.Observable
import type.CurrencyCode

open class MockApolloClient : ApolloClientType {
    override fun sendVerificationEmail(): Observable<SendEmailVerificationMutation.Data> {
        return Observable.just(SendEmailVerificationMutation.Data(SendEmailVerificationMutation.UserSendEmailVerification("",
                "12345")))
    }

    override fun updateUserCurrencyPreference(currency: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data> {
        return Observable.just(UpdateUserCurrencyMutation.Data(UpdateUserCurrencyMutation.UpdateUserProfile("",
                UpdateUserCurrencyMutation.User("", "USD"))))
    }

    override fun updateUserPassword(currentPassword: String, newPassword: String, confirmPassword: String): Observable<UpdateUserPasswordMutation.Data> {
        return Observable.just(UpdateUserPasswordMutation.Data(UpdateUserPasswordMutation.UpdateUserAccount("",
                UpdateUserPasswordMutation.User("", "some@email.com", true))))
    }

    override fun updateUserEmail(email: String, currentPassword: String): Observable<UpdateUserEmailMutation.Data> {
        return Observable.just(UpdateUserEmailMutation.Data(UpdateUserEmailMutation.UpdateUserAccount("",
                UpdateUserEmailMutation.User("", "Some Name", "some@email.com"))))
    }

    override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
        return Observable.just(UserPrivacyQuery.Data(UserPrivacyQuery.Me("", "Some Name",
                "some@email.com", true, true, true, true, "USD")))
    }
}

