package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.PaymentSourceFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.StoredCardFactory
import com.kickstarter.models.Backing
import com.stripe.android.model.Card
import org.junit.Test
import rx.observers.TestSubscriber
import java.util.*

class RewardCardViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: RewardCardViewHolderViewModel.ViewModel

    private val buttonCTA = TestSubscriber.create<Int>()
    private val buttonEnabled = TestSubscriber.create<Boolean>()
    private val expirationDate = TestSubscriber.create<String>()
    private val failedIndicatorIconIsVisible = TestSubscriber.create<Boolean>()
    private val id = TestSubscriber.create<String>()
    private val issuer = TestSubscriber.create<String>()
    private val issuerImage = TestSubscriber.create<Int>()
    private val lastFour = TestSubscriber.create<String>()
    private val notAvailableCopyIsVisible = TestSubscriber.create<Boolean>()
    private val projectCountry = TestSubscriber.create<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = RewardCardViewHolderViewModel.ViewModel(environment)

        this.vm.outputs.buttonCTA().subscribe(this.buttonCTA)
        this.vm.outputs.buttonEnabled().subscribe(this.buttonEnabled)
        this.vm.outputs.expirationDate().subscribe(this.expirationDate)
        this.vm.outputs.failedIndicatorIconIsVisible().subscribe(this.failedIndicatorIconIsVisible)
        this.vm.outputs.id().subscribe(this.id)
        this.vm.outputs.issuer().subscribe(this.issuer)
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
    fun testButtonCTA_whenCardIsBackingPaymentSource() {
        setUpEnvironment(environment())
        val visa = StoredCardFactory.visa()

        val paymentSource = PaymentSourceFactory.visa()
                .toBuilder()
                .id(visa.id())
                .build()
        val backing = BackingFactory.backing()
                .toBuilder()
                .paymentSource(paymentSource)
                .build()
        val project = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        this.vm.inputs.configureWith(Pair(visa, project))

        this.buttonCTA.assertValue(R.string.Selected)
    }

    @Test
    fun testButtonCTA_whenCardIsNotBackingPaymentSource() {
        setUpEnvironment(environment())
        val discover = StoredCardFactory.discoverCard()

        val backing = BackingFactory.backing()
                .toBuilder()
                .paymentSource(PaymentSourceFactory.visa())
                .build()
        val project = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        this.vm.inputs.configureWith(Pair(discover, project))

        this.buttonCTA.assertValue(R.string.Select)
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
    fun testButtonEnabled_whenCardIsBackingPaymentSource() {
        setUpEnvironment(environment())
        val visa = StoredCardFactory.visa()

        val paymentSource = PaymentSourceFactory.visa()
                .toBuilder()
                .id(visa.id())
                .build()
        val backing = BackingFactory.backing()
                .toBuilder()
                .paymentSource(paymentSource)
                .build()
        val project = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        this.vm.inputs.configureWith(Pair(visa, project))

        this.buttonEnabled.assertValue(false)
    }

    @Test
    fun testButtonEnabled_whenCardNotBackingPaymentSource() {
        setUpEnvironment(environment())
        val discover = StoredCardFactory.discoverCard()

        val backing = BackingFactory.backing()
                .toBuilder()
                .paymentSource(PaymentSourceFactory.visa())
                .build()
        val project = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        this.vm.inputs.configureWith(Pair(discover, project))

        this.buttonEnabled.assertValue(true)
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
    fun testFailedIndicatorIconIsVisible_whenCardIsBackingPaymentSource_backingIsErrored() {
        setUpEnvironment(environment())
        val visa = StoredCardFactory.visa()

        val paymentSource = PaymentSourceFactory.visa()
                .toBuilder()
                .id(visa.id())
                .build()
        val backing = BackingFactory.backing()
                .toBuilder()
                .paymentSource(paymentSource)
                .status(Backing.STATUS_ERRORED)
                .build()
        val project = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        this.vm.inputs.configureWith(Pair(visa, project))

        this.failedIndicatorIconIsVisible.assertValue(true)
    }

    @Test
    fun testFailedIndicatorIconIsVisible_whenCardIsBackingPaymentSource_backingIsNotErrored() {
        setUpEnvironment(environment())
        val visa = StoredCardFactory.visa()

        val paymentSource = PaymentSourceFactory.visa()
                .toBuilder()
                .id(visa.id())
                .build()
        val backing = BackingFactory.backing()
                .toBuilder()
                .paymentSource(paymentSource)
                .build()
        val project = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        this.vm.inputs.configureWith(Pair(visa, project))

        this.failedIndicatorIconIsVisible.assertValue(false)
    }

    @Test
    fun testFailedIndicatorIconIsVisible_whenCardIsNotBackingPaymentSource_backingIsErrored() {
        setUpEnvironment(environment())
        val discover = StoredCardFactory.discoverCard()

        val backing = BackingFactory.backing()
                .toBuilder()
                .paymentSource(PaymentSourceFactory.visa())
                .status(Backing.STATUS_ERRORED)
                .build()
        val project = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        this.vm.inputs.configureWith(Pair(discover, project))

        this.failedIndicatorIconIsVisible.assertValue(false)
    }

    @Test
    fun testFailedIndicatorIconIsVisible_whenCardIsNotBackingPaymentSource_backingIsNotErrored() {
        setUpEnvironment(environment())
        val discover = StoredCardFactory.discoverCard()

        val backing = BackingFactory.backing()
                .toBuilder()
                .paymentSource(PaymentSourceFactory.visa())
                .build()
        val project = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        this.vm.inputs.configureWith(Pair(discover, project))

        this.failedIndicatorIconIsVisible.assertValue(false)
    }

    @Test
    fun testId() {
        setUpEnvironment(environment())
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.id.assertValue(creditCard.id())
    }

    @Test
    fun testIssuer() {
        setUpEnvironment(environment())
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.issuer.assertValue(Card.CardBrand.DISCOVER)
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
    fun testProjectCountry() {
        setUpEnvironment(environment())

        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.projectCountry.assertValue("United States")
    }

}
