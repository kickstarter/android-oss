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

class AccountActivityViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: AccountActivityViewModel.ViewModel

    private val chosenCurrency = TestSubscriber<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = AccountActivityViewModel.ViewModel(environment)

        this.vm.outputs.chosenCurrency().subscribe(this.chosenCurrency)
    }

    @Test
    fun testUserCurrency() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                return Observable.just(UserPrivacyQuery.Data(UserPrivacyQuery.Me("", "",
                        "", "", "MXN")))
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
    }
}


