package com.kickstarter.viewmodels

import UserPaymentsQuery
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.ui.activities.PaymentMethodsActivity
import com.kickstarter.ui.adapters.PaymentMethodsAdapter
import rx.Observable
import rx.subjects.BehaviorSubject
import timber.log.Timber

interface PaymentMethodsViewModel {
    interface Inputs {

    }

    interface Outputs {

        fun getCards(): Observable<MutableList<UserPaymentsQuery.Node>>

    }

    class ViewModel(environment: Environment) : ActivityViewModel<PaymentMethodsActivity>(environment), PaymentMethodsAdapter.Delegate, Inputs, Outputs {


        private val cards = BehaviorSubject.create<MutableList<UserPaymentsQuery.Node>>()

        private val client = environment.apolloClient()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            this.client.getStoredCards()
                    .compose(bindToLifecycle())
                    .map { cards -> cards.me()?.storedCards()?.nodes() }
                    .subscribe {
                        cards.onNext(it)
                        Timber.d("Got a card ${it.toString()}")
                    }

        }

        override fun getCards(): Observable<MutableList<UserPaymentsQuery.Node>> = this.cards

    }
}