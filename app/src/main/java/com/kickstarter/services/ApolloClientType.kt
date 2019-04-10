package com.kickstarter.services

import CreatePasswordMutation
import DeletePaymentSourceMutation
import SavePaymentMethodMutation
import SendEmailVerificationMutation
import UpdateUserCurrencyMutation
import UpdateUserEmailMutation
import UpdateUserPasswordMutation
import UserPrivacyQuery
import com.kickstarter.models.StoredCard
import rx.Observable
import type.CurrencyCode
import type.PaymentTypes

interface ApolloClientType {

    fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data>

    fun getStoredCards(): Observable<List<StoredCard>>

    fun savePaymentMethod(paymentTypes: PaymentTypes, stripeToken: String, cardId: String): Observable<SavePaymentMethodMutation.Data>

    fun createPassword(password: String, confirmPassword: String): Observable<CreatePasswordMutation.Data>

    fun sendVerificationEmail(): Observable<SendEmailVerificationMutation.Data>

    fun updateUserCurrencyPreference(currency: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data>

    fun updateUserEmail(email: String, currentPassword: String): Observable<UpdateUserEmailMutation.Data>

    fun updateUserPassword(currentPassword: String, newPassword: String, confirmPassword: String): Observable<UpdateUserPasswordMutation.Data>

    fun userPrivacy(): Observable<UserPrivacyQuery.Data>
}
