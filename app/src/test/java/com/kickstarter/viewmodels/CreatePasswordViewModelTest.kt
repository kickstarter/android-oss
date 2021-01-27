package com.kickstarter.viewmodels

import CreatePasswordMutation
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.mock.services.MockApolloClient
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class CreatePasswordViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: CreatePasswordViewModel.ViewModel

    private val error = TestSubscriber<String>()
    private val passwordWarning = TestSubscriber<Int>()
    private val progressBarIsVisible = TestSubscriber<Boolean>()
    private val saveButtonIsEnabled = TestSubscriber<Boolean>()
    private val success = TestSubscriber<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = CreatePasswordViewModel.ViewModel(environment)

        this.vm.outputs.error().subscribe(this.error)
        this.vm.outputs.passwordWarning().subscribe(this.passwordWarning)
        this.vm.outputs.progressBarIsVisible().subscribe(this.progressBarIsVisible)
        this.vm.outputs.saveButtonIsEnabled().subscribe(this.saveButtonIsEnabled)
        this.vm.outputs.success().subscribe(this.success)
    }

    @Test
    fun testError() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun createPassword(password: String, confirmPassword: String): Observable<CreatePasswordMutation.Data> {
                return Observable.error(Exception("Oops"))
            }
        }).build())

        this.vm.inputs.newPassword("passwo")
        this.vm.inputs.confirmPassword("password")
        this.vm.inputs.createPasswordClicked()
        this.error.assertValue("Oops")
    }

    @Test
    fun testPasswordWarning() {
        setUpEnvironment(environment())

        this.vm.inputs.newPassword("p")
        this.passwordWarning.assertValues(null, R.string.Password_min_length_message)
        this.vm.inputs.newPassword("password")
        this.passwordWarning.assertValues(null, R.string.Password_min_length_message, null)
        this.vm.inputs.confirmPassword("p")
        this.passwordWarning.assertValues(null, R.string.Password_min_length_message, null, R.string.Passwords_matching_message)
        this.vm.inputs.confirmPassword("passw")
        this.passwordWarning.assertValues(null, R.string.Password_min_length_message, null, R.string.Passwords_matching_message)
        this.vm.inputs.confirmPassword("password")
        this.passwordWarning.assertValues(null, R.string.Password_min_length_message, null, R.string.Passwords_matching_message, null)
    }

    @Test
    fun testProgressBarIsVisible() {
        setUpEnvironment(environment())

        this.vm.inputs.newPassword("password")
        this.vm.inputs.confirmPassword("password")
        this.vm.inputs.createPasswordClicked()
        this.progressBarIsVisible.assertValues(true, false)
    }

    @Test
    fun testSaveButtonIsEnabled() {
        setUpEnvironment(environment())

        this.vm.inputs.newPassword("password")
        this.vm.inputs.confirmPassword("password")
        this.saveButtonIsEnabled.assertValues(false, true)
        this.vm.inputs.confirmPassword("pass")
        this.saveButtonIsEnabled.assertValues(false, true, false)
        this.vm.inputs.confirmPassword("passwerd")
        this.saveButtonIsEnabled.assertValues(false, true, false)
    }

    @Test
    fun testSuccess() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun createPassword(password: String, confirmPassword: String): Observable<CreatePasswordMutation.Data> {
                return Observable.just(CreatePasswordMutation.Data(CreatePasswordMutation.UpdateUserAccount("",
                        CreatePasswordMutation.User("", "test@emai", true))))
            }
        }).build())

        this.vm.inputs.newPassword("password")
        this.vm.inputs.confirmPassword("password")
        this.vm.inputs.createPasswordClicked()
        this.success.assertValue("test@emai")
    }
}
