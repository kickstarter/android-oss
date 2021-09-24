package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.models.ProjectFaq
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.fragments.FrequentlyAskedQuestionFragment
import rx.Observable
import rx.subjects.BehaviorSubject

interface FrequentlyAskedQuestionViewModel {

    interface Inputs

    interface Outputs {
        /** Emits the current list [ProjectFaq]. */
        fun projectFaqList(): Observable<List<ProjectFaq>>
    }

    class ViewModel(@NonNull val environment: Environment) :
        FragmentViewModel<FrequentlyAskedQuestionFragment>(environment), Inputs, Outputs {

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val projectFaqList = BehaviorSubject.create<List<ProjectFaq>>()

        init {
            arguments()
                .map { it.getParcelableArrayList<ProjectFaq>(ArgumentsKey.PROJECT_QUESTIONS_ANSWERS) }
                .map { it?.toList() }
                .map { requireNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe { this.projectFaqList.onNext(it) }
        }

        @NonNull
        override fun projectFaqList(): Observable<List<ProjectFaq>> = this.projectFaqList
    }
}
