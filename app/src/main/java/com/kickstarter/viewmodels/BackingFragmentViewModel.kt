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
        /** Call when the pledge has been successfully updated. */
        fun pledgeSuccessfullyUpdated()

        /** Configure with current project.  */
        fun project(project: Project)
    }

    interface Outputs {
        /** Emits the current project. */
        fun project(): Observable<Project>

        /** Emits when the backing has successfully been updated. */
        fun showUpdatePledgeSuccess(): Observable<Void>
    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<BackingFragment>(environment), Inputs, Outputs {

        private val pledgeSuccessfullyCancelled = PublishSubject.create<Void>()
        private val projectInput = PublishSubject.create<Project>()

        private val showUpdatePledgeSuccess = PublishSubject.create<Void>()
        private val project = BehaviorSubject.create<Project>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            this.projectInput
                    .compose(bindToLifecycle())
                    .subscribe(this.project)

            this.pledgeSuccessfullyCancelled
                    .compose(bindToLifecycle())
                    .subscribe(this.showUpdatePledgeSuccess)
        }

        override fun project(project: Project) {
            this.projectInput.onNext(project)
        }

        override fun pledgeSuccessfullyUpdated() {
            this.showUpdatePledgeSuccess.onNext(null)
        }

        @NonNull
        override fun project(): Observable<Project> = this.project

        @NonNull
        override fun showUpdatePledgeSuccess(): Observable<Void> = this.showUpdatePledgeSuccess

    }
}
