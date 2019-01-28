package com.kickstarter.services

import CreatePasswordMutation
import SendEmailVerificationMutation
import UpdateUserCurrencyMutation
import UpdateUserEmailMutation
import UpdateUserPasswordMutation
import UserPrivacyQuery
import rx.Observable
import type.CurrencyCode

interface ApolloClientType {
    fun createPassword(password: String, confirmPassword: String): Observable<CreatePasswordMutation.Data>

    fun sendVerificationEmail(): Observable<SendEmailVerificationMutation.Data>

    fun updateUserCurrencyPreference(currency: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data>

    fun updateUserEmail(email: String, currentPassword: String): Observable<UpdateUserEmailMutation.Data>

    fun updateUserPassword(currentPassword: String, newPassword: String, confirmPassword: String): Observable<UpdateUserPasswordMutation.Data>

    fun userPrivacy(): Observable<UserPrivacyQuery.Data>
}
