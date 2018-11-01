package com.kickstarter.viewmodels

import UserPaymentsQuery
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.neverError
import com.kickstarter.ui.activities.PaymentMethodsActivity
import com.kickstarter.ui.adapters.PaymentMethodsAdapter
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface PaymentMethodsViewModel {

    interface Inputs {
        /** Call when a new card has been added and the list needs to be updated. */
        fun refreshCards()
    }


    interface Outputs {
        /** Emits a list of stored cards for a user. */
        fun cards(): Observable<MutableList<UserPaymentsQuery.Node>>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<PaymentMethodsActivity>(environment), PaymentMethodsAdapter.Delegate, Inputs, Outputs {

        private val refreshCards = PublishSubject.create<Void>()

        private val cards = BehaviorSubject.create<MutableList<UserPaymentsQuery.Node>>()

        private val client = environment.apolloClient()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            getListOfStoredCards()
                    .subscribe { this.cards.onNext(it) }

            this.refreshCards
                    .switchMap { getListOfStoredCards() }
                    .subscribe { this.cards.onNext(it) }

        }

        private fun getListOfStoredCards(): Observable<MutableList<UserPaymentsQuery.Node>> {
            return this.client.getStoredCards()
                    .compose(bindToLifecycle())
                    .compose(neverError())
                    .map { cards -> cards.me()?.storedCards()?.nodes() }
        }

        override fun refreshCards() {
            this.refreshCards.onNext(null)
        }

        override fun cards(): Observable<MutableList<UserPaymentsQuery.Node>> = this.cards
    }
}
