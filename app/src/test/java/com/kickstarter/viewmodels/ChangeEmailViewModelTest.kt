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

    private val email = TestSubscriber<String>()
    private val error = TestSubscriber<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ChangeEmailViewModel.ViewModel(environment)

        this.vm.outputs.email().subscribe(this.email)
        this.vm.errors.error().subscribe(this.error)
    }

    @Test
    fun testEmail() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                return Observable.just(UserPrivacyQuery.Data(UserPrivacyQuery.Me("", "", "rashad@test.com")))
            }
        }).build())

        this.email.assertValue("rashad@test.com")
    }

    @Test
    fun testEmailMutation() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun updateUserEmail(email: String, currentPassword: String): Observable<UpdateUserEmailMutation.Data> {
                return Observable.just(UpdateUserEmailMutation.Data(UpdateUserEmailMutation
                        .UpdateUserAccount("", UpdateUserEmailMutation.User("", "", email))))
            }
        }).build())

        this.email.assertValue("some@email.com")
        this.vm.inputs.updateEmailClicked("rashad@gmail.com", "somepassword")
        this.email.assertValues("some@email.com", "rashad@gmail.com")
    }
}
