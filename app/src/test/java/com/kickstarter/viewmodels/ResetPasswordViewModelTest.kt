package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.ApiExceptionFactory
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.models.User
import com.kickstarter.ui.IntentKey
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

        this.lakeTest.assertValue("Forgot Password Page Viewed")
    }

    @Test
    fun testResetPasswordViewModel_resetSuccess() {
        val vm = ResetPasswordViewModel.ViewModel(environment())
        val test = TestSubscriber<Void>()

        vm.outputs.resetSuccess().subscribe(test)

        vm.inputs.resetPasswordClick()
        test.assertNoValues()

        vm.inputs.email("hello@kickstarter.com")
        test.assertNoValues()

        vm.inputs.resetPasswordClick()
        test.assertValueCount(1)

        this.lakeTest.assertValue("Forgot Password Page Viewed")
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

        this.lakeTest.assertValue("Forgot Password Page Viewed")
    }

    @Test
    fun testPrefillEmail() {
        val preFillEmail = TestSubscriber<String>()
        val vm = ResetPasswordViewModel.ViewModel(environment())
        vm.outputs.prefillEmail().subscribe(preFillEmail)

        // Start the view model with an email to prefill.
        vm.intent(Intent().putExtra(IntentKey.EMAIL, "hello@kickstarter.com"))

        preFillEmail.assertValue("hello@kickstarter.com")
    }
}
