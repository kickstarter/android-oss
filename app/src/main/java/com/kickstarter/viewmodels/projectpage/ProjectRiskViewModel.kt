package com.kickstarter.viewmodels.projectpage

import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.models.EnvironmentalCommitment
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.fragments.projectpage.ProjectRiskFragment
import rx.Observable
import rx.subjects.BehaviorSubject

interface ProjectRiskViewModel {
    interface Inputs {
        /** Configure with current [ProjectData]. */
        fun configureWith(projectData: ProjectData)
        fun onLearnAboutAccountabilityOnKickstarterClicked()
    }

    interface Outputs {

        /** Emits the current list [EnvironmentalCommitment]. */
        fun projectRisks(): Observable<String>

        fun openLearnAboutAccountabilityOnKickstarter(): Observable<String>
    }

    class ViewModel(@NonNull val environment: Environment) :
        FragmentViewModel<ProjectRiskFragment>(environment), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this
        private val projectDataInput = BehaviorSubject.create<ProjectData>()
        private val onLearnAboutAccountabilityOnKickstarterClicked = BehaviorSubject.create<Void>()

        private val projectRisks = BehaviorSubject.create<String>()
        private val openLearnAboutAccountabilityOnKickstarter = BehaviorSubject.create<String>()

        init {
            val project = projectDataInput
                .map { it.project() }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }

            project.map { project ->
                project.risks()
            }.filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe {
                    projectRisks.onNext(it)
                }

            onLearnAboutAccountabilityOnKickstarterClicked
                .compose(bindToLifecycle())
                .subscribe {
                    this.openLearnAboutAccountabilityOnKickstarter.onNext(
                        UrlUtils
                            .appendPath(
                                environment.webEndpoint(),
                                ACCOUNTABILITY
                            )
                    )
                }
        }

        override fun configureWith(projectData: ProjectData) =
            this.projectDataInput.onNext(projectData)

        override fun onLearnAboutAccountabilityOnKickstarterClicked() =
            this.onLearnAboutAccountabilityOnKickstarterClicked.onNext(null)

        @NonNull
        override fun projectRisks(): Observable<String> = this.projectRisks
        @NonNull
        override fun openLearnAboutAccountabilityOnKickstarter(): Observable<String> = this.openLearnAboutAccountabilityOnKickstarter
    }

    companion object {
        const val ACCOUNTABILITY = "help/hc/sections/115001107133"
    }
}
