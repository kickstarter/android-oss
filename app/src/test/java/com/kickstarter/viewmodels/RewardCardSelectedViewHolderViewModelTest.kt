package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.StoredCardFactory
import com.stripe.android.model.CardBrand
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test
import java.util.Date
import java.util.GregorianCalendar

class RewardCardSelectedViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: RewardCardSelectedViewHolderViewModel.ViewModel

    private val expirationDate = TestSubscriber.create<String>()
    private val issuer = TestSubscriber.create<String>()
    private val issuerImage = TestSubscriber.create<Int>()
    private val lastFour = TestSubscriber.create<String>()
    private val disposables = CompositeDisposable()

    private fun setUpEnvironment() {
        this.vm = RewardCardSelectedViewHolderViewModel.ViewModel()

        this.vm.outputs.expirationDate().subscribe { this.expirationDate.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.issuer().subscribe { this.issuer.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.issuerImage().subscribe { this.issuerImage.onNext(it) }
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
        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.expirationDate.assertValue("03/2019")
    }

    @Test
    fun testIssuerImage() {
        setUpEnvironment()
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.issuerImage.assertValue(R.drawable.discover_md)
    }

    @Test
    fun testIssuer() {
        setUpEnvironment()
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.issuer.assertValue(CardBrand.Discover.code)
    }

    @Test
    fun testLastFourDigits() {
        setUpEnvironment()

        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.lastFour.assertValue("1234")
    }

    @After
    fun clear() {
        disposables.clear()
    }
}
