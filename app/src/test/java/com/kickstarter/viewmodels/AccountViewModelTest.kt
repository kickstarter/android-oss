package com.kickstarter.viewmodels

import UpdateUserCurrencyMutation
import UserPrivacyQuery
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.viewmodels.AccountViewModel.AccountViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test
import type.CurrencyCode

class AccountViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: AccountViewModel

    private val chosenCurrency = TestSubscriber<String>()
    private val email = TestSubscriber<String>()
    private val error = TestSubscriber<String>()
    private val passwordRequiredContainerIsVisible = TestSubscriber<Boolean>()
    private val showEmailErrorIcon = TestSubscriber<Boolean>()
    private val success = TestSubscriber<String>()

    private val disposables = CompositeDisposable()

    @After
    fun cleanUp() {
        disposables.clear()
    }
    private fun setUpEnvironment(environment: Environment) {
        this.vm = AccountViewModel(environment)

        this.vm.outputs.chosenCurrency().subscribe { this.chosenCurrency.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.email().subscribe { this.email.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.error().subscribe { this.error.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.passwordRequiredContainerIsVisible().subscribe { this.passwordRequiredContainerIsVisible.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.showEmailErrorIcon().subscribe { this.showEmailErrorIcon.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.success().subscribe { this.success.onNext(it) }.addToDisposable(disposables)
    }

    @Test
    fun testUserCurrency() {
        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                    return Observable.just(
                        UserPrivacyQuery.Data(
                            UserPrivacyQuery.Me(
                                "", "",
                                "", true, true, true, true, "MXN"
                            )
                        )
                    )
                }
            }).build()
        )

        this.chosenCurrency.assertValue("MXN")
        this.showEmailErrorIcon.assertValue(false)
    }

    @Test
    fun testUserEmail() {
        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                    return Observable.just(
                        UserPrivacyQuery.Data(
                            UserPrivacyQuery.Me(
                                "", "",
                                "r@ksr.com", true, true, true, true, "USD"
                            )
                        )
                    )
                }
            }).build()
        )

        this.email.assertValue("r@ksr.com")
        this.showEmailErrorIcon.assertValue(false)
    }

    @Test
    fun testChosenCurrencyMutation() {
        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                    return Observable.just(
                        UserPrivacyQuery.Data(
                            UserPrivacyQuery.Me(
                                "", "",
                                "r@ksr.com", true, true, true, true, "USD"
                            )
                        )
                    )
                }

                override fun updateUserCurrencyPreference(currency: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data> {
                    return Observable.just(
                        UpdateUserCurrencyMutation.Data(
                            UpdateUserCurrencyMutation
                                .UpdateUserProfile("", UpdateUserCurrencyMutation.User("", currency.rawValue()))
                        )
                    )
                }
            }).build()
        )

        this.chosenCurrency.assertValue("USD")
        this.vm.inputs.onSelectedCurrency(CurrencyCode.AUD)
        this.chosenCurrency.assertValues("USD", CurrencyCode.AUD.rawValue())
        this.success.assertValue(CurrencyCode.AUD.rawValue())
        this.vm.inputs.onSelectedCurrency(CurrencyCode.AUD)
        this.chosenCurrency.assertValues("USD", CurrencyCode.AUD.rawValue())
    }

    @Test
    fun testShowEmailErrorIcon() {
        val hasPassword = true
        val isCreator = true
        val isDeliverable = false
        val isEmailVerified = true
        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                    return Observable.just(
                        UserPrivacyQuery.Data(
                            UserPrivacyQuery.Me(
                                "", "",
                                "", hasPassword, isCreator, isDeliverable, isEmailVerified, "MXN"
                            )
                        )
                    )
                }
            }).build()
        )

        this.showEmailErrorIcon.assertValue(true)
    }

    @Test
    fun testShowEmailErrorIconForBackerUndeliverable() {
        val hasPassword = true
        val isCreator = false
        val isDeliverable = false
        val isEmailVerified = false
        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                    return Observable.just(
                        UserPrivacyQuery.Data(
                            UserPrivacyQuery.Me(
                                "", "",
                                "", hasPassword, isCreator, isDeliverable, isEmailVerified, "MXN"
                            )
                        )
                    )
                }
            }).build()
        )

        this.showEmailErrorIcon.assertValue(true)
    }

    @Test
    fun testShowEmailErrorIconGoneForBackerUnverified() {
        val hasPassword = true
        val isCreator = false
        val isDeliverable = true
        val isEmailVerified = true
        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                    return Observable.just(
                        UserPrivacyQuery.Data(
                            UserPrivacyQuery.Me(
                                "", "",
                                "", hasPassword, isCreator, isDeliverable, isEmailVerified, "MXN"
                            )
                        )
                    )
                }
            }).build()
        )

        this.showEmailErrorIcon.assertValue(false)
    }

    @Test
    fun testPasswordRequiredContainerIsVisible_hasPassword() {
        val hasPassword = true
        val isCreator = false
        val isDeliverable = true
        val isEmailVerified = false
        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                    return Observable.just(
                        UserPrivacyQuery.Data(
                            UserPrivacyQuery.Me(
                                "", "",
                                "", hasPassword, isCreator, isDeliverable, isEmailVerified, "MXN"
                            )
                        )
                    )
                }
            }).build()
        )

        this.passwordRequiredContainerIsVisible.assertValue(true)
    }

    @Test
    fun testPasswordRequiredContainerIsVisible_noPassword() {
        val hasPassword = false
        val isCreator = false
        val isDeliverable = true
        val isEmailVerified = false
        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                    return Observable.just(
                        UserPrivacyQuery.Data(
                            UserPrivacyQuery.Me(
                                "", "",
                                "", hasPassword, isCreator, isDeliverable, isEmailVerified, "MXN"
                            )
                        )
                    )
                }
            }).build()
        )

        this.passwordRequiredContainerIsVisible.assertValue(false)
    }

    @Test
    fun testShowEmailErrorIconGoneForBackerDeliverable() {
        val hasPassword = true
        val isCreator = false
        val isDeliverable = true
        val isEmailVerified = false
        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                    return Observable.just(
                        UserPrivacyQuery.Data(
                            UserPrivacyQuery.Me(
                                "", "",
                                "", hasPassword, isCreator, isDeliverable, isEmailVerified, "MXN"
                            )
                        )
                    )
                }
            }).build()
        )

        this.showEmailErrorIcon.assertValue(false)
    }

    @Test
    fun testShowEmailErrorIconForCreatorUnverified() {
        val hasPassword = true
        val isCreator = true
        val isDeliverable = false
        val isEmailVerified = false
        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                    return Observable.just(
                        UserPrivacyQuery.Data(
                            UserPrivacyQuery.Me(
                                "", "",
                                "", hasPassword, isCreator, isDeliverable, isEmailVerified, "MXN"
                            )
                        )
                    )
                }
            }).build()
        )

        this.showEmailErrorIcon.assertValue(true)
    }
}
