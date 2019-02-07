package com.kickstarter.mock.services

import CreatePasswordMutation
import type.CurrencyCode

import DeletePaymentSourceMutation
import SavePaymentMethodMutation
import SendEmailVerificationMutation
import UpdateUserCurrencyMutation
import UpdateUserEmailMutation
import UpdateUserPasswordMutation
import UserPaymentsQuery
import UserPrivacyQuery
import com.kickstarter.services.ApolloClientType
import rx.Observable
import type.*
import java.util.*

open class MockApolloClient : ApolloClientType {
    override fun createPassword(password: String, confirmPassword: String): Observable<CreatePasswordMutation.Data> {
        return Observable.just(CreatePasswordMutation.Data(CreatePasswordMutation.UpdateUserAccount("",
                CreatePasswordMutation.User("", "sample@ksr.com", true))))
    }


    override fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data> {
        return Observable.just(DeletePaymentSourceMutation.Data(DeletePaymentSourceMutation.PaymentSourceDelete("", "")))
    }

    override fun getStoredCards(): Observable<UserPaymentsQuery.Data> {
        return Observable.just(UserPaymentsQuery.Data(UserPaymentsQuery.Me("",
                UserPaymentsQuery.StoredCards("", List(1)
                { _ ->
                    UserPaymentsQuery.Node("", "4333", Date(), "1234",
                            CreditCardState.ACTIVE, CreditCardPaymentType.CREDIT_CARD, CreditCardTypes.VISA)
                }))))
    }

    override fun savePaymentMethod(paymentTypes: PaymentTypes, stripeToken: String, cardId: String): Observable<SavePaymentMethodMutation.Data> {
        return Observable.just(SavePaymentMethodMutation.Data(SavePaymentMethodMutation.CreatePaymentSource("", null, true)))
    }

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
