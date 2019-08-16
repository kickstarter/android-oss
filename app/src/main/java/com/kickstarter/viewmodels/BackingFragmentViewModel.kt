package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.models.Project
import com.kickstarter.ui.fragments.BackingFragment
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface BackingFragmentViewModel {
    interface Inputs {
        /** Configure with current project.  */
        fun project(project: Project)
    }

    interface Outputs {
        /** Emits the current project. */
        fun project(): Observable<Project>
    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<BackingFragment>(environment), Inputs, Outputs {

        private val projectInput = PublishSubject.create<Project>()

        private val project = BehaviorSubject.create<Project>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            this.projectInput
                    .compose(bindToLifecycle())
                    .subscribe(this.project)
        }

        override fun project(project: Project) {
            this.projectInput.onNext(project)
        }

        @NonNull
        override fun project(): Observable<Project> = this.project

    }
}
