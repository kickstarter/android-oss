package com.kickstarter.viewmodels

import android.content.SharedPreferences
import android.util.Pair
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.ApiPaginator
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.EventContextValues
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.libs.utils.extensions.storeCurrentCookieRefTag
import com.kickstarter.models.Project
import com.kickstarter.models.Update
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.apiresponses.UpdatesEnvelope
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
        private val client: ApiClientType?
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
            client = requireNotNull(environment.apiClient())
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

            val paginator = ApiPaginator.builder<Update, UpdatesEnvelope, Project?>()
                .nextPage(nextPage)
                .startOverWith(startOverWith)
                .envelopeToListOfData { it.updates() }
                .envelopeToMoreUrl { it.urls().api().moreUpdates() }
                .loadWithParams {
                    client.fetchUpdates(
                        it
                    )
                }
                .loadWithPaginationPath {
                    client.fetchUpdates(
                        it
                    )
                }
                .clearWhenStartingOver(false)
                .concater { xs: List<Update>, ys: List<Update> ->
                    ListUtils.concatDistinct(xs, ys)
                }
                .build()

            project
                .compose(Transformers.combineLatestPair(paginator.paginatedData().share()))
                .compose(bindToLifecycle())
                .subscribe(projectAndUpdates)

            paginator
                .isFetching
                .distinctUntilChanged()
                .take(2)
                .map { it.negate() }
                .compose(bindToLifecycle())
                .subscribe(horizontalProgressBarIsGone)

            paginator
                .isFetching
                .compose(bindToLifecycle())
                .subscribe(isFetchingUpdates)

            project
                .compose(Transformers.takePairWhen(updateClicked))
                .compose(bindToLifecycle())
                .subscribe { startUpdateActivity.onNext(it) }
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
