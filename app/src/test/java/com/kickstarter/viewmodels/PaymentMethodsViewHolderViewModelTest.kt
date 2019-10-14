package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.StoredCardFactory
import com.stripe.android.model.Card
import org.junit.Test
import rx.observers.TestSubscriber
import java.util.*

class PaymentMethodsViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: PaymentMethodsViewHolderViewModel.ViewModel

    private val id = TestSubscriber<String>()
    private val issuer = TestSubscriber<String>()
    private val issuerImage = TestSubscriber<Int>()
    private val expirationDate = TestSubscriber<String>()
    private val lastFour = TestSubscriber<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = PaymentMethodsViewHolderViewModel.ViewModel(environment)

        this.vm.outputs.id().subscribe(this.id)
        this.vm.outputs.issuer().subscribe(this.issuer)
        this.vm.outputs.issuerImage().subscribe(this.issuerImage)
        this.vm.outputs.expirationDate().subscribe(this.expirationDate)
        this.vm.outputs.lastFour().subscribe(this.lastFour)
    }

    @Test
    fun testExpirationDate() {
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
    fun testLastFour() {
        setUpEnvironment(environment())
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.card(creditCard)

        this.lastFour.assertValue("1234")
    }

    @Test
    fun testIssuer() {
        setUpEnvironment(environment())
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.card(creditCard)

        this.issuer.assertValue(Card.CardBrand.DISCOVER)
    }

    @Test
    fun testIssuerImage() {
        setUpEnvironment(environment())
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.card(creditCard)

        this.issuerImage.assertValue(R.drawable.discover_md)
    }
}
