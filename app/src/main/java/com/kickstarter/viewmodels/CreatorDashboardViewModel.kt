package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ListUtils

import com.kickstarter.libs.utils.extensions.isFalse
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Project
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.CreatorDashboardActivity
import com.kickstarter.ui.adapters.CreatorDashboardAdapter
import com.kickstarter.ui.adapters.CreatorDashboardBottomSheetAdapter
import com.kickstarter.ui.adapters.data.ProjectDashboardData
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface CreatorDashboardViewModel {
    interface Inputs :
        CreatorDashboardBottomSheetAdapter.Delegate,
        CreatorDashboardAdapter.Delegate {
        /** Call when the back button is clicked and the bottom sheet is expanded.  */
        fun backClicked()

        /** Call when project selection should be shown.  */
        override fun projectsListButtonClicked()

        /** Call when the scrim is clicked.  */
        fun scrimClicked()
    }

    interface Outputs {
        /** Emits a boolean determining if the bottom sheet should expand.  */
        fun bottomSheetShouldExpand(): Observable<Boolean>

        /** Emits a boolean determining if the progress bar should be visible.  */
        fun progressBarIsVisible(): Observable<Boolean>

        /** Emits the current project dashboard data.  */
        fun projectDashboardData(): Observable<ProjectDashboardData>

        /** Emits the current project's name.  */
        fun projectName(): Observable<String>

        /** Emits when project dropdown should be shown.  */
        fun projectsForBottomSheet(): Observable<List<Project>>
    }

    class ViewModel(environment: Environment) :
        ActivityViewModel<CreatorDashboardActivity?>(environment), Inputs, Outputs {
        private val client: ApiClientType

        private val backClicked = PublishSubject.create<Void>()
        private val projectSelectionInput = PublishSubject.create<Project?>()
        private val projectsListButtonClicked = PublishSubject.create<Void>()
        private val scrimClicked = PublishSubject.create<Void>()

        private val bottomSheetShouldExpand = BehaviorSubject.create<Boolean>()
        private val progressBarIsVisible = BehaviorSubject.create<Boolean>()
        private val projectDashboardData = BehaviorSubject.create<ProjectDashboardData>()
        private val projectName = BehaviorSubject.create<String>()
        private val projectsForBottomSheet = BehaviorSubject.create<List<Project>>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            client = requireNotNull(environment.apiClient())

            val isViewingSingleProject = intent()
                .map { it.hasExtra(IntentKey.PROJECT) }

            val projectsNotification = isViewingSingleProject
                .filter { it.isFalse() }
                .switchMap {
                    client.fetchProjects(true)
                        .materialize()
                        .share()
                }

            val projects = projectsNotification
                .compose(Transformers.values())
                .map { it.projects() }

            val firstProject = projects
                .map { ListUtils.first(it) }

            val intentProject = intent()
                .map { it.getParcelableExtra<Project>(IntentKey.PROJECT) }
                .filter { it.isNotNull() }

            val currentProject = Observable
                .merge(firstProject, projectSelectionInput, intentProject)
                .filter { it.isNotNull() }
                .map { requireNotNull(it) }

            currentProject
                .map { it.name() }
                .compose(bindToLifecycle())
                .subscribe(projectName)

            currentProject
                .compose(bindToLifecycle())
                .subscribe { analyticEvents.trackCreatorDashboardPageViewed(it) }

            val projectStatsEnvelopeNotification = currentProject
                .switchMap {
                    client.fetchProjectStats(it)
                        .doOnSubscribe { progressBarIsVisible.onNext(true) }
                        .doAfterTerminate { progressBarIsVisible.onNext(false) }
                }
                .share()
                .materialize()

            val projectStatsEnvelope = projectStatsEnvelopeNotification
                .compose(Transformers.values())

            Observable.combineLatest(
                projects.filter { it.size > 1 },
                currentProject
            ) { projectList: List<Project>?, project: Project? ->
                Observable
                    .from(projectList)
                    .filter { it.id() != project?.id() }
                    .toList()
            }.flatMap { listObservable: Observable<List<Project>>? -> listObservable }
                .compose(bindToLifecycle())
                .subscribe(projectsForBottomSheet)

            Observable.combineLatest(
                currentProject,
                projectStatsEnvelope,
                isViewingSingleProject
            ) { project: Project, projectStatsEnvelope: ProjectStatsEnvelope, isViewingSingleProject: Boolean ->
                ProjectDashboardData(
                    project, projectStatsEnvelope, isViewingSingleProject
                )
            }
                .compose(bindToLifecycle())
                .distinctUntilChanged()
                .subscribe(projectDashboardData)

            projectsListButtonClicked
                .map { true }
                .compose(bindToLifecycle())
                .subscribe(bottomSheetShouldExpand)

            projectsListButtonClicked
                .map { true }
                .compose<Pair<Boolean, Project>>(Transformers.combineLatestPair(currentProject))
                .compose(bindToLifecycle())
                .subscribe {
                    analyticEvents.trackCreatorDashboardSelectAnotherProjectCTA(it.second)
                }

            Observable.merge(backClicked, scrimClicked, projectSelectionInput)
                .map { false }
                .compose(bindToLifecycle())
                .subscribe(bottomSheetShouldExpand)
        }
        override fun backClicked() {
            backClicked.onNext(null)
        }

        override fun projectSelectionInput(project: Project?) {
            projectSelectionInput.onNext(project)
        }

        override fun projectsListButtonClicked() {
            projectsListButtonClicked.onNext(null)
        }

        override fun scrimClicked() {
            scrimClicked.onNext(null)
        }

        override fun bottomSheetShouldExpand(): Observable<Boolean> = bottomSheetShouldExpand

        override fun progressBarIsVisible(): Observable<Boolean> = progressBarIsVisible

        override fun projectDashboardData(): Observable<ProjectDashboardData> = projectDashboardData

        override fun projectName(): Observable<String> = projectName

        override fun projectsForBottomSheet(): Observable<List<Project>> = projectsForBottomSheet
    }
}
