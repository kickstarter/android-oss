package com.kickstarter.services

import SavePaymentMethodMutation
import UpdateUserCurrencyMutation
import UpdateUserEmailMutation
import UpdateUserPasswordMutation
import UserPaymentsQuery
import UserPrivacyQuery
import rx.Observable
import type.CurrencyCode
import type.PaymentTypes

interface ApolloClientType {
    fun getStoredCards(): Observable<UserPaymentsQuery.Data>

    fun savePaymentMethod(paymentTypes: PaymentTypes, stripeToken: String, cardId: String): Observable<SavePaymentMethodMutation.Data>

    fun updateUserCurrencyPreference(currency: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data>

    fun updateUserEmail(email: String, currentPassword: String): Observable<UpdateUserEmailMutation.Data>

    fun updateUserPassword(currentPassword: String, newPassword: String, confirmPassword: String): Observable<UpdateUserPasswordMutation.Data>

    fun userPrivacy(): Observable<UserPrivacyQuery.Data>
}
