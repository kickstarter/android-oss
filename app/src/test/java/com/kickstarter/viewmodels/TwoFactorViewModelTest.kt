package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.utils.EventName
import com.kickstarter.mock.factories.ApiExceptionFactory
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope.Companion.builder
import com.kickstarter.ui.IntentKey
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class TwoFactorViewModelTest : KSRobolectricTestCase() {
  
    private lateinit var vm: TwoFactorViewModel.ViewModel
  
    private val formIsValid = TestSubscriber<Boolean>()
    private val formSubmitting = TestSubscriber<Boolean>()
    private val genericTfaError = TestSubscriber<Void>()
    private val showResendCodeConfirmation = TestSubscriber<Void>()
    private val tfaCodeMismatchError = TestSubscriber<Void>()
    private val tfaSuccess = TestSubscriber<Void>()
   
    @Test
    fun testTwoFactorViewModel_FormValidation() {
        val intent = Intent()
    
        intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com")
        intent.putExtra(IntentKey.PASSWORD, "hello")
        intent.putExtra(IntentKey.FACEBOOK_LOGIN, false)
        intent.putExtra(IntentKey.FACEBOOK_TOKEN, "")

        vm = TwoFactorViewModel.ViewModel(environment())
        vm.intent(intent)

        vm.outputs.formIsValid().subscribe(formIsValid)
        formIsValid.assertNoValues()

        vm.inputs.code("444444")
        formIsValid.assertValue(true)

        vm.inputs.code("")

        formIsValid.assertValues(true, false)
        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testTwoFactorViewModel_TfaSuccess() {
        val intent = Intent()

        intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com")
        intent.putExtra(IntentKey.PASSWORD, "hello")
        intent.putExtra(IntentKey.FACEBOOK_LOGIN, false)
        intent.putExtra(IntentKey.FACEBOOK_TOKEN, "")

        vm = TwoFactorViewModel.ViewModel(environment())
        vm.intent(intent)

        vm.outputs.tfaSuccess().subscribe(tfaSuccess)
        vm.outputs.formSubmitting().subscribe(formSubmitting)

        vm.inputs.code("88888")
        vm.inputs.loginClick()

        formSubmitting.assertValues(true, false)
        tfaSuccess.assertValueCount(1)

        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testTwoFactorViewModel_TfaSuccessFacebook() {
        val intent = Intent()

        intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com")
        intent.putExtra(IntentKey.PASSWORD, "hello")
        intent.putExtra(IntentKey.FACEBOOK_LOGIN, true)
        intent.putExtra(IntentKey.FACEBOOK_TOKEN, "pajamas1234")

        vm = TwoFactorViewModel.ViewModel(environment())
        vm.intent(intent)

        vm.outputs.tfaSuccess().subscribe(tfaSuccess)
        vm.outputs.formSubmitting().subscribe(formSubmitting)

        vm.inputs.code("88888")
        vm.inputs.loginClick()

        formSubmitting.assertValues(true, false)
        tfaSuccess.assertValueCount(1)

        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testTwoFactorViewModel_ResendCode() {
        val intent = Intent()

        intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com")
        intent.putExtra(IntentKey.PASSWORD, "hello")
        intent.putExtra(IntentKey.FACEBOOK_LOGIN, false)
        intent.putExtra(IntentKey.FACEBOOK_TOKEN, "")

        vm = TwoFactorViewModel.ViewModel(environment())
        vm.intent(intent)

        vm.outputs.showResendCodeConfirmation().subscribe(showResendCodeConfirmation)

        vm.inputs.resendClick()

        showResendCodeConfirmation.assertValueCount(1)
        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testTwoFactorViewModel_ResendCodeFacebook() {
        val intent = Intent()

        intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com")
        intent.putExtra(IntentKey.PASSWORD, "hello")
        intent.putExtra(IntentKey.FACEBOOK_LOGIN, true)
        intent.putExtra(IntentKey.FACEBOOK_TOKEN, "pajamas1234")

        vm = TwoFactorViewModel.ViewModel(environment())
        vm.intent(intent)

        vm.outputs.showResendCodeConfirmation().subscribe(showResendCodeConfirmation)

        vm.inputs.resendClick()

        showResendCodeConfirmation.assertValueCount(1)
        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testTwoFactorViewModel_GenericError() {
        val apiClient: ApiClientType = object : MockApiClient() {
            override fun login(
                email: String, password: String,
                code: String
            ): Observable<AccessTokenEnvelope> {
                return Observable.error(
                    ApiExceptionFactory.apiError(
                        builder().httpCode(400).build()
                    )
                )
            }
        }

        val environment = environment().toBuilder().apiClient(apiClient).build()

        val intent = Intent()

        intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com")
        intent.putExtra(IntentKey.PASSWORD, "hello")
        intent.putExtra(IntentKey.FACEBOOK_LOGIN, false)
        intent.putExtra(IntentKey.FACEBOOK_TOKEN, "")

        vm = TwoFactorViewModel.ViewModel(environment)

        vm.intent(intent)

        vm.outputs.tfaSuccess().subscribe(tfaSuccess)
        vm.outputs.formSubmitting().subscribe(formSubmitting)
        vm.outputs.genericTfaError().subscribe(genericTfaError)

        vm.inputs.code("88888")
        vm.inputs.loginClick()

        formSubmitting.assertValues(true, false)
        tfaSuccess.assertNoValues()
        genericTfaError.assertValueCount(1)
        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testTwoFactorViewModel_CodeMismatchError() {
        val apiClient: ApiClientType = object : MockApiClient() {
            override fun login(
                email: String, password: String,
                code: String
            ): Observable<AccessTokenEnvelope> {
                return Observable.error(ApiExceptionFactory.tfaFailed())
            }
        }

        val environment = environment().toBuilder().apiClient(apiClient).build()
        val intent = Intent()

        intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com")
        intent.putExtra(IntentKey.PASSWORD, "hello")
        intent.putExtra(IntentKey.FACEBOOK_LOGIN, false)
        intent.putExtra(IntentKey.FACEBOOK_TOKEN, "")

        vm = TwoFactorViewModel.ViewModel(environment)

        vm.intent(intent)

        vm.outputs.tfaSuccess().subscribe(tfaSuccess)
        vm.outputs.formSubmitting().subscribe(formSubmitting)
        vm.outputs.tfaCodeMismatchError().subscribe(tfaCodeMismatchError)

        vm.inputs.code("88888")
        vm.inputs.loginClick()

        formSubmitting.assertValues(true, false)
        tfaSuccess.assertNoValues()
        tfaCodeMismatchError.assertValueCount(1)
        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }
}