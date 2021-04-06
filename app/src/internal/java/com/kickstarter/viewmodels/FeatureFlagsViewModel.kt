package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Config
import com.kickstarter.libs.CurrentConfigType
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.ExperimentsClientType
import com.kickstarter.libs.utils.ConfigFeatureName.SEGMENT_ENABLED
import com.kickstarter.model.FeatureFlagsModel
import com.kickstarter.ui.activities.FeatureFlagsActivity
import rx.Observable
import rx.subjects.BehaviorSubject

interface FeatureFlagsViewModel {
    interface Inputs {
        /** call function to edit segment feature flag in internal tool */
        fun updateSegmentFlag(flag: Boolean)
    }

    interface Outputs {
        /** Emits "android_" prefixed feature flags from the [Config]. */
        fun configFeatures(): Observable<List<FeatureFlagsModel>>

        /** Emits [OptimizelyExperimentsClient] feature flags. */
        fun optimizelyFeatures(): Observable<List<FeatureFlagsModel>>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<FeatureFlagsActivity>(environment), Inputs, Outputs {

        private val currentConfig: CurrentConfigType = environment.currentConfig()
        private val currentUser: CurrentUserType = environment.currentUser()
        private val optimizely: ExperimentsClientType = environment.optimizely()

        private val configFeatures = BehaviorSubject.create<List<FeatureFlagsModel>>()
        private val optimizelyFeatures = BehaviorSubject.create<List<FeatureFlagsModel>>()
        private val updateSegmentFlag = BehaviorSubject.create<Boolean>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private var config: Config? = null
        init {

            val currentConfig = this.currentConfig
                .observable()

            currentConfig.subscribe { c ->
                this.config = c
            }

            currentConfig
                .map { it.features() }
                .map { it?.entries?.toList() ?: listOf<Map.Entry<String, Boolean>>() }
                .map { it.filter { entry -> entry.key.startsWith("android_") } }
                .map { it.sortedBy { entry -> entry.key } }
                .map { it.map { entry -> FeatureFlagsModel(entry.key, entry.value, entry.key.equals(SEGMENT_ENABLED.configFeatureName)) }.toList() }
                .compose(bindToLifecycle())
                .subscribe(this.configFeatures)

            this.currentUser
                .observable()
                .map { this.optimizely.enabledFeatures(it) }
                .map { it.map { entry -> FeatureFlagsModel(entry, isFeatureFlagEnabled = true, isFeatureFlagChangeable = false) }.toList() }
                .compose(bindToLifecycle())
                .subscribe(this.optimizelyFeatures)

            this.updateSegmentFlag
                .compose(bindToLifecycle())
                .subscribe {
                    config?.features()?.put(SEGMENT_ENABLED.configFeatureName, it)
                    environment.currentConfig().config(config)
                }
        }

        override fun configFeatures(): Observable<List<FeatureFlagsModel>> = this.configFeatures

        override fun optimizelyFeatures(): Observable<List<FeatureFlagsModel>> = this.optimizelyFeatures

        override fun updateSegmentFlag(flag: Boolean) = this.updateSegmentFlag.onNext(flag)
    }
}
