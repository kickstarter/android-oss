package com.kickstarter.viewmodels.projectpage

import android.content.Intent
import android.net.Uri
import android.util.Pair
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.rx.transformers.TakeWhenTransformerV2
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.libs.utils.RefTagUtils
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.libs.utils.extensions.updateProjectWith
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.intentmappers.ProjectIntentMapper
import com.kickstarter.viewmodels.usecases.SendThirdPartyEventUseCaseV2
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

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

        /** Call when the creator Info is clicked  */
        fun creatorInfoClicked()
    }

    interface Outputs {
        fun project(): Observable<Project>

        /** Emits when we should show the share sheet with the name of the project and share URL.  */
        fun showShareSheet(): Observable<Pair<String, String>>

        /** Emits when we should start [com.kickstarter.ui.activities.LoginToutActivity].  */
        fun startLoginToutActivity(): Observable<Unit>

        /** Emits when the success prompt for saving should be displayed.  */
        fun showSavedPrompt(): Observable<Unit>

        fun startCreatorView(): Observable<Project>
    }

    class PrelaunchProjectViewModel(val environment: Environment) : ViewModel(), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this
        private val disposables = CompositeDisposable()

        private val cookieManager = requireNotNull(environment.cookieManager())
        private val currentUser = requireNotNull(environment.currentUserV2())
        private val apolloClient = requireNotNull(environment.apolloClientV2())
        private val apolloClientLegacy = requireNotNull(environment.apolloClientV2())
        private val currentConfig = requireNotNull(environment.currentConfigV2())
        private val sharedPreferences = requireNotNull(environment.sharedPreferences())
        private val ffClient = requireNotNull(environment.featureFlagClient())
        private val attributionEvents = requireNotNull(environment.attributionEvents())

        private val intent = BehaviorSubject.create<Intent>()
        private val creatorInfoClicked = PublishSubject.create<Unit>()
        private val shareButtonClicked = PublishSubject.create<Unit>()
        private val bookmarkButtonClicked = PublishSubject.create<Unit>()
        private val projectData = BehaviorSubject.create<ProjectData>()

        private val project = PublishSubject.create<Project>()
        private val startCreatorView = PublishSubject.create<Project>()
        private val showShareSheet = PublishSubject.create<Pair<String, String>>()
        private val startLoginToutActivity = PublishSubject.create<Unit>()
        private val showSavedPrompt = PublishSubject.create<Unit>()
        private val currentProject2 = PublishSubject.create<Project>()

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        val onThirdPartyEventSent = BehaviorSubject.create<Boolean>()

        init {

            val reducedProject = this.intent.filter {
                it.getParcelableExtra<Project>(IntentKey.PROJECT).isNotNull()
            }.map { it.getParcelableExtra(IntentKey.PROJECT) as Project? }
                .filter { it.isNotNull() }
                .map { it }
                .withLatestFrom(
                    currentConfig.observable(),
                    currentUser.observable(),
                ) { project, config, user ->
                    if (user.isPresent()) {
                        return@withLatestFrom project.updateProjectWith(config, user.getValue())
                    } else {
                        return@withLatestFrom project.updateProjectWith(config, null)
                    }
                }.delay(1000, TimeUnit.MILLISECONDS, environment.schedulerV2())

            val loadedProject = intent
                .filter {
                    it.getParcelableExtra<Project>(IntentKey.PROJECT).isNull()
                }.switchMap { intent ->
                    loadProject(intent)
                }
                .share()

            val initialProject = Observable.merge(reducedProject, loadedProject)

            // An observable of the ref tag stored in the cookie for the project. Emits an optional since this value can be null.
            val cookieRefTag = initialProject
                .take(1)
                .map {
                        p ->
                    KsOptional.of(RefTagUtils.storedCookieRefTagForProject(p, this.cookieManager, this.sharedPreferences))
                }

            val refTag = intent
                .flatMap { ProjectIntentMapper.refTag(it) }

            val fullDeeplink = intent
                .flatMap { Observable.just(KsOptional.of(it.data)) }

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

            val projectOnUserChangeSave = initialProject
                .compose(Transformers.takePairWhenV2(loggedInUserOnHeartClick))
                .withLatestFrom(projectData) { initProject, latestProjectData ->
                    mapProject(latestProjectData, initProject)
                }
                .switchMap {
                    this.toggleProjectSave(it)
                }
                .share()
                .filter { it.isNotNull() }
                .map { it }

            val currentProject = Observable.mergeArray(
                initialProject,
                savedProjectOnLoginSuccess,
                projectOnUserChangeSave,
            )

            val currentProjectData = Observable.combineLatest<KsOptional<RefTag?>, KsOptional<RefTag?>, KsOptional<Uri?>, Project, ProjectData>(
                refTag,
                cookieRefTag,
                fullDeeplink,
                currentProject
            ) { refTagFromIntent, refTagFromCookie, fullDeeplink, project ->
                projectData(refTagFromIntent, refTagFromCookie, fullDeeplink, project)
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
                            RefTag.projectShare().tag(),
                        ),
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

            currentProject
                .compose(Transformers.takePairWhenV2(creatorInfoClicked))
                .map { it.first }
                .subscribe {
                    startCreatorView.onNext(it)
                }.addToDisposable(disposables)

            loggedOutUserOnHeartClick
                .subscribe {
                    this.startLoginToutActivity.onNext(Unit)
                }.addToDisposable(disposables)

            savedProjectOnLoginSuccess
                .doOnError {
                    println(it.message)
                }
                .filter { it.isNotNull() }
                .map { it }
                .subscribe {
                    this.project.onNext(it)
                }.addToDisposable(disposables)

            projectOnUserChangeSave
                .filter { it.isNotNull() }
                .map { it }
                .subscribe {
                    this.project.onNext(it)
                }.addToDisposable(disposables)

            projectOnUserChangeSave
                .filter { p -> p.isStarred() }
                .subscribe {
                    this.showSavedPrompt.onNext(Unit)
                }.addToDisposable(disposables)

            var previousScreen = ""
            this.intent
                .subscribe { previousScreen = it.getStringExtra(IntentKey.PREVIOUS_SCREEN) ?: "" }
                .addToDisposable(disposables)

            SendThirdPartyEventUseCaseV2(sharedPreferences, ffClient)
                .sendThirdPartyEvent(
                    loadedProject,
                    apolloClient,
                    currentUser = currentUser,
                    eventName = ThirdPartyEventValues.EventName.SCREEN_VIEW,
                    firebaseScreen = ThirdPartyEventValues.ScreenName.PRELAUNCH.value,
                    firebasePreviousScreen = previousScreen,
                )
                .subscribe {
                    onThirdPartyEventSent.onNext(it.first)
                }.addToDisposable(disposables)

            // Tracking
            currentProjectData
                .take(1)
                .subscribe { data ->
                    // If a cookie hasn't been set for this ref+project then do so.
                    if (data.refTagFromCookie() == null) {
                        data.refTagFromIntent()?.let { RefTagUtils.storeCookie(it, data.project(), this.cookieManager, this.sharedPreferences) }
                    }
                    val dataWithStoredCookieRefTag = storeCurrentCookieRefTag(data)

                    // Send event to backend event attribution
                    this.attributionEvents.trackProjectPageViewed(dataWithStoredCookieRefTag)
                }.addToDisposable(disposables)
        }

        private fun mapProject(
            latestProjectData: ProjectData,
            initProject: Pair<Project, KsOptional<User>>,
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

        private fun projectData(
            refTagFromIntent: KsOptional<RefTag?>,
            refTagFromCookie: KsOptional<RefTag?>,
            fullDeeplink: KsOptional<Uri?>,
            project: Project,
        ): ProjectData {
            return ProjectData
                .builder()
                .refTagFromIntent(refTagFromIntent.getValue())
                .refTagFromCookie(refTagFromCookie.getValue())
                .fullDeeplink(fullDeeplink.getValue())
                .project(project)
                .build()
        }
        private fun storeCurrentCookieRefTag(data: ProjectData): ProjectData {
            return data
                .toBuilder()
                .refTagFromCookie(RefTagUtils.storedCookieRefTagForProject(data.project(), cookieManager, sharedPreferences))
                .build()
        }

        private fun loadProject(intent: Intent) = ProjectIntentMapper
            .project(intent, this.apolloClient).doOnError {
                // TODO Show Retry layout
            }
            .onErrorResumeNext(Observable.empty())
            .withLatestFrom(
                currentConfig.observable(),
                currentUser.observable(),
            ) { project, config, user ->
                if (user.isPresent()) {
                    return@withLatestFrom project.updateProjectWith(config, user.getValue())
                } else {
                    return@withLatestFrom project.updateProjectWith(config, null)
                }
            }

        override fun onCleared() {
            apolloClient.cleanDisposables()
            disposables.clear()
            super.onCleared()
        }

        // - Inputs
        override fun configureWith(intent: Intent) = this.intent.onNext(intent)
        override fun creatorInfoButtonClicked() = this.creatorInfoClicked.onNext(Unit)
        override fun bookmarkButtonClicked() = this.bookmarkButtonClicked.onNext(Unit)
        override fun shareButtonClicked() = shareButtonClicked.onNext(Unit)
        override fun creatorInfoClicked() = creatorInfoClicked.onNext(Unit)

        // - Outputs
        override fun project(): Observable<Project> = this.project
        override fun showShareSheet(): Observable<Pair<String, String>> = this.showShareSheet
        override fun startLoginToutActivity(): Observable<Unit> = this.startLoginToutActivity
        override fun showSavedPrompt(): Observable<Unit> = this.showSavedPrompt
        override fun startCreatorView(): Observable<Project> = this.startCreatorView
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PrelaunchProjectViewModel(environment) as T
        }
    }
}
