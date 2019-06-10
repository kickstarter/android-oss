package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.StoredCardFactory
import org.junit.Test
import rx.observers.TestSubscriber
import java.util.*

class RewardLoadingCardViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: RewardLoadingCardViewHolderViewModel.ViewModel

    private val estimatedDelivery = TestSubscriber.create<String>()
    private val id = TestSubscriber.create<String>()
    private val issuerImage = TestSubscriber.create<Int>()
    private val lastFour = TestSubscriber.create<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = RewardLoadingCardViewHolderViewModel.ViewModel(environment)

        this.vm.outputs.expirationDate().subscribe(this.estimatedDelivery)
        this.vm.outputs.id().subscribe(this.id)
        this.vm.outputs.issuerImage().subscribe(this.issuerImage)
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
        this.vm.inputs.configureWith(creditCard)

        this.estimatedDelivery.assertValue("03/2019")
    }

    @Test
    fun testId() {
        setUpEnvironment(environment())
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(creditCard)

        this.id.assertValue(creditCard.id())
    }

    @Test
    fun testIssuerImage() {
        setUpEnvironment(environment())
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(creditCard)

        this.issuerImage.assertValue(R.drawable.discover_md)
    }

    @Test
    fun testLastFourDigits() {
        setUpEnvironment(environment())

        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(creditCard)

        this.lastFour.assertValue("1234")
    }

}
