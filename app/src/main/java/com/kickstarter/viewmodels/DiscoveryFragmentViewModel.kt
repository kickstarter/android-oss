package com.kickstarter.viewmodels

import android.content.SharedPreferences
import android.util.Pair
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.ExperimentsClientType
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.loadmore.ApolloPaginate.Companion.builder
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.libs.preferences.IntPreferenceType
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.EventContextValues
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.DISCOVER
import com.kickstarter.libs.utils.ExperimentData
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.RefTagUtils
import com.kickstarter.libs.utils.extensions.combineProjectsAndParams
import com.kickstarter.libs.utils.extensions.fillRootCategoryForFeaturedProjects
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.updateStartedProjectAndDiscoveryParamsList
import com.kickstarter.models.Activity
import com.kickstarter.models.Category
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.ApolloClientType
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.apiresponses.DiscoverEnvelope
import com.kickstarter.ui.adapters.DiscoveryActivitySampleAdapter
import com.kickstarter.ui.adapters.DiscoveryEditorialAdapter
import com.kickstarter.ui.adapters.DiscoveryOnboardingAdapter
import com.kickstarter.ui.adapters.DiscoveryProjectCardAdapter
import com.kickstarter.ui.data.Editorial
import com.kickstarter.ui.data.ProjectData.Companion.builder
import com.kickstarter.ui.fragments.DiscoveryFragment
import com.kickstarter.ui.viewholders.ActivitySampleFriendBackingViewHolder
import com.kickstarter.ui.viewholders.ActivitySampleFriendFollowViewHolder
import com.kickstarter.ui.viewholders.ActivitySampleProjectViewHolder
import com.kickstarter.ui.viewholders.DiscoveryOnboardingViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.net.CookieManager
import java.util.concurrent.TimeUnit

interface DiscoveryFragmentViewModel {
    interface Inputs :
        DiscoveryProjectCardAdapter.Delegate,
        DiscoveryOnboardingAdapter.Delegate,
        DiscoveryEditorialAdapter.Delegate,
        DiscoveryActivitySampleAdapter.Delegate {
        /** Call when the page content should be cleared.   */
        fun clearPage()

        /** Call when user clicks hearts to start animation.   */
        fun heartContainerClicked()

        /** Call for project pagination.  */
        fun nextPage()

        /** Call when params from Discovery Activity change.  */
        fun paramsFromActivity(params: DiscoveryParams)

        /** Call when the projects should be refreshed.  */
        fun refresh()

        /**  Call when we should load the root categories.  */
        fun rootCategories(rootCategories: List<Category>)
    }

    interface Outputs {
        /**  Emits an activity for the activity sample view.  */
        fun activity(): Observable<Activity>

        /** Emits a boolean indicating whether projects are being fetched from the API.  */
        fun isFetchingProjects(): Observable<Boolean>

        /** Emits a list of projects to display. */
        fun projectList(): Observable<List<Pair<Project, DiscoveryParams>>>

        /** Emits a boolean that determines if an editorial should be shown.  */
        fun shouldShowEditorial(): Observable<Editorial>

        /** Emits a boolean that determines if the saved empty view should be shown.  */
        fun shouldShowEmptySavedView(): Observable<Boolean>

        /** Emits a boolean that determines if the onboarding view should be shown.  */
        fun shouldShowOnboardingView(): Observable<Boolean>

        /** Emits when the activity feed should be shown.  */
        fun showActivityFeed(): Observable<Boolean>

        /** Emits when the login tout activity should be shown.  */
        fun showLoginTout(): Observable<Boolean>

        /** Emits when the heart animation should play.  */
        fun startHeartAnimation(): Observable<Void?>

        /** Emits an Editorial when we should start the [com.kickstarter.ui.activities.EditorialActivity].  */
        fun startEditorialActivity(): Observable<Editorial>

        /** Emits a Project and RefTag pair when we should start the [com.kickstarter.ui.activities.ProjectActivity].  */
        fun startProjectActivity(): Observable<Triple<Project, RefTag, Boolean>>

        /** Emits an activity when we should start the [com.kickstarter.ui.activities.UpdateActivity].  */
        fun startUpdateActivity(): Observable<Activity>

        /** Emits when we should start [com.kickstarter.ui.activities.LoginToutActivity].  */
        fun startLoginToutActivityToSaveProject(): Observable<Project>

        /** Emits when we need to scroll to saved project */
        fun scrollToSavedProjectPosition(): Observable<Int>

        /** Emits when the success prompt for saving should be displayed.  */
        fun showSavedPrompt(): Observable<Void>
    }

