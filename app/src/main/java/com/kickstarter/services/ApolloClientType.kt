package com.kickstarter.services

import CreatePasswordMutation
import DeletePaymentSourceMutation
import SendEmailVerificationMutation
import UpdateUserCurrencyMutation
import UpdateUserEmailMutation
import UpdateUserPasswordMutation
import UserPrivacyQuery
import com.kickstarter.models.*
import rx.Observable
import type.CurrencyCode
import type.PaymentTypes

interface ApolloClientType {
    fun cancelBacking(backing: Backing, note: String): Observable<Any>

    fun createBacking(project: Project, amount: String, paymentSourceId: String, locationId: String?, reward: Reward?): Observable<Boolean>

    fun clearUnseenActivity(): Observable<Long>

    fun createPassword(password: String, confirmPassword: String): Observable<CreatePasswordMutation.Data>

    fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data>

    fun getStoredCards(): Observable<List<StoredCard>>

    fun savePaymentMethod(paymentTypes: PaymentTypes, stripeToken: String, cardId: String, reusable: Boolean): Observable<StoredCard>

    fun sendMessage(project: Project, recipient: User, body: String): Observable<Long>

    fun sendVerificationEmail(): Observable<SendEmailVerificationMutation.Data>

    fun updateBacking(backing: Backing, amount: String, locationId: String?, reward: Reward?): Observable<Boolean>

    fun updateUserCurrencyPreference(currency: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data>

    fun updateUserEmail(email: String, currentPassword: String): Observable<UpdateUserEmailMutation.Data>

    fun updateUserPassword(currentPassword: String, newPassword: String, confirmPassword: String): Observable<UpdateUserPasswordMutation.Data>

    fun userPrivacy(): Observable<UserPrivacyQuery.Data>
}
