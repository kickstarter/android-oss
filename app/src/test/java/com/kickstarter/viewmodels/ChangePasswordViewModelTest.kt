package com.kickstarter.viewmodels

import UpdateUserPasswordMutation
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.services.MockApolloClient
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class ChangePasswordViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: ChangePasswordViewModel.ViewModel

    private val error = TestSubscriber<String>()
    private val passwordConfirmationWarningIsVisible = TestSubscriber<Boolean>()
    private val passwordLengthWarningIsVisible = TestSubscriber<Boolean>()
    private val progressBarIsVisible = TestSubscriber<Boolean>()
    private val saveButtonIsEnabled = TestSubscriber<Boolean>()
    private val success = TestSubscriber<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ChangePasswordViewModel.ViewModel(environment)

        this.vm.outputs.error().subscribe(this.error)
        this.vm.outputs.passwordConfirmationWarningIsVisible().subscribe(this.passwordConfirmationWarningIsVisible)
        this.vm.outputs.passwordLengthWarningIsVisible().subscribe(this.passwordLengthWarningIsVisible)
        this.vm.outputs.progressBarIsVisible().subscribe(this.progressBarIsVisible)
        this.vm.outputs.saveButtonIsEnabled().subscribe(this.saveButtonIsEnabled)
        this.vm.outputs.success().subscribe(this.success)
    }

    @Test
    fun testError() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun updateUserPassword(currentPassword: String, newPassword: String, confirmPassword: String): Observable<UpdateUserPasswordMutation.Data> {
                return Observable.error(Exception("Oops"))
            }
        }).build())

        this.vm.inputs.currentPassword("password")
        this.vm.inputs.newPassword("password")
        this.vm.inputs.confirmPassword("password")
        this.vm.inputs.changePasswordClicked()
        this.error.assertValue("Oops")
    }

    @Test
    fun testPasswordConfirmationWarningIsVisible() {
        setUpEnvironment(environment())

        this.vm.inputs.newPassword("password")

        this.passwordConfirmationWarningIsVisible.assertNoValues()
        this.vm.inputs.confirmPassword("p")
        this.passwordConfirmationWarningIsVisible.assertValues(true)
        this.vm.inputs.confirmPassword("password")
        this.passwordConfirmationWarningIsVisible.assertValues(true, false)
    }

    @Test
    fun testPasswordLengthWarningIsVisible() {
        setUpEnvironment(environment())

        this.passwordLengthWarningIsVisible.assertNoValues()
        this.vm.inputs.newPassword("p")
        this.passwordLengthWarningIsVisible.assertValues(true)
        this.vm.inputs.newPassword("passw")
        this.passwordLengthWarningIsVisible.assertValues(true)
        this.vm.inputs.newPassword("password")
        this.passwordLengthWarningIsVisible.assertValues(true, false)
    }

    @Test
    fun testProgressBarIsVisible() {
        setUpEnvironment(environment())

        this.vm.inputs.currentPassword("password")
        this.vm.inputs.newPassword("password")
        this.vm.inputs.confirmPassword("password")
        this.vm.inputs.changePasswordClicked()
        this.progressBarIsVisible.assertValues(true, false)
    }

    @Test
    fun testSaveButtonIsEnabled() {
        setUpEnvironment(environment())

        this.vm.inputs.currentPassword("password")
        this.vm.inputs.newPassword("password")
        this.vm.inputs.confirmPassword("password")
        this.saveButtonIsEnabled.assertValue(true)
        this.vm.inputs.confirmPassword("pass")
        this.saveButtonIsEnabled.assertValues(true, false)
        this.vm.inputs.confirmPassword("passwerd")
        this.saveButtonIsEnabled.assertValues(true, false)
    }

    @Test
    fun testSuccess() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun updateUserPassword(currentPassword: String, newPassword: String, confirmPassword: String): Observable<UpdateUserPasswordMutation.Data> {
                return Observable.just(UpdateUserPasswordMutation.Data(UpdateUserPasswordMutation.UpdateUserAccount("",
                        UpdateUserPasswordMutation.User("", "test@email.com"))))
            }
        }).build())

        this.vm.inputs.currentPassword("password")
        this.vm.inputs.newPassword("password")
        this.vm.inputs.confirmPassword("password")
        this.vm.inputs.changePasswordClicked()
        this.success.assertValue("test@email.com")
    }
}
