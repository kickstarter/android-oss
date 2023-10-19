package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.StoredCardFactory
import com.stripe.android.model.CardBrand
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test
import java.util.Date
import java.util.GregorianCalendar

class PaymentMethodsViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: PaymentMethodsViewHolderViewModel.PaymentMethodsViewHolderViewModel

    private val id = TestSubscriber<String>()
    private val issuer = TestSubscriber<String>()
    private val issuerImage = TestSubscriber<Int>()
    private val expirationDate = TestSubscriber<String>()
    private val lastFour = TestSubscriber<String>()
    private val disposables = CompositeDisposable()

    private fun setUpEnvironment() {
        this.vm = PaymentMethodsViewHolderViewModel.PaymentMethodsViewHolderViewModel()

        this.vm.outputs.id().subscribe { this.id.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.issuer().subscribe { this.issuer.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.issuerImage().subscribe { this.issuerImage.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.expirationDate().subscribe { this.expirationDate.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.lastFour().subscribe { this.lastFour.onNext(it) }
            .addToDisposable(disposables)
    }

    @Test
    fun testExpirationDate() {
        setUpEnvironment()
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
        setUpEnvironment()
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.card(creditCard)

        this.lastFour.assertValue("1234")
    }

    @Test
    fun testIssuer() {
        setUpEnvironment()
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.card(creditCard)

        this.issuer.assertValue(CardBrand.Discover.code)
    }

    @Test
    fun testIssuerImage() {
        setUpEnvironment()
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.card(creditCard)

        this.issuerImage.assertValue(R.drawable.discover_md)
    }

    @After
    fun clear() {
        disposables.clear()
    }
}
