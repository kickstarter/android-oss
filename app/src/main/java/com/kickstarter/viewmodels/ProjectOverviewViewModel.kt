package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.fragments.ProjectOverviewFragment

class ProjectOverviewViewModel {
    interface Inputs {
        /** Configure with current [ProjectData]. */
        fun configureWith(projectData: ProjectData)
    }

    interface Outputs

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<ProjectOverviewFragment>(environment), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
        }

        override fun configureWith(projectData: ProjectData) {
        }
    }
}
