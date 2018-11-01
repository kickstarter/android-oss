package com.kickstarter.viewmodels

import UserPaymentsQuery
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import org.junit.Test
import rx.observers.TestSubscriber
import type.CreditCardPaymentType
import type.CreditCardState
import type.CreditCardTypes
import java.util.*



class PaymentMethodsViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: PaymentMethodsViewHolderViewModel.ViewModel

    private val expirationDate = TestSubscriber<String>()
    private val lastFour = TestSubscriber<String>()
    private val paymentType = TestSubscriber<CreditCardPaymentType>()
    private val type = TestSubscriber<Int>()

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
        val calendar = GregorianCalendar(2019, 2, 1)
        val date: Date = calendar.time

        this.vm.inputs.card(UserPaymentsQuery.Node("", "", date, "", CreditCardState.ACTIVE,
                CreditCardPaymentType.CREDIT_CARD, CreditCardTypes.DISCOVER))

        this.expirationDate.assertValue("03/2019")
    }

    @Test
    fun testCardLastFourDigits() {
        setUpEnvironment(environment())

        this.vm.inputs.card(UserPaymentsQuery.Node("", "", Date(), "1234",
                CreditCardState.ACTIVE, CreditCardPaymentType.CREDIT_CARD, CreditCardTypes.MASTERCARD))

        this.lastFour.assertValue("1234")
    }

    @Test
    fun testCardPaymentType() {
        setUpEnvironment(environment())

        this.vm.inputs.card(UserPaymentsQuery.Node("", "", Date(), "",
                CreditCardState.ACTIVE, CreditCardPaymentType.ANDROID_PAY, CreditCardTypes.VISA))

        this.paymentType.assertValue(CreditCardPaymentType.ANDROID_PAY)
    }

    @Test
    fun testCardType() {
        setUpEnvironment(environment())

        this.vm.inputs.card(UserPaymentsQuery.Node("", "", Date(), "",
                CreditCardState.ACTIVE, CreditCardPaymentType.BANK_ACCOUNT, CreditCardTypes.DISCOVER))

        this.type.assertValue(R.drawable.discover_md)
    }
}
