package com.kickstarter.viewmodels

import android.content.SharedPreferences
import android.util.Pair
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.loadmore.ApolloPaginate
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.EventContextValues
import com.kickstarter.libs.utils.extensions.storeCurrentCookieRefTag
import com.kickstarter.models.Project
import com.kickstarter.models.Update
import com.kickstarter.services.ApolloClientType
import com.kickstarter.services.apiresponses.updatesresponse.UpdatesGraphQlEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.ProjectUpdatesActivity
import com.kickstarter.ui.data.ProjectData
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.net.CookieManager

interface ProjectUpdatesViewModel {
    interface Inputs {
        /** Call when pagination should happen. */
        fun nextPage()

        /** Call when the feed should be refreshed.  */
        fun refresh()

        /** Call when an Update is clicked.  */
        fun updateClicked(update: Update)
    }

    interface Outputs {
        /** Emits a boolean indicating whether the horizontal ProgressBar is visible.  */
        fun horizontalProgressBarIsGone(): Observable<Boolean>

        /** Emits a boolean indicating whether updates are being fetched from the API.  */
        fun isFetchingUpdates(): Observable<Boolean>

        /** Emits the current project and its updates.  */
        fun projectAndUpdates(): Observable<Pair<Project, List<Update>>>

        /** Emits a project and an update to start the update activity with.  */
        fun startUpdateActivity(): Observable<Pair<Project, Update>>
    }

    class ViewModel(environment: Environment) :
        ActivityViewModel<ProjectUpdatesActivity>(environment), Inputs, Outputs {
        private val client: ApolloClientType?
        private val cookieManager: CookieManager?
        private val sharedPreferences: SharedPreferences?
        private val nextPage = PublishSubject.create<Void>()
        private val refresh = PublishSubject.create<Void>()
        private val updateClicked = PublishSubject.create<Update>()
        private val horizontalProgressBarIsGone = BehaviorSubject.create<Boolean>()
        private val isFetchingUpdates = BehaviorSubject.create<Boolean>()
        private val projectAndUpdates = BehaviorSubject.create<Pair<Project, List<Update>>>()
        private val startUpdateActivity = PublishSubject.create<Pair<Project, Update>>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            client = requireNotNull(environment.apolloClient())
            cookieManager = requireNotNull(environment.cookieManager())
            sharedPreferences = requireNotNull(environment.sharedPreferences())

            val projectData = intent()
                .map<Any?> { it.getParcelableExtra(IntentKey.PROJECT_DATA) }
                .ofType(ProjectData::class.java)
                .take(1)

            val project = projectData
                .map { it.project() }

            projectData
                .map {
                    it.storeCurrentCookieRefTag(
                        cookieManager, sharedPreferences
                    )
                }
                .compose(bindToLifecycle())
                .subscribe {
                    analyticEvents.trackProjectScreenViewed(
                        it, EventContextValues.ContextSectionName.UPDATES.contextName
                    )
                }

            val startOverWith = Observable.merge(
                project,
                project.compose(Transformers.takeWhen(refresh))
            )

            val paginator =
                ApolloPaginate.builder<Update, UpdatesGraphQlEnvelope, Project?>()
                    .nextPage(nextPage)
                    .distinctUntilChanged(true)
                    .startOverWith(startOverWith)
                    .envelopeToListOfData { it.updates }
                    .loadWithParams {
                        loadWithProjectUpdatesList(Observable.just(it.first), it.second)
                    }
                    .clearWhenStartingOver(false)
                    .build()

            paginator.paginatedData()
                ?.share()
                ?.let {
                    project
                        .compose<Pair<Project, List<Update>>>(Transformers.combineLatestPair(it))
                        .compose(bindToLifecycle())
                        .subscribe(projectAndUpdates)
                }

            paginator
                .isFetching()
                .compose(bindToLifecycle<Boolean>())
                .subscribe {
                    horizontalProgressBarIsGone.onNext(it)
                }

            paginator
                .isFetching()
                .compose(bindToLifecycle())
                .subscribe(isFetchingUpdates)

            project
                .compose(Transformers.takePairWhen(updateClicked))
                .compose(bindToLifecycle())
                .subscribe { startUpdateActivity.onNext(it) }
        }

        private fun loadWithProjectUpdatesList(
            project: Observable<Project>,
            cursor: String?
        ): Observable<UpdatesGraphQlEnvelope> {
            return project.switchMap {
                return@switchMap client ?.getProjectUpdates(it.slug() ?: "", cursor)
            }.onErrorResumeNext(Observable.empty())
        }
        override fun nextPage() {
            nextPage.onNext(null)
        }

        override fun refresh() {
            refresh.onNext(null)
        }

        override fun updateClicked(update: Update) {
            updateClicked.onNext(update)
        }

        override fun horizontalProgressBarIsGone(): Observable<Boolean> = horizontalProgressBarIsGone
        override fun isFetchingUpdates(): Observable<Boolean> = isFetchingUpdates
        override fun projectAndUpdates(): Observable<Pair<Project, List<Update>>> = projectAndUpdates
        override fun startUpdateActivity(): Observable<Pair<Project, Update>> = startUpdateActivity
    }
}
