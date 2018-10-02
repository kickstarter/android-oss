package com.kickstarter.viewmodels

import android.support.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.services.ApolloClientType
import com.kickstarter.ui.activities.AccountActivity
import rx.Observable
import rx.subjects.PublishSubject

interface AccountActivityViewModel {

    interface Inputs {


    }

    interface Outputs {

        /** Emits the current user's chosen Currency. */
        fun chosenCurrency(): Observable<String>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<AccountActivity>(environment), Inputs, Outputs {


        val inputs: Inputs = this
        val outputs: Outputs = this

        private val chosenCurrency = PublishSubject.create<String>()

        private val apolloClient: ApolloClientType = environment.apolloClient()

        init {

            this.apolloClient.userPrivacy()
                    .compose(bindToLifecycle())
                    .subscribe {
                        val currency = it.me()?.chosenCurrency()
                        this@ViewModel.chosenCurrency.onNext(currency)
                    }

        }

        override fun chosenCurrency() = this.chosenCurrency
    }
}