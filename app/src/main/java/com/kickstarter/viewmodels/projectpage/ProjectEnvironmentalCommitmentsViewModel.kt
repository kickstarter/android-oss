package com.kickstarter.viewmodels.projectpage

import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.models.EnvironmentalCommitment
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.fragments.projectpage.ProjectEnvironmentalCommitmentsFragment
import rx.Observable
import rx.subjects.BehaviorSubject

class ProjectEnvironmentalCommitmentsViewModel {
    interface Inputs {
        /** Configure with current [ProjectData]. */
        fun configureWith(projectData: ProjectData)
        fun onVisitOurEnvironmentalResourcesCenterClicked()
    }

    interface Outputs {
        /** Emits the current list [EnvironmentalCommitment]. */
        fun projectEnvironmentalCommitment(): Observable<List<EnvironmentalCommitment>>

        fun openVisitOurEnvironmentalResourcesCenter(): Observable<String>
    }

    class ViewModel(@NonNull val environment: Environment) :
        FragmentViewModel<ProjectEnvironmentalCommitmentsFragment>(environment),
        Inputs,
        Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        private val projectDataInput = BehaviorSubject.create<ProjectData>()
        private val onVisitOurEnvironmentalResourcesCenterClicked = BehaviorSubject.create<Void>()

        private val projectEnvironmentalCommitment = BehaviorSubject.create<List<EnvironmentalCommitment>>()
        private val openVisitOurEnvironmentalResourcesCenter = BehaviorSubject.create<String>()

        init {
            val project = projectDataInput
                .map { it.project() }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }

            project.map { project ->
                project.envCommitments()?.sortedBy { it.id }
            }.filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe { this.projectEnvironmentalCommitment.onNext(it) }

            onVisitOurEnvironmentalResourcesCenterClicked
                .compose(bindToLifecycle())
                .subscribe {
                    this.openVisitOurEnvironmentalResourcesCenter.onNext(
                        UrlUtils
                            .appendPath(
                                environment.webEndpoint(),
                                ENVIROMENT
                            )
                    )
                }
        }

        override fun configureWith(projectData: ProjectData) =
            this.projectDataInput.onNext(projectData)

        override fun onVisitOurEnvironmentalResourcesCenterClicked() =
            this.onVisitOurEnvironmentalResourcesCenterClicked.onNext(null)

        @NonNull
        override fun projectEnvironmentalCommitment(): Observable<List<EnvironmentalCommitment>> = this.projectEnvironmentalCommitment
        @NonNull
        override fun openVisitOurEnvironmentalResourcesCenter(): Observable<String> = this.openVisitOurEnvironmentalResourcesCenter
    }

    companion object {
        const val ENVIROMENT = "environment"
    }
}
