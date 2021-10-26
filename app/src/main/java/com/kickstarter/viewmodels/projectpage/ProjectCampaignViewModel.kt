package com.kickstarter.viewmodels.projectpage

import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.HTMLParser
import com.kickstarter.libs.ViewElement
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.fragments.projectpage.ProjectOverviewFragment
import rx.Observable
import rx.subjects.BehaviorSubject

class ProjectCampaignViewModel {
    interface Inputs {
        /** Configure with current [ProjectData]. */
        fun configureWith(projectData: ProjectData)
    }

    interface Outputs {
        /** Emits in a list format the DOM elements  */
        fun storyViewElements(): Observable<List<ViewElement>>
    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<ProjectOverviewFragment>(environment), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        private val htmlParser = HTMLParser()
        private val projectDataInput = BehaviorSubject.create<ProjectData>()
        private val storyViewElementsList: Observable<List<ViewElement>>

        init {
            val project = projectDataInput
                .map { it.project() }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }

            storyViewElementsList = project
                .filter { ObjectUtils.isNotNull(it.story()) }
                .map { requireNotNull(it.story()) }
                .map { htmlParser.parse(it) }
                .doOnError {
                    // TODO: emmit parser error state
                }
        }

        // - Inputs
        override fun configureWith(projectData: ProjectData) =
            this.projectDataInput.onNext(projectData)

        override fun storyViewElements(): Observable<List<ViewElement>> = storyViewElementsList
    }
}
