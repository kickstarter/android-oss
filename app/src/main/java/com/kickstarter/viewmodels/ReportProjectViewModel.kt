package com.kickstarter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Project
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import rx.Observable

interface ReportProjectViewModel {

    interface Inputs {
        /** Configure with current [Project]. */
        fun configureWith(project: Project)
    }

    interface Outputs {
        fun emailAndProject(): Observable<Pair<String, String>>
    }

    class ReportProjectViewModel(environment: Environment) : ViewModel(), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        val apolloClient = requireNotNull(environment.apolloClient())
        val currentUser = requireNotNull(environment.currentUser()?.observable())
        private val projectInput = BehaviorSubject.create<Project>()
        private val emailAndProjectUrl = PublishSubject.create<Pair<String, String>>()

        private val disposables = CompositeDisposable()

        init {
            val projectUrl = projectInput
                .filter { ObjectUtils.isNotNull(it) }
                .map { it }
                .map { it.urls().api()?.project() }

            val email = currentUser
                .filter { ObjectUtils.isNotNull(it) }
                //.map { it.email }

        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }

        // - Inputs
        override fun configureWith(project: Project) =
            this.projectInput.onNext(project)

        // - Outputs
        override fun emailAndProject() = Observable.empty<Pair<String, String>>()

    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReportProjectViewModel(environment) as T
        }
    }
}
