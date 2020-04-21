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

class RewardCardSelectedViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: RewardCardSelectedViewHolderViewModel.ViewModel

    private val expirationDate = TestSubscriber.create<String>()
    private val issuer = TestSubscriber.create<String>()
    private val issuerImage = TestSubscriber.create<Int>()
    private val lastFour = TestSubscriber.create<String>()
    private val retryCopyIsVisible = TestSubscriber.create<Boolean>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = RewardCardSelectedViewHolderViewModel.ViewModel(environment)

        this.vm.outputs.expirationDate().subscribe(this.expirationDate)
        this.vm.outputs.issuer().subscribe(this.issuer)
        this.vm.outputs.issuerImage().subscribe(this.issuerImage)
        this.vm.outputs.lastFour().subscribe(this.lastFour)
        this.vm.outputs.retryCopyIsVisible().subscribe(this.retryCopyIsVisible)
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
    fun testIssuerImage() {
        setUpEnvironment(environment())
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.issuerImage.assertValue(R.drawable.discover_md)
    }

    @Test
    fun testIssuer() {
        setUpEnvironment(environment())
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.issuer.assertValue(Card.CardBrand.DISCOVER)
    }

    @Test
    fun testLastFourDigits() {
        setUpEnvironment(environment())

        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.lastFour.assertValue("1234")
    }

    @Test
    fun testRetryCopyIsVisible_whenCardIsBackingPaymentSource_backingIsErrored() {
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

        this.retryCopyIsVisible.assertValue(true)
    }

    @Test
    fun testRetryCopyIsVisible_whenCardIsBackingPaymentSource_backingIsNotErrored() {
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

        this.retryCopyIsVisible.assertValue(false)
    }

    @Test
    fun testRetryCopyIsVisible_whenCardIsNotBackingPaymentSource_backingIsErrored() {
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

        this.retryCopyIsVisible.assertValue(false)
    }

    @Test
    fun testRetryCopyIsVisible_whenCardIsNotBackingPaymentSource_backingIsNotErrored() {
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

        this.retryCopyIsVisible.assertValue(false)
    }

}
