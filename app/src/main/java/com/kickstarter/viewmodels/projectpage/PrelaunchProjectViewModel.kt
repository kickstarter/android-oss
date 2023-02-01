package com.kickstarter.viewmodels.projectpage

import android.content.Intent
import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.rx.transformers.TakeWhenTransformerV2
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.updateProjectWith
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.intentmappers.ProjectIntentMapper
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface PrelaunchProjectViewModel {

    interface Inputs {
        /** Configure with current [projectSlug]. */
        fun configureWith(intent: Intent)

        /** Call when the creator button is clicked  */
        fun creatorInfoButtonClicked()

        /** Call when the heart button is clicked.  */
        fun bookmarkButtonClicked()

        /** Call when the share button is clicked.  */
        fun shareButtonClicked()
    }

    interface Outputs {
        fun project(): Observable<Project>

        /** Emits when we should show the share sheet with the name of the project and share URL.  */
        fun showShareSheet(): Observable<Pair<String, String>>

        /** Emits when we should start [com.kickstarter.ui.activities.LoginToutActivity].  */
        fun startLoginToutActivity(): Observable<Unit>

        /** Emits when the success prompt for saving should be displayed.  */
        fun showSavedPrompt(): Observable<Unit>
    }

    class PrelaunchProjectViewModel(environment: Environment) : ViewModel(), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this
        private val disposables = CompositeDisposable()

        private val currentUser = requireNotNull(environment.currentUserV2())
        private val apolloClient = requireNotNull(environment.apolloClientV2())
        private val currentConfig = requireNotNull(environment.currentConfigV2())

        private val intent = BehaviorSubject.create<Intent>()
        private val creatorInfoClicked = PublishSubject.create<Unit>()
        private val shareButtonClicked = PublishSubject.create<Unit>()
        private val bookmarkButtonClicked = PublishSubject.create<Unit>()
        private val projectData = BehaviorSubject.create<ProjectData>()

        private val project = PublishSubject.create<Project>()
        private val showShareSheet = PublishSubject.create<Pair<String, String>>()
        private val startLoginToutActivity = PublishSubject.create<Unit>()
        private val showSavedPrompt = PublishSubject.create<Unit>()
        init {

            val initialProject = intent
                .switchMap { slug ->
                    loadProject(slug)
                }
                .share()

            val loggedInUserOnHeartClick = this.currentUser.observable()
                .compose(Transformers.takeWhenV2(this.bookmarkButtonClicked))
                .filter {
                    it.isPresent()
                }

            val loggedOutUserOnHeartClick = this.currentUser.observable()
                .compose(Transformers.takeWhenV2(this.bookmarkButtonClicked))
                .filter {
                    !it.isPresent()
                }.map { !it.isPresent() }

            val savedProjectOnLoginSuccess = this.startLoginToutActivity
                .compose(Transformers.takePairWhenV2(this.currentUser.observable()))
                .doOnError {
                    println(it.message)
                }
                .filter { su ->
                    su.second != null
                }
                .withLatestFrom<Project, Project>(initialProject) { _, p -> p }
                .take(1)
                .switchMap {
                    this.saveProject(it)
                }

            // An observable of the ref tag stored in the cookie for the project. Can emit `null`.

            val projectOnUserChangeSave = initialProject
                .compose(Transformers.takePairWhenV2(loggedInUserOnHeartClick))
                .withLatestFrom(projectData) { initProject, latestProjectData ->
                    mapProject(latestProjectData, initProject)
                }
                .switchMap {
                    this.toggleProjectSave(it)
                }
                .share()
                .filter { ObjectUtils.isNotNull(it) }
                .map { it }

            var currentProject = Observable.merge(initialProject, savedProjectOnLoginSuccess, projectOnUserChangeSave)

            val currentProjectData = currentProject.map {
                projectData(null, null, it)
            }

            initialProject
                .subscribe {
                    project.onNext(it)
                }.addToDisposable(disposables)

            initialProject
                .compose(TakeWhenTransformerV2(this.shareButtonClicked))
                .map {
                    Pair(
                        it.name(),
                        UrlUtils.appendRefTag(
                            it.webProjectUrl(),
                            RefTag.projectShare().tag()
                        )
                    )
                }
                .subscribe {
                    this.showShareSheet.onNext(it)
                }.addToDisposable(disposables)

            currentProjectData
                .distinctUntilChanged()
                .subscribe {
                    this.projectData.onNext(it)
                }.addToDisposable(disposables)

            loggedOutUserOnHeartClick
                .subscribe {
                    this.startLoginToutActivity.onNext(Unit)
                }.addToDisposable(disposables)

            savedProjectOnLoginSuccess
                .doOnError {
                    println(it.message)
                }
                .filter { ObjectUtils.isNotNull(it) }
                .map { it }
                .subscribe {
                    this.project.onNext(it)
                }.addToDisposable(disposables)

            projectOnUserChangeSave
                .filter { ObjectUtils.isNotNull(it) }
                .map { it }
                .subscribe {
                    this.project.onNext(it)
                }.addToDisposable(disposables)

            projectOnUserChangeSave
                .filter { p -> p.isStarred() }
                .subscribe {
                    this.showSavedPrompt.onNext(Unit)
                }.addToDisposable(disposables)
        }

        private fun mapProject(
            latestProjectData: ProjectData,
            initProject: Pair<Project, KsOptional<User>>
        ): Project {
            return if (latestProjectData.project().isStarred() != initProject.first.isStarred()) {
                latestProjectData.project()
            } else {
                initProject.first
            }
        }

        private fun saveProject(project: Project): Observable<Project> {
            return this.apolloClient.watchProject(project)
                .compose(Transformers.neverErrorV2())
        }

        private fun unSaveProject(project: Project): Observable<Project> {
            return this.apolloClient.unWatchProject(project).compose(Transformers.neverErrorV2())
        }

        private fun toggleProjectSave(project: Project): Observable<Project> {
            return if (project.isStarred()) {
                unSaveProject(project)
            } else {
                saveProject(project)
            }
        }
        private fun projectData(refTagFromIntent: RefTag?, refTagFromCookie: RefTag?, project: Project): ProjectData {
            return ProjectData
                .builder()
                .refTagFromIntent(refTagFromIntent)
                .refTagFromCookie(refTagFromCookie)
                .project(project)
                .build()
        }
        private fun loadProject(intent: Intent) = ProjectIntentMapper
            .project(intent, this.apolloClient)
            .withLatestFrom(
                currentConfig.observable(),
                currentUser.observable()
            ) { project, config, user ->
                if (user.isPresent()) {
                    return@withLatestFrom project.updateProjectWith(config, user.getValue())
                } else {
                    return@withLatestFrom project.updateProjectWith(config, null)
                }
            }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }

        // - Inputs
        override fun configureWith(intent: Intent) = this.intent.onNext(intent)
        override fun creatorInfoButtonClicked() = this.creatorInfoClicked.onNext(Unit)
        override fun bookmarkButtonClicked() = this.bookmarkButtonClicked.onNext(Unit)
        override fun shareButtonClicked() = shareButtonClicked.onNext(Unit)

        // - Outputs
        override fun project(): Observable<Project> = this.project
        override fun showShareSheet(): Observable<Pair<String, String>> = this.showShareSheet
        override fun startLoginToutActivity(): Observable<Unit> = this.startLoginToutActivity
        override fun showSavedPrompt(): Observable<Unit> = this.showSavedPrompt
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PrelaunchProjectViewModel(environment) as T
        }
    }
}
