package com.kickstarter.services

import CreatePasswordMutation
import DeletePaymentSourceMutation
import SendEmailVerificationMutation
import UpdateUserCurrencyMutation
import UpdateUserEmailMutation
import UpdateUserPasswordMutation
import UserPrivacyQuery
import com.kickstarter.libs.RefTag
import com.kickstarter.models.*
import com.kickstarter.services.mutations.SavePaymentMethodData
import com.kickstarter.services.mutations.UpdateBacking
import rx.Observable
import type.CurrencyCode

interface ApolloClientType {
    fun cancelBacking(backing: Backing, note: String): Observable<Any>

    fun createBacking(project: Project, amount: String, paymentSourceId: String, locationId: String?, reward: Reward?, refTag: RefTag?): Observable<Boolean>

    fun clearUnseenActivity(): Observable<Int>

    fun createPassword(password: String, confirmPassword: String): Observable<CreatePasswordMutation.Data>

    fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data>

    fun getStoredCards(): Observable<List<StoredCard>>

    fun savePaymentMethod(savePaymentMethodData: SavePaymentMethodData): Observable<StoredCard>

    fun sendMessage(project: Project, recipient: User, body: String): Observable<Long>

    fun sendVerificationEmail(): Observable<SendEmailVerificationMutation.Data>

    fun updateBacking(updateBacking: UpdateBacking): Observable<Boolean>

    fun updateUserCurrencyPreference(currency: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data>

    fun updateUserEmail(email: String, currentPassword: String): Observable<UpdateUserEmailMutation.Data>

    fun updateUserPassword(currentPassword: String, newPassword: String, confirmPassword: String): Observable<UpdateUserPasswordMutation.Data>

    fun userPrivacy(): Observable<UserPrivacyQuery.Data>
}
