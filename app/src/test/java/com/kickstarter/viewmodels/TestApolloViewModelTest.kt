package com.kickstarter.viewmodels

import UserPrivacyQuery
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.services.MockApolloClient
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class TestApolloViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: TestApolloViewModel.ViewModel

    private val email = TestSubscriber<String>()
    private val error = TestSubscriber<String>()
    private val name = TestSubscriber<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = TestApolloViewModel.ViewModel(environment)

        this.vm.outputs.email().subscribe(this.email)
        this.vm.outputs.name().subscribe(this.name)
        this.vm.errors.error().subscribe(this.error)
    }

    @Test
    fun testEmail() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                return Observable.just(UserPrivacyQuery.Data(UserPrivacyQuery.Me("", "", "izzy@test.com")))
            }
        }).build())

        this.vm.inputs.makeNetworkCallClicked()
        this.email.assertValue("izzy@test.com")

        this.vm.inputs.makeNetworkCallWithErrorsClicked()
        this.email.assertValues("izzy@test.com", "izzy@test.com")
    }

    @Test
    fun testName() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                return Observable.just(UserPrivacyQuery.Data(UserPrivacyQuery.Me("", "Izzy", "")))
            }
        }).build())

        this.vm.inputs.makeNetworkCallClicked()
        this.name.assertValue("Izzy")

        this.vm.inputs.makeNetworkCallWithErrorsClicked()
        this.name.assertValues("Izzy","Izzy")
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
