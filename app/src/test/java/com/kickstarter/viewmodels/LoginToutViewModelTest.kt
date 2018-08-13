package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase

import org.junit.Test

import rx.observers.TestSubscriber

class LoginToutViewModelTest : KSRobolectricTestCase() {

    @Test
    fun testLoginButtonClicked() {
        val vm = LoginToutViewModel.ViewModel(environment())

        val startLoginActivity = TestSubscriber<Void>()
        vm.outputs.startLoginActivity().subscribe(startLoginActivity)

        startLoginActivity.assertNoValues()

        vm.inputs.loginClick()
        startLoginActivity.assertValueCount(1)
    }

    @Test
    fun testSignupButtonClicked() {
        val vm = LoginToutViewModel.ViewModel(environment())

        val startSignupActivity = TestSubscriber<Void>()
        vm.outputs.startSignupActivity().subscribe(startSignupActivity)

        startSignupActivity.assertNoValues()

        vm.inputs.signupClick()
        startSignupActivity.assertValueCount(1)
    }
}
