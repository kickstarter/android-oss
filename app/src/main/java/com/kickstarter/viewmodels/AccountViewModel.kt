package com.kickstarter.viewmodels

import UpdateUserCurrencyMutation
import UserPrivacyQuery
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.values
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.services.ApolloClientType
import com.kickstarter.ui.activities.AccountActivity
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import type.CurrencyCode

interface AccountViewModel {

    interface Inputs {
        /** Notifies when the spinner has been selected. */
        fun onSelectedCurrency(currencyCode: CurrencyCode)
    }

    interface Outputs {
        /** Emits the current user's chosen Currency. */
        fun chosenCurrency(): Observable<String>

        /** Emits whenever there is an error updating the user's currency.  */
        fun error(): Observable<String>

        /** Emits when the password required container should be visible. */
        fun passwordRequiredContainerIsVisible(): Observable<Boolean>

        /** Emits when the progress bar should be visible. */
        fun progressBarIsVisible(): Observable<Boolean>

        /** Emits a boolean determining when we should show the email error icon. */
        fun showEmailErrorIcon(): Observable<Boolean>

        /** Emits when the currency update was successful. */
        fun success(): Observable<String>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<AccountActivity>(environment), Inputs, Outputs {

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val onSelectedCurrency = PublishSubject.create<CurrencyCode>()

        private val chosenCurrency = BehaviorSubject.create<String>()
        private val passwordRequiredContainerIsVisible = BehaviorSubject.create<Boolean>()
        private val progressBarIsVisible = BehaviorSubject.create<Boolean>()
        private val showEmailErrorIcon = BehaviorSubject.create<Boolean>()
        private val success = BehaviorSubject.create<String>()

        private val error = BehaviorSubject.create<String>()

        private val apolloClient: ApolloClientType = environment.apolloClient()

        init {

            val userPrivacy = this.apolloClient.userPrivacy()

            userPrivacy
                    .map { it.me()?.chosenCurrency() }
                    .compose(Transformers.neverError())
                    .map { ObjectUtils.coalesce(it, CurrencyCode.USD.rawValue()) }
                    .compose(bindToLifecycle())
                    .subscribe { this.chosenCurrency.onNext(it) }

            userPrivacy
                    .map { it?.me()?.hasPassword() ?: false }
                    .subscribe { this.passwordRequiredContainerIsVisible.onNext(it) }

            userPrivacy
                    .map { showEmailErrorImage(it) }
                    .subscribe { this.showEmailErrorIcon.onNext(it) }

            val updateCurrencyNotification = this.onSelectedCurrency
                    .compose(combineLatestPair<CurrencyCode, String>(this.chosenCurrency))
                    .filter { it.first.rawValue() != it.second }
                    .map<CurrencyCode> { it.first }
                    .switchMap { updateUserCurrency(it).materialize() }
                    .compose(bindToLifecycle())
                    .share()

            updateCurrencyNotification
                    .compose(values())
                    .map { it.updateUserProfile()?.user()?.chosenCurrency() }
                    .filter { ObjectUtils.isNotNull(it) }
                    .subscribe {
                        this.chosenCurrency.onNext(it)
                        this.success.onNext(it)
                        this.koala.trackSelectedChosenCurrency(it)
                    }

            updateCurrencyNotification
                    .compose(Transformers.errors())
                    .subscribe { this.error.onNext(it.localizedMessage) }

            this.koala.trackViewedAccount()
        }

        override fun onSelectedCurrency(currencyCode: CurrencyCode) {
            this.onSelectedCurrency.onNext(currencyCode)
        }

        override fun chosenCurrency(): BehaviorSubject<String> = this.chosenCurrency

        override fun error(): Observable<String> = this.error

        override fun passwordRequiredContainerIsVisible(): Observable<Boolean> = this.passwordRequiredContainerIsVisible

        override fun progressBarIsVisible(): Observable<Boolean> = this.progressBarIsVisible

        override fun showEmailErrorIcon(): Observable<Boolean> = this.showEmailErrorIcon

        override fun success(): BehaviorSubject<String> = this.success

        private fun showEmailErrorImage(userPrivacy: UserPrivacyQuery.Data?): Boolean? {
            val creator = userPrivacy?.me()?.isCreator ?: false
            val deliverable = userPrivacy?.me()?.isDeliverable ?: false
            val isEmailVerified = userPrivacy?.me()?.isEmailVerified ?: false

            return if (!deliverable) {
                return true
            } else if (creator && !isEmailVerified) {
                return true
            } else {
                false
            }
        }

        private fun updateUserCurrency(currencyCode: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data> {
            return this.apolloClient.updateUserCurrencyPreference(currencyCode)
                    .doOnSubscribe { this.progressBarIsVisible.onNext(true) }
                    .doAfterTerminate { this.progressBarIsVisible.onNext(false) }

        }
    }
}
