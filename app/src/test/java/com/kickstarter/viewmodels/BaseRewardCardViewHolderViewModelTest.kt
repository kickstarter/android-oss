package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.PaymentSourceFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.StoredCardFactory
import com.kickstarter.models.Backing
import com.stripe.android.model.CardBrand
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test
import java.util.Date
import java.util.GregorianCalendar

class BaseRewardCardViewHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: BaseRewardCardViewHolderViewModel.ViewModel

    private val expirationDate = TestSubscriber.create<String>()
    private val issuer = TestSubscriber.create<String>()
    private val issuerImage = TestSubscriber.create<Int>()
    private val lastFour = TestSubscriber.create<String>()
    private val retryCopyIsVisible = TestSubscriber.create<Boolean>()
    private val expirationIsGone = TestSubscriber.create<Boolean>()
    private val disposables = CompositeDisposable()

    @After
    fun clear() {
        disposables.clear()
    }

    private fun setupEnvironment() {
        this.vm = BaseRewardCardViewHolderViewModel.ViewModel()

        this.vm.expirationDate().subscribe { this.expirationDate.onNext(it) }.addToDisposable(disposables)
        this.vm.issuer().subscribe { this.issuer.onNext(it) }.addToDisposable(disposables)
        this.vm.issuerImage().subscribe { this.issuerImage.onNext(it) }.addToDisposable(disposables)
        this.vm.lastFour().subscribe { this.lastFour.onNext(it) }.addToDisposable(disposables)
        this.vm.retryCopyIsVisible().subscribe { this.retryCopyIsVisible.onNext(it) }.addToDisposable(disposables)
        this.vm.expirationIsGone().subscribe { this.expirationIsGone.onNext(it) }.addToDisposable(disposables)
    }

    @Test
    fun expirationDate_whenExpirationNull_shouldNotEmitValue() {
        setupEnvironment()
        val creditCard = StoredCardFactory.discoverCard()
                .toBuilder()
                .expiration(null)
                .build()
        this.vm.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.expirationDate.assertNoValues()
    }
    @Test
    fun expirationDate_whenExpirationNotNull_shouldEmitCorrectValue() {
        setupEnvironment()
        val calendar = GregorianCalendar(2023, 10, 20)
        val date: Date = calendar.time

        val creditCard = StoredCardFactory.discoverCard()
                .toBuilder()
                .expiration(date)
                .build()
        this.vm.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.expirationDate.assertValue("11/2023")
    }

    @Test
    fun expirationIsGone_whenExpirationNull_emitsTrue() {
        setupEnvironment()
        val creditCard = StoredCardFactory.discoverCard()
                .toBuilder()
                .expiration(null)
                .build()
        this.vm.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.expirationIsGone.assertValue(true)
    }

    @Test
    fun expirationIsGone_whenExpirationNotNull_emitsFalse() {
        setupEnvironment()
        val calendar = GregorianCalendar(2023, 10, 20)
        val date: Date = calendar.time

        val creditCard = StoredCardFactory.discoverCard()
                .toBuilder()
                .expiration(date)
                .build()
        this.vm.configureWith(Pair(creditCard, ProjectFactory.project()))
        this.expirationIsGone.assertValue(false)
    }

    @Test
    fun lastFour_whenLastFourNotNull_emitsValue() {
        setupEnvironment()
        val lastFourDigits = "1203"

        val creditCard = StoredCardFactory.discoverCard()
                .toBuilder()
                .lastFourDigits(lastFourDigits)
                .build()
        this.vm.configureWith(Pair(creditCard, ProjectFactory.project()))
        this.lastFour.assertValue(lastFourDigits)
    }

    @Test
    fun lastFour_whenLastFourNull_emitsNoValue() {
        setupEnvironment()

        val creditCard = StoredCardFactory.discoverCard()
                .toBuilder()
                .lastFourDigits(null)
                .build()
        this.vm.configureWith(Pair(creditCard, ProjectFactory.project()))
        this.lastFour.assertNoValues()
    }

    @Test
    fun cardType_whenCardAndTypeNotNull_emitsCardValues() {
        setupEnvironment()

        val creditCard = StoredCardFactory.discoverCard()
                .toBuilder()
                .build()

        this.vm.configureWith(Pair(creditCard, ProjectFactory.project()))
        this.issuerImage.assertValue(R.drawable.discover_md)
        this.issuer.assertValue(CardBrand.Discover.code)
    }

    @Test
    fun cardType_whenCardNull_emitsNoCardValues() {
        setupEnvironment()

        this.vm.configureWith(Pair(null, ProjectFactory.project()))
        this.issuerImage.assertNoValues()
        this.issuer.assertNoValues()
    }

    @Test
    fun cardType_whenCardTypeNull_emitsNoDrawableButNoCard() {
        setupEnvironment()

        val creditCard = StoredCardFactory.discoverCard()
                .toBuilder()
                .type(null)
                .build()

        this.vm.configureWith(Pair(creditCard, ProjectFactory.project()))
        this.issuerImage.assertValue(R.drawable.generic_bank_md)
        this.issuer.assertNoValues()
    }

    @Test
    fun retryCopyVisible_whenBackingNull_emitNoValues() {
        setupEnvironment()

        this.vm.configureWith(Pair(StoredCardFactory.discoverCard(), ProjectFactory.project()))
        this.retryCopyIsVisible.assertNoValues()
    }

    @Test
    fun retryCopyVisible_whenPaymentSourceNull_emitFalse() {
        setupEnvironment()

        val backing = BackingFactory.backing().toBuilder()
                .paymentSource(null)
                .build()

        this.vm.configureWith(Pair(StoredCardFactory.discoverCard(), ProjectFactory.project().toBuilder().backing(backing).build()))
        this.retryCopyIsVisible.assertValue(false)
    }

    @Test
    fun retryCopyVisible_whenCardIdsDontMatch_emitFalse() {
        setupEnvironment()

        val backing = BackingFactory.backing().toBuilder()
                .paymentSource(PaymentSourceFactory.visa().toBuilder().id("1234532").build())
                .build()

        this.vm.configureWith(Pair(StoredCardFactory.discoverCard().toBuilder().id("193408").build(), ProjectFactory.project().toBuilder().backing(backing).build()))
        this.retryCopyIsVisible.assertValue(false)
    }

    @Test
    fun retryCopyVisible_whenBackingIsNotErrored_emitFalse() {
        setupEnvironment()

        val id = "89274"

        val backing = BackingFactory.backing().toBuilder()
                .paymentSource(PaymentSourceFactory.visa().toBuilder().id(id).build())
                .status(Backing.STATUS_PLEDGED)
                .build()

        this.vm.configureWith(Pair(StoredCardFactory.visa().toBuilder().id(id).build(), ProjectFactory.project().toBuilder().backing(backing).build()))
        this.retryCopyIsVisible.assertValue(false)
    }

    @Test
    fun retryCopyVisible_whenBackingIsErroredAndPaymentIdsMatch_emitTrue() {
        setupEnvironment()

        val id = "89274"

        val backing = BackingFactory.backing().toBuilder()
                .paymentSource(PaymentSourceFactory.visa().toBuilder().id(id).build())
                .status(Backing.STATUS_ERRORED)
                .build()

        this.vm.configureWith(Pair(StoredCardFactory.visa().toBuilder().id(id).build(), ProjectFactory.project().toBuilder().backing(backing).build()))
        this.retryCopyIsVisible.assertValue(true)
    }
}