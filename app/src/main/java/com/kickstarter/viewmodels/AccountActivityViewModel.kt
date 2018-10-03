package com.kickstarter.viewmodels

import UpdateUserCurrencyMutation
import android.support.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.values
import com.kickstarter.services.ApolloClientType
import com.kickstarter.ui.activities.AccountActivity
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import type.CurrencyCode

interface AccountActivityViewModel {

    interface Inputs {
        /** Notifies when the spinner has been selected. */
        fun onSelectedCurrency(currencyCode: CurrencyCode)
    }

    interface Outputs {
        /** Emits the current user's chosen Currency. */
        fun chosenCurrency(): Observable<String>
    }

    interface Errors {
        /** Emits a string to display when user could not be found.  */
        fun error(): Observable<String>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<AccountActivity>(environment), Inputs, Outputs, Errors {

        val inputs: Inputs = this
        val outputs: Outputs = this
        val errors: Errors = this

        private val onSelectedCurrency = PublishSubject.create<CurrencyCode>()

        private val chosenCurrency = BehaviorSubject.create<String>()

        private val error = BehaviorSubject.create<String>()

        private val apolloClient: ApolloClientType = environment.apolloClient()

        init {

            this.apolloClient.userPrivacy()
                    .map { it.me()?.chosenCurrency() }
                    .compose(bindToLifecycle())
                    .subscribe { this.chosenCurrency.onNext(it) }

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
                    .subscribe { this.chosenCurrency.onNext(it) }

            updateCurrencyNotification
                    .compose(Transformers.errors())
                    .subscribe { this.error.onNext(it.localizedMessage) }

        }

        override fun onSelectedCurrency(currencyCode: CurrencyCode) {
            this.onSelectedCurrency.onNext(currencyCode)
        }

        override fun chosenCurrency(): BehaviorSubject<String> = this.chosenCurrency

        override fun error(): Observable<String> = this.error

        private fun updateUserCurrency(currencyCode: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data> {
            return this.apolloClient.updateUserCurrencyPreference(currencyCode)

        }
    }
}
