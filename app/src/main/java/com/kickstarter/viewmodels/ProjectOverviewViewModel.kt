package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.fragments.ProjectOverviewFragment
import rx.subjects.BehaviorSubject

class ProjectOverviewViewModel {
    interface Inputs {
        /** Configure with current [ProjectData]. */
        fun configureWith(projectData: ProjectData)
    }

    interface Outputs

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<ProjectOverviewFragment>(environment), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        private val projectDataInput = BehaviorSubject.create<ProjectData>()

        init {
            val project = projectDataInput
                .map { it.project() }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }
        }

        override fun configureWith(projectData: ProjectData) = this.projectDataInput.onNext(projectData)
    }
}
