package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.mock.MockExperimentsClientType
import com.kickstarter.mock.MockFeatureFlagClient
import com.kickstarter.mock.factories.ApiExceptionFactory
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.models.User
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.ResetPasswordScreenState
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class ResetPasswordViewModelTest : KSRobolectricTestCase() {

    @Test
    fun testResetPasswordViewModel_formValidation() {
        val vm = ResetPasswordViewModel.ViewModel(environment())
        val test = TestSubscriber<Boolean>()

        vm.outputs.isFormValid().subscribe(test)

        vm.inputs.email("incorrect@kickstarter")
        test.assertValues(false)

        vm.inputs.email("hello@kickstarter.com")
        test.assertValues(false, true)
    }

    @Test
    fun testResetPasswordViewModel_resetSuccess() {
        val vm = ResetPasswordViewModel.ViewModel(environment())
        val resetLoginPasswordSuccess = TestSubscriber<Void>()
        val resetFacebookLoginPasswordSuccess = TestSubscriber<Void>()
        val resetPasswordScreenStatus = TestSubscriber<ResetPasswordScreenState>()

        vm.outputs.resetLoginPasswordSuccess().subscribe(resetLoginPasswordSuccess)
        vm.outputs.resetFacebookLoginPasswordSuccess().subscribe(resetFacebookLoginPasswordSuccess)
        vm.outputs.resetPasswordScreenStatus().subscribe(resetPasswordScreenStatus)

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
        val vm = ResetPasswordViewModel.ViewModel(environment())
        val resetLoginPasswordSuccess = TestSubscriber<Void>()
        val resetFacebookLoginPasswordSuccess = TestSubscriber<Void>()
        val resetPasswordScreenStatus = TestSubscriber<ResetPasswordScreenState>()

        vm.outputs.resetLoginPasswordSuccess().subscribe(resetLoginPasswordSuccess)
        vm.outputs.resetFacebookLoginPasswordSuccess().subscribe(resetFacebookLoginPasswordSuccess)
        vm.outputs.resetPasswordScreenStatus().subscribe(resetPasswordScreenStatus)

        vm.inputs.resetPasswordClick()
        resetLoginPasswordSuccess.assertNoValues()
        resetFacebookLoginPasswordSuccess.assertNoValues()
        resetPasswordScreenStatus.assertNoValues()

        vm.intent(Intent().putExtra(IntentKey.RESET_PASSWORD_FACEBOOK_LOGIN, true))

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
            .featureFlagClient(mockFeatureFlagClientType)
            .build()

        val vm = ResetPasswordViewModel.ViewModel(environment)
        val resetLoginPasswordSuccess = TestSubscriber<Void>()
        val resetFacebookLoginPasswordSuccess = TestSubscriber<Void>()
        val resetPasswordScreenStatus = TestSubscriber<ResetPasswordScreenState>()

        vm.outputs.resetLoginPasswordSuccess().subscribe(resetLoginPasswordSuccess)
        vm.outputs.resetFacebookLoginPasswordSuccess().subscribe(resetFacebookLoginPasswordSuccess)
        vm.outputs.resetPasswordScreenStatus().subscribe(resetPasswordScreenStatus)

        vm.inputs.resetPasswordClick()
        resetLoginPasswordSuccess.assertNoValues()
        resetFacebookLoginPasswordSuccess.assertNoValues()
        resetPasswordScreenStatus.assertNoValues()

        vm.intent(Intent().putExtra(IntentKey.RESET_PASSWORD_FACEBOOK_LOGIN, true))
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
        val apiClient = object : MockApiClient() {
            override fun resetPassword(email: String): Observable<User> {
                return Observable.error(ApiExceptionFactory.badRequestException())
            }
        }

        val environment = environment().toBuilder()
            .apiClient(apiClient)
            .build()

        val vm = ResetPasswordViewModel.ViewModel(environment)
        val errorTest = TestSubscriber<String>()

        vm.outputs.resetError().subscribe(errorTest)

        vm.inputs.email("hello@kickstarter.com")
        vm.inputs.resetPasswordClick()

        errorTest.assertValue("bad request")
    }

    @Test
    fun testPrefillEmail() {
        val preFillEmail = TestSubscriber<String>()
        val resetPasswordScreenStatus = TestSubscriber<ResetPasswordScreenState>()

        val vm = ResetPasswordViewModel.ViewModel(environment())

        vm.outputs.resetPasswordScreenStatus().subscribe(resetPasswordScreenStatus)
        vm.outputs.prefillEmail().subscribe(preFillEmail)

        // Start the view model with an email to prefill.
        vm.intent(Intent().putExtra(IntentKey.EMAIL, "hello@kickstarter.com"))

        preFillEmail.assertValue("hello@kickstarter.com")
        resetPasswordScreenStatus.assertValue(ResetPasswordScreenState.ForgetPassword)
    }

    @Test
    fun testResetScreenState_ForgetPassword() {
        val preFillEmail = TestSubscriber<String>()
        val resetPasswordScreenStatus = TestSubscriber<ResetPasswordScreenState>()

        val mockExperimentsClientType: MockExperimentsClientType =
            object : MockExperimentsClientType() {
                override fun isFeatureEnabled(feature: OptimizelyFeature.Key): Boolean {
                    return true
                }
            }
        val environment = environment()
            .toBuilder()
            .optimizely(mockExperimentsClientType)
            .build()

        val vm = ResetPasswordViewModel.ViewModel(environment)

        vm.outputs.resetPasswordScreenStatus().subscribe(resetPasswordScreenStatus)
        vm.outputs.prefillEmail().subscribe(preFillEmail)

        // Start the view model with an email to prefill.
        vm.intent(Intent().putExtra(IntentKey.EMAIL, "hello@kickstarter.com"))

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

        val vm = ResetPasswordViewModel.ViewModel(environment)
        vm.intent(Intent().putExtra(IntentKey.RESET_PASSWORD_FACEBOOK_LOGIN, true))

        vm.outputs.resetPasswordScreenStatus().subscribe(resetPasswordScreenStatus)
        vm.outputs.prefillEmail().subscribe(preFillEmail)

        preFillEmail.assertNoValues()
        resetPasswordScreenStatus.assertValue(ResetPasswordScreenState.ResetPassword)
    }
}
