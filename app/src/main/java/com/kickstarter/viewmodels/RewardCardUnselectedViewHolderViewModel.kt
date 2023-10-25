package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.R
import com.kickstarter.libs.rx.transformers.Transformers.takePairWhenV2
import com.kickstarter.libs.utils.extensions.acceptedCardType
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.models.StoredCard
import com.kickstarter.models.extensions.isFromPaymentSheet
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface RewardCardUnselectedViewHolderViewModel : BaseRewardCardViewHolderViewModel {
    interface Inputs : BaseRewardCardViewHolderViewModel.Inputs {
        /** Call when the user selects this card. */
        fun cardSelected(position: Int)
    }

    interface Outputs : BaseRewardCardViewHolderViewModel.Outputs {
        /** Emits a boolean that determines if the card is clickable. */
        fun isClickable(): Observable<Boolean>

        /** Emits the alpha value for the card issuer image. */
        fun issuerImageAlpha(): Observable<Float>

        /** Emits the text color resource ID for the last four copy. */
        fun lastFourTextColor(): Observable<Int>

        /** Emits a boolean that determines if the not available copy should be visible. */
        fun notAvailableCopyIsVisible(): Observable<Boolean>

        /** Emits when we should notify the delegate the card was selected. */
        fun notifyDelegateCardSelected(): Observable<Pair<StoredCard, Int>>

        /** Emits a boolean that determines if the select image should be visible. */
        fun selectImageIsVisible(): Observable<Boolean>
    }

    class ViewModel : BaseRewardCardViewHolderViewModel.ViewModel(), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        private val cardSelected = PublishSubject.create<Int>()

        private val isClickable = BehaviorSubject.create<Boolean>()
        private val issuerImageAlpha = BehaviorSubject.create<Float>()
        private val lastFourTextColor = BehaviorSubject.create<Int>()
        private val notAvailableCopyIsVisible = BehaviorSubject.create<Boolean>()
        private val notifyDelegateCardSelected = BehaviorSubject.create<Pair<StoredCard, Int>>()
        private val selectImageIsVisible = BehaviorSubject.create<Boolean>()

        init {

            val card = this.cardAndProject
                .map { it.second.acceptedCardType(it.first.type()) || it.first.isFromPaymentSheet() }

            card
                .subscribe(this.isClickable)

            card
                .map { if (it) 1.0f else .5f }
                .subscribe(this.issuerImageAlpha)

            card
                .map { if (it) R.color.text_primary else R.color.text_secondary }
                .subscribe(this.lastFourTextColor)

            card
                .map { it.negate() }
                .subscribe(this.notAvailableCopyIsVisible)

            card
                .subscribe(this.selectImageIsVisible)

            this.cardAndProject
                .map { it.first }
                .compose<Pair<StoredCard, Int>>(takePairWhenV2(this.cardSelected))
                .subscribe { this.notifyDelegateCardSelected.onNext(it) }
                .addToDisposable(disposables)
        }

        override fun cardSelected(position: Int) = this.cardSelected.onNext(position)

        override fun isClickable(): Observable<Boolean> = this.isClickable

        override fun issuerImageAlpha(): Observable<Float> = this.issuerImageAlpha

        override fun lastFourTextColor(): Observable<Int> = this.lastFourTextColor

        override fun notAvailableCopyIsVisible(): Observable<Boolean> = this.notAvailableCopyIsVisible

        override fun notifyDelegateCardSelected(): Observable<Pair<StoredCard, Int>> = this.notifyDelegateCardSelected

        override fun selectImageIsVisible(): Observable<Boolean> = this.selectImageIsVisible
    }
}
