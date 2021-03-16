package com.kickstarter.mock.services

import CreatePasswordMutation
import DeletePaymentSourceMutation
import SendEmailVerificationMutation
import UpdateUserCurrencyMutation
import UpdateUserEmailMutation
import UpdateUserPasswordMutation
import UserPrivacyQuery
import com.kickstarter.mock.factories.*
import com.kickstarter.models.*
import com.kickstarter.services.ApolloClientType
import com.kickstarter.services.mutations.CreateBackingData
import com.kickstarter.services.mutations.SavePaymentMethodData
import com.kickstarter.services.mutations.UpdateBackingData
import rx.Observable
import type.CurrencyCode
import java.util.*

open class MockApolloClient : ApolloClientType {

    override fun getProjectBacking(slug: String): Observable<Backing> {
        return Observable.just(BackingFactory.backing())
    }

    override fun getProjectAddOns(slug: String, location: Location): Observable<List<Reward>> {
        val reward = RewardFactory.reward().toBuilder().isAddOn(true).quantity(2).build()
        return Observable.just(listOf(reward, reward))
    }

    override fun cancelBacking(backing: Backing, note: String): Observable<Any> {
        return Observable.just(true)
    }

    override fun createBacking(createBackingData: CreateBackingData): Observable<Checkout> {
        return Observable.just(CheckoutFactory.requiresAction(false))
    }

    override fun getBacking(backingId: String): Observable<Backing> {
        return Observable.just(BackingFactory.backing())
    }

    override fun clearUnseenActivity(): Observable<Int> {
        return Observable.just(0)
    }

    override fun createPassword(password: String, confirmPassword: String): Observable<CreatePasswordMutation.Data> {
        return Observable.just(
            CreatePasswordMutation.Data(
                CreatePasswordMutation.UpdateUserAccount(
                    "",
                    CreatePasswordMutation.User("", "sample@ksr.com", true)
                )
            )
        )
    }

    override fun creatorDetails(slug: String): Observable<CreatorDetails> {
        return Observable.just(CreatorDetailsFactory.creatorDetails())
    }

    override fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data> {
        return Observable.just(DeletePaymentSourceMutation.Data(DeletePaymentSourceMutation.PaymentSourceDelete("", "")))
    }

    override fun erroredBackings(): Observable<List<ErroredBacking>> {
        return Observable.just(Collections.singletonList(ErroredBackingFactory.erroredBacking()))
    }

    override fun getStoredCards(): Observable<List<StoredCard>> {
        return Observable.just(Collections.singletonList(StoredCardFactory.discoverCard()))
    }

    override fun savePaymentMethod(savePaymentMethodData: SavePaymentMethodData): Observable<StoredCard> {
        return Observable.just(StoredCardFactory.discoverCard())
    }

    override fun sendMessage(project: Project, recipient: User, body: String): Observable<Long> {
        return Observable.just(1L)
    }

    override fun sendVerificationEmail(): Observable<SendEmailVerificationMutation.Data> {
        return Observable.just(
            SendEmailVerificationMutation.Data(
                SendEmailVerificationMutation.UserSendEmailVerification(
                    "",
                    "12345"
                )
            )
        )
    }

    override fun updateBacking(updateBackingData: UpdateBackingData): Observable<Checkout> {
        return Observable.just(CheckoutFactory.requiresAction(false))
    }

    override fun updateUserCurrencyPreference(currency: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data> {
        return Observable.just(
            UpdateUserCurrencyMutation.Data(
                UpdateUserCurrencyMutation.UpdateUserProfile(
                    "",
                    UpdateUserCurrencyMutation.User("", "USD")
                )
            )
        )
    }

    override fun updateUserPassword(currentPassword: String, newPassword: String, confirmPassword: String): Observable<UpdateUserPasswordMutation.Data> {
        return Observable.just(
            UpdateUserPasswordMutation.Data(
                UpdateUserPasswordMutation.UpdateUserAccount(
                    "",
                    UpdateUserPasswordMutation.User("", "some@email.com", true)
                )
            )
        )
    }

    override fun updateUserEmail(email: String, currentPassword: String): Observable<UpdateUserEmailMutation.Data> {
        return Observable.just(
            UpdateUserEmailMutation.Data(
                UpdateUserEmailMutation.UpdateUserAccount(
                    "",
                    UpdateUserEmailMutation.User("", "Some Name", "some@email.com")
                )
            )
        )
    }

    override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
        return Observable.just(
            UserPrivacyQuery.Data(
                UserPrivacyQuery.Me(
                    "", "Some Name",
                    "some@email.com", true, true, true, true, "USD"
                )
            )
        )
    }
}
