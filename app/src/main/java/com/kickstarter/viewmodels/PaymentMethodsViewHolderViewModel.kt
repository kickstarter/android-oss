package com.kickstarter.viewmodels

import UserPaymentsQuery
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.ui.viewholders.PaymentMethodsViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import type.CreditCardPaymentType
import type.CreditCardType
import java.util.*

interface PaymentMethodsViewHolderViewModel {

    interface Inputs {
        /** Adding a card from the list of cards in the activity. */
        fun addCards(creditCard: UserPaymentsQuery.Node)
    }

    interface Outputs {
        /** Emits the expiration date for a credit card. */
        fun expirationDate(): Observable<Date>

        /** Emits the credit card id. */
        fun id(): Observable<String>

        /** Emits the last four digits of the credit card. */
        fun lastFour(): Observable<String>

        /** Emits the payment type ex) Credit_Card or Bank_Account. */
        fun paymentType(): Observable<CreditCardPaymentType>

        /** Emits the card issuer ex) Visa or Mastercard. */
        fun type(): Observable<CreditCardType>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<PaymentMethodsViewHolder>(environment), Inputs, Outputs {

        private val card = PublishSubject.create<UserPaymentsQuery.Node>()

        private val expirationDate = BehaviorSubject.create<Date>()
        private val id = BehaviorSubject.create<String>()
        private val lastFour = BehaviorSubject.create<String>()
        private val paymentType = BehaviorSubject.create<CreditCardPaymentType>()
        private val type = BehaviorSubject.create<CreditCardType>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            this.card.map { expiration -> expiration.expirationDate() }
                    .subscribe { this.expirationDate.onNext(it) }

            this.card.map { last -> last.lastFour() }
                    .subscribe { this.lastFour.onNext(it) }

            this.card.map { payment -> payment.paymentType() }
                    .subscribe { this.paymentType.onNext(it) }

            this.card.map { type -> type.type() }
                    .subscribe { this.type.onNext(it) }

        }

        override fun addCards(creditCard: UserPaymentsQuery.Node) {
            this.card.onNext(creditCard)
        }

        override fun expirationDate(): Observable<Date> = this.expirationDate

        override fun id(): Observable<String> = this.id

        override fun lastFour(): Observable<String> = this.lastFour

        override fun paymentType(): Observable<CreditCardPaymentType> = this.paymentType

        override fun type(): Observable<CreditCardType> = this.type
    }
}
