package com.kickstarter.viewmodels

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject

interface ProjectSocialViewModel {
    interface Outputs {
        fun project(): Observable<Project>
    }

    class ProjectSocialViewModel(private val environment: Environment, private val intent: Intent? = null) :
        ViewModel(), Outputs {

        private val project = BehaviorSubject.create<Project>()
        private val disposables = CompositeDisposable()

        private fun intent() = intent?.let { Observable.just(it) } ?: Observable.empty()

        val outputs: Outputs = this

        init {
            intent()
                .map<Any> { it.getParcelableExtra(IntentKey.PROJECT) }
                .ofType(Project::class.java)
                .subscribe {
                    project.onNext(it)
                }
                .addToDisposable(disposables)
        }

        override fun project(): Observable<Project> = project

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }
    }

    class Factory(private val environment: Environment, private val intent: Intent? = null) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProjectSocialViewModel(environment, intent) as T
        }
    }
}
