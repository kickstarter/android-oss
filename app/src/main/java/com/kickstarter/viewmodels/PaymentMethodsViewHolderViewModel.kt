package com.kickstarter.viewmodels

import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.StoredCard
import com.kickstarter.models.extensions.getCardTypeDrawable
import com.stripe.android.model.Card
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.Locale

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

    class PaymentMethodsViewHolderViewModel : Inputs, Outputs {

        private val card = PublishSubject.create<StoredCard>()
        private val deleteCardClick = PublishSubject.create<Unit>()

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
                .filter { it.isNotNull() }
                .map { sdf.format(it).toString() }
                .subscribe(this.expirationDate)

            this.card
                .map { it.id() }
                .compose<String>(takeWhenV2(this.deleteCardClick))
                .subscribe(this.id)

            this.card
                .map { it.lastFourDigits() }
                .subscribe(this.lastFour)

            this.card
                .map { it.getCardTypeDrawable() }
                .subscribe(this.issuerImage)

            this.card
                .map { it.type() }
                .filter { it.isNotNull() }
                .map { StoredCard.issuer(it) }
                .subscribe(this.issuer)
        }

        override fun card(creditCard: StoredCard) = this.card.onNext(creditCard)

        override fun deleteIconClicked() = this.deleteCardClick.onNext(Unit)

        override fun issuer(): Observable<String> = this.issuer

        override fun issuerImage(): Observable<Int> = this.issuerImage

        override fun expirationDate(): Observable<String> = this.expirationDate

        override fun id(): Observable<String> = this.id

        override fun lastFour(): Observable<String> = this.lastFour
    }
}
