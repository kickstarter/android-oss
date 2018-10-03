package com.kickstarter.services

import UpdateUserCurrencyMutation
import UpdateUserEmailMutation
import UserPrivacyQuery
import rx.Observable
import type.CurrencyCode

interface ApolloClientType {
    fun updateUserCurrencyPreference(currency: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data>

    fun updateUserEmail(email: String, currentPassword: String): Observable<UpdateUserEmailMutation.Data>

    fun userPrivacy(): Observable<UserPrivacyQuery.Data>
}