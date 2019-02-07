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
    private val dividerIsVisible = TestSubscriber<Boolean>()
    private val error = TestSubscriber<String>()
    private val progressBarIsVisible = TestSubscriber<Boolean>()
    private val showDeleteCardDialog = TestSubscriber<Void>()
    private val success = TestSubscriber<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = PaymentMethodsViewModel.ViewModel(environment)

        this.vm.outputs.error().subscribe(this.error)
        this.vm.outputs.cards().subscribe(this.cards)
        this.vm.outputs.dividerIsVisible().subscribe(this.dividerIsVisible)
        this.vm.outputs.progressBarIsVisible().subscribe(this.progressBarIsVisible)
        this.vm.outputs.showDeleteCardDialog().subscribe(this.showDeleteCardDialog)
        this.vm.outputs.success().subscribe(this.success)
    }

    @Test
    fun testCards() {
        val node = UserPaymentsQuery.Node("", "5555", Date(), "9876",
                CreditCardState.ACTIVE, CreditCardPaymentType.CREDIT_CARD, CreditCardTypes.MASTERCARD)

        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun getStoredCards(): Observable<UserPaymentsQuery.Data> {
                return Observable.just(UserPaymentsQuery.Data(UserPaymentsQuery.Me("",
                        UserPaymentsQuery.StoredCards("", List(1) { _ -> node }))))
            }
        }).build())

        this.cards.assertValue(Collections.singletonList(node))
    }

    @Test
    fun testDividerIsVisible_hasCards() {
        setUpEnvironment(environment())

        this.dividerIsVisible.assertValues(true)
    }

    @Test
    fun testDividerIsVisible_noCards() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun getStoredCards(): Observable<UserPaymentsQuery.Data> {
                return Observable.just(UserPaymentsQuery.Data(UserPaymentsQuery.Me("",
                        UserPaymentsQuery.StoredCards("", Collections.emptyList()))))
            }
        }).build())

        this.dividerIsVisible.assertValues(false)
    }

    @Test
    fun testErrorGettingCards() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun getStoredCards(): Observable<UserPaymentsQuery.Data> {
                return Observable.error(Exception("oops"))
            }
        }).build())

        this.cards.assertNoValues()
        this.error.assertNoValues()
    }

    @Test
    fun testErrorDeletingCard() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data> {
                return Observable.error(Throwable("eek"))
            }
        }).build())

        this.vm.inputs.deleteCardClicked("id")
        this.vm.confirmDeleteCardClicked()
        this.error.assertValue("eek")
    }

    @Test
    fun testProgressBarIsVisible() {
        setUpEnvironment(environment())

        //getting the cards initially
        this.progressBarIsVisible.assertValues(false)
        this.vm.inputs.deleteCardClicked("id")
        this.vm.inputs.confirmDeleteCardClicked()
        //make the call to delete and reload the cards
        this.progressBarIsVisible.assertValues( false, true, false)
    }

    @Test
    fun testShowDeleteCardDialog() {
        setUpEnvironment(environment())

        this.vm.inputs.deleteCardClicked("5555")
        this.showDeleteCardDialog.assertValueCount(1)
    }

    @Test
    fun testSuccess() {
        setUpEnvironment(environment())

        this.cards.assertValueCount(1)
        this.vm.inputs.deleteCardClicked("id")
        this.vm.inputs.confirmDeleteCardClicked()
        this.success.assertValueCount(1)
        this.cards.assertValueCount(2)
    }
}
