package com.kickstarter.viewmodels

import UserPaymentsQuery
import com.kickstarter.R
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.ui.viewholders.PaymentMethodsViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import type.CreditCardPaymentType
import type.CreditCardTypes
import java.text.SimpleDateFormat
import java.util.*

interface PaymentMethodsViewHolderViewModel {

    interface Inputs {
        /** Supply the view holder with the list item. */
        fun card(creditCard: UserPaymentsQuery.Node)

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

        /** Emits the payment type(Credit_Card or Bank_Account). */
        fun paymentType(): Observable<CreditCardPaymentType>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<PaymentMethodsViewHolder>(environment), Inputs, Outputs {

        private val card = PublishSubject.create<UserPaymentsQuery.Node>()
        private val deleteCardClick = PublishSubject.create<Void>()

        private val cardIssuer = BehaviorSubject.create<Int>()
        private val expirationDate = BehaviorSubject.create<String>()
        private val id = BehaviorSubject.create<String>()
        private val lastFour = BehaviorSubject.create<String>()
        private val paymentType = BehaviorSubject.create<CreditCardPaymentType>()

        private val sdf = SimpleDateFormat("MM/yyyy", Locale.getDefault())

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            this.card.map { expiration -> expiration.expirationDate() }
                    .map { sdf.format(it).toString() }
                    .subscribe { this.expirationDate.onNext(it) }

            this.card.map { id -> id.id() }
                    .compose<String>(takeWhen(this.deleteCardClick))
                    .subscribe { this.id.onNext(it) }

            this.card.map { last -> last.lastFour() }
                    .subscribe { this.lastFour.onNext(it) }

            this.card.map { payment -> payment.paymentType() }
                    .subscribe { this.paymentType.onNext(it) }

            this.card.map { type -> type.type() }
                    .map { setCreditCardType(it) }
                    .subscribe { this.cardIssuer.onNext(it) }

        }

        override fun card(creditCard: UserPaymentsQuery.Node) {
            this.card.onNext(creditCard)
        }

        override fun deleteIconClicked() = this.deleteCardClick.onNext(null)

        override fun cardIssuer(): Observable<Int> = this.cardIssuer

        override fun expirationDate(): Observable<String> = this.expirationDate

        override fun id(): Observable<String> = this.id

        override fun lastFour(): Observable<String> = this.lastFour

        override fun paymentType(): Observable<CreditCardPaymentType> = this.paymentType

        private fun setCreditCardType(cardType: CreditCardTypes): Int {
            return when (cardType) {
                CreditCardTypes.AMEX -> R.drawable.amex_md
                CreditCardTypes.DISCOVER -> R.drawable.discover_md
                CreditCardTypes.JCB -> R.drawable.jcb_md
                CreditCardTypes.MASTERCARD -> R.drawable.mastercard_md
                CreditCardTypes.VISA -> R.drawable.visa_md
                else -> R.drawable.generic_bank_md
            }
        }
    }
}
