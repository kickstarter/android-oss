package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.StoredCardFactory
import org.junit.Test
import rx.observers.TestSubscriber
import java.util.*

class PaymentMethodsViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: PaymentMethodsViewHolderViewModel.ViewModel

    private val cardIssuer = TestSubscriber<Int>()
    private val expirationDate = TestSubscriber<String>()
    private val lastFour = TestSubscriber<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = PaymentMethodsViewHolderViewModel.ViewModel(environment)

        this.vm.outputs.cardIssuer().subscribe(this.cardIssuer)
        this.vm.outputs.expirationDate().subscribe(this.expirationDate)
        this.vm.outputs.lastFour().subscribe(this.lastFour)
    }

    @Test
    fun testCardExpirationDate() {
        setUpEnvironment(environment())
        val calendar = GregorianCalendar(2019, 2, 1)
        val date: Date = calendar.time

        val creditCard = StoredCardFactory.discoverCard()
                .toBuilder()
                .expiration(date)
                .build()

        this.vm.inputs.card(creditCard)

        this.expirationDate.assertValue("03/2019")
    }

    @Test
    fun testCardLastFourDigits() {
        setUpEnvironment(environment())
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.card(creditCard)

        this.lastFour.assertValue("1234")
    }

    @Test
    fun testCardType() {
        setUpEnvironment(environment())
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.card(creditCard)

        this.cardIssuer.assertValue(R.drawable.discover_md)
    }
}
