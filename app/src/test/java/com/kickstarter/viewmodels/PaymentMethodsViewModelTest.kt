package com.kickstarter.viewmodels

import UserPaymentsQuery
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.services.MockApolloClient
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import type.CreditCardPaymentType
import type.CreditCardState
import type.CreditCardType
import java.util.*

class PaymentMethodsViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: PaymentMethodsViewModel.ViewModel

    private val cards = TestSubscriber<MutableList<UserPaymentsQuery.Node>>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = PaymentMethodsViewModel.ViewModel(environment)

        this.vm.outputs.getCards().subscribe(this.cards)
    }

    @Test
    fun testGetCards() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun getStoredCards(): Observable<UserPaymentsQuery.Data> {
                return Observable.just(UserPaymentsQuery.Data(UserPaymentsQuery.Me("",
                        UserPaymentsQuery.StoredCards("", List(1
                        ) { _ -> UserPaymentsQuery.Node("","5555", Date(), "9876",
                                CreditCardState.ACTIVE, CreditCardPaymentType.CREDIT_CARD, CreditCardType.MASTERCARD )}))))
            }
        }).build())

        this.cards.assertValueCount(1)
    }
}
