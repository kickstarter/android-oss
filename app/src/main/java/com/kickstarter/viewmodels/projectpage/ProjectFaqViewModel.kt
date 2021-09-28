package com.kickstarter.viewmodels.projectpage

import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.ProjectFaq
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.fragments.projectpage.ProjectFaqFragment
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class ProjectFaqViewModel {
    interface Inputs {
        /** Configure with current [ProjectData]. */
        fun configureWith(projectData: ProjectData)
    }

    interface Outputs {
        fun projectFaqs(): Observable<List<ProjectFaq>>
    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<ProjectFaqFragment>(environment), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        // - Inputs
        private val projectDataInput = BehaviorSubject.create<ProjectData>()

        // - Outputs
        private val listFaqs = PublishSubject.create<List<ProjectFaq>>()

        init {
            // TODO: FAQ's ready for complete https://kickstarter.atlassian.net/browse/NTV-209
            projectDataInput
                .map { it?.project()?.projectFaqs() }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe {
                    this.listFaqs.onNext(it.toList())
                }
        }

        // - Inputs
        override fun configureWith(projectData: ProjectData) = this.projectDataInput.onNext(projectData)

        override fun projectFaqs(): Observable<List<ProjectFaq>> = this.listFaqs
    }
}
