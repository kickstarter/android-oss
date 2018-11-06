package com.kickstarter.viewmodels

import DeletePaymentSourceMutation
import UserPaymentsQuery
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.services.MockApolloClient
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import type.CreditCardPaymentType
import type.CreditCardState
import type.CreditCardTypes
import java.util.*

class PaymentMethodsViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: PaymentMethodsViewModel.ViewModel

    private val cards = TestSubscriber<MutableList<UserPaymentsQuery.Node>>()
    private val error = TestSubscriber<String>()
    private val showDeleteCardDialog = TestSubscriber<Void>()
    private val success = TestSubscriber<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = PaymentMethodsViewModel.ViewModel(environment)

        this.vm.outputs.error().subscribe(this.error)
        this.vm.outputs.getCards().subscribe(this.cards)
        this.vm.outputs.showDeleteCardDialog().subscribe(this.showDeleteCardDialog)
        this.vm.outputs.success().subscribe(this.success)
    }

    @Test
    fun testAPIError() {
        val apolloClient = object : MockApolloClient() {
            override fun getStoredCards(): Observable<UserPaymentsQuery.Data> {
                return Observable.error(Exception("oops"))
            }
        }

        setUpEnvironment(environment().toBuilder().apolloClient(apolloClient).build())

        this.vm.inputs.confirmDeleteCardClicked()
    }

    @Test
    fun testErrors() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun getStoredCards(): Observable<UserPaymentsQuery.Data> {
                return Observable.error(Throwable("error"))
            }

            override fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data> {
                return Observable.error(Throwable("error"))
            }
        }).build())


        this.cards.assertNoValues()
        this.error.assertNoValues()

        this.vm.inputs.deleteCardClicked("error")
        this.vm.confirmDeleteCardClicked()
        this.error.assertValue("error")
    }


    @Test
    fun testGetCards() {
        val node = UserPaymentsQuery.Node("", "5555", Date(), "9876",
                CreditCardState.ACTIVE, CreditCardPaymentType.CREDIT_CARD, CreditCardTypes.MASTERCARD)

        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun getStoredCards(): Observable<UserPaymentsQuery.Data> {
                return Observable.just(UserPaymentsQuery.Data(UserPaymentsQuery.Me("",
                        UserPaymentsQuery.StoredCards("", List(1
                        ) { _ -> node }))))
            }
        }).build())

        this.cards.assertValue(Collections.singletonList(node))
    }

    @Test
    fun testDeleteCards() {
        val node = UserPaymentsQuery.Node("", "5555", Date(), "9876",
                CreditCardState.ACTIVE, CreditCardPaymentType.CREDIT_CARD, CreditCardTypes.MASTERCARD)

        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun getStoredCards(): Observable<UserPaymentsQuery.Data> {
                return Observable.just(UserPaymentsQuery.Data(UserPaymentsQuery.Me("",
                        UserPaymentsQuery.StoredCards("", List(1
                        ) { _ -> node }))))
            }
        }).build())

        this.cards.assertValue(Collections.singletonList(node))
        this.vm.inputs.deleteCardClicked("5555")
        this.vm.inputs.confirmDeleteCardClicked()
        this.cards.assertValueCount(0)
        this.success.assertValueCount(1)

    }

    @Test
    fun testSuccess() {
        val node = UserPaymentsQuery.Node("", "5555", Date(), "9876",
                CreditCardState.ACTIVE, CreditCardPaymentType.CREDIT_CARD, CreditCardTypes.MASTERCARD)

        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data> {
                return super.deletePaymentSource(paymentSourceId)
            }
        }).build())

        this.cards.assertValueCount(1)
    }
}
