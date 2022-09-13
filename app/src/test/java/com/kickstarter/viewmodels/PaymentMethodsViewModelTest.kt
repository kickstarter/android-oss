package com.kickstarter.viewmodels

import DeletePaymentSourceMutation
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.StoredCardFactory
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.models.Project
import com.kickstarter.models.StoredCard
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import java.util.Collections

class PaymentMethodsViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: PaymentMethodsViewModel.ViewModel

    private val cards = TestSubscriber<List<StoredCard>>()
    private val dividerIsVisible = TestSubscriber<Boolean>()
    private val error = TestSubscriber<String>()
    private val progressBarIsVisible = TestSubscriber<Boolean>()
    private val showDeleteCardDialog = TestSubscriber<Void>()
    private val success = TestSubscriber<String>()
    private val presentPaymentSheet = TestSubscriber<String>()
    private val showError = TestSubscriber<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = PaymentMethodsViewModel.ViewModel(environment)

        this.vm.outputs.error().subscribe(this.error)
        this.vm.outputs.cards().subscribe(this.cards)
        this.vm.outputs.dividerIsVisible().subscribe(this.dividerIsVisible)
        this.vm.outputs.progressBarIsVisible().subscribe(this.progressBarIsVisible)
        this.vm.outputs.showDeleteCardDialog().subscribe(this.showDeleteCardDialog)
        this.vm.outputs.success().subscribe(this.success)
        this.vm.outputs.presentPaymentSheet().subscribe(this.presentPaymentSheet)
        this.vm.outputs.showError().subscribe(this.showError)
    }

    @Test
    fun testCards() {
        val card = StoredCardFactory.discoverCard()

        setUpEnvironment(
            environment().toBuilder().apolloClient(object : MockApolloClient() {
                override fun getStoredCards(): Observable<List<StoredCard>> {
                    return Observable.just(Collections.singletonList(card))
                }
            }).build()
        )

        this.cards.assertValue(Collections.singletonList(card))
    }

    @Test
    fun testDividerIsVisible_hasCards() {
        setUpEnvironment(environment())

        this.dividerIsVisible.assertValues(true)
    }

    @Test
    fun testDividerIsVisible_noCards() {
        setUpEnvironment(
            environment().toBuilder().apolloClient(object : MockApolloClient() {
                override fun getStoredCards(): Observable<List<StoredCard>> {
                    return Observable.just(Collections.emptyList())
                }
            }).build()
        )

        this.dividerIsVisible.assertValues(false)
    }

    @Test
    fun testErrorGettingCards() {
        setUpEnvironment(
            environment().toBuilder().apolloClient(object : MockApolloClient() {
                override fun getStoredCards(): Observable<List<StoredCard>> {
                    return Observable.error(Exception("oops"))
                }
            }).build()
        )

        this.cards.assertNoValues()
        this.error.assertNoValues()
    }

    @Test
    fun testErrorDeletingCard() {
        setUpEnvironment(
            environment().toBuilder().apolloClient(object : MockApolloClient() {
                override fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data> {
                    return Observable.error(Throwable("eek"))
                }
            }).build()
        )

        this.vm.inputs.deleteCardClicked("id")
        this.vm.confirmDeleteCardClicked()
        this.error.assertValue("eek")
    }

    @Test
    fun testProgressBarIsVisible() {
        setUpEnvironment(environment())

        // getting the cards initially
        this.progressBarIsVisible.assertValues(false)
        this.vm.inputs.deleteCardClicked("id")
        this.vm.inputs.confirmDeleteCardClicked()
        // make the call to delete and reload the cards
        this.progressBarIsVisible.assertValues(false, true, false)
    }

    @Test
    fun testShowDeleteCardDialog() {
        setUpEnvironment(environment())

        this.vm.inputs.deleteCardClicked("5555")
        this.showDeleteCardDialog.assertValueCount(1)
    }

    @Test
    fun testSuccess() {
        setUpEnvironment(environment())

        this.cards.assertValueCount(1)
        this.vm.inputs.deleteCardClicked("id")
        this.vm.inputs.confirmDeleteCardClicked()
        this.success.assertValueCount(1)
        this.cards.assertValueCount(2)
    }

    @Test
    fun testPresentPaymentSheetSuccess() {
        val setupClientId = "seti_1KbABk4VvJ2PtfhKV8E7dvGe_secret_LHjfXxFl9UDucYtsL5a3WtySqjgqf5F"

        setUpEnvironment(
            environment().toBuilder().apolloClient(object : MockApolloClient() {
                override fun createSetupIntent(project: Project?): Observable<String> {
                    return Observable.just(setupClientId)
                }
            }).build()
        )

        this.vm.inputs.newCardButtonClicked()

        this.presentPaymentSheet.assertValue(setupClientId)
        this.progressBarIsVisible.assertValues(false, true, false, true, false)
        this.showError.assertNoValues()
    }

    @Test
    fun testPresentPaymentSheetError() {
        val errorString = "Something went wrong"
        setUpEnvironment(
            environment().toBuilder().apolloClient(object : MockApolloClient() {
                override fun createSetupIntent(project: Project?): Observable<String> {
                    return Observable.error(Exception(errorString))
                }
            }).build()
        )

        this.vm.inputs.newCardButtonClicked()

        this.presentPaymentSheet.assertNoValues()
        this.progressBarIsVisible.assertValues(false, true, false, true, false)
        this.showError.assertValue(errorString)
    }
}
