package com.kickstarter.mock.services

import CreatePasswordMutation
import DeletePaymentSourceMutation
import SendEmailVerificationMutation
import UpdateUserCurrencyMutation
import UpdateUserEmailMutation
import UpdateUserPasswordMutation
import UserPrivacyQuery
import com.kickstarter.mock.factories.CheckoutBackingFactory
import com.kickstarter.mock.factories.StoredCardFactory
import com.kickstarter.models.*
import com.kickstarter.services.ApolloClientType
import com.kickstarter.services.mutations.CreateBackingData
import com.kickstarter.services.mutations.UpdateBacking
import rx.Observable
import type.CurrencyCode
import type.PaymentTypes
import java.util.*

open class MockApolloClient : ApolloClientType {

    override fun cancelBacking(backing: Backing, note: String): Observable<Any> {
        return Observable.just(true)
    }

    override fun createBacking(createBackingData: CreateBackingData): Observable<Checkout.Backing> {
        return Observable.just(CheckoutBackingFactory.requiresAction(false))
    }

    override fun clearUnseenActivity(): Observable<Int> {
        return Observable.just(0)
    }

    override fun createPassword(password: String, confirmPassword: String): Observable<CreatePasswordMutation.Data> {
        return Observable.just(CreatePasswordMutation.Data(CreatePasswordMutation.UpdateUserAccount("",
                CreatePasswordMutation.User("", "sample@ksr.com", true))))
    }

    override fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data> {
        return Observable.just(DeletePaymentSourceMutation.Data(DeletePaymentSourceMutation.PaymentSourceDelete("", "")))
    }

    override fun getStoredCards(): Observable<List<StoredCard>> {
        return Observable.just(Collections.singletonList(StoredCardFactory.discoverCard()))
    }

    override fun savePaymentMethod(paymentTypes: PaymentTypes, stripeToken: String, cardId: String, reusable: Boolean): Observable<StoredCard> {
        return Observable.just(StoredCardFactory.discoverCard())
    }

    override fun sendMessage(project: Project, recipient: User, body: String): Observable<Long> {
        return Observable.just(1L)
    }

    override fun sendVerificationEmail(): Observable<SendEmailVerificationMutation.Data> {
        return Observable.just(SendEmailVerificationMutation.Data(SendEmailVerificationMutation.UserSendEmailVerification("",
                "12345")))
    }

    override fun updateBacking(updateBacking: UpdateBacking): Observable<Boolean> {
        return Observable.just(true)
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
