package com.kickstarter.viewmodels.projectpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.EnvironmentalCommitment
import com.kickstarter.ui.data.ProjectData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject

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

    class ProjectRiskViewModel(environment: Environment) : ViewModel(), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        private val projectDataInput = BehaviorSubject.create<ProjectData>()
        private val onLearnAboutAccountabilityOnKickstarterClicked = BehaviorSubject.create<Unit>()

        private val projectRisks = BehaviorSubject.create<String>()
        private val openLearnAboutAccountabilityOnKickstarter = BehaviorSubject.create<String>()

        private val disposables = CompositeDisposable()

        init {
            val project = projectDataInput
                .filter { it.project().isNotNull() }
                .map { requireNotNull(it.project()) }

            disposables.add(
                project
                    .filter { it.risks().isNotNull() }
                    .map { requireNotNull(it.risks()) }
                    .subscribe {
                        projectRisks.onNext(it)
                    }
            )

            disposables.add(
                onLearnAboutAccountabilityOnKickstarterClicked
                    .subscribe {
                        this.openLearnAboutAccountabilityOnKickstarter.onNext(
                            UrlUtils
                                .appendPath(
                                    environment.webEndpoint(),
                                    ACCOUNTABILITY
                                )
                        )
                    }
            )
        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }

        // - Inputs
        override fun configureWith(projectData: ProjectData) =
            this.projectDataInput.onNext(projectData)

        override fun onLearnAboutAccountabilityOnKickstarterClicked() =
            this.onLearnAboutAccountabilityOnKickstarterClicked.onNext(Unit)

        // - Outputs
        override fun projectRisks(): Observable<String> = this.projectRisks
        override fun openLearnAboutAccountabilityOnKickstarter(): Observable<String> = this.openLearnAboutAccountabilityOnKickstarter
    }

    companion object {
        const val ACCOUNTABILITY = "help/hc/sections/115001107133"
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProjectRiskViewModel(environment) as T
        }
    }
}
