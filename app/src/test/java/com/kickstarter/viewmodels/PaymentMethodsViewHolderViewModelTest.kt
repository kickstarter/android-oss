package com.kickstarter.viewmodels

import UserPaymentsQuery
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import org.junit.Test
import rx.observers.TestSubscriber
import type.CreditCardPaymentType
import type.CreditCardState
import type.CreditCardType
import java.util.*

class PaymentMethodsViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: PaymentMethodsViewHolderViewModel.ViewModel

    private val expirationDate = TestSubscriber<Date>()
    private val lastFour = TestSubscriber<String>()
    private val paymentType = TestSubscriber<CreditCardPaymentType>()
    private val type = TestSubscriber<CreditCardType>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = PaymentMethodsViewHolderViewModel.ViewModel(environment)

        this.vm.outputs.expirationDate().subscribe(this.expirationDate)
        this.vm.outputs.lastFour().subscribe(this.lastFour)
        this.vm.outputs.paymentType().subscribe(this.paymentType)
        this.vm.outputs.type().subscribe(this.type)
    }

    @Test
    fun testCardExpirationDate() {
        setUpEnvironment(environment())

        this.vm.inputs.addCards(UserPaymentsQuery.Node("", "", Date(), "", CreditCardState.ACTIVE,
                CreditCardPaymentType.CREDIT_CARD, CreditCardType.DISCOVER))

        this.expirationDate.assertValue(Date())
    }

    @Test
    fun testCardLastFourDigits() {
        setUpEnvironment(environment())

        this.vm.inputs.addCards(UserPaymentsQuery.Node("", "", Date(), "1234",
                CreditCardState.ACTIVE, CreditCardPaymentType.CREDIT_CARD, CreditCardType.MASTERCARD))

        this.lastFour.assertValue("1234")
    }

    @Test
    fun testCardPaymentType() {
        setUpEnvironment(environment())

        this.vm.inputs.addCards(UserPaymentsQuery.Node("", "", Date(), "",
                CreditCardState.ACTIVE, CreditCardPaymentType.ANDROID_PAY, CreditCardType.VISA))

        this.paymentType.assertValue(CreditCardPaymentType.ANDROID_PAY)
    }

    @Test
    fun testCardType() {
        setUpEnvironment(environment())

        this.vm.inputs.addCards(UserPaymentsQuery.Node("", "", Date(), "",
                CreditCardState.ACTIVE, CreditCardPaymentType.BANK_ACCOUNT, CreditCardType.DISCOVER))

        this.type.assertValue(CreditCardType.DISCOVER)
    }
}
