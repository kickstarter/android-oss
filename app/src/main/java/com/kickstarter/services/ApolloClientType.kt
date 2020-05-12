package com.kickstarter.services

import CreatePasswordMutation
import DeletePaymentSourceMutation
import SendEmailVerificationMutation
import UpdateUserCurrencyMutation
import UpdateUserEmailMutation
import UpdateUserPasswordMutation
import UserPrivacyQuery
import com.kickstarter.models.*
import com.kickstarter.services.mutations.CreateBackingData
import com.kickstarter.services.mutations.SavePaymentMethodData
import com.kickstarter.services.mutations.UpdateBackingData
import rx.Observable
import type.CurrencyCode

interface ApolloClientType {
    fun cancelBacking(backing: Backing, note: String): Observable<Any>

    fun createBacking(createBackingData: CreateBackingData): Observable<Checkout>

    fun clearUnseenActivity(): Observable<Int>

    fun createPassword(password: String, confirmPassword: String): Observable<CreatePasswordMutation.Data>

    fun creatorDetails(slug: String): Observable<CreatorDetails>

    fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data>

    fun erroredBackings(): Observable<List<ErroredBacking>>

    fun getProjectBacking(slug: String): Observable<Backing>

    fun getStoredCards(): Observable<List<StoredCard>>

    fun savePaymentMethod(savePaymentMethodData: SavePaymentMethodData): Observable<StoredCard>

    fun sendMessage(project: Project, recipient: User, body: String): Observable<Long>

    fun sendVerificationEmail(): Observable<SendEmailVerificationMutation.Data>

    fun updateBacking(updateBackingData: UpdateBackingData): Observable<Checkout>

    fun updateUserCurrencyPreference(currency: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data>

    fun updateUserEmail(email: String, currentPassword: String): Observable<UpdateUserEmailMutation.Data>

    fun updateUserPassword(currentPassword: String, newPassword: String, confirmPassword: String): Observable<UpdateUserPasswordMutation.Data>

    fun userPrivacy(): Observable<UserPrivacyQuery.Data>
}
