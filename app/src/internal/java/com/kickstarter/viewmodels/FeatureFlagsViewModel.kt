package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentConfigType
import com.kickstarter.libs.Environment
import com.kickstarter.ui.activities.FeatureFlagsActivity
import com.kickstarter.ui.activities.ProjectActivity
import rx.Observable
import rx.subjects.BehaviorSubject

interface FeatureFlagsViewModel {
    interface Inputs
    interface Outputs {
        fun features(): Observable<List<Pair<String, Boolean>>>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<FeatureFlagsActivity>(environment), Inputs, Outputs {

        private val currentConfig: CurrentConfigType = environment.currentConfig()

        private val features = BehaviorSubject.create<List<Pair<String, Boolean>>>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            this.currentConfig
                    .observable()
                    .map { it.features() }
                    .map { it?.entries?.toList()?: listOf<Map.Entry<String, Boolean>>() }
                    .map { it.filter { entry -> entry.key.startsWith("android_") } }
                    .map { it.sortedBy { entry -> entry.key } }
                    .map { it.map { entry -> Pair(entry.key, entry.value) }.toList() }
                    .compose(bindToLifecycle())
                    .subscribe(this.features)
        }

        override fun features(): Observable<List<Pair<String, Boolean>>> = this.features
    }
}