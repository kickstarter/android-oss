package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.StoredCardFactory
import org.junit.Test
import rx.observers.TestSubscriber
import java.util.*

class RewardCardViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: RewardCardViewHolderViewModel.ViewModel

    private val buttonCTA = TestSubscriber.create<Int>()
    private val buttonEnabled = TestSubscriber.create<Boolean>()
    private val expirationDate = TestSubscriber.create<String>()
    private val id = TestSubscriber.create<String>()
    private val issuerImage = TestSubscriber.create<Int>()
    private val lastFour = TestSubscriber.create<String>()
    private val notAvailableCopyIsVisible = TestSubscriber.create<Boolean>()
    private val projectCountry = TestSubscriber.create<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = RewardCardViewHolderViewModel.ViewModel(environment)

        this.vm.outputs.buttonCTA().subscribe(this.buttonCTA)
        this.vm.outputs.buttonEnabled().subscribe(this.buttonEnabled)
        this.vm.outputs.expirationDate().subscribe(this.expirationDate)
        this.vm.outputs.id().subscribe(this.id)
        this.vm.outputs.issuerImage().subscribe(this.issuerImage)
        this.vm.outputs.lastFour().subscribe(this.lastFour)
        this.vm.outputs.notAvailableCopyIsVisible().subscribe(this.notAvailableCopyIsVisible)
        this.vm.outputs.projectCountry().subscribe(this.projectCountry)
    }

    @Test
    fun testButtonCTA_whenCardIsAccepted() {
        setUpEnvironment(environment())
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.buttonCTA.assertValue(R.string.Select)
    }

    @Test
    fun testButtonCTA_whenCardNotAccepted() {
        setUpEnvironment(environment())

        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.mxProject()))

        this.buttonCTA.assertValue(R.string.Not_available)
    }

    @Test
    fun testButtonEnabled_whenCardIsAccepted() {
        setUpEnvironment(environment())
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.buttonEnabled.assertValue(true)
    }

    @Test
    fun testButtonEnabled_whenCardNotAccepted() {
        setUpEnvironment(environment())

        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.mxProject()))

        this.buttonEnabled.assertValue(false)
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
        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.expirationDate.assertValue("03/2019")
    }

    @Test
    fun testId() {
        setUpEnvironment(environment())
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.id.assertValue(creditCard.id())
    }

    @Test
    fun testIssuerImage() {
        setUpEnvironment(environment())
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.issuerImage.assertValue(R.drawable.discover_md)
    }

    @Test
    fun testLastFourDigits() {
        setUpEnvironment(environment())

        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.lastFour.assertValue("1234")
    }

    @Test
    fun testNotAvailableCopyIsVisible_whenCardIsAccepted() {
        setUpEnvironment(environment())

        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.notAvailableCopyIsVisible.assertValue(false)
    }

    @Test
    fun testNotAvailableCopyIsVisible_whenCardIsNotAccepted() {
        setUpEnvironment(environment())

        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.mxProject()))

        this.notAvailableCopyIsVisible.assertValue(true)
    }

    @Test
    fun testProjectCountry_whenCardIsAccepted() {
        setUpEnvironment(environment())

        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.projectCountry.assertNoValues()
    }

    @Test
    fun testProjectCountry_whenCardIsNotAccepted() {
        setUpEnvironment(environment())

        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.mxProject()))

        this.projectCountry.assertValue("Mexico City, Mexico")
    }

}
