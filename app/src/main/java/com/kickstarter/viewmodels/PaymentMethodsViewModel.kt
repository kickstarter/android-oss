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
    }

    interface Outputs {
        /** Emits whenever there is an error updating the user's currency.  */
        fun error(): Observable<String>

        /** Emits a list of stored cards for a user. */
        fun getCards(): Observable<MutableList<UserPaymentsQuery.Node>>

        fun showDeleteCardDialog(): Observable<Void>

        /** Emits when the currency update was successful. */
        fun success(): Observable<String>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<PaymentMethodsActivity>(environment), PaymentMethodsAdapter.Delegate, Inputs, Outputs {


        private val cards = BehaviorSubject.create<MutableList<UserPaymentsQuery.Node>>()
        private val confirmDeleteCardClicked = BehaviorSubject.create<Void>()

        private val deleteCardClicked = PublishSubject.create<String>()

        private val showDeleteCardDialog = BehaviorSubject.create<Void>()
        private val success = BehaviorSubject.create<String>()

        private val error = BehaviorSubject.create<String>()

        private val client = environment.apolloClient()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            this.client.getStoredCards()
                    .compose(neverError())
                    .compose(bindToLifecycle())
                    .map { cards -> cards.me()?.storedCards()?.nodes() }
                    .subscribe { this.cards.onNext(it) }

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
        }

        override fun deleteCardClicked(paymentSourceId: String) = this.deleteCardClicked.onNext(paymentSourceId)

        override fun deleteCardButtonClicked(paymentMethodsViewHolder: PaymentMethodsViewHolder, paymentSourceId: String) {
            return this.deleteCardClicked(paymentSourceId)
        }

        override fun confirmDeleteCardClicked() = this.confirmDeleteCardClicked.onNext(null)

        override fun getCards(): Observable<MutableList<UserPaymentsQuery.Node>> = this.cards

        override fun error(): Observable<String> = this.error

        override fun showDeleteCardDialog(): Observable<Void> = this.showDeleteCardDialog

        override fun success(): Observable<String> = this.success

        private fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data> {
            return this.client.deletePaymentSource(paymentSourceId)
        }

    }
}
