package com.kickstarter.viewmodels

import DeletePaymentSourceMutation
import UserPaymentsQuery
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.*
import com.kickstarter.ui.activities.PaymentMethodsActivity
import com.kickstarter.ui.adapters.PaymentMethodsAdapter
import com.kickstarter.ui.viewholders.PaymentMethodsViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface PaymentMethodsViewModel {

    interface Inputs {
        /** Delete a payment source from the list. */
        fun deleteCardClicked(paymentSourceId: String)

        /** Invokes when the user clicks the delete button. */
        fun confirmDeleteCardClicked()

        /** Call when a new card has been added and the list needs to be updated. */
        fun refreshCards()
    }

    interface Outputs {
        /** Emits a list of stored cards for a user. */
        fun cards(): Observable<MutableList<UserPaymentsQuery.Node>>

        /** Emits whenever there is an error updating the user's currency.  */
        fun error(): Observable<String>

        fun showDeleteCardDialog(): Observable<Void>

        /** Emits when the currency update was successful. */
        fun success(): Observable<String>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<PaymentMethodsActivity>(environment), PaymentMethodsAdapter.Delegate, Inputs, Outputs {

        private val confirmDeleteCardClicked = BehaviorSubject.create<Void>()
        private val deleteCardClicked = PublishSubject.create<String>()
        private val refreshCards = PublishSubject.create<Void>()

        private val cards = BehaviorSubject.create<MutableList<UserPaymentsQuery.Node>>()
        private val error = BehaviorSubject.create<String>()
        private val showDeleteCardDialog = BehaviorSubject.create<Void>()
        private val success = BehaviorSubject.create<String>()

        private val client = environment.apolloClient()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            getListOfStoredCards()
                    .subscribe { this.cards.onNext(it) }

            this.deleteCardClicked.subscribe { this.showDeleteCardDialog.onNext(null) }

            val deleteCardNotification = this.deleteCardClicked
                    .compose<String>(takeWhen(this.confirmDeleteCardClicked))
                    .switchMap { deletePaymentSource(it).materialize() }
                    .compose(bindToLifecycle())
                    .share()

            deleteCardNotification
                    .compose(values())
                    .map { it.paymentSourceDelete()?.clientMutationId() }
                    .subscribe { this.success.onNext(it) }

            deleteCardNotification
                    .compose(Transformers.errors())
                    .subscribe { this.error.onNext(it.localizedMessage) }

            this.refreshCards
                    .switchMap { getListOfStoredCards() }
                    .subscribe { this.cards.onNext(it) }
        }

        override fun deleteCardClicked(paymentSourceId: String) = this.deleteCardClicked.onNext(paymentSourceId)

        override fun deleteCardButtonClicked(paymentMethodsViewHolder: PaymentMethodsViewHolder, paymentSourceId: String) {
            return this.deleteCardClicked(paymentSourceId)
        }

        override fun confirmDeleteCardClicked() = this.confirmDeleteCardClicked.onNext(null)

        override fun cards(): Observable<MutableList<UserPaymentsQuery.Node>> = this.cards

        override fun error(): Observable<String> = this.error

        override fun refreshCards() = this.refreshCards.onNext(null)

        override fun showDeleteCardDialog(): Observable<Void> = this.showDeleteCardDialog

        override fun success(): Observable<String> = this.success

        private fun getListOfStoredCards(): Observable<MutableList<UserPaymentsQuery.Node>> {
            return this.client.getStoredCards()
                    .compose(bindToLifecycle())
                    .compose(neverError())
                    .map { cards -> cards.me()?.storedCards()?.nodes() }
        }

        private fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data> {
            return this.client.deletePaymentSource(paymentSourceId)
        }

    }
}
