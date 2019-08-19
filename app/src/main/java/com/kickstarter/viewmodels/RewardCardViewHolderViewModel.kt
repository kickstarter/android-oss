package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.utils.BooleanUtils
import com.kickstarter.libs.utils.ProjectUtils
import com.kickstarter.models.Project
import rx.Observable
import rx.subjects.BehaviorSubject

interface RewardCardViewHolderViewModel : BaseRewardCardViewHolderViewModel {
    interface Inputs : BaseRewardCardViewHolderViewModel.Inputs
    interface Outputs : BaseRewardCardViewHolderViewModel.Outputs {
        /** Emits the string resource ID for the pledge button. */
        fun buttonCTA() : Observable<Int>

        /** Emits a boolean that determines if the pledge button should be enabled. */
        fun buttonEnabled() : Observable<Boolean>

        /** Emits a boolean that determines if the not available copy should be visible. */
        fun notAvailableCopyIsVisible(): Observable<Boolean>

        /** Emits a string representing the project's country when the card is not accepted. */
        fun projectCountry(): Observable<String>
    }

    class ViewModel(environment: Environment) : BaseRewardCardViewHolderViewModel.ViewModel(environment), Inputs, Outputs  {
        val inputs: Inputs = this
        val outputs: Outputs = this

        private val buttonCTA = BehaviorSubject.create<Int>()
        private val buttonEnabled = BehaviorSubject.create<Boolean>()
        private val notAvailableCopyIsVisible = BehaviorSubject.create<Boolean>()
        private val projectCountry = BehaviorSubject.create<String>()

        init {
            val project = this.cardAndProject
                    .map { it.second }

            val allowedCard = this.cardAndProject
                    .map { ProjectUtils.acceptedCardType(it.first.type(), it.second) }

            allowedCard
                    .compose(bindToLifecycle())
                    .subscribe(this.buttonEnabled)

            allowedCard
                    .map { BooleanUtils.negate(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.notAvailableCopyIsVisible)

            allowedCard
                    .map { if (it) R.string.Select else R.string.Not_available }
                    .compose(bindToLifecycle())
                    .subscribe(this.buttonCTA)

            allowedCard
                    .filter { !it }
                    .compose<Pair<Boolean, Project>>(combineLatestPair(project))
                    .map { it.second }
                    .map { it.location()?.expandedCountry()?: "" }
                    .compose(bindToLifecycle())
                    .subscribe(this.projectCountry)

        }

        override fun buttonCTA() : Observable<Int> = this.buttonCTA

        override fun buttonEnabled() : Observable<Boolean> = this.buttonEnabled

        override fun notAvailableCopyIsVisible(): Observable<Boolean> = this.notAvailableCopyIsVisible

        override fun projectCountry(): Observable<String> = this.projectCountry
    }
}
