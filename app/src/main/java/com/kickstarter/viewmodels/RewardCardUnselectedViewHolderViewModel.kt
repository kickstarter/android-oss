package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.takePairWhen
import com.kickstarter.libs.utils.BooleanUtils
import com.kickstarter.libs.utils.ProjectUtils
import com.kickstarter.models.StoredCard
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface RewardCardUnselectedViewHolderViewModel : BaseRewardCardViewHolderViewModel {
    interface Inputs : BaseRewardCardViewHolderViewModel.Inputs {
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

        /**  */
        fun notifyDelegateCardSelected(): Observable<Pair<StoredCard, Int>>

        /** Emits a string representing the project's country when the card is not accepted. */
        fun projectCountry(): Observable<String>

        /** Emits a boolean that determines if the select image should be visible. */
        fun selectImageIsVisible(): Observable<Boolean>
    }

    class ViewModel(environment: Environment) : BaseRewardCardViewHolderViewModel.ViewModel(environment), Inputs, Outputs  {
        val inputs: Inputs = this
        val outputs: Outputs = this

        private val cardSelected = PublishSubject.create<Int>()

        private val isClickable = BehaviorSubject.create<Boolean>()
        private val issuerImageAlpha = BehaviorSubject.create<Float>()
        private val lastFourTextColor = BehaviorSubject.create<Int>()
        private val notAvailableCopyIsVisible = BehaviorSubject.create<Boolean>()
        private val notifyDelegateCardSelected = BehaviorSubject.create<Pair<StoredCard, Int>>()
        private val projectCountry = BehaviorSubject.create<String>()
        private val selectImageIsVisible = BehaviorSubject.create<Boolean>()

        init {

            val project = this.cardAndProject
                    .map { it.second }

            val allowedCardType = this.cardAndProject
                    .map { ProjectUtils.acceptedCardType(it.first.type(), it.second) }

            allowedCardType
                    .compose(bindToLifecycle())
                    .subscribe(this.isClickable)

            allowedCardType
                    .map { if (it) 1.0f else .5f }
                    .compose(bindToLifecycle())
                    .subscribe(this.issuerImageAlpha)

            allowedCardType
                    .map { if (it) R.color.text_primary else R.color.text_secondary }
                    .compose(bindToLifecycle())
                    .subscribe(this.lastFourTextColor)

            project
                    .map { it.location()?.expandedCountry()?: "" }
                    .compose(bindToLifecycle())
                    .subscribe(this.projectCountry)

            allowedCardType
                    .map { BooleanUtils.negate(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.notAvailableCopyIsVisible)

            allowedCardType
                    .compose(bindToLifecycle())
                    .subscribe(this.selectImageIsVisible)

            this.cardAndProject
                    .map { it.first }
                    .compose<Pair<StoredCard, Int>>(takePairWhen(this.cardSelected))
                    .compose(bindToLifecycle())
                    .subscribe { this.notifyDelegateCardSelected.onNext(it) }
        }

        override fun cardSelected(position: Int) = this.cardSelected.onNext(position)

        override fun isClickable() : Observable<Boolean> = this.isClickable

        override fun issuerImageAlpha() : Observable<Float> = this.issuerImageAlpha

        override fun lastFourTextColor() : Observable<Int> = this.lastFourTextColor

        override fun notAvailableCopyIsVisible(): Observable<Boolean> = this.notAvailableCopyIsVisible

        override fun notifyDelegateCardSelected(): Observable<Pair<StoredCard, Int>> = this.notifyDelegateCardSelected

        override fun projectCountry(): Observable<String> = this.projectCountry

        override fun selectImageIsVisible(): Observable<Boolean> = this.selectImageIsVisible
    }
}
