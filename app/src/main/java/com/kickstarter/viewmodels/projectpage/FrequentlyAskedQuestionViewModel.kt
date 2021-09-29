package com.kickstarter.viewmodels.projectpage

import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.ProjectFaq
import com.kickstarter.ui.fragments.projectpage.FrequentlyAskedQuestionFragment
import rx.Observable
import rx.subjects.BehaviorSubject

interface FrequentlyAskedQuestionViewModel {

    interface Inputs {
        /** Configure with current [ProjectFaq]. */
        fun configureWith(projectFaqList: List<ProjectFaq>?)
    }

    interface Outputs {
        /** Emits the current list [ProjectFaq]. */
        fun projectFaqList(): Observable<List<ProjectFaq>>
        fun bindEmptyState(): Observable<Void>
    }

    class ViewModel(@NonNull val environment: Environment) :
        FragmentViewModel<FrequentlyAskedQuestionFragment>(environment), Inputs, Outputs {

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val projectFaqListInput = BehaviorSubject.create<List<ProjectFaq>?>()

        private val projectFaqList = BehaviorSubject.create<List<ProjectFaq>>()
        private val bindEmptyState = BehaviorSubject.create<Void>()

        init {
            val projectFaqList = projectFaqListInput
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }

            projectFaqList
                .filter { it.isNotEmpty() }
                .compose(bindToLifecycle())
                .subscribe { this.projectFaqList.onNext(it) }

            projectFaqList
                .filter { it.isNullOrEmpty() }
                .compose(bindToLifecycle())
                .subscribe { this.bindEmptyState.onNext(null) }
        }

        override fun configureWith(projectFaqList: List<ProjectFaq>?) = this.projectFaqListInput
            .onNext(projectFaqList)

        @NonNull
        override fun projectFaqList(): Observable<List<ProjectFaq>> = this.projectFaqList
        @NonNull
        override fun bindEmptyState(): Observable<Void> = this.bindEmptyState
    }
}
