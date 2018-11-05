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
        /** Adding a card from the list of cards in the activity. */
        fun card(creditCard: UserPaymentsQuery.Node)

        fun deleteCardClick()
    }

    interface Outputs {
        /** Emits the expiration date for a credit card. */
        fun expirationDate(): Observable<String>

        /** Emits the credit card id. */
        fun id(): Observable<String>

        /** Emits the last four digits of the credit card. */
        fun lastFour(): Observable<String>

        /** Emits the payment type ex) Credit_Card or Bank_Account. */
        fun paymentType(): Observable<CreditCardPaymentType>

        /** Emits the card issuer ex) Visa or Mastercard. */
        fun type(): Observable<Int>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<PaymentMethodsViewHolder>(environment), Inputs, Outputs {

        private val card = PublishSubject.create<UserPaymentsQuery.Node>()
        private val deleteCardClick = PublishSubject.create<Void>()

        private val expirationDate = BehaviorSubject.create<String>()
        private val id = BehaviorSubject.create<String>()
        private val lastFour = BehaviorSubject.create<String>()
        private val paymentType = BehaviorSubject.create<CreditCardPaymentType>()
        private val type = BehaviorSubject.create<Int>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            this.card.map { expiration -> expiration.expirationDate() }
                    .map {
                        val sdf = SimpleDateFormat("MM/yyyy", Locale.getDefault())
                        sdf.format(it).toString()
                    }
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
                    .subscribe { this.type.onNext(it) }

        }

        override fun card(creditCard: UserPaymentsQuery.Node) {
            this.card.onNext(creditCard)
        }

        override fun deleteCardClick() = this.deleteCardClick.onNext(null)

        override fun expirationDate(): Observable<String> = this.expirationDate

        override fun id(): Observable<String> = this.id

        override fun lastFour(): Observable<String> = this.lastFour

        override fun paymentType(): Observable<CreditCardPaymentType> = this.paymentType

        override fun type(): Observable<Int> = this.type

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
