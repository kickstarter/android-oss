package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentConfigType
import com.kickstarter.libs.Environment
import com.kickstarter.ui.activities.ProjectActivity
import rx.Observable
import rx.subjects.BehaviorSubject

interface FeatureFlagsViewModel {
    interface Inputs
    interface Outputs {
        fun features(): Observable<List<Map.Entry<String, Boolean>>>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<ProjectActivity>(environment), Inputs, Outputs {

        private val currentConfig: CurrentConfigType = environment.currentConfig()

        private val features = BehaviorSubject.create<List<Map.Entry<String, Boolean>>>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            this.currentConfig
                    .observable()
                    .map { it.features() }
                    .map { it?.entries?.toList()?: listOf<Map.Entry<String, Boolean>>() }
                    .map { it.filter { entry -> entry.key.startsWith("android_") } }
                    .map { it.sortedBy { entry -> entry.key } }
                    .compose(bindToLifecycle())
                    .subscribe(this.features)
        }

        override fun features(): Observable<List<Map.Entry<String, Boolean>>> = this.features
    }
}