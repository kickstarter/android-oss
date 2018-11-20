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
    private val showEmailErrorIcon = TestSubscriber<Boolean>()
    private val success = TestSubscriber<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = AccountViewModel.ViewModel(environment)

        this.vm.outputs.chosenCurrency().subscribe(this.chosenCurrency)
        this.vm.outputs.error().subscribe(this.error)
        this.vm.outputs.showEmailErrorIcon().subscribe(this.showEmailErrorIcon)
        this.vm.outputs.success().subscribe(this.success)
    }

    @Test
    fun testUserCurrency() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                return Observable.just(UserPrivacyQuery.Data(UserPrivacyQuery.Me("", "",
                        "",  true, true, true, "MXN")))
            }
        }).build())

        this.chosenCurrency.assertValue("MXN")
        this.showEmailErrorIcon.assertValue(false)
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

    @Test
    fun testShowEmailErrorIcon() {
        val isCreator = true
        val isDeliverable = false
        val isEmailVerified = true
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                return Observable.just(UserPrivacyQuery.Data(UserPrivacyQuery.Me("", "",
                        "",  isCreator, isDeliverable, isEmailVerified, "MXN")))
            }
        }).build())

        this.showEmailErrorIcon.assertValue(true)
    }

    @Test
    fun testShowEmailErrorIconForBackerUndeliverable() {
        val isCreator = false
        val isDeliverable = false
        val isEmailVerified = false
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                return Observable.just(UserPrivacyQuery.Data(UserPrivacyQuery.Me("", "",
                        "",  isCreator, isDeliverable, isEmailVerified, "MXN")))
            }
        }).build())

        this.showEmailErrorIcon.assertValue(true)
    }

    @Test
    fun testShowEmailErrorIconGoneForBackerUnverified() {
        val isCreator = false
        val isDeliverable = true
        val isEmailVerified = true
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                return Observable.just(UserPrivacyQuery.Data(UserPrivacyQuery.Me("", "",
                        "",  isCreator, isDeliverable, isEmailVerified, "MXN")))
            }
        }).build())

        this.showEmailErrorIcon.assertValue(false)
    }

    @Test
    fun testShowEmailErrorIconGoneForBackerDeliverable() {
        val isCreator = false
        val isDeliverable = true
        val isEmailVerified = false
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                return Observable.just(UserPrivacyQuery.Data(UserPrivacyQuery.Me("", "",
                        "",  isCreator, isDeliverable, isEmailVerified, "MXN")))
            }
        }).build())

        this.showEmailErrorIcon.assertValue(false)
    }

    @Test
    fun testShowEmailErrorIconForCreatorUnverified() {
        val isCreator = true
        val isDeliverable = false
        val isEmailVerified = false
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                return Observable.just(UserPrivacyQuery.Data(UserPrivacyQuery.Me("", "",
                        "",  isCreator, isDeliverable, isEmailVerified, "MXN")))
            }
        }).build())

        this.showEmailErrorIcon.assertValue(true)
    }
}
