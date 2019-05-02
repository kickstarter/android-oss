package com.kickstarter.viewmodels

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.models.StoredCard
import com.kickstarter.ui.viewholders.PaymentMethodsViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.*

interface PaymentMethodsViewHolderViewModel {

    interface Inputs {
        /** Supply the view holder with the list item. */
        fun card(creditCard: StoredCard)

        /** Call when the user clicks the delete icon. */
        fun deleteIconClicked()
    }

    interface Outputs {
        /** Emits the card issuer (ex Visa, Mastercard, AMEX). */
        fun cardIssuer(): Observable<Int>

        /** Emits the expiration date for a credit card. */
        fun expirationDate(): Observable<String>

        /** Emits the credit card id. */
        fun id(): Observable<String>

        /** Emits the last four digits of the credit card. */
        fun lastFour(): Observable<String>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<PaymentMethodsViewHolder>(environment), Inputs, Outputs {

        private val card = PublishSubject.create<StoredCard>()
        private val deleteCardClick = PublishSubject.create<Void>()

        private val cardIssuer = BehaviorSubject.create<Int>()
        private val expirationDate = BehaviorSubject.create<String>()
        private val id = BehaviorSubject.create<String>()
        private val lastFour = BehaviorSubject.create<String>()

        private val sdf = SimpleDateFormat(StoredCard.DATE_FORMAT, Locale.getDefault())

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            this.card.map { it.expiration() }
                    .map { sdf.format(it).toString() }
                    .subscribe { this.expirationDate.onNext(it) }

            this.card.map { it.id() }
                    .compose<String>(takeWhen(this.deleteCardClick))
                    .subscribe { this.id.onNext(it) }

            this.card.map { it.lastFourDigits() }
                    .subscribe { this.lastFour.onNext(it) }

            this.card.map { it.type() }
                    .map { StoredCard.getCardTypeDrawable(it) }
                    .subscribe { this.cardIssuer.onNext(it) }

        }

        override fun card(creditCard: StoredCard) = this.card.onNext(creditCard)

        override fun deleteIconClicked() = this.deleteCardClick.onNext(null)

        override fun cardIssuer(): Observable<Int> = this.cardIssuer

        override fun expirationDate(): Observable<String> = this.expirationDate

        override fun id(): Observable<String> = this.id

        override fun lastFour(): Observable<String> = this.lastFour
    }
}
