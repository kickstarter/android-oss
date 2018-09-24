package com.kickstarter.viewmodels

import UserPrivacyQuery
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.services.MockApolloClient
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class ChangeEmailViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: TestApolloViewModel.ViewModel

    private val email = TestSubscriber<String>()
    private val error = TestSubscriber<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = TestApolloViewModel.ViewModel(environment)

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

        this.vm.inputs.makeNetworkCallClicked()
        this.email.assertValue("rashad@test.com")

        this.vm.inputs.makeNetworkCallWithErrorsClicked()
        this.email.assertValues("rashad@test.com", "rashad@test.com")
    }

    @Test
    fun testError() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                return Observable.error(NullPointerException())
            }
        }).build())

        this.vm.inputs.makeNetworkCallClicked()
        this.error.assertNoValues()

        this.vm.inputs.makeNetworkCallWithErrorsClicked()
        this.error.assertValueCount(1)
    }
}