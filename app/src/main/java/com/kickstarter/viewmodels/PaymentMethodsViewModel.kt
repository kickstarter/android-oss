package com.kickstarter.viewmodels

import UserPaymentsQuery
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.errors
import com.kickstarter.libs.rx.transformers.Transformers.values
import com.kickstarter.ui.activities.PaymentMethodsActivity
import com.kickstarter.ui.adapters.PaymentMethodsAdapter
import rx.Observable
import rx.subjects.BehaviorSubject

interface PaymentMethodsViewModel {

    interface Outputs {
        /** Emits if there's an error with retrieving a card. */
        fun error(): Observable<String>

        /** Emits a list of stored cards for a user. */
        fun getCards(): Observable<MutableList<UserPaymentsQuery.Node>>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<PaymentMethodsActivity>(environment), PaymentMethodsAdapter.Delegate, Outputs {

        private val cards = BehaviorSubject.create<MutableList<UserPaymentsQuery.Node>>()
        private val error = BehaviorSubject.create<String>()

        private val client = environment.apolloClient()

        val outputs: Outputs = this

        init {

            val getCardNotification = this.client.getStoredCards().materialize()
                    .compose(bindToLifecycle())
                    .share()

            getCardNotification
                    .compose(values())
                    .map { cards -> cards.me()?.storedCards()?.nodes() }
                    .subscribe {
                        cards.onNext(it)
                    }

            getCardNotification
                    .compose(errors())
                    .subscribe { this.error.onNext(it.localizedMessage) }

        }

        override fun error(): Observable<String> = this.error

        override fun getCards(): Observable<MutableList<UserPaymentsQuery.Node>> = this.cards
    }
}
