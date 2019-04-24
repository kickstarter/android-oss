package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.ApiExceptionFactory
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.models.User
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class ResetPasswordViewModelTest : KSRobolectricTestCase() {

    @Test
    fun testResetPasswordViewModel_formValidation() {
        val vm = ResetPasswordViewModel.ViewModel(environment())
        val test = TestSubscriber<Boolean>()

        koalaTest.assertValues("Forgot Password View")

        vm.outputs.isFormValid().subscribe(test)

        vm.inputs.email("incorrect@kickstarter")
        test.assertValues(false)

        vm.inputs.email("hello@kickstarter.com")
        test.assertValues(false, true)

        koalaTest.assertValueCount(1)
    }

    @Test
    fun testResetPasswordViewModel_resetSuccess() {
        val vm = ResetPasswordViewModel.ViewModel(environment())
        val test = TestSubscriber<Void>()

        koalaTest.assertValues("Forgot Password View")

        vm.outputs.resetSuccess().subscribe(test)

        vm.inputs.resetPasswordClick()
        test.assertNoValues()

        vm.inputs.email("hello@kickstarter.com")
        test.assertNoValues()

        vm.inputs.resetPasswordClick()
        test.assertValueCount(1)

        koalaTest.assertValues("Forgot Password View", "Forgot Password Requested")
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

        koalaTest.assertValues("Forgot Password View")

        vm.outputs.resetError().subscribe(errorTest)

        vm.inputs.email("hello@kickstarter.com")
        vm.inputs.resetPasswordClick()

        errorTest.assertValue("bad request")

        koalaTest.assertValues("Forgot Password View", "Forgot Password Errored")
    }
}
