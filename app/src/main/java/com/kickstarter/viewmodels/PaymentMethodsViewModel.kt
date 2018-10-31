package com.kickstarter.viewmodels

import UserPaymentsQuery
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.ui.activities.PaymentMethodsActivity
import com.kickstarter.ui.adapters.PaymentMethodsAdapter
import rx.Observable
import rx.subjects.BehaviorSubject

interface PaymentMethodsViewModel {

    interface Outputs {
        /** Emits a list of stored cards for a user. */
        fun getCards(): Observable<MutableList<UserPaymentsQuery.Node>>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<PaymentMethodsActivity>(environment), PaymentMethodsAdapter.Delegate, Outputs {

        private val cards = BehaviorSubject.create<MutableList<UserPaymentsQuery.Node>>()

        private val client = environment.apolloClient()

        val outputs: Outputs = this

        init {
            this.client.getStoredCards()
                    .compose(bindToLifecycle())
                    .map { cards -> cards.me()?.storedCards()?.nodes() }
                    .subscribe {
                        cards.onNext(it)
                    }
        }

        override fun getCards(): Observable<MutableList<UserPaymentsQuery.Node>> = this.cards
    }
}
