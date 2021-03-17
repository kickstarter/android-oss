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
import com.kickstarter.models.StoredCard
import com.stripe.android.model.Card
import org.junit.Test
import rx.observers.TestSubscriber
import java.util.Date
import java.util.GregorianCalendar

class RewardCardUnselectedViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: RewardCardUnselectedViewHolderViewModel.ViewModel

    private val expirationDate = TestSubscriber.create<String>()
    private val isClickable = TestSubscriber.create<Boolean>()
    private val issuer = TestSubscriber.create<String>()
    private val issuerImage = TestSubscriber.create<Int>()
    private val issuerImageAlpha = TestSubscriber.create<Float>()
    private val lastFourTextColor = TestSubscriber.create<Int>()
    private val lastFour = TestSubscriber.create<String>()
    private val notAvailableCopyIsVisible = TestSubscriber.create<Boolean>()
    private val notifyDelegateCardSelected = TestSubscriber.create<Pair<StoredCard, Int>>()
    private val retryCopyIsVisible = TestSubscriber.create<Boolean>()
    private val selectImageIsVisible = TestSubscriber.create<Boolean>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = RewardCardUnselectedViewHolderViewModel.ViewModel(environment)

        this.vm.outputs.expirationDate().subscribe(this.expirationDate)
        this.vm.outputs.isClickable().subscribe(this.isClickable)
        this.vm.outputs.issuer().subscribe(this.issuer)
        this.vm.outputs.issuerImage().subscribe(this.issuerImage)
        this.vm.outputs.issuerImageAlpha().subscribe(this.issuerImageAlpha)
        this.vm.outputs.lastFourTextColor().subscribe(this.lastFourTextColor)
        this.vm.outputs.lastFour().subscribe(this.lastFour)
        this.vm.outputs.notAvailableCopyIsVisible().subscribe(this.notAvailableCopyIsVisible)
        this.vm.outputs.notifyDelegateCardSelected().subscribe(this.notifyDelegateCardSelected)
        this.vm.outputs.retryCopyIsVisible().subscribe(this.retryCopyIsVisible)
        this.vm.outputs.selectImageIsVisible().subscribe(this.selectImageIsVisible)
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
    fun testIsClickable_whenCardIsAllowedType() {
        setUpEnvironment(environment())
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.isClickable.assertValue(true)
    }

    @Test
    fun testIsClickable_whenCardIsNotAllowedType() {
        setUpEnvironment(environment())
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.mxProject()))

        this.isClickable.assertValue(false)
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
    fun testIssuerImageAlpha_whenCardIsAllowedType() {
        setUpEnvironment(environment())
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.issuerImageAlpha.assertValue(1.0f)
    }

    @Test
    fun testIssuerImageAlpha_whenCardIsNotAllowedType() {
        setUpEnvironment(environment())
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.mxProject()))

        this.issuerImageAlpha.assertValue(.5f)
    }

    @Test
    fun testLastFourDigits() {
        setUpEnvironment(environment())

        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.lastFour.assertValue("1234")
    }

    @Test
    fun testLastFourTextColor_whenCardIsAllowedType() {
        setUpEnvironment(environment())
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.lastFourTextColor.assertValue(R.color.text_primary)
    }

    @Test
    fun testLastFourTextColor_whenCardIsNotAllowedType() {
        setUpEnvironment(environment())
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.mxProject()))

        this.lastFourTextColor.assertValue(R.color.text_secondary)
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
    fun testNotifyDelegateCardSelected() {
        setUpEnvironment(environment())

        val creditCard = StoredCardFactory.visa()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.vm.inputs.cardSelected(2)
        this.notifyDelegateCardSelected.assertValue(Pair(creditCard, 2))
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

    @Test
    fun testSelectImageIsVisible_whenCardIsAllowedType() {
        setUpEnvironment(environment())
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.selectImageIsVisible.assertValue(true)
    }

    @Test
    fun testSelectImageIsVisible_whenCardIsNotAllowedType() {
        setUpEnvironment(environment())
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.mxProject()))

        this.selectImageIsVisible.assertValue(false)
    }
}
