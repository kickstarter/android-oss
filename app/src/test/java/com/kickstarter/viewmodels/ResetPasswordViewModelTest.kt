package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.MockFeatureFlagClient
import com.kickstarter.mock.factories.ApiExceptionFactory
import com.kickstarter.mock.services.MockApiClientV2
import com.kickstarter.models.User
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.ResetPasswordScreenState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.Observable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class ResetPasswordViewModelTest : KSRobolectricTestCase() {

    private val disposables = CompositeDisposable()

    @Test
    fun testResetPasswordViewModel_formValidation() {
        val vm = ResetPasswordViewModel.ResetPasswordViewModel(environment())
        val test = TestSubscriber<Boolean>()

        vm.outputs.isFormValid().subscribe { test.onNext(it) }.addToDisposable(disposables)

        vm.inputs.email("incorrect@kickstarter")
        test.assertValues(false)

        vm.inputs.email("hello@kickstarter.com")
        test.assertValues(false, true)
    }

    @Test
    fun testResetPasswordViewModel_resetSuccess() {
        val vm = ResetPasswordViewModel.ResetPasswordViewModel(environment().toBuilder().apiClientV2(MockApiClientV2()).build())
        val resetLoginPasswordSuccess = TestSubscriber<Unit>()
        val resetFacebookLoginPasswordSuccess = TestSubscriber<Unit>()
        val resetPasswordScreenStatus = TestSubscriber<ResetPasswordScreenState>()

        vm.outputs.resetLoginPasswordSuccess().subscribe{ resetLoginPasswordSuccess.onNext(it) }.addToDisposable(disposables)
        vm.outputs.resetFacebookLoginPasswordSuccess().subscribe{ resetFacebookLoginPasswordSuccess.onNext(it) }.addToDisposable(disposables)
        vm.outputs.resetPasswordScreenStatus().subscribe { resetPasswordScreenStatus.onNext(it) }.addToDisposable(disposables)

        vm.inputs.resetPasswordClick()
        resetLoginPasswordSuccess.assertNoValues()
        resetFacebookLoginPasswordSuccess.assertNoValues()
        resetPasswordScreenStatus.assertNoValues()

        vm.inputs.email("hello@kickstarter.com")
        resetLoginPasswordSuccess.assertNoValues()
        resetFacebookLoginPasswordSuccess.assertNoValues()
        resetPasswordScreenStatus.assertNoValues()

        vm.inputs.resetPasswordClick()
        resetLoginPasswordSuccess.assertValueCount(1)
        resetFacebookLoginPasswordSuccess.assertNoValues()
        resetPasswordScreenStatus.assertNoValues()
    }

    @Test
    fun testResetFacebookPasswordViewModel_resetFailed_withDisabled_feature_flag() {
        val vm = ResetPasswordViewModel.ResetPasswordViewModel(environment().toBuilder().apiClientV2(MockApiClientV2()).build())
        val resetLoginPasswordSuccess = TestSubscriber<Unit>()
        val resetFacebookLoginPasswordSuccess = TestSubscriber<Unit>()
        val resetPasswordScreenStatus = TestSubscriber<ResetPasswordScreenState>()

        vm.outputs.resetLoginPasswordSuccess().subscribe{ resetLoginPasswordSuccess.onNext(it) }.addToDisposable(disposables)
        vm.outputs.resetFacebookLoginPasswordSuccess().subscribe{ resetFacebookLoginPasswordSuccess.onNext(it) }.addToDisposable(disposables)
        vm.outputs.resetPasswordScreenStatus().subscribe{ resetPasswordScreenStatus.onNext(it) }.addToDisposable(disposables)

        vm.inputs.resetPasswordClick()
        resetLoginPasswordSuccess.assertNoValues()
        resetFacebookLoginPasswordSuccess.assertNoValues()
        resetPasswordScreenStatus.assertNoValues()

        vm.configureWith(Intent().putExtra(IntentKey.RESET_PASSWORD_FACEBOOK_LOGIN, true))

        vm.inputs.email("hello@kickstarter.com")
        resetLoginPasswordSuccess.assertNoValues()
        resetFacebookLoginPasswordSuccess.assertNoValues()
        resetPasswordScreenStatus.assertNoValues()

        vm.inputs.resetPasswordClick()
        resetLoginPasswordSuccess.assertValueCount(1)
        resetFacebookLoginPasswordSuccess.assertNoValues()
        resetPasswordScreenStatus.assertNoValues()
    }

    @Test
    fun testResetFacebookPasswordViewModel_resetSuccess() {
        val mockFeatureFlagClientType: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return true
                }
            }
        val environment = environment()
            .toBuilder()
                .apiClientV2(MockApiClientV2())
            .featureFlagClient(mockFeatureFlagClientType)
            .build()

        val vm = ResetPasswordViewModel.ResetPasswordViewModel(environment)
        val resetLoginPasswordSuccess = TestSubscriber<Unit>()
        val resetFacebookLoginPasswordSuccess = TestSubscriber<Unit>()
        val resetPasswordScreenStatus = TestSubscriber<ResetPasswordScreenState>()

        vm.outputs.resetLoginPasswordSuccess().subscribe{ resetLoginPasswordSuccess.onNext(it) }.addToDisposable(disposables)
        vm.outputs.resetFacebookLoginPasswordSuccess().subscribe{ resetFacebookLoginPasswordSuccess.onNext(it) }.addToDisposable(disposables)
        vm.outputs.resetPasswordScreenStatus().subscribe{ resetPasswordScreenStatus.onNext(it) }.addToDisposable(disposables)

        vm.inputs.resetPasswordClick()
        resetLoginPasswordSuccess.assertNoValues()
        resetFacebookLoginPasswordSuccess.assertNoValues()
        resetPasswordScreenStatus.assertNoValues()

        vm.configureWith(Intent().putExtra(IntentKey.RESET_PASSWORD_FACEBOOK_LOGIN, true))
        vm.inputs.email("hello@kickstarter.com")
        resetLoginPasswordSuccess.assertNoValues()
        resetFacebookLoginPasswordSuccess.assertNoValues()
        resetPasswordScreenStatus.assertValue(ResetPasswordScreenState.ResetPassword)

        vm.inputs.resetPasswordClick()
        resetLoginPasswordSuccess.assertNoValues()
        resetFacebookLoginPasswordSuccess.assertValueCount(1)
        resetPasswordScreenStatus.assertValue(ResetPasswordScreenState.ResetPassword)
    }

    @Test
    fun testResetPasswordViewModel_resetFailure() {
        val apiClient = object : MockApiClientV2() {
            override fun resetPassword(email: String): Observable<User> {
                return Observable.error(ApiExceptionFactory.badRequestException())
            }
        }

        val environment = environment().toBuilder()
            .apiClientV2(apiClient)
            .build()

        val vm = ResetPasswordViewModel.ResetPasswordViewModel(environment)
        val errorTest = TestSubscriber<String>()

        vm.outputs.resetError().subscribe{ errorTest.onNext(it) }.addToDisposable(disposables)

        vm.inputs.email("hello@kickstarter.com")
        vm.inputs.resetPasswordClick()

        errorTest.assertValue("bad request")
    }

    @Test
    fun testPrefillEmail() {
        val preFillEmail = TestSubscriber<String>()
        val resetPasswordScreenStatus = TestSubscriber<ResetPasswordScreenState>()

        val vm = ResetPasswordViewModel.ResetPasswordViewModel(environment())

        vm.outputs.resetPasswordScreenStatus().subscribe{ resetPasswordScreenStatus.onNext(it) }.addToDisposable(disposables)
        vm.outputs.prefillEmail().subscribe{ preFillEmail.onNext(it) }.addToDisposable(disposables)

        // Start the view model with an email to prefill.
        vm.configureWith(Intent().putExtra(IntentKey.EMAIL, "hello@kickstarter.com"))

        preFillEmail.assertValue("hello@kickstarter.com")
        resetPasswordScreenStatus.assertValue(ResetPasswordScreenState.ForgetPassword)
    }

    @Test
    fun testResetScreenState_ForgetPassword() {
        val preFillEmail = TestSubscriber<String>()
        val resetPasswordScreenStatus = TestSubscriber<ResetPasswordScreenState>()

        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return true
                }
            }

        val environment = environment()
            .toBuilder()
            .featureFlagClient(mockFeatureFlagClient)
            .build()

        val vm = ResetPasswordViewModel.ResetPasswordViewModel(environment)

        vm.outputs.resetPasswordScreenStatus().subscribe { resetPasswordScreenStatus.onNext(it) }.addToDisposable(disposables)
        vm.outputs.prefillEmail().subscribe { preFillEmail.onNext(it) }.addToDisposable(disposables)

        // Start the view model with an email to prefill.
        vm.configureWith(Intent().putExtra(IntentKey.EMAIL, "hello@kickstarter.com"))

        preFillEmail.assertValue("hello@kickstarter.com")
        resetPasswordScreenStatus.assertValue(ResetPasswordScreenState.ForgetPassword)
    }

    @Test
    fun testResetScreenState_ResetPassword() {
        val preFillEmail = TestSubscriber<String>()
        val resetPasswordScreenStatus = TestSubscriber<ResetPasswordScreenState>()

        val mockFeatureFlagClientType: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return true
                }
            }

        val environment = environment()
            .toBuilder()
            .featureFlagClient(mockFeatureFlagClientType)
            .build()

        val vm = ResetPasswordViewModel.ResetPasswordViewModel(environment)
        vm.configureWith(Intent().putExtra(IntentKey.RESET_PASSWORD_FACEBOOK_LOGIN, true))

        vm.outputs.resetPasswordScreenStatus().subscribe { resetPasswordScreenStatus.onNext(it) }.addToDisposable(disposables)
        vm.outputs.prefillEmail().subscribe { preFillEmail.onNext(it) }.addToDisposable(disposables)

        preFillEmail.assertNoValues()
        resetPasswordScreenStatus.assertValue(ResetPasswordScreenState.ResetPassword)
    }

    @After
    fun clear() {
        disposables.clear()
    }
}
