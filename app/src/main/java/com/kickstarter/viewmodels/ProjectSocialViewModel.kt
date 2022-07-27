package com.kickstarter.viewmodels

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.ProjectSocialActivity
import rx.Observable
import rx.subjects.BehaviorSubject

interface ProjectSocialViewModel {
    interface Outputs {
        fun project(): Observable<Project>
    }

    class ViewModel(environment: Environment) :
        ActivityViewModel<ProjectSocialActivity>(environment), Outputs {

        private val project = BehaviorSubject.create<Project>()
        val outputs: Outputs = this

        init {
            intent()
                .map<Any> { it.getParcelableExtra(IntentKey.PROJECT) }
                .ofType(Project::class.java)
                .compose(bindToLifecycle())
                .subscribe(project)
        }

        override fun project(): Observable<Project> = project
    }
}
