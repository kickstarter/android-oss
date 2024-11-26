package com.kickstarter.viewmodels

import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.AnalyticEvents
import com.kickstarter.libs.ApiPaginatorV2
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.EventContextValues
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.intValueOrZero
import com.kickstarter.libs.utils.extensions.isNonZero
import com.kickstarter.models.Activity
import com.kickstarter.models.ErroredBacking
import com.kickstarter.models.Project
import com.kickstarter.models.SurveyResponse
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.apiresponses.ActivityEnvelope
import com.kickstarter.ui.adapters.ActivityFeedAdapter
import com.kickstarter.ui.viewholders.EmptyActivityFeedViewHolder
import com.kickstarter.ui.viewholders.FriendBackingViewHolder
import com.kickstarter.ui.viewholders.ProjectStateChangedPositiveViewHolder
import com.kickstarter.ui.viewholders.ProjectStateChangedViewHolder
import com.kickstarter.ui.viewholders.ProjectUpdateViewHolder
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface ActivityFeedViewModel {
    interface Inputs : ActivityFeedAdapter.Delegate {
        /** Invoke when pagination should happen.  */
        fun nextPage()

        /** Invoke when activity's onResume runs  */
        fun resume()

        /** Invoke when the feed should be refreshed.  */
        fun refresh()
    }

    interface Outputs {
        /** Emits a list of activities representing the user's activity feed.  */
        fun activityList(): Observable<List<Activity>>

        /** Emits a list of the user's errored backings.  */
        fun erroredBackings(): Observable<List<ErroredBacking>>

        /** Emits when view should be returned to Discovery projects.  */
        fun goToDiscovery(): Observable<Unit>

        /** Emits when login tout should be shown.  */
        fun goToLogin(): Observable<Unit>

        /** Emits a project when it should be shown.  */
        fun goToProject(): Observable<Project>

        /** Emits a SurveyResponse when it should be shown.  */
        fun goToSurvey(): Observable<SurveyResponse>

        /** Emits a boolean indicating whether activities are being fetched from the API.  */
        fun isFetchingActivities(): Observable<Boolean>

        /** Emits a boolean that determines if a logged-out, empty state should be displayed.  */
        fun loggedOutEmptyStateIsVisible(): Observable<Boolean>

        /** Emits a logged-in user with zero activities in order to display an empty state.  */
        fun loggedInEmptyStateIsVisible(): Observable<Boolean>

        /** Emits when we should start the [com.kickstarter.ui.activities.ProjectPageActivity].  */
        fun startFixPledge(): Observable<String>

        /** Emits when we should start the [com.kickstarter.ui.activities.UpdateActivity].  */
        fun startUpdateActivity(): Observable<Activity>

        /** Emits a list of unanswered surveys to be shown in the user's activity feed  */
        fun surveys(): Observable<List<SurveyResponse>>
    }

    class ActivityFeedViewModel(environment: Environment) : ViewModel(), Inputs, Outputs {

        private val apiClient: ApiClientTypeV2
        private val apolloClient: ApolloClientTypeV2
        private val currentUser: CurrentUserTypeV2
        private val analyticEvents: AnalyticEvents

        private val discoverProjectsClick = PublishSubject.create<Unit>()
        private val friendBackingClick = PublishSubject.create<Activity>()
        private val loginClick = PublishSubject.create<Unit>()
        private val managePledgeClicked = PublishSubject.create<String>()
        private val nextPage = PublishSubject.create<Unit>()
        private val projectStateChangedClick = PublishSubject.create<Activity>()
        private val projectStateChangedPositiveClick = PublishSubject.create<Activity>()
        private val projectUpdateClick = PublishSubject.create<Activity>()
        private val projectUpdateProjectClick = PublishSubject.create<Activity>()
        private val refresh = PublishSubject.create<Unit>()
        private val resume = PublishSubject.create<Unit>()
        private val surveyClick = PublishSubject.create<SurveyResponse>()
        private val activityList = BehaviorSubject.create<List<Activity>>()
        private val erroredBackings = BehaviorSubject.create<List<ErroredBacking>>()
        private val goToDiscovery: Observable<Unit>
        private val goToLogin: Observable<Unit>
        private val goToProject: Observable<Project>
        private val goToSurvey: Observable<SurveyResponse>
        private val isFetchingActivities = BehaviorSubject.create<Boolean>()
        private val loggedInEmptyStateIsVisible = BehaviorSubject.create<Boolean>()
        private val loggedOutEmptyStateIsVisible = BehaviorSubject.create<Boolean>()
        private val startFixPledge = PublishSubject.create<String>()
        private val startUpdateActivity: Observable<Activity>
        private val surveys = BehaviorSubject.create<List<SurveyResponse>>()

        private val disposables = CompositeDisposable()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            apiClient = requireNotNull(environment.apiClientV2())
            apolloClient = requireNotNull(environment.apolloClientV2())
            currentUser = requireNotNull(environment.currentUserV2())
            analyticEvents = requireNotNull(environment.analytics())

            goToDiscovery = discoverProjectsClick
            goToLogin = loginClick
            goToSurvey = surveyClick

            goToProject = Observable.merge(
                friendBackingClick,
                projectStateChangedClick,
                projectStateChangedPositiveClick,
                projectUpdateProjectClick
            ).map { obj: Activity -> obj.project() }

            goToProject
                .subscribe { p: Project ->
                    analyticEvents.trackProjectCardClicked(
                        p,
                        EventContextValues.ContextPageName.ACTIVITY_FEED.contextName
                    )
                }
                .addToDisposable(disposables)

            startUpdateActivity = projectUpdateClick

            val refreshOrResume = Observable.merge(refresh, resume).share()

            val loggedInUser = currentUser.loggedInUser()

            loggedInUser
                .compose(Transformers.takeWhenV2(refreshOrResume))
                .switchMap {
                    apiClient.fetchUnansweredSurveys().compose(Transformers.neverErrorV2()).share()
                }
                .subscribe { surveys.onNext(it) }
                .addToDisposable(disposables)

            loggedInUser
                .compose(Transformers.takeWhenV2(refreshOrResume))
                .switchMap {
                    apolloClient.erroredBackings().compose(Transformers.neverErrorV2()).share()
                }
                .subscribe { v: List<ErroredBacking> -> erroredBackings.onNext(v) }
                .addToDisposable(disposables)

            loggedInUser
                .compose(Transformers.takeWhenV2(refreshOrResume))
                .map { user: User ->
                    user.unseenActivityCount().intValueOrZero() + user.erroredBackingsCount()
                        .intValueOrZero()
                }
                .filter { it.isNonZero() }
                .distinctUntilChanged()
                .switchMap {
                    apolloClient.clearUnseenActivity().compose(Transformers.neverErrorV2())
                }
                .switchMap {
                    apiClient.fetchCurrentUser().compose(Transformers.neverErrorV2())
                }
                .subscribe {
                    currentUser.refresh(it)
                }
                .addToDisposable(disposables)

            val paginator = ApiPaginatorV2.builder<Activity, ActivityEnvelope, Unit>()
                .nextPage(nextPage)
                .startOverWith(refresh)
                .envelopeToListOfData { obj: ActivityEnvelope -> obj.activities() }
                .envelopeToMoreUrl { env: ActivityEnvelope -> env.urls().api().moreActivities() }
                .loadWithParams {
                    apiClient.fetchActivities().compose(Transformers.neverErrorV2())
                }
                .loadWithPaginationPath { paginationPath: String ->
                    apiClient.fetchActivitiesWithPaginationPath(
                        paginationPath
                    )
                }
                .build()

            paginator.paginatedData()
                .subscribe { activityList.onNext(it) }
                .addToDisposable(disposables)

            paginator.isFetching
                .subscribe { isFetchingActivities.onNext(it) }
                .addToDisposable(disposables)

            currentUser.loggedInUser()
                .take(1)
                .subscribe { refresh() }
                .addToDisposable(disposables)

            currentUser.isLoggedIn
                .map { loggedIn: Boolean -> !loggedIn }
                .subscribe { loggedOutEmptyStateIsVisible.onNext(it) }
                .addToDisposable(disposables)

            managePledgeClicked
                .subscribe { v: String -> startFixPledge.onNext(v) }
                .addToDisposable(disposables)

            currentUser.observable()
                .compose(Transformers.takePairWhenV2(activityList))
                .map { ua: Pair<KsOptional<User>, List<Activity>> -> ua.first != null && ua.second.isEmpty() }
                .subscribe { loggedInEmptyStateIsVisible.onNext(it) }
                .addToDisposable(disposables)

            // Track viewing and paginating activity.
            val feedViewed = nextPage
                .compose(Transformers.incrementalCountV2())
                .startWith(0)

            feedViewed
                .take(1)
                .subscribe { analyticEvents.trackActivityFeedPageViewed() }
                .addToDisposable(disposables)

            discoverProjectsClick
                .subscribe { analyticEvents.trackDiscoverProjectCTAClicked() }
                .addToDisposable(disposables)
        }

        override fun onCleared() {
            apolloClient.cleanDisposables()
            disposables.clear()
            super.onCleared()
        }

        override fun emptyActivityFeedDiscoverProjectsClicked(viewHolder: EmptyActivityFeedViewHolder?) {
            discoverProjectsClick.onNext(Unit)
        }

        override fun emptyActivityFeedLoginClicked(viewHolder: EmptyActivityFeedViewHolder?) {
            loginClick.onNext(Unit)
        }

        override fun managePledgeClicked(projectSlug: String) {
            managePledgeClicked.onNext(projectSlug)
        }

        override fun friendBackingClicked(
            viewHolder: FriendBackingViewHolder?,
            activity: Activity?
        ) {
            if (activity != null) {
                friendBackingClick.onNext(activity)
            }
        }

        override fun nextPage() {
            nextPage.onNext(Unit)
        }

        override fun projectStateChangedClicked(
            viewHolder: ProjectStateChangedViewHolder?,
            activity: Activity?
        ) {
            if (activity != null) {
                projectStateChangedClick.onNext(activity)
            }
        }

        override fun projectStateChangedPositiveClicked(
            viewHolder: ProjectStateChangedPositiveViewHolder?,
            activity: Activity?
        ) {
            if (activity != null) {
                projectStateChangedPositiveClick.onNext(activity)
            }
        }

        override fun projectUpdateClicked(
            viewHolder: ProjectUpdateViewHolder?,
            activity: Activity?
        ) {
            if (activity != null) {
                projectUpdateClick.onNext(activity)
            }
        }

        override fun projectUpdateProjectClicked(
            viewHolder: ProjectUpdateViewHolder?,
            activity: Activity?
        ) {
            if (activity != null) {
                projectUpdateProjectClick.onNext(activity)
            }
        }

        override fun refresh() {
            refresh.onNext(Unit)
        }

        override fun resume() {
            resume.onNext(Unit)
        }

        override fun activityList(): Observable<List<Activity>> = activityList
        override fun erroredBackings(): Observable<List<ErroredBacking>> = erroredBackings
        override fun goToDiscovery(): Observable<Unit> = goToDiscovery
        override fun goToLogin(): Observable<Unit> = goToLogin
        override fun goToProject(): Observable<Project> = goToProject
        override fun goToSurvey(): Observable<SurveyResponse> = goToSurvey
        override fun isFetchingActivities(): Observable<Boolean> = isFetchingActivities
        override fun loggedInEmptyStateIsVisible(): Observable<Boolean> = loggedInEmptyStateIsVisible
        override fun loggedOutEmptyStateIsVisible(): Observable<Boolean> = loggedOutEmptyStateIsVisible
        override fun startFixPledge(): Observable<String> = startFixPledge
        override fun startUpdateActivity(): Observable<Activity> = startUpdateActivity
        override fun surveys(): Observable<List<SurveyResponse>> = surveys
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ActivityFeedViewModel(environment) as T
        }
    }
}