    class ViewModel(environment: Environment) :
        FragmentViewModel<DiscoveryFragment?>(environment),
        Inputs,
        Outputs {
        private val apiClient: ApiClientType = environment.apiClient()
        private val apolloClient: ApolloClientType = environment.apolloClient()
        private val activitySamplePreference: IntPreferenceType = environment.activitySamplePreference()
        private val optimizely: ExperimentsClientType = environment.optimizely()
        private val sharedPreferences: SharedPreferences = environment.sharedPreferences()
        private val cookieManager: CookieManager = environment.cookieManager()
        private val currentUser: CurrentUserType = environment.currentUser()
        @JvmField
        val inputs: Inputs = this
        @JvmField
        val outputs: Outputs = this

        private val activityClick = PublishSubject.create<Boolean>()
        private val activitySampleProjectClick = PublishSubject.create<Project>()
        private val activityUpdateClick = PublishSubject.create<Activity>()
        private val clearPage = PublishSubject.create<Void?>()
        private val discoveryOnboardingLoginToutClick = PublishSubject.create<Boolean>()
        private val editorialClicked = PublishSubject.create<Editorial>()
        private val nextPage = PublishSubject.create<Void?>()
        private val paramsFromActivity = PublishSubject.create<DiscoveryParams>()
        private val projectCardClicked = PublishSubject.create<Project>()
        private val onHeartButtonClicked = PublishSubject.create<Project>()
        private val refresh = PublishSubject.create<Void?>()
        private val rootCategories = PublishSubject.create<List<Category>>()
        private val activity = BehaviorSubject.create<Activity?>()
        private val heartContainerClicked = BehaviorSubject.create<Void?>()
        private val isFetchingProjects: BehaviorSubject<Boolean> = BehaviorSubject.create()
        private val projectList = BehaviorSubject.create<List<Pair<Project, DiscoveryParams>>>()
        private val showActivityFeed: Observable<Boolean>
        private val showLoginTout: Observable<Boolean>
        private val shouldShowEditorial = BehaviorSubject.create<Editorial?>()
        private val shouldShowEmptySavedView = BehaviorSubject.create<Boolean>()
        private val shouldShowOnboardingView = BehaviorSubject.create<Boolean>()
        private val startEditorialActivity = PublishSubject.create<Editorial>()
        private val startProjectActivity: Observable<Triple<Project, RefTag, Boolean>>
        private val startUpdateActivity: Observable<Activity>
        private val startHeartAnimation = BehaviorSubject.create<Void?>()
        private val startLoginToutActivityToSaveProject = PublishSubject.create<Project>()
        private val scrollToSavedProjectPosition = PublishSubject.create<Int>()
        private val showSavedPrompt = PublishSubject.create<Void>()

        init {
            val changedUser = currentUser.observable()
                .distinctUntilChanged()
            val userIsLoggedIn = currentUser.isLoggedIn
                .distinctUntilChanged()
            val selectedParams = Observable.combineLatest(
                changedUser,
                paramsFromActivity.distinctUntilChanged()
            ) { _, params ->
                params
            }

            val startOverWith = Observable.merge(
                selectedParams,
                selectedParams.compose(Transformers.takeWhen(refresh))
            )

            val paginator = builder<Project, DiscoverEnvelope, DiscoveryParams?>()
                .nextPage(nextPage)
                .distinctUntilChanged(true)
                .startOverWith(startOverWith)
                .envelopeToListOfData { it.projects() }
                .loadWithParams {
                    makeCallWithParams(
                        it
                    )
                }
                .clearWhenStartingOver(false)
                .concater { xs, ys ->
                    xs?.let { firstList ->
                        ys?.let { secondList ->
                            ListUtils.concatDistinct(firstList, secondList)
                        }
                    }
                }
                .build()

            paginator.isFetching()
                .compose(bindToLifecycle())
                .subscribe(isFetchingProjects)

            projectList
                .compose(Transformers.ignoreValues())
                .compose(bindToLifecycle())
                .subscribe { isFetchingProjects.onNext(false) }

            val activitySampleProjectClick = activitySampleProjectClick
                .map<Pair<Project, RefTag>> {
                    Pair.create(
                        it,
                        RefTag.activitySample()
                    )
                }

            projectCardClicked
                .compose(bindToLifecycle())
                .subscribe {
                    analyticEvents.trackProjectCardClicked(it, EventContextValues.ContextPageName.DISCOVER.contextName)
                }

            paramsFromActivity
                .compose(Transformers.takePairWhen(projectCardClicked))
                .compose(bindToLifecycle())
                .subscribe {
                    val refTag =
                        RefTagUtils.projectAndRefTagFromParamsAndProject(it.first, it.second)
                    val cookieRefTag = RefTagUtils.storedCookieRefTagForProject(
                        it.second,
                        cookieManager,
                        sharedPreferences
                    )
                    val projectData = builder()
                        .refTagFromIntent(refTag.second)
                        .refTagFromCookie(cookieRefTag)
                        .project(it.second)
                        .build()
                    analyticEvents.trackDiscoverProjectCtaClicked(it.first, projectData)
                }

            val projectCardClick = paramsFromActivity
                .compose(Transformers.takePairWhen(projectCardClicked))
                .map {
                    RefTagUtils.projectAndRefTagFromParamsAndProject(it.first, it.second!!)
                }

            val projects = Observable.combineLatest(
                paginator.paginatedData(),
                rootCategories
            ) { projects, rootCategories ->
                projects.fillRootCategoryForFeaturedProjects(rootCategories)
            }

            Observable.combineLatest(
                projects,
                selectedParams.distinctUntilChanged()
            ) { projects, params ->
                params?.let {
                    combineProjectsAndParams(
                        projects,
                        it
                    )
                }
            }.compose(bindToLifecycle())
                .subscribe(
                    projectList
                )

            showActivityFeed = activityClick
            startUpdateActivity = activityUpdateClick
            showLoginTout = discoveryOnboardingLoginToutClick

            val isProjectPageEnabled = Observable.just(
                optimizely.isFeatureEnabled(
                    OptimizelyFeature.Key.PROJECT_PAGE_V2
                )
            )

            startProjectActivity = Observable.merge(
                activitySampleProjectClick,
                projectCardClick
            )
                .withLatestFrom(isProjectPageEnabled) { a: Pair<Project, RefTag>, b: Boolean ->
                    Triple(
                        a.first,
                        a.second,
                        b
                    )
                }

            clearPage
                .compose(bindToLifecycle())
                .subscribe {
                    shouldShowOnboardingView.onNext(false)
                    activity.onNext(null)
                    projectList.onNext(emptyList())
                }

            val userWhenOptimizelyReady = Observable.merge(
                changedUser,
                changedUser.compose(Transformers.takeWhen(optimizelyReady))
            )

            val lightsOnEnabled = userWhenOptimizelyReady
                .map { user: User? ->
                    optimizely.isFeatureEnabled(
                        OptimizelyFeature.Key.LIGHTS_ON,
                        ExperimentData(user, null, null)
                    )
                }
                .distinctUntilChanged()

            currentUser.observable()
                .compose(Transformers.combineLatestPair(paramsFromActivity))
                .compose(Transformers.combineLatestPair(lightsOnEnabled))
                .map { defaultParamsAndEnabled: Pair<Pair<User, DiscoveryParams>, Boolean> ->
                    isDefaultParams(
                        defaultParamsAndEnabled.first
                    ) && defaultParamsAndEnabled.second.isTrue()
                }
                .map { shouldShow: Boolean -> if (shouldShow) Editorial.LIGHTS_ON else null }
                .compose(bindToLifecycle())
                .subscribe(shouldShowEditorial)

            editorialClicked
                .compose(bindToLifecycle())
                .subscribe(startEditorialActivity)

            paramsFromActivity
                .compose(Transformers.combineLatestPair(userIsLoggedIn))
                .map { pu: Pair<DiscoveryParams, Boolean> ->
                    isOnboardingVisible(
                        pu.first,
                        pu.second
                    )
                }
                .compose(bindToLifecycle())
                .subscribe(shouldShowOnboardingView)

            paramsFromActivity
                .map { params: DiscoveryParams -> isSavedVisible(params) }
                .compose(Transformers.combineLatestPair(projectList))
                .map { it.first && it.second.isEmpty() }
                .compose(bindToLifecycle())
                .distinctUntilChanged()
                .subscribe(shouldShowEmptySavedView)

            shouldShowEmptySavedView
                .filter { it.isTrue() }
                .map<Any?> { null }
                .mergeWith(heartContainerClicked)
                .subscribe { startHeartAnimation.onNext(null) }

            val loggedInUserAndParams = currentUser.loggedInUser()
                .distinctUntilChanged()
                .compose(Transformers.combineLatestPair(paramsFromActivity))

            // Activity should show on the user's default params
            loggedInUserAndParams
                .filter {
                    isDefaultParams(
                        it
                    )
                }
                .flatMap { fetchActivity() }
                .filter { activityHasNotBeenSeen(it) }
                .doOnNext { saveLastSeenActivityId(it) }
                .compose(bindToLifecycle())
                .subscribe(activity)

            // Clear activity sample when params change from default
            loggedInUserAndParams
                .filter {
                    !isDefaultParams(it)
                }
                .map { null }
                .compose(bindToLifecycle())
                .subscribe(activity)

            paramsFromActivity
                .compose(
                    Transformers.combineLatestPair(
                        paginator.loadingPage()!!.distinctUntilChanged()
                    )
                )
                .filter { it.second == 1 }
                .compose(bindToLifecycle())
                .subscribe {
                    analyticEvents.trackDiscoveryPageViewed(it.first)
                }

            discoveryOnboardingLoginToutClick
                .compose(bindToLifecycle())
                .subscribe {
                    analyticEvents.trackLoginOrSignUpCtaClicked(
                        null,
                        EventContextValues.ContextPageName.DISCOVER.contextName
                    )
                }

            val loggedInUserOnHeartClick = userIsLoggedIn
                .compose(Transformers.takePairWhen(this.onHeartButtonClicked))
                .filter { it.first == true }

            val loggedOutUserOnHeartClick = userIsLoggedIn
                .compose(Transformers.takePairWhen(this.onHeartButtonClicked))
                .filter { it.first == false }

            val projectOnUserChangeSave = loggedInUserOnHeartClick
                .switchMap {
                    this.toggleProjectSave(it.second)
                }
                .share()

            loggedOutUserOnHeartClick
                .map { it }
                .subscribe {
                    this.startLoginToutActivityToSaveProject.onNext(it.second)
                }

            val savedProjectOnLoginSuccess = this.startLoginToutActivityToSaveProject
                .compose(Transformers.combineLatestPair(this.currentUser.observable()))
                .filter { su ->
                    su.second != null
                }.take(1)
                .switchMap {
                    this.saveProject(it.first)
                }
                .share()

            this.projectList
                .compose(Transformers.takePairWhen(projectOnUserChangeSave))
                .map {
                    it.second.updateStartedProjectAndDiscoveryParamsList(it.first)
                }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe {
                    this.projectList.onNext(it)
                }

            this.projectList
                .compose(Transformers.takePairWhen(savedProjectOnLoginSuccess))
                .map {
                    it.first.indexOfFirst { item ->
                        item.first.id() == it.second.id() && item.first.slug() == it.second.slug()
                    }
                }
                .distinctUntilChanged()
                .delay(300, TimeUnit.MILLISECONDS, environment.scheduler())
                .compose(bindToLifecycle())
                .subscribe {
                    scrollToSavedProjectPosition.onNext(it)
                }

            val projectSavedStatus = projectOnUserChangeSave.mergeWith(savedProjectOnLoginSuccess)

            projectSavedStatus
                .compose(bindToLifecycle())
                .subscribe { this.analyticEvents.trackWatchProjectCTA(it, DISCOVER) }

            projectSavedStatus
                .filter { p -> p.isStarred() && p.isLive && !p.isApproachingDeadline }
                .compose(Transformers.ignoreValues())
                .compose(bindToLifecycle())
                .subscribe(this.showSavedPrompt)
        }

        /**
         * Calls to GraphQL client to fetch projects filtering by DiscoveryParams
         * @param discoveryParamsStringPair .first discovery params.
         * @param discoveryParamsStringPair .second cursor for pagination, null on the first call.
         * @return Observable<DiscoverEnvelope>
         </DiscoverEnvelope> */
        private fun makeCallWithParams(discoveryParamsStringPair: Pair<DiscoveryParams?, String?>): Observable<DiscoverEnvelope> {
            return apolloClient.getProjects(
                discoveryParamsStringPair.first!!,
                discoveryParamsStringPair.second
            )
        }

        private fun activityHasNotBeenSeen(activity: Activity?): Boolean {
            return activity != null && activity.id() != activitySamplePreference.get().toLong()
        }

        private fun fetchActivity(): Observable<Activity?> {
            return apiClient.fetchActivities(1)
                .map { it.activities() }
                .map { it.firstOrNull() }
                .filter { ObjectUtils.isNotNull(it) }
                .compose(Transformers.neverError())
        }

        private fun isDefaultParams(userAndParams: Pair<User, DiscoveryParams>): Boolean {
            val discoveryParams = userAndParams.second
            val user = userAndParams.first
            return discoveryParams == DiscoveryParams.getDefaultParams(user)
        }

        private fun isOnboardingVisible(params: DiscoveryParams, isLoggedIn: Boolean): Boolean {
            val sort = params.sort()
            val isSortHome = DiscoveryParams.Sort.MAGIC == sort
            return params.isAllProjects.isTrue() && isSortHome && !isLoggedIn
        }

        private fun isSavedVisible(params: DiscoveryParams): Boolean {
            return params.isSavedProjects
        }

        private fun saveLastSeenActivityId(activity: Activity?) {
            if (activity != null) {
                activitySamplePreference.set(activity.id().toInt())
            }
        }

        private fun saveProject(project: Project): Observable<Project> {
            return this.apolloClient.watchProject(project)
                .compose(Transformers.neverError())
        }

        private fun unSaveProject(project: Project): Observable<Project> {
            return this.apolloClient.unWatchProject(project).compose(Transformers.neverError())
        }

        private fun toggleProjectSave(project: Project): Observable<Project> {
            return if (project.isStarred())
                unSaveProject(project)
            else
                saveProject(project)
        }

        override fun activitySampleFriendBackingViewHolderSeeActivityClicked(viewHolder: ActivitySampleFriendBackingViewHolder) =
            activityClick.onNext(true)
        override fun activitySampleFriendFollowViewHolderSeeActivityClicked(viewHolder: ActivitySampleFriendFollowViewHolder) =
            activityClick.onNext(true)
        override fun activitySampleProjectViewHolderSeeActivityClicked(viewHolder: ActivitySampleProjectViewHolder) =
            activityClick.onNext(true)
        override fun editorialViewHolderClicked(editorial: Editorial) = editorialClicked.onNext(editorial)
        override fun refresh() = refresh.onNext(null)
        override fun rootCategories(rootCategories: List<Category>) = this.rootCategories.onNext(rootCategories)
        override fun clearPage() = clearPage.onNext(null)
        override fun heartContainerClicked() = heartContainerClicked.onNext(null)
        override fun activitySampleFriendBackingViewHolderProjectClicked(
            viewHolder: ActivitySampleFriendBackingViewHolder,
            project: Project?
        ) = activitySampleProjectClick.onNext(project)
        override fun activitySampleProjectViewHolderProjectClicked(
            viewHolder: ActivitySampleProjectViewHolder,
            project: Project?
        ) = activitySampleProjectClick.onNext(project)
        override fun activitySampleProjectViewHolderUpdateClicked(
            viewHolder: ActivitySampleProjectViewHolder,
            activity: Activity?
        ) =
            activityUpdateClick.onNext(activity)
        override fun discoveryOnboardingViewHolderLoginToutClick(viewHolder: DiscoveryOnboardingViewHolder?) =
            discoveryOnboardingLoginToutClick.onNext(true)
        override fun projectCardViewHolderClicked(project: Project?) = projectCardClicked.onNext(project)
        override fun nextPage() = nextPage.onNext(null)
        override fun paramsFromActivity(params: DiscoveryParams) = paramsFromActivity.onNext(params)

        override fun activity(): Observable<Activity> = activity
        override fun isFetchingProjects(): Observable<Boolean> = isFetchingProjects
        override fun projectList(): Observable<List<Pair<Project, DiscoveryParams>>> = projectList
        override fun showActivityFeed(): Observable<Boolean> = showActivityFeed
        override fun showLoginTout(): Observable<Boolean> = showLoginTout
        override fun shouldShowEditorial(): Observable<Editorial> = shouldShowEditorial
        override fun shouldShowEmptySavedView(): Observable<Boolean> = shouldShowEmptySavedView
        override fun startHeartAnimation(): Observable<Void?> = startHeartAnimation
        override fun startEditorialActivity(): Observable<Editorial> = startEditorialActivity
        override fun startProjectActivity(): Observable<Triple<Project, RefTag, Boolean>> = startProjectActivity
        override fun shouldShowOnboardingView(): Observable<Boolean> = shouldShowOnboardingView
        override fun startUpdateActivity(): Observable<Activity> = startUpdateActivity
        override fun onHeartButtonClicked(project: Project) = onHeartButtonClicked.onNext(project)
        override fun startLoginToutActivityToSaveProject(): Observable<Project> = this.startLoginToutActivityToSaveProject
        override fun scrollToSavedProjectPosition(): Observable<Int> = this.scrollToSavedProjectPosition
        override fun showSavedPrompt(): Observable<Void> = this.showSavedPrompt
    }
}
