package com.kickstarter.viewmodels

import com.kickstarter.R
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.ui.viewholders.FeatureFlagViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface FeatureFlagViewHolderViewModel {
    interface Inputs {
        /** Call with the current feature flag. */
        fun featureFlag(flag: Pair<String, Boolean>)
    }

    interface Outputs {
        /** Emits the name of the flag. */
        fun key(): Observable<String>

        /** Emits the value of the flag. */
        fun value(): Observable<String>

        /** Emits the text color of the flag value. */
        fun valueTextColor(): Observable<Int>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<FeatureFlagViewHolder>(environment), Inputs, Outputs {

        private val featureFlag = PublishSubject.create<Pair<String, Boolean>>()

        private val key = BehaviorSubject.create<String>()
        private val value = BehaviorSubject.create<String>()
        private val valueTextColor = BehaviorSubject.create<Int>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            this.featureFlag
                    .map { it.first }
                    .compose(bindToLifecycle())
                    .subscribe(this.key)

            val enabled = this.featureFlag
                    .map { it.second }

            enabled
                    .map { it.toString() }
                    .compose(bindToLifecycle())
                    .subscribe(this.value)

            enabled
                    .map { if (it) R.color.text_primary else R.color.text_secondary }
                    .compose(bindToLifecycle())
                    .subscribe(this.valueTextColor)
        }

        override fun featureFlag(flag: Pair<String, Boolean>) = this.featureFlag.onNext(flag)

        override fun key(): Observable<String> = this.key

        override fun value(): Observable<String> = this.value

        override fun valueTextColor(): Observable<Int> = this.valueTextColor
    }
}
