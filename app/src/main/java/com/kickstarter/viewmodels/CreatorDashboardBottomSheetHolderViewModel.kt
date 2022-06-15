package com.kickstarter.viewmodels

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.models.Project
import com.kickstarter.ui.viewholders.CreatorDashboardBottomSheetViewHolder
import org.joda.time.DateTime
import rx.Observable
import rx.subjects.PublishSubject

interface CreatorDashboardBottomSheetHolderViewModel {
    interface Inputs {
        /** Current project.  */
        fun projectInput(project: Project)

        /** Call when project is selected.  */
        fun projectSwitcherProjectClicked()
    }

    interface Outputs {
        /** Emits the project launch date to be formatted for display.  */
        fun projectLaunchDate(): Observable<DateTime>

        /** Emits the project name for display.  */
        fun projectNameText(): Observable<String>

        /** Emits when project is selected.  */
        fun projectSwitcherProject(): Observable<Project>
    }

    class ViewModel(environment: Environment) :
        ActivityViewModel<CreatorDashboardBottomSheetViewHolder?>(environment), Inputs, Outputs {

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val currentProject = PublishSubject.create<Project>()
        private val projectSwitcherClicked = PublishSubject.create<Void?>()
        private val projectLaunchDate: Observable<DateTime>
        private val projectNameText: Observable<String>
        private val projectSwitcherProject: Observable<Project>

        override fun projectInput(project: Project) {
            currentProject.onNext(project)
        }

        override fun projectSwitcherProjectClicked() {
            projectSwitcherClicked.onNext(null)
        }

        override fun projectLaunchDate(): Observable<DateTime> {
            return projectLaunchDate
        }

        override fun projectNameText(): Observable<String> {
            return projectNameText
        }

        override fun projectSwitcherProject(): Observable<Project> {
            return projectSwitcherProject
        }

        init {
            projectNameText = currentProject
                .map { it.name() }

            projectLaunchDate = currentProject
                .map { it.launchedAt() }

            projectSwitcherProject = currentProject
                .compose(Transformers.takeWhen(projectSwitcherClicked))
        }
    }
}
