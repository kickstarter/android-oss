package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.utils.extensions.isErrored
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.StoredCard
import com.kickstarter.ui.viewholders.KSViewHolder
import com.stripe.android.model.Card
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.Locale

interface BaseRewardCardViewHolderViewModel {
    interface Inputs {
        /** Call to configure view model with a stored card and project. */
        fun configureWith(cardAndProject: Pair<StoredCard, Project>)
    }

    interface Outputs {
        /** Emits the expiration date for a credit card. */
        fun expirationDate(): Observable<String>

        /** Emits the name of the card issuer from [Card.CardBrand]. */
        fun issuer(): Observable<String>

        /** Emits the drawable for the card issuer (ex Visa, Mastercard, AMEX). */
        fun issuerImage(): Observable<Int>

        /** Emits the last four digits of the credit card. */
        fun lastFour(): Observable<String>

        /** Emits a boolean that determines if the retry copy should be visible. */
        fun retryCopyIsVisible(): Observable<Boolean>
    }

    abstract class ViewModel(val environment: Environment) : ActivityViewModel<KSViewHolder>(environment), Inputs, Outputs {
        protected val cardAndProject: PublishSubject<Pair<StoredCard, Project>> = PublishSubject.create()

        private val expirationDate = BehaviorSubject.create<String>()
        private val issuer = BehaviorSubject.create<String>()
        private val issuerImage = BehaviorSubject.create<Int>()
        private val lastFour = BehaviorSubject.create<String>()
        private val retryCopyIsVisible = PublishSubject.create<Boolean>()

        private val sdf = SimpleDateFormat(StoredCard.DATE_FORMAT, Locale.getDefault())

        init {

            val card = cardAndProject
                .map { it.first }

            card
                .map { it.expiration() }
                .map { sdf.format(it).toString() }
                .subscribe(this.expirationDate)

            card
                .map { it.lastFourDigits() }
                .subscribe(this.lastFour)

            card
                .map { it.type() }
                .map { StoredCard.getCardTypeDrawable(it) }
                .subscribe(this.issuerImage)

            card
                .map { it.type() }
                .map { StoredCard.issuer(it) }
                .subscribe(this.issuer)

            val project = this.cardAndProject
                .map { it.second }

            val backing = project
                .map { it.backing() }

            val isBackingPaymentSource = backing
                .compose<Pair<Backing?, StoredCard>>(combineLatestPair(card))
                .map { backingAndCard -> backingAndCard.first?.let { b -> b.paymentSource()?.let { it.id() == backingAndCard.second.id() } } ?: false }

            isBackingPaymentSource
                .compose<Pair<Boolean, Backing?>>(combineLatestPair(backing))
                .map { it.first && it.second?.isErrored() ?: false }
                .subscribe(this.retryCopyIsVisible)
        }
        override fun configureWith(cardAndProject: Pair<StoredCard, Project>) = this.cardAndProject.onNext(cardAndProject)

        override fun issuer(): Observable<String> = this.issuer

        override fun issuerImage(): Observable<Int> = this.issuerImage

        override fun expirationDate(): Observable<String> = this.expirationDate

        override fun lastFour(): Observable<String> = this.lastFour

        override fun retryCopyIsVisible(): Observable<Boolean> = this.retryCopyIsVisible
    }
}
