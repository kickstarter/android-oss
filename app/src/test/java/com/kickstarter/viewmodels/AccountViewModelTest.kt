package com.kickstarter.viewmodels

import UpdateUserCurrencyMutation
import UserPrivacyQuery
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.services.MockApolloClient
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import type.CurrencyCode

class AccountViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: AccountViewModel.ViewModel

    private val chosenCurrency = TestSubscriber<String>()
    private val error = TestSubscriber<String>()
    private val success = TestSubscriber<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = AccountViewModel.ViewModel(environment)

        this.vm.outputs.chosenCurrency().subscribe(this.chosenCurrency)
        this.vm.outputs.error().subscribe(this.error)
        this.vm.outputs.success().subscribe(this.success)
    }

    @Test
    fun testUserCurrency() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                return Observable.just(UserPrivacyQuery.Data(UserPrivacyQuery.Me("", "",
                        "",  true, true, true, "MXN", "1234")))
            }
        }).build())

        this.chosenCurrency.assertValue("MXN")
    }

    @Test
    fun testChosenCurrencyMutation() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun updateUserCurrencyPreference(currency: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data> {
                return Observable.just(UpdateUserCurrencyMutation.Data(UpdateUserCurrencyMutation.
                        UpdateUserProfile("", UpdateUserCurrencyMutation.User("", currency.rawValue()))))
            }
        }).build())

        this.chosenCurrency.assertValue("USD")
        this.vm.inputs.onSelectedCurrency(CurrencyCode.AUD)
        this.chosenCurrency.assertValues("USD", CurrencyCode.AUD.rawValue())
        this.success.assertValue(CurrencyCode.AUD.rawValue())
        this.vm.inputs.onSelectedCurrency(CurrencyCode.AUD)
        this.chosenCurrency.assertValues("USD", CurrencyCode.AUD.rawValue())
    }

}
