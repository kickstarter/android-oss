package com.kickstarter.viewmodels

import SavePaymentMethodMutation
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.CardFactory
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.mock.services.MockStripe
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import type.PaymentTypes

class NewCardFragmentViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: NewCardFragmentViewModel.ViewModel
    private val cardWidgetFocusDrawable = TestSubscriber<Int>()
    private val error = TestSubscriber<String>()
    private val progressBarIsVisible = TestSubscriber<Boolean>()
    private val saveButtonIsEnabled = TestSubscriber<Boolean>()
    private val success = TestSubscriber<Void>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = NewCardFragmentViewModel.ViewModel(environment)
        this.vm.outputs.cardWidgetFocusDrawable().subscribe(this.cardWidgetFocusDrawable)
        this.vm.outputs.error().subscribe(this.error)
        this.vm.outputs.progressBarIsVisible().subscribe(this.progressBarIsVisible)
        this.vm.outputs.saveButtonIsEnabled().subscribe(this.saveButtonIsEnabled)
        this.vm.outputs.success().subscribe(this.success)
    }

    @Test
    fun testCardWidgetFocusDrawable() {
        setUpEnvironment(environment())

        this.vm.inputs.cardFocus(true)
        this.cardWidgetFocusDrawable.assertValuesAndClear(R.drawable.divider_green_horizontal)

        this.vm.inputs.cardFocus(false)
        this.cardWidgetFocusDrawable.assertValue(R.drawable.divider_dark_grey_500_horizontal)
    }

    @Test
    fun testAPIError() {
        val apolloClient = object : MockApolloClient() {
            override fun savePaymentMethod(paymentTypes: PaymentTypes, stripeToken: String, cardId: String): Observable<SavePaymentMethodMutation.Data> {
                return Observable.error(Exception("oops"))
            }
        }
        setUpEnvironment(environment().toBuilder().apolloClient(apolloClient).build())

        this.vm.inputs.name("Nathan Squid")
        this.vm.inputs.postalCode("11222")
        this.vm.inputs.card(CardFactory.card())
        this.vm.inputs.saveCardClicked()
        this.error.assertValue("oops")
    }

    @Test
    fun testStripeError() {
        val mockStripe = MockStripe(context(), true)
        setUpEnvironment(environment().toBuilder().stripe(mockStripe).build())

        this.vm.inputs.name("Nathan Squid")
        this.vm.inputs.postalCode("11222")
        this.vm.inputs.card(CardFactory.card())
        this.vm.inputs.saveCardClicked()
        this.error.assertValue("Stripe error")
    }

    @Test
    fun testProgressBarIsVisible() {
        setUpEnvironment(environment())

        this.vm.inputs.name("Nathan Squid")
        this.vm.inputs.postalCode("11222")
        this.vm.inputs.card(CardFactory.card())
        this.vm.inputs.saveCardClicked()
        this.progressBarIsVisible.assertValues(true, false)
    }

    @Test
    fun testSaveButtonIsEnabled() {
        setUpEnvironment(environment())

        this.vm.inputs.name("Nathan Squid")
        this.saveButtonIsEnabled.assertValues(false)
        this.vm.inputs.card(CardFactory.card("4242424242424242", null, null, null))
        this.saveButtonIsEnabled.assertValues(false)
        this.vm.inputs.card(CardFactory.card("4242424242424242", 1, null, null))
        this.saveButtonIsEnabled.assertValues(false)
        this.vm.inputs.card(CardFactory.card("4242424242424242", 1, 2020, null))
        this.saveButtonIsEnabled.assertValues(false)
        this.vm.inputs.card(CardFactory.card("4242424242424242", 1, 2020, "555"))
        this.saveButtonIsEnabled.assertValues(false)
        this.vm.inputs.postalCode("11222")
        this.saveButtonIsEnabled.assertValues(false, true)
        this.vm.inputs.card(CardFactory.card("424242424242424", 1, 2020, "555"))
        this.saveButtonIsEnabled.assertValues(false, true, false)
        this.vm.inputs.card(CardFactory.card("4242424242424242", null, 2020, "555"))
        this.saveButtonIsEnabled.assertValues(false, true, false)
        this.vm.inputs.card(CardFactory.card("4242424242424242", 1, null, "555"))
        this.saveButtonIsEnabled.assertValues(false, true, false)
        this.vm.inputs.card(CardFactory.card("4242424242424242", 1, 2020, null))
        this.saveButtonIsEnabled.assertValues(false, true, false)
    }

    @Test
    fun testSuccess() {
        setUpEnvironment(environment())

        this.vm.inputs.name("Nathan Squid")
        this.vm.inputs.postalCode("11222")
        this.vm.inputs.card(CardFactory.card())
        this.vm.inputs.saveCardClicked()
        this.success.assertValues()
    }
}
