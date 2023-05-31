package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.utils.EventName
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.ApiExceptionFactory
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope.Companion.builder
import com.kickstarter.ui.IntentKey
import io.reactivex.disposables.CompositeDisposable
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import rx.subjects.BehaviorSubject

class TwoFactorViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: TwoFactorViewModel.TwoFactorViewModel

    private val formIsValid = TestSubscriber<Boolean>()
    private val formSubmitting = TestSubscriber<Boolean>()
    private val genericTfaError = TestSubscriber<Unit>()
    private val showResendCodeConfirmation = TestSubscriber<Unit>()
    private val tfaCodeMismatchError = TestSubscriber<Unit>()
    private val tfaSuccess = TestSubscriber<Unit>()
    private val disposables = CompositeDisposable()

    @Test
    fun testTwoFactorViewModel_FormValidation() {
        val intent = Intent()

        intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com")
        intent.putExtra(IntentKey.PASSWORD, "hello")
        intent.putExtra(IntentKey.FACEBOOK_LOGIN, false)
        intent.putExtra(IntentKey.FACEBOOK_TOKEN, "")

        vm = TwoFactorViewModel.TwoFactorViewModel(environment(), intent)

        vm.outputs.formIsValid().subscribe { formIsValid.onNext(it) }.addToDisposable(disposables)
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

        val user = BehaviorSubject.create<User>()
        environment().currentUser()?.loggedInUser()?.subscribe(user)

        vm = TwoFactorViewModel.TwoFactorViewModel(environment(), intent)

        vm.outputs.tfaSuccess().subscribe { tfaSuccess.onNext(it) }.addToDisposable(disposables)
        vm.outputs.formSubmitting()
            .subscribe { formSubmitting.onNext(it) }
            .addToDisposable(disposables)

        vm.inputs.code("88888")
        vm.inputs.loginClick()

        formSubmitting.assertValues(true, false)
        tfaSuccess.assertValueCount(1)

        assertEquals("some@email.com", user.value?.email())

        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testTwoFactorViewModel_TfaSuccessFacebook() {
        val intent = Intent()

        intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com")
        intent.putExtra(IntentKey.PASSWORD, "hello")
        intent.putExtra(IntentKey.FACEBOOK_LOGIN, true)
        intent.putExtra(IntentKey.FACEBOOK_TOKEN, "pajamas1234")

        vm = TwoFactorViewModel.TwoFactorViewModel(environment())

        vm.outputs.tfaSuccess().subscribe { tfaSuccess.onNext(it) }.addToDisposable(disposables)
        vm.outputs.formSubmitting()
            .subscribe { formSubmitting.onNext(it) }
            .addToDisposable(disposables)

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

        vm = TwoFactorViewModel.TwoFactorViewModel(environment(), intent)

        vm.outputs.showResendCodeConfirmation()
            .subscribe { showResendCodeConfirmation.onNext(it) }
            .addToDisposable(disposables)

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

        vm = TwoFactorViewModel.TwoFactorViewModel(environment(), intent)

        vm.outputs.showResendCodeConfirmation()
            .subscribe { showResendCodeConfirmation .onNext(it) }
            .addToDisposable(disposables)

        vm.inputs.resendClick()

        showResendCodeConfirmation.assertValueCount(1)
        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testTwoFactorViewModel_GenericError() {
        val apiClient: ApiClientType = object : MockApiClient() {
            override fun login(
                email: String,
                password: String,
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

        vm = TwoFactorViewModel.TwoFactorViewModel(environment, intent)

        vm.outputs.tfaSuccess().subscribe { tfaSuccess.onNext(it) }.addToDisposable(disposables)
        vm.outputs.formSubmitting()
            .subscribe { formSubmitting.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.genericTfaError()
            .subscribe { genericTfaError.onNext(it) }
            .addToDisposable(disposables)

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
                email: String,
                password: String,
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

        vm = TwoFactorViewModel.TwoFactorViewModel(environment, intent)

        vm.outputs.tfaSuccess().subscribe { tfaSuccess.onNext(it) }.addToDisposable(disposables)
        vm.outputs.formSubmitting()
            .subscribe { formSubmitting.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.tfaCodeMismatchError()
            .subscribe { tfaCodeMismatchError.onNext(it) }
            .addToDisposable(disposables)

        vm.inputs.code("88888")
        vm.inputs.loginClick()

        formSubmitting.assertValues(true, false)
        tfaSuccess.assertNoValues()
        tfaCodeMismatchError.assertValueCount(1)
        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }
}
