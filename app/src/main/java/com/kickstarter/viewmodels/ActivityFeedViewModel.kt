package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.ApiPaginator
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.EventContextValues
import com.kickstarter.libs.utils.extensions.intValueOrZero
import com.kickstarter.libs.utils.extensions.isNonZero
import com.kickstarter.models.Activity
import com.kickstarter.models.ErroredBacking
import com.kickstarter.models.Project
import com.kickstarter.models.SurveyResponse
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.ApolloClientType
import com.kickstarter.services.apiresponses.ActivityEnvelope
import com.kickstarter.ui.activities.ActivityFeedActivity
import com.kickstarter.ui.adapters.ActivityFeedAdapter
import com.kickstarter.ui.viewholders.EmptyActivityFeedViewHolder
import com.kickstarter.ui.viewholders.FriendBackingViewHolder
import com.kickstarter.ui.viewholders.ProjectStateChangedPositiveViewHolder
import com.kickstarter.ui.viewholders.ProjectStateChangedViewHolder
import com.kickstarter.ui.viewholders.ProjectUpdateViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

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
        fun goToDiscovery(): Observable<Void>

        /** Emits when login tout should be shown.  */
        fun goToLogin(): Observable<Void>

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

    class ViewModel(environment: Environment) :
        ActivityViewModel<ActivityFeedActivity>(environment), Inputs, Outputs {

        private val apiClient: ApiClientType
        private val apolloClient: ApolloClientType
        private val currentUser: CurrentUserType

        private val discoverProjectsClick = PublishSubject.create<Void>()
        private val friendBackingClick = PublishSubject.create<Activity>()
        private val loginClick = PublishSubject.create<Void>()
        private val managePledgeClicked = PublishSubject.create<String>()
        private val nextPage = PublishSubject.create<Void>()
        private val projectStateChangedClick = PublishSubject.create<Activity>()
        private val projectStateChangedPositiveClick = PublishSubject.create<Activity>()
        private val projectUpdateClick = PublishSubject.create<Activity>()
        private val projectUpdateProjectClick = PublishSubject.create<Activity>()
        private val refresh = PublishSubject.create<Void>()
        private val resume = PublishSubject.create<Void>()
        private val surveyClick = PublishSubject.create<SurveyResponse>()
        private val activityList = BehaviorSubject.create<List<Activity>>()
        private val erroredBackings = BehaviorSubject.create<List<ErroredBacking>>()
        private val goToDiscovery: Observable<Void>
        private val goToLogin: Observable<Void>
        private val goToProject: Observable<Project>
        private val goToSurvey: Observable<SurveyResponse>
        private val isFetchingActivities = BehaviorSubject.create<Boolean>()
        private val loggedInEmptyStateIsVisible = BehaviorSubject.create<Boolean>()
        private val loggedOutEmptyStateIsVisible = BehaviorSubject.create<Boolean>()
        private val startFixPledge = PublishSubject.create<String>()
        private val startUpdateActivity: Observable<Activity>
        private val surveys = BehaviorSubject.create<List<SurveyResponse>>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            apiClient = requireNotNull(environment.apiClient())
            apolloClient = requireNotNull(environment.apolloClient())
            currentUser = requireNotNull(environment.currentUser())

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
                .compose(bindToLifecycle())
                .subscribe { p: Project ->
                    analyticEvents.trackProjectCardClicked(
                        p,
                        EventContextValues.ContextPageName.ACTIVITY_FEED.contextName
                    )
                }

            startUpdateActivity = projectUpdateClick

            val refreshOrResume = Observable.merge(refresh, resume).share()

            val loggedInUser = currentUser.loggedInUser()

            loggedInUser
                .compose(Transformers.takeWhen(refreshOrResume))
                .switchMap {
                    apiClient.fetchUnansweredSurveys().compose(Transformers.neverError()).share()
                }
                .compose(bindToLifecycle())
                .subscribe(surveys)

            loggedInUser
                .compose(Transformers.takeWhen(refreshOrResume))
                .switchMap {
                    apolloClient.erroredBackings().compose(Transformers.neverError()).share()
                }
                .compose(bindToLifecycle())
                .subscribe { v: List<ErroredBacking> -> erroredBackings.onNext(v) }

            loggedInUser
                .compose(Transformers.takeWhen(refreshOrResume))
                .map { user: User ->
                    user.unseenActivityCount().intValueOrZero() + user.erroredBackingsCount()
                        .intValueOrZero()
                }
                .filter { it.isNonZero() }
                .distinctUntilChanged()
                .switchMap {
                    apolloClient.clearUnseenActivity().compose(Transformers.neverError())
                }
                .switchMap {
                    apiClient.fetchCurrentUser().compose(Transformers.neverError())
                }
                .compose(bindToLifecycle())
                .subscribe {
                    currentUser.refresh(it)
                }

            val paginator = ApiPaginator.builder<Activity, ActivityEnvelope, Void>()
                .nextPage(nextPage)
                .startOverWith(refresh)
                .envelopeToListOfData { obj: ActivityEnvelope -> obj.activities() }
                .envelopeToMoreUrl { env: ActivityEnvelope -> env.urls().api().moreActivities() }
                .loadWithParams {
                    apiClient.fetchActivities().compose(Transformers.neverError())
                }
                .loadWithPaginationPath { paginationPath: String ->
                    apiClient.fetchActivitiesWithPaginationPath(
                        paginationPath
                    )
                }
                .build()

            paginator.paginatedData()
                .compose(bindToLifecycle())
                .subscribe(activityList)

            paginator.isFetching
                .compose(bindToLifecycle())
                .subscribe(isFetchingActivities)

            currentUser.loggedInUser()
                .take(1)
                .compose(bindToLifecycle())
                .subscribe { refresh() }

            currentUser.isLoggedIn
                .map { loggedIn: Boolean -> !loggedIn }
                .compose(bindToLifecycle())
                .subscribe(loggedOutEmptyStateIsVisible)

            managePledgeClicked
                .compose(bindToLifecycle())
                .subscribe { v: String -> startFixPledge.onNext(v) }

            currentUser.observable()
                .compose(Transformers.takePairWhen(activityList))
                .map { ua: Pair<User, List<Activity>> -> ua.first != null && ua.second.isEmpty() }
                .compose(bindToLifecycle())
                .subscribe(loggedInEmptyStateIsVisible)

            // Track viewing and paginating activity.
            val feedViewed = nextPage
                .compose(Transformers.incrementalCount())
                .startWith(0)

            feedViewed
                .take(1)
                .compose(bindToLifecycle())
                .subscribe { analyticEvents.trackActivityFeedPageViewed() }

            discoverProjectsClick
                .compose(bindToLifecycle())
                .subscribe { analyticEvents.trackDiscoverProjectCTAClicked() }
        }

        override fun emptyActivityFeedDiscoverProjectsClicked(viewHolder: EmptyActivityFeedViewHolder?) {
            discoverProjectsClick.onNext(null)
        }

        override fun emptyActivityFeedLoginClicked(viewHolder: EmptyActivityFeedViewHolder?) {
            loginClick.onNext(null)
        }

        override fun managePledgeClicked(projectSlug: String) {
            managePledgeClicked.onNext(projectSlug)
        }

        override fun friendBackingClicked(
            viewHolder: FriendBackingViewHolder?,
            activity: Activity?
        ) {
            friendBackingClick.onNext(activity)
        }

        override fun nextPage() {
            nextPage.onNext(null)
        }

        override fun projectStateChangedClicked(
            viewHolder: ProjectStateChangedViewHolder?,
            activity: Activity?
        ) {
            projectStateChangedClick.onNext(activity)
        }

        override fun projectStateChangedPositiveClicked(
            viewHolder: ProjectStateChangedPositiveViewHolder?,
            activity: Activity?
        ) {
            projectStateChangedPositiveClick.onNext(activity)
        }

        override fun projectUpdateClicked(
            viewHolder: ProjectUpdateViewHolder?,
            activity: Activity?
        ) {
            projectUpdateClick.onNext(activity)
        }

        override fun projectUpdateProjectClicked(
            viewHolder: ProjectUpdateViewHolder?,
            activity: Activity?
        ) {
            projectUpdateProjectClick.onNext(activity)
        }

        override fun refresh() {
            refresh.onNext(null)
        }

        override fun resume() {
            resume.onNext(null)
        }

        override fun activityList(): Observable<List<Activity>> = activityList
        override fun erroredBackings(): Observable<List<ErroredBacking>> = erroredBackings
        override fun goToDiscovery(): Observable<Void> = goToDiscovery
        override fun goToLogin(): Observable<Void> = goToLogin
        override fun goToProject(): Observable<Project> = goToProject
        override fun goToSurvey(): Observable<SurveyResponse> = goToSurvey
        override fun isFetchingActivities(): Observable<Boolean> = isFetchingActivities
        override fun loggedInEmptyStateIsVisible(): Observable<Boolean> = loggedInEmptyStateIsVisible
        override fun loggedOutEmptyStateIsVisible(): Observable<Boolean> = loggedOutEmptyStateIsVisible
        override fun startFixPledge(): Observable<String> = startFixPledge
        override fun startUpdateActivity(): Observable<Activity> = startUpdateActivity
        override fun surveys(): Observable<List<SurveyResponse>> = surveys
    }
}
