package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.model.FeatureFlagsModel
import com.kickstarter.ui.viewholders.FeatureFlagViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface FeatureFlagViewHolderViewModel {
    interface Inputs {
        /** Call with the current feature flag. */
        fun featureFlag(flag: FeatureFlagsModel)

        /** Call with the current feature flag Checked Change. */
        fun featureFlagCheckedChange(isChecked: Boolean)
    }

    interface Outputs {
        /** Emits the name of the flag. */
        fun key(): Observable<String>

        /** Emits the value of the flag. */
        fun value(): Observable<Boolean>

        /** Emits the if the flag value can be changed  */
        fun isClickable(): Observable<Boolean>
        
        fun featureAlpha(): Observable<Float>

        /** Emits when we should notify the delegate the feature state changed . */
        fun notifyDelegateFeatureStateChanged(): Observable<Pair<String, Boolean>>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<FeatureFlagViewHolder>(environment), Inputs, Outputs {

        private val featureFlag = PublishSubject.create<FeatureFlagsModel>()

        private val featureFlagCheckedChange = PublishSubject.create<Boolean>()

        private val key = BehaviorSubject.create<String>()
        private val value = BehaviorSubject.create<Boolean>()
        private val isClickable = BehaviorSubject.create<Boolean>()
        private val featureAlpha = BehaviorSubject.create<Float>()
        private val notifyDelegateFeatureStateChanged = BehaviorSubject.create<Pair<String, Boolean>>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            this.featureFlag
                .map { it.featureFlagsName }
                .compose(bindToLifecycle())
                .subscribe(this.key)

            this.featureFlag
                .map { it.isFeatureFlagEnabled }
                .compose(bindToLifecycle())
                .subscribe(this.value)

            this.featureFlag
                .map { it.isFeatureFlagChangeable }
                .compose(bindToLifecycle())
                .subscribe(this.isClickable)

            this.featureFlag
                .map { if (it.isFeatureFlagChangeable) 1f else 0.5f }
                .compose(bindToLifecycle())
                .subscribe(this.featureAlpha)

            this.featureFlag
                .map { it.featureFlagsName }
                .compose<Pair<String, Boolean>>(Transformers.takePairWhen(this.featureFlagCheckedChange))
                .compose(bindToLifecycle())
                .subscribe { this.notifyDelegateFeatureStateChanged.onNext(it) }
        }

        override fun featureFlag(flag: FeatureFlagsModel) = this.featureFlag.onNext(flag)

        override fun key(): Observable<String> = this.key

        override fun value(): Observable<Boolean> = this.value

        override fun isClickable(): Observable<Boolean> = this.isClickable

        override fun featureAlpha(): Observable<Float> = this.featureAlpha

        override fun notifyDelegateFeatureStateChanged(): Observable<Pair<String, Boolean>> = this.notifyDelegateFeatureStateChanged

        override fun featureFlagCheckedChange(isChecked: Boolean) = this.featureFlagCheckedChange.onNext(isChecked)
    }
}