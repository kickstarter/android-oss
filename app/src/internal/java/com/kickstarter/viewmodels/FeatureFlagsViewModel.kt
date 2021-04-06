package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentConfigType
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.ExperimentsClientType
import com.kickstarter.ui.activities.FeatureFlagsActivity
import rx.Observable
import rx.subjects.BehaviorSubject

interface FeatureFlagsViewModel {
    interface Inputs
    interface Outputs {
        /** Emits "android_" prefixed feature flags from the [Config]. */
        fun configFeatures(): Observable<List<Pair<String, Boolean>>>

        /** Emits [OptimizelyExperimentsClient] feature flags. */
        fun optimizelyFeatures(): Observable<List<Pair<String, Boolean>>>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<FeatureFlagsActivity>(environment), Inputs, Outputs {

        private val currentConfig: CurrentConfigType = environment.currentConfig()
        private val currentUser: CurrentUserType = environment.currentUser()
        private val optimizely: ExperimentsClientType = environment.optimizely()

        private val configFeatures = BehaviorSubject.create<List<Pair<String, Boolean>>>()
        private val optimizelyFeatures = BehaviorSubject.create<List<Pair<String, Boolean>>>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            this.currentConfig
                .observable()
                .map { it.features() }
                .map { it?.entries?.toList() ?: listOf<Map.Entry<String, Boolean>>() }
                .map { it.filter { entry -> entry.key.startsWith("android_") } }
                .map { it.sortedBy { entry -> entry.key } }
                .map { it.map { entry -> Pair(entry.key, entry.value) }.toList() }
                .compose(bindToLifecycle())
                .subscribe(this.configFeatures)

            this.currentUser
                .observable()
                .map { this.optimizely.enabledFeatures(it) }
                .map { it.map { entry -> Pair(entry, true) }.toList() }
                .compose(bindToLifecycle())
                .subscribe(this.optimizelyFeatures)
        }

        override fun configFeatures(): Observable<List<Pair<String, Boolean>>> = this.configFeatures

        override fun optimizelyFeatures(): Observable<List<Pair<String, Boolean>>> = this.optimizelyFeatures
    }
}
