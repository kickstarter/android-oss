package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.utils.EventName
import com.kickstarter.mock.factories.ApiExceptionFactory
import com.kickstarter.mock.factories.ConfigFactory.config
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope.Companion.builder
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class SignupViewModelTest : KSRobolectricTestCase() {

    @Test
    fun testSignupViewModel_FormValidation() {
        val environment = environment()

        environment.currentConfig()?.config(config())

        val vm = SignupViewModel.ViewModel(environment)
        val formIsValidTest = TestSubscriber<Boolean>()

        vm.outputs.formIsValid().subscribe(formIsValidTest)

        vm.inputs.name("brandon")

        formIsValidTest.assertNoValues()

        vm.inputs.email("incorrect@kickstarter")

        formIsValidTest.assertNoValues()

        vm.inputs.password("danisawesome")

        formIsValidTest.assertValues(false)

        vm.inputs.email("hello@kickstarter.com")

        formIsValidTest.assertValues(false, true)
    }

    @Test
    fun testSignupViewModel_SuccessfulSignup() {
        val environment = environment()
        environment.currentConfig()?.config(config())

        val vm = SignupViewModel.ViewModel(environment)
        val signupSuccessTest = TestSubscriber<Void>()

        vm.outputs.signupSuccess().subscribe(signupSuccessTest)

        val formSubmittingTest = TestSubscriber<Boolean>()

        vm.outputs.formSubmitting().subscribe(formSubmittingTest)

        vm.inputs.name("brandon")
        vm.inputs.email("hello@kickstarter.com")
        vm.inputs.email("incorrect@kickstarter")
        vm.inputs.password("danisawesome")
        vm.inputs.sendNewslettersClick(true)
        vm.inputs.signupClick()

        formSubmittingTest.assertValues(true, false)
        signupSuccessTest.assertValueCount(1)

        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testSignupViewModel_ApiValidationError() {
        val apiClient: ApiClientType = object : MockApiClient() {
            override fun signup(
                name: String,
                email: String,
                password: String,
                passwordConfirmation: String,
                sendNewsletters: Boolean
            ): Observable<AccessTokenEnvelope> {
                return Observable.error(
                    ApiExceptionFactory.apiError(
                        builder().httpCode(422).build()
                    )
                )
            }
        }

        val environment = environment().toBuilder().apiClient(apiClient).build()
        val vm = SignupViewModel.ViewModel(environment)
        val signupSuccessTest = TestSubscriber<Void>()

        vm.outputs.signupSuccess().subscribe(signupSuccessTest)

        val signupErrorTest = TestSubscriber<String>()

        vm.outputs.errorString().subscribe(signupErrorTest)

        val formSubmittingTest = TestSubscriber<Boolean>()

        vm.outputs.formSubmitting().subscribe(formSubmittingTest)

        vm.inputs.name("brandon")
        vm.inputs.email("hello@kickstarter.com")
        vm.inputs.email("incorrect@kickstarter")
        vm.inputs.password("danisawesome")
        vm.inputs.sendNewslettersClick(true)
        vm.inputs.signupClick()

        formSubmittingTest.assertValues(true, false)
        signupSuccessTest.assertValueCount(0)
        signupErrorTest.assertValueCount(1)

        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testSignupViewModel_ApiError() {
        val apiClient: ApiClientType = object : MockApiClient() {
            override fun signup(
                name: String,
                email: String,
                password: String,
                passwordConfirmation: String,
                sendNewsletters: Boolean
            ): Observable<AccessTokenEnvelope> {
                return Observable.error(ApiExceptionFactory.badRequestException())
            }
        }

        val environment = environment().toBuilder().apiClient(apiClient).build()
        val vm = SignupViewModel.ViewModel(environment)
        val signupSuccessTest = TestSubscriber<Void>()

        vm.outputs.signupSuccess().subscribe(signupSuccessTest)

        val signupErrorTest = TestSubscriber<String>()
        vm.outputs.errorString().subscribe(signupErrorTest)

        val formSubmittingTest = TestSubscriber<Boolean>()

        vm.outputs.formSubmitting().subscribe(formSubmittingTest)

        vm.inputs.name("brandon")
        vm.inputs.email("hello@kickstarter.com")
        vm.inputs.email("incorrect@kickstarter")
        vm.inputs.password("danisawesome")
        vm.inputs.sendNewslettersClick(true)
        vm.inputs.signupClick()

        formSubmittingTest.assertValues(true, false)
        signupSuccessTest.assertValueCount(0)
        signupErrorTest.assertValueCount(1)

        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }
}
