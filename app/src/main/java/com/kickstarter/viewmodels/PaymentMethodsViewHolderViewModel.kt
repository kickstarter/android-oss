package com.kickstarter.viewmodels

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.models.StoredCard
import com.kickstarter.ui.viewholders.PaymentMethodsViewHolder
import com.stripe.android.model.Card
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
        /** Emits the expiration date for a credit card. */
        fun expirationDate(): Observable<String>

        /** Emits the credit card id. */
        fun id(): Observable<String>

        /** Emits the name of the card issuer from [Card.CardBrand]. */
        fun issuer(): Observable<String>

        /** Emits the drawable for the card issuer (ex Visa, Mastercard, AMEX). */
        fun issuerImage(): Observable<Int>

        /** Emits the last four digits of the credit card. */
        fun lastFour(): Observable<String>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<PaymentMethodsViewHolder>(environment), Inputs, Outputs {

        private val card = PublishSubject.create<StoredCard>()
        private val deleteCardClick = PublishSubject.create<Void>()

        private val expirationDate = BehaviorSubject.create<String>()
        private val id = BehaviorSubject.create<String>()
        private val issuer = BehaviorSubject.create<String>()
        private val issuerImage = BehaviorSubject.create<Int>()
        private val lastFour = BehaviorSubject.create<String>()

        private val sdf = SimpleDateFormat(StoredCard.DATE_FORMAT, Locale.getDefault())

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            this.card
                    .map { it.expiration() }
                    .map { sdf.format(it).toString() }
                    .subscribe(this.expirationDate)

            this.card
                    .map { it.id() }
                    .compose<String>(takeWhen(this.deleteCardClick))
                    .subscribe(this.id)

            this.card
                    .map { it.lastFourDigits() }
                    .subscribe(this.lastFour)

            this.card
                    .map { it.type() }
                    .map { StoredCard.getCardTypeDrawable(it) }
                    .subscribe(this.issuerImage)

            this.card
                    .map { it.type() }
                    .map { StoredCard.issuer(it) }
                    .subscribe(this.issuer)
        }

        override fun card(creditCard: StoredCard) = this.card.onNext(creditCard)

        override fun deleteIconClicked() = this.deleteCardClick.onNext(null)

        override fun issuer(): Observable<String> = this.issuer

        override fun issuerImage(): Observable<Int> = this.issuerImage

        override fun expirationDate(): Observable<String> = this.expirationDate

        override fun id(): Observable<String> = this.id

        override fun lastFour(): Observable<String> = this.lastFour
    }
}
