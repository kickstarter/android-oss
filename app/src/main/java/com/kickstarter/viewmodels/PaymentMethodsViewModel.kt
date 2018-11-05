package com.kickstarter.viewmodels

import DeletePaymentSourceMutation
import UserPaymentsQuery
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.values
import com.kickstarter.ui.activities.PaymentMethodsActivity
import com.kickstarter.ui.adapters.PaymentMethodsAdapter
import com.kickstarter.ui.viewholders.PaymentMethodsViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface PaymentMethodsViewModel {
    interface Inputs {
        /** Delete a payment source from the list. */
        fun deleteCard(paymentSourceId: String)

        /** Invokes when the user clicks the delete button. */
        fun deleteCardClick()
    }

    interface Outputs {
        /** Emits whenever there is an error updating the user's currency.  */
        fun error(): Observable<String>

        /** Emits a list of stored cards for a user. */
        fun getCards(): Observable<MutableList<UserPaymentsQuery.Node>>

        /** Emits when the currency update was successful. */
        fun success(): Observable<String>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<PaymentMethodsActivity>(environment), PaymentMethodsAdapter.Delegate, Inputs, Outputs {


        private val cards = BehaviorSubject.create<MutableList<UserPaymentsQuery.Node>>()
        private val deleteCardClicked = PublishSubject.create<String>()

        private val success = BehaviorSubject.create<String>()

        private val error = BehaviorSubject.create<String>()

        private val client = environment.apolloClient()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            this.client.getStoredCards()
                    .compose(bindToLifecycle())
                    .map { cards -> cards.me()?.storedCards()?.nodes() }
                    .subscribe { this.cards.onNext(it) }

            val deleteCardNotification = this.deleteCardClicked
                    .compose(combineLatestPair<Void, String>(this.))
                    .switchMap { deletePaymentSource(it.second).materialize() }
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

        override fun deleteCard(paymentSourceId: String) = this.deleteCardClicked.onNext(paymentSourceId)

        override fun deleteCardButtonClicked(paymentMethodsViewHolder: PaymentMethodsViewHolder, paymentSourceId: String) {
            return this.deleteCard(paymentSourceId)
        }

        override fun deleteCardClick() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getCards(): Observable<MutableList<UserPaymentsQuery.Node>> = this.cards

        override fun error(): Observable<String> = this.error

        override fun success(): Observable<String> = this.success

        private fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data> {
            return this.client.deletePaymentSource(paymentSourceId)
        }

    }
}
