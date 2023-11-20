package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isErrored
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.StoredCard
import com.kickstarter.models.extensions.getCardTypeDrawable
import com.stripe.android.model.Card
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
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

        /** Emits when the expiration date label should be gone. */
        fun expirationIsGone(): Observable<Boolean>

        /** Emits the name of the card issuer from [Card.CardBrand]. */
        fun issuer(): Observable<String>

        /** Emits the drawable for the card issuer (ex Visa, Mastercard, AMEX). */
        fun issuerImage(): Observable<Int>

        /** Emits the last four digits of the credit card. */
        fun lastFour(): Observable<String>

        /** Emits a boolean that determines if the retry copy should be visible. */
        fun retryCopyIsVisible(): Observable<Boolean>
    }

    open class ViewModel : Inputs, Outputs {
        protected val cardAndProject: PublishSubject<Pair<StoredCard, Project>> = PublishSubject.create()

        private val expirationDate = BehaviorSubject.create<String>()
        private val issuer = BehaviorSubject.create<String>()
        private val issuerImage = BehaviorSubject.create<Int>()
        private val lastFour = BehaviorSubject.create<String>()
        private val retryCopyIsVisible = PublishSubject.create<Boolean>()
        private val expirationIsGone = PublishSubject.create<Boolean>()

        private val sdf = SimpleDateFormat(StoredCard.DATE_FORMAT, Locale.getDefault())

        val disposables = CompositeDisposable()

        init {

            val card = cardAndProject
                .filter { it.first.isNotNull() }
                .map { it.first }

            card
                .filter { it.expiration().isNotNull() }
                .map { it.expiration() }
                .map { sdf.format(it).toString() }
                .subscribe { this.expirationDate.onNext(it) }
                .addToDisposable(disposables)

            card
                .map { it.expiration().isNull() }
                .subscribe { this.expirationIsGone.onNext(it) }
                .addToDisposable(disposables)

            card
                .filter { it.lastFourDigits().isNotNull() }
                .map { requireNotNull(it.lastFourDigits()) }
                .subscribe { this.lastFour.onNext(it) }
                .addToDisposable(disposables)

            card
                .filter { it.isNotNull() }
                .map { it.getCardTypeDrawable() }
                .subscribe { this.issuerImage.onNext(it) }
                .addToDisposable(disposables)

            card
                .filter { it.type().isNotNull() }
                .map { it.type() }
                .map { StoredCard.issuer(it) }
                .subscribe { this.issuer.onNext(it) }
                .addToDisposable(disposables)

            val project = this.cardAndProject
                .filter { it.second.isNotNull() }
                .map { it.second }

            val backing = project
                .filter { it.backing().isNotNull() }
                .map { it.backing() }

            val isBackingPaymentSource = backing
                .compose<Pair<Backing?, StoredCard>>(combineLatestPair(card))
                .map { backingAndCard -> backingAndCard.first?.let { b -> b.paymentSource()?.let { it.id() == backingAndCard.second.id() } } ?: false }

            isBackingPaymentSource
                .compose<Pair<Boolean, Backing?>>(combineLatestPair(backing))
                .map {
                    it.first && it.second?.isErrored() ?: false
                }
                .subscribe { this.retryCopyIsVisible.onNext(it) }
                .addToDisposable(disposables)
        }
        override fun configureWith(cardAndProject: Pair<StoredCard, Project>) = this.cardAndProject.onNext(cardAndProject)

        override fun issuer(): Observable<String> = this.issuer

        override fun issuerImage(): Observable<Int> = this.issuerImage

        override fun expirationDate(): Observable<String> = this.expirationDate

        override fun lastFour(): Observable<String> = this.lastFour

        override fun retryCopyIsVisible(): Observable<Boolean> = this.retryCopyIsVisible

        override fun expirationIsGone(): Observable<Boolean> = this.expirationIsGone

        fun onCleared() {
            disposables.clear()
        }
    }
}
