package com.kickstarter.viewmodels

import UpdateUserEmailMutation
import UserPrivacyQuery
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.services.MockApolloClient
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class ChangeEmailViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ChangeEmailViewModel.ViewModel

    private val currentEmail = TestSubscriber<String>()
    private val emailErrorIsVisible = TestSubscriber<Boolean>()
    private val error = TestSubscriber<String>()
    private val progressBarIsVisible = TestSubscriber<Boolean>()
    private val saveButtonIsEnabled = TestSubscriber<Boolean>()
    private val success = TestSubscriber<Void>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ChangeEmailViewModel.ViewModel(environment)

        this.vm.outputs.currentEmail().subscribe(this.currentEmail)
        this.vm.outputs.emailErrorIsVisible().subscribe(this.emailErrorIsVisible)
        this.vm.outputs.error().subscribe(this.error)
        this.vm.outputs.progressBarIsVisible().subscribe(this.progressBarIsVisible)
        this.vm.outputs.saveButtonIsEnabled().subscribe(this.saveButtonIsEnabled)
        this.vm.outputs.success().subscribe(this.success)
    }

    @Test
    fun testCurrentEmail() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                return Observable.just(UserPrivacyQuery.Data(UserPrivacyQuery.Me("", "",
                        "rashad@test.com", "")))
            }
        }).build())

        this.currentEmail.assertValue("rashad@test.com")
    }

    @Test
    fun testEmailErrorIsVisible() {
        setUpEnvironment(environment())

        this.vm.inputs.emailFocus(true)
        this.vm.inputs.email("izzy@")
        this.emailErrorIsVisible.assertValue(false)
        this.vm.inputs.emailFocus(false)
        this.emailErrorIsVisible.assertValues(false, true)
    }

    @Test
    fun testError() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                return Observable.error(Throwable("beep"))
            }

            override fun updateUserEmail(email: String, currentPassword: String): Observable<UpdateUserEmailMutation.Data> {
                return Observable.error(Throwable("boop"))
            }
        }).build())

        this.currentEmail.assertNoValues()
        this.error.assertNoValues()

        this.vm.inputs.email("test@email.com")
        this.vm.inputs.password("password")
        this.vm.inputs.updateEmailClicked()
        this.error.assertValue("boop")
    }

    @Test
    fun testProgressBarIsVisible() {
        setUpEnvironment(environment())

        this.vm.inputs.email("izzy@email.com")
        this.vm.inputs.password("password")
        this.vm.inputs.updateEmailClicked()
        this.progressBarIsVisible.assertValues(true, false)
    }

    @Test
    fun testSaveButtonIsEnabled() {
        setUpEnvironment(environment())

        this.vm.inputs.email("izzy@email.com")
        this.vm.inputs.password("password")
        this.saveButtonIsEnabled.assertValue(true)
        this.vm.inputs.email("izzy@emailcom")
        this.saveButtonIsEnabled.assertValues(true, false)
        this.vm.inputs.email("izzy@email.com")
        this.saveButtonIsEnabled.assertValues(true, false, true)
        this.vm.inputs.password("passw")
        this.saveButtonIsEnabled.assertValues(true, false, true, false)
    }

    @Test
    fun testSuccess() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun updateUserEmail(email: String, currentPassword: String): Observable<UpdateUserEmailMutation.Data> {
                return Observable.just(UpdateUserEmailMutation.Data(UpdateUserEmailMutation
                        .UpdateUserAccount("", UpdateUserEmailMutation.User("", "", email))))
            }
        }).build())

        this.currentEmail.assertValue("some@email.com")
        this.vm.inputs.email("rashad@gmail.com")
        this.vm.inputs.password("password")
        this.vm.inputs.updateEmailClicked()
        this.currentEmail.assertValues("some@email.com", "rashad@gmail.com")
        this.success.assertValueCount(1)
    }
}
