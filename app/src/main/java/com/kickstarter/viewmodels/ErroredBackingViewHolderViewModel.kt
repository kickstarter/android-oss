package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.models.ErroredBacking
import com.kickstarter.ui.viewholders.ErroredBackingViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface ErroredBackingViewHolderViewModel {
    interface Inputs {
        /** Configure with the current [ErroredBacking]. */
        fun configureWith(erroredBacking: ErroredBacking)
    }

    interface Outputs {
        /** Emits the title of the [ErroredBacking.Project]. */
        fun projectTitle(): Observable<String>
    }

    class ViewModel(@NonNull environment: Environment) : ActivityViewModel<ErroredBackingViewHolder>(environment), Inputs, Outputs {

        private val erroredBacking = PublishSubject.create<ErroredBacking>()

        private val projectTitle = BehaviorSubject.create<String>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

        }

        override fun configureWith(erroredBacking: ErroredBacking) {
            this.erroredBacking.onNext(erroredBacking)
        }

        override fun projectTitle(): Observable<String> = this.projectTitle

    }
}
