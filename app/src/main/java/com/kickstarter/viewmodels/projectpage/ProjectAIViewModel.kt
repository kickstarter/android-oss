package com.kickstarter.viewmodels.projectpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.models.AiDisclosure
import com.kickstarter.ui.data.ProjectData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject

interface ProjectAIViewModel {

    interface Inputs {
        /** Configure with current [ProjectData]. */
        fun configureWith(projectData: ProjectData)
        fun learnAboutAIPolicyOnKickstarterClicked()
    }

    interface Outputs {

        /** Emits the current [AiDisclosure]. */
        fun projectAiDisclosure(): Observable<AiDisclosure>

        fun openLearnAboutAIPolicyOnKickstarter(): Observable<String>
    }

    class ProjectAIViewModel(environment: Environment) : ViewModel(), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        private val projectDataInput = BehaviorSubject.create<ProjectData>()
        private val openLearnAboutAIPolicyOnKickstarterClicked = BehaviorSubject.create<Unit>()

        private val projectAIDisclosure = BehaviorSubject.create<AiDisclosure>()
        private val openLearnAboutAIPolicyOnKickstarter = BehaviorSubject.create<String>()

        private val disposables = CompositeDisposable()

        init {
            // TODO: MBL-901
        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }

        // - Inputs
        override fun configureWith(projectData: ProjectData) =
            this.projectDataInput.onNext(projectData)

        override fun learnAboutAIPolicyOnKickstarterClicked() =
            this.openLearnAboutAIPolicyOnKickstarterClicked.onNext(Unit)

        // - Outputs
        override fun projectAiDisclosure(): Observable<AiDisclosure> = this.projectAIDisclosure
        override fun openLearnAboutAIPolicyOnKickstarter(): Observable<String> = this.openLearnAboutAIPolicyOnKickstarter
    }

    companion object {
        // "https://help.kickstarter.com/hc/en-us/articles/16848396410267" // TODO not accesible yet to users
        const val AIPOLICY = "hc/en-us/articles/16848396410267"
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProjectAIViewModel(environment) as T
        }
    }
}
