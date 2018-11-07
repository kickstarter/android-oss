package com.kickstarter.viewmodels

import DeletePaymentSourceMutation
import UserPaymentsQuery
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.*
import com.kickstarter.ui.activities.PaymentMethodsSettingsActivity
import com.kickstarter.ui.adapters.PaymentMethodsAdapter
import com.kickstarter.ui.viewholders.PaymentMethodsViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface PaymentMethodsViewModel {

    interface Inputs {
        /** Invokes when the user clicks the delete button. */
        fun confirmDeleteCardClicked()

        /** Delete a payment source from the list. */
        fun deleteCardClicked(paymentSourceId: String)

        /** Call when a card has been added or removed and the list needs to be updated. */
        fun refreshCards()
    }

    interface Outputs {
        /** Emits a list of stored cards for a user. */
        fun cards(): Observable<MutableList<UserPaymentsQuery.Node>>

        /** Emits whenever there is an error deleting a stored card.  */
        fun error(): Observable<String>

        /** Emits when the progress bar should be visible (during a network call). */
        fun progressBarIsVisible(): Observable<Boolean>

        /** Emits whenever the user tries to delete a card.  */
        fun showDeleteCardDialog(): Observable<Void>

        /** Emits when the card was successfully deleted. */
        fun success(): Observable<String>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<PaymentMethodsSettingsActivity>(environment), PaymentMethodsAdapter.Delegate, Inputs, Outputs {

        private val confirmDeleteCardClicked = PublishSubject.create<Void>()
        private val deleteCardClicked = PublishSubject.create<String>()
        private val refreshCards = PublishSubject.create<Void>()

        private val cards = BehaviorSubject.create<MutableList<UserPaymentsQuery.Node>>()
        private val error = BehaviorSubject.create<String>()
        private val progressBarIsVisible = BehaviorSubject.create<Boolean>()
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
                    .subscribe {
                        this.refreshCards.onNext(null)
                        this.success.onNext(it)
                    }

            deleteCardNotification
                    .compose(Transformers.errors())
                    .subscribe { this.error.onNext(it.localizedMessage) }

            this.refreshCards
                    .switchMap { getListOfStoredCards() }
                    .subscribe { this.cards.onNext(it) }
        }

        override fun deleteCardButtonClicked(paymentMethodsViewHolder: PaymentMethodsViewHolder, paymentSourceId: String) {
            return this.deleteCardClicked(paymentSourceId)
        }

        override fun confirmDeleteCardClicked() = this.confirmDeleteCardClicked.onNext(null)

        override fun deleteCardClicked(paymentSourceId: String) = this.deleteCardClicked.onNext(paymentSourceId)

        override fun refreshCards() = this.refreshCards.onNext(null)

        override fun cards(): Observable<MutableList<UserPaymentsQuery.Node>> = this.cards

        override fun error(): Observable<String> = this.error

        override fun progressBarIsVisible(): Observable<Boolean> = this.progressBarIsVisible

        override fun showDeleteCardDialog(): Observable<Void> = this.showDeleteCardDialog

        override fun success(): Observable<String> = this.success

        private fun getListOfStoredCards(): Observable<MutableList<UserPaymentsQuery.Node>> {
            return this.client.getStoredCards()
                    .doOnSubscribe { this.progressBarIsVisible.onNext(true) }
                    .doAfterTerminate { this.progressBarIsVisible.onNext(false) }
                    .compose(bindToLifecycle())
                    .compose(neverError())
                    .map { cards -> cards.me()?.storedCards()?.nodes() }
        }

        private fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data> {
            return this.client.deletePaymentSource(paymentSourceId)
                    .doOnSubscribe { this.progressBarIsVisible.onNext(true) }
                    .doAfterTerminate { this.progressBarIsVisible.onNext(false) }
        }

    }
}
