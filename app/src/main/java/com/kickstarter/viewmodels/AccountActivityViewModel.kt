package com.kickstarter.viewmodels

import UpdateUserCurrencyMutation
import android.support.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.services.ApolloClientType
import com.kickstarter.ui.activities.AccountActivity
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import timber.log.Timber
import type.CurrencyCode

interface AccountActivityViewModel {

    interface Inputs {
        /** Notifies when the spinner has been selected. */
        fun onSelectedCurrency(currencyCode: CurrencyCode)

        /** Emits when Following switch should be turned back on after user cancels opting out.  */
        fun hideCurrencyDialog(): Observable<Void>
    }

    interface Outputs {
        /** Emits the current user's chosen Currency. */
        fun chosenCurrency(): Observable<String>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<AccountActivity>(environment), Inputs, Outputs {

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val chosenCurrency = PublishSubject.create<String>()

        private val hideCurrencyDialog = BehaviorSubject.create<Void>()
        private val onSelectedCurrency = BehaviorSubject.create<CurrencyCode>()

        private val apolloClient: ApolloClientType = environment.apolloClient()

        init {

            this.apolloClient.userPrivacy()
                    .compose(bindToLifecycle())
                    .subscribe {
                        val currency = it.me()?.chosenCurrency()
                        this@ViewModel.chosenCurrency.onNext(currency)
                    }

            this.onSelectedCurrency
                    .compose(combineLatestPair<CurrencyCode, String>(this.chosenCurrency))
                    .compose(bindToLifecycle())
                    .filter { it.first.rawValue() != it.second }
                    .map<CurrencyCode> { it.first }
                    .switchMap { updateUserCurrency(it) }
                    .subscribe {
                        Timber.d("Currency change was successful")
                    }

        }

        override fun chosenCurrency(): Observable<String> = this.chosenCurrency

        override fun onSelectedCurrency(currencyCode: CurrencyCode) {
            this.onSelectedCurrency.onNext(currencyCode)
        }

        override fun hideCurrencyDialog(): BehaviorSubject<Void> = this.hideCurrencyDialog

        private fun updateUserCurrency(currencyCode: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data> {
            return this.apolloClient.updateUserCurrencyPreference(currencyCode)

        }
    }
}
