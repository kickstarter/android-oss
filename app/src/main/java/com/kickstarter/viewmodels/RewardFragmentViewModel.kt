package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.models.Project
import com.kickstarter.ui.fragments.RewardsFragment
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class RewardFragmentViewModel {
    interface Inputs {
        /** Call with a reward and project when data is bound to the view.  */
        fun project(project: Project)
    }

    interface Outputs {
        /** Emits the current project. */
        fun project(): Observable<Project>

    }

    class ViewModel(@NonNull environment: Environment) : FragmentViewModel<RewardsFragment>(environment), Inputs, Outputs {

        private val projectInput = PublishSubject.create<Project>()

        private val currentProject: Observable<Project>
        private val project = BehaviorSubject.create<Project>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            this.currentProject = this.projectInput

            this.currentProject
                    .compose(bindToLifecycle())
                    .subscribe { this.project.onNext(it) }

        }

        override fun project(project: Project) {
            this.projectInput.onNext(project)
        }

        override fun project(): Observable<Project> = this.project
    }
}
