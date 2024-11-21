package com.kickstarter.viewmodels

import android.util.Pair
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.loadmore.ApolloPaginateV2
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.DISCOVER
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.libs.utils.RefTagUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.combineProjectsAndParams
import com.kickstarter.libs.utils.extensions.fillRootCategoryForFeaturedProjects
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.updateStartedProjectAndDiscoveryParamsList
import com.kickstarter.models.Activity
import com.kickstarter.models.Category
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.apiresponses.DiscoverEnvelope
import com.kickstarter.ui.adapters.DiscoveryActivitySampleAdapter
import com.kickstarter.ui.adapters.DiscoveryEditorialAdapter
import com.kickstarter.ui.adapters.DiscoveryOnboardingAdapter
import com.kickstarter.ui.adapters.DiscoveryProjectCardAdapter
import com.kickstarter.ui.data.Editorial
import com.kickstarter.ui.data.ProjectData.Companion.builder
import com.kickstarter.ui.viewholders.ActivitySampleFriendBackingViewHolder
import com.kickstarter.ui.viewholders.ActivitySampleFriendFollowViewHolder
import com.kickstarter.ui.viewholders.ActivitySampleProjectViewHolder
import com.kickstarter.ui.viewholders.DiscoveryOnboardingViewHolder
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface DiscoveryFragmentViewModel {
    interface Inputs :
        DiscoveryProjectCardAdapter.Delegate,
        DiscoveryOnboardingAdapter.Delegate,
        DiscoveryEditorialAdapter.Delegate,
        DiscoveryActivitySampleAdapter.Delegate {

        fun fragmentLifeCycle(lifecycleEvent: Lifecycle.State)

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

        /** Emits when activities should be cleared from selection. */
        fun clearActivities(): Observable<Unit>

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
        fun startHeartAnimation(): Observable<Unit>

        /** Emits an Editorial when we should start the [com.kickstarter.ui.activities.EditorialActivity].  */
        fun startEditorialActivity(): Observable<Editorial>

        /** Emits a Project and RefTag pair when we should start the [com.kickstarter.ui.activities.ProjectActivity].  */
        fun startProjectActivity(): Observable<Pair<Project, RefTag>>

        /** Emits a Project and RefTag pair when we should start the [com.kickstarter.ui.activities.PreLaunchProjectPageActivity].  */
        fun startPreLaunchProjectActivity(): Observable<Pair<Project, RefTag>>

        /** Emits an activity when we should start the [com.kickstarter.ui.activities.UpdateActivity].  */
        fun startUpdateActivity(): Observable<Activity>

        /** Emits when we should start [com.kickstarter.ui.activities.LoginToutActivity].  */
        fun startLoginToutActivityToSaveProject(): Observable<Project>

        /** Emits when we need to scroll to saved project */
        fun scrollToSavedProjectPosition(): Observable<Int>

        /** Emits when the success prompt for saving should be displayed.  */
        fun showSavedPrompt(): Observable<Unit>

        /** Emits when the setPassword should be started.  */
        fun startSetPasswordActivity(): Observable<String>
    }

    class DiscoveryFragmentViewModel(environment: Environment) :
        ViewModel(),
        Inputs,
        Outputs {
        private val apiClient = requireNotNull(environment.apiClientV2())
        private val apolloClient = requireNotNull(environment.apolloClientV2())
        private val activitySamplePreference = environment.activitySamplePreference()
        private val ffClient = requireNotNull(environment.featureFlagClient())
        private val sharedPreferences = requireNotNull(environment.sharedPreferences())
        private val cookieManager = requireNotNull(environment.cookieManager())
        private val currentUser = requireNotNull(environment.currentUserV2())
        private val lifecycleObservable = BehaviorSubject.create<Lifecycle.State>()
        private val featureFlagClient = environment.featureFlagClient()
        private val analyticEvents = requireNotNull(environment.analytics())

        @JvmField
        val inputs: Inputs = this

        @JvmField
        val outputs: Outputs = this

        private val activityClick = PublishSubject.create<Boolean>()
        private val activitySampleProjectClick = PublishSubject.create<Project>()
        private val activityUpdateClick = PublishSubject.create<Activity>()
        private val clearPage = PublishSubject.create<Unit>()
        private val discoveryOnboardingLoginToutClick = PublishSubject.create<Boolean>()
        private val editorialClicked = PublishSubject.create<Editorial>()
        private val nextPage = PublishSubject.create<Unit>()
        private val paramsFromActivity = PublishSubject.create<DiscoveryParams>()
        private val projectCardClicked = PublishSubject.create<Project>()
        private val onHeartButtonClicked = PublishSubject.create<Project>()
        private val refresh = PublishSubject.create<Unit>()
        private val rootCategories = PublishSubject.create<List<Category>>()
        private val activity = BehaviorSubject.create<Activity>()
        private val clearActivities = BehaviorSubject.create<Unit>()
        private val heartContainerClicked = BehaviorSubject.create<Unit>()
        private val isFetchingProjects: BehaviorSubject<Boolean> = BehaviorSubject.create()
        private val projectList = BehaviorSubject.create<List<Pair<Project, DiscoveryParams>>>()
        private val showActivityFeed: Observable<Boolean>
        private val showLoginTout: Observable<Boolean>
        private val shouldShowEditorial = BehaviorSubject.create<Editorial?>()
        private val shouldShowEmptySavedView = BehaviorSubject.create<Boolean>()
        private val shouldShowOnboardingView = BehaviorSubject.create<Boolean>()
        private val startSetPasswordActivity = BehaviorSubject.create<String>()
        private val startEditorialActivity = PublishSubject.create<Editorial>()
        private val startPreLaunchProjectActivity = PublishSubject.create<Pair<Project, RefTag>>()
        private val startProjectActivity = PublishSubject.create<Pair<Project, RefTag>>()
        private val startUpdateActivity: Observable<Activity>
        private val startHeartAnimation = BehaviorSubject.create<Unit>()
        private val startLoginToutActivityToSaveProject = PublishSubject.create<Project>()
        private val scrollToSavedProjectPosition = PublishSubject.create<Int>()
        private val showSavedPrompt = PublishSubject.create<Unit>()

        private val disposables = CompositeDisposable()

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
                selectedParams.compose(Transformers.takeWhenV2(refresh))
            )

            val paginator = ApolloPaginateV2.builder<Project, DiscoverEnvelope, DiscoveryParams?>()
                .nextPage(nextPage)
                .distinctUntilChanged(true)
                .startOverWith(startOverWith)
                .envelopeToListOfData { it.projects() }
                .loadWithParams { params ->
                    params.first?.let {
                        makeCallWithParams(
                            Pair(it, params.second)
                        )
                    }
                }
                .clearWhenStartingOver(false)
                .concater { xs, ys ->
                    ListUtils.concatDistinct(xs, ys)
                }
                .build()

            paginator.isFetching
                .subscribe { isFetchingProjects.onNext(it) }
                .addToDisposable(disposables)

            projectList
                .compose(Transformers.ignoreValuesV2())
                .subscribe { isFetchingProjects.onNext(false) }
                .addToDisposable(disposables)

            val activitySampleProjectClick = activitySampleProjectClick
                .map<Pair<Project, RefTag>> {
                    Pair.create(
                        it,
                        RefTag.activitySample()
                    )
                }

            projectCardClicked
                .subscribe {
                    analyticEvents.trackProjectCardClicked(it, DISCOVER.contextName)
                }
                .addToDisposable(disposables)

            paramsFromActivity
                .compose(Transformers.takePairWhenV2(projectCardClicked))
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
                .addToDisposable(disposables)

            val projectCardClick = paramsFromActivity
                .compose(Transformers.takePairWhenV2(projectCardClicked))
                .map {
                    RefTagUtils.projectAndRefTagFromParamsAndProject(it.first, it.second)
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
                combineProjectsAndParams(
                    projects,
                    params
                )
            }
                .filter { it.isNotNull() }
                .map { it }
                .subscribe {
                    projectList.onNext(it.toList())
                }.addToDisposable(disposables)

            showActivityFeed = activityClick
            startUpdateActivity = activityUpdateClick
            showLoginTout = discoveryOnboardingLoginToutClick

            val startProject = Observable.merge(
                activitySampleProjectClick,
                projectCardClick
            )

            startProject.subscribe {
                if (it.first.displayPrelaunch() == true &&
                    ffClient.getBoolean(FlagKey.ANDROID_PRE_LAUNCH_SCREEN)
                ) {
                    startPreLaunchProjectActivity.onNext(it)
                } else {
                    startProjectActivity.onNext(it)
                }
            }.addToDisposable(disposables)

            clearPage
                .subscribe {
                    shouldShowOnboardingView.onNext(false)
                    clearActivities.onNext(Unit)
                    projectList.onNext(emptyList())
                }.addToDisposable(disposables)

            currentUser.observable()
                .compose(Transformers.combineLatestPair(paramsFromActivity))
                .filter { it.first.isPresent() }
                .map { Pair(requireNotNull(it.first.getValue()), it.second) }
                .map { defaultParamsAndEnabled: Pair<User, DiscoveryParams> ->
                    isDefaultParams(
                        defaultParamsAndEnabled
                    ) && defaultParamsAndEnabled.second.tagId() == Editorial.LIGHTS_ON.tagId
                }
                .filter { shouldShow -> shouldShow }
                .map {
                    Editorial.LIGHTS_ON
                }
                .subscribe { shouldShowEditorial.onNext(it) }
                .addToDisposable(disposables)

            editorialClicked
                .subscribe { startEditorialActivity.onNext(it) }
                .addToDisposable(disposables)

            paramsFromActivity
                .compose(Transformers.combineLatestPair(userIsLoggedIn))
                .map { pu: Pair<DiscoveryParams, Boolean> ->
                    isOnboardingVisible(
                        pu.first,
                        pu.second
                    )
                }
                .subscribe { shouldShowOnboardingView.onNext(it) }
                .addToDisposable(disposables)

            paramsFromActivity
                .compose(Transformers.combineLatestPair(userIsLoggedIn))
                .filter {
                    it.second && featureFlagClient?.getBoolean(
                        FlagKey.ANDROID_FACEBOOK_LOGIN_REMOVE
                    ) == true
                }
                .compose(Transformers.combineLatestPair(currentUser.loggedInUser()))
                .filter {
                    it.second.needsPassword() == true
                }
                .switchMap {
                    fetchUserEmail()
                }
                .distinctUntilChanged()
                .subscribe {
                    startSetPasswordActivity.onNext(it)
                }.addToDisposable(disposables)

            paramsFromActivity
                .map { params: DiscoveryParams -> isSavedVisible(params) }
                .compose(Transformers.combineLatestPair(projectList))
                .map { it.first && it.second.isEmpty() }
                .distinctUntilChanged()
                .subscribe { shouldShowEmptySavedView.onNext(it) }
                .addToDisposable(disposables)

            shouldShowEmptySavedView
                .filter { it.isTrue() }
                .map<Any?> { }
                .mergeWith(heartContainerClicked)
                .subscribe { startHeartAnimation.onNext(Unit) }
                .addToDisposable(disposables)

            val loggedInUserAndParams = currentUser.loggedInUser()
                .distinctUntilChanged()
                .compose(Transformers.combineLatestPair(paramsFromActivity.distinctUntilChanged()))

            // Activity should show on the user's default params
            loggedInUserAndParams
                .filter {
                    isDefaultParams(
                        it
                    )
                }
                .switchMap {
                    fetchActivity()
                }
                .filter { activityHasNotBeenSeen(it) }
                .doOnNext { saveLastSeenActivityId(it) }
                .subscribe { activity.onNext(it) }
                .addToDisposable(disposables)

            // Clear activity sample when params change from default
            loggedInUserAndParams
                .filter {
                    !isDefaultParams(it)
                }
                .subscribe { clearActivities.onNext(Unit) }
                .addToDisposable(disposables)

            paramsFromActivity
                .compose(
                    Transformers.combineLatestPair(
                        this.lifecycleObservable
                    )
                )
                .filter {
                    it.second == Lifecycle.State.RESUMED
                }
                .distinctUntilChanged()
                .subscribe {
                    analyticEvents.trackDiscoveryPageViewed(it.first)
                }.addToDisposable(disposables)

            discoveryOnboardingLoginToutClick
                .subscribe {
                    analyticEvents.trackLoginOrSignUpCtaClicked(
                        null,
                        DISCOVER.contextName
                    )
                }.addToDisposable(disposables)

            val loggedInUserOnHeartClick = userIsLoggedIn
                .compose(Transformers.takePairWhenV2(this.onHeartButtonClicked))
                .filter { it.first == true }

            val loggedOutUserOnHeartClick = userIsLoggedIn
                .compose(Transformers.takePairWhenV2(this.onHeartButtonClicked))
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
                }.addToDisposable(disposables)

            val savedProjectOnLoginSuccess = this.startLoginToutActivityToSaveProject
                .compose(Transformers.combineLatestPair(this.currentUser.observable()))
                .filter { su ->
                    su.second.isPresent()
                }.take(1)
                .switchMap {
                    this.saveProject(it.first)
                }
                .share()

            this.projectList
                .compose(Transformers.takePairWhenV2(projectOnUserChangeSave))
                .map {
                    it.second.updateStartedProjectAndDiscoveryParamsList(it.first)
                }
                .distinctUntilChanged()
                .subscribe {
                    this.projectList.onNext(it)
                }.addToDisposable(disposables)

            this.projectList
                .compose(Transformers.takePairWhenV2(savedProjectOnLoginSuccess))
                .map {
                    it.first.indexOfFirst { item ->
                        item.first.id() == it.second.id() && item.first.slug() == it.second.slug()
                    }
                }
                .distinctUntilChanged()
                .subscribe {
                    scrollToSavedProjectPosition.onNext(it)
                }.addToDisposable(disposables)

            val projectSavedStatus = projectOnUserChangeSave.mergeWith(savedProjectOnLoginSuccess)

            projectSavedStatus
                .subscribe { this.analyticEvents.trackWatchProjectCTA(it, DISCOVER) }
                .addToDisposable(disposables)

            projectSavedStatus
                .filter { p -> p.isStarred() && p.isLive && !p.isApproachingDeadline }
                .compose(Transformers.ignoreValuesV2())
                .subscribe { this.showSavedPrompt.onNext(it) }
                .addToDisposable(disposables)
        }

        override fun onCleared() {
            apolloClient.cleanDisposables()
            disposables.clear()
            super.onCleared()
        }

        /**
         * Calls to GraphQL client to fetch projects filtering by DiscoveryParams
         * @param discoveryParamsStringPair .first discovery params.
         * @param discoveryParamsStringPair .second cursor for pagination, null on the first call.
         * @return Observable<DiscoverEnvelope>
         </DiscoverEnvelope> */
        private fun makeCallWithParams(discoveryParamsStringPair: Pair<DiscoveryParams, String?>): Observable<DiscoverEnvelope> {
            return apolloClient.getProjects(
                discoveryParamsStringPair.first,
                discoveryParamsStringPair.second
            ).compose(Transformers.neverErrorV2())
        }

        private fun activityHasNotBeenSeen(activity: Activity?): Boolean {
            return activity != null && activity.id() != activitySamplePreference?.get()?.toLong()
        }

        private fun fetchActivity(): Observable<Activity> {
            return apiClient.fetchActivities(1)
                .distinctUntilChanged()
                .materialize()
                .share()
                .filter { it.value.isNotNull() }
                .map { it.value }
                .map { it.activities() }
                .filter { it.isNotEmpty() }
                .map { it.first() }
                .onErrorResumeNext(Observable.empty())
        }

        private fun fetchUserEmail(): Observable<String> {
            return this.apolloClient.userPrivacy()
                .distinctUntilChanged()
                .materialize()
                .share()
                .filter { it.value.isNotNull() }
                .map { it.value }
                .map { it.email }
                .filter { it.isNotNull() }
                .onErrorResumeNext(Observable.empty())
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
                activitySamplePreference?.set(activity.id().toInt())
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

        override fun activitySampleFriendBackingViewHolderSeeActivityClicked(viewHolder: ActivitySampleFriendBackingViewHolder?) =
            activityClick.onNext(true)
        override fun activitySampleFriendFollowViewHolderSeeActivityClicked(viewHolder: ActivitySampleFriendFollowViewHolder?) =
            activityClick.onNext(true)
        override fun activitySampleProjectViewHolderSeeActivityClicked(viewHolder: ActivitySampleProjectViewHolder?) =
            activityClick.onNext(true)
        override fun editorialViewHolderClicked(editorial: Editorial) = editorialClicked.onNext(editorial)
        override fun refresh() = refresh.onNext(Unit)
        override fun rootCategories(rootCategories: List<Category>) = this.rootCategories.onNext(rootCategories)
        override fun clearPage() = clearPage.onNext(Unit)
        override fun heartContainerClicked() = heartContainerClicked.onNext(Unit)
        override fun activitySampleFriendBackingViewHolderProjectClicked(
            viewHolder: ActivitySampleFriendBackingViewHolder,
            project: Project?
        ) {
            project?.let { activitySampleProjectClick.onNext(it) }
        }
        override fun activitySampleProjectViewHolderProjectClicked(
            viewHolder: ActivitySampleProjectViewHolder,
            project: Project?
        ) {
            project?.let { activitySampleProjectClick.onNext(it) }
        }
        override fun activitySampleProjectViewHolderUpdateClicked(
            viewHolder: ActivitySampleProjectViewHolder?,
            activity: Activity
        ) =
            activityUpdateClick.onNext(activity)
        override fun discoveryOnboardingViewHolderLoginToutClick(viewHolder: DiscoveryOnboardingViewHolder?) =
            discoveryOnboardingLoginToutClick.onNext(true)
        override fun projectCardViewHolderClicked(project: Project) = projectCardClicked.onNext(project)
        override fun nextPage() = nextPage.onNext(Unit)
        override fun paramsFromActivity(params: DiscoveryParams) = paramsFromActivity.onNext(params)
        override fun fragmentLifeCycle(lifecycleEvent: Lifecycle.State) =
            this.lifecycleObservable.onNext(lifecycleEvent)

        override fun activity(): Observable<Activity> = activity
        override fun clearActivities(): Observable<Unit> = clearActivities
        override fun isFetchingProjects(): Observable<Boolean> = isFetchingProjects
        override fun projectList(): Observable<List<Pair<Project, DiscoveryParams>>> = projectList
        override fun showActivityFeed(): Observable<Boolean> = showActivityFeed
        override fun showLoginTout(): Observable<Boolean> = showLoginTout
        override fun shouldShowEditorial(): Observable<Editorial> = shouldShowEditorial
        override fun shouldShowEmptySavedView(): Observable<Boolean> = shouldShowEmptySavedView
        override fun startHeartAnimation(): Observable<Unit> = startHeartAnimation
        override fun startEditorialActivity(): Observable<Editorial> = startEditorialActivity
        override fun startProjectActivity(): Observable<Pair<Project, RefTag>> = startProjectActivity
        override fun startPreLaunchProjectActivity(): Observable<Pair<Project, RefTag>> = startPreLaunchProjectActivity
        override fun shouldShowOnboardingView(): Observable<Boolean> = shouldShowOnboardingView
        override fun startUpdateActivity(): Observable<Activity> = startUpdateActivity
        override fun onHeartButtonClicked(project: Project) = onHeartButtonClicked.onNext(project)
        override fun startLoginToutActivityToSaveProject(): Observable<Project> = this.startLoginToutActivityToSaveProject
        override fun scrollToSavedProjectPosition(): Observable<Int> = this.scrollToSavedProjectPosition
        override fun showSavedPrompt(): Observable<Unit> = this.showSavedPrompt
        override fun startSetPasswordActivity(): Observable<String> = this.startSetPasswordActivity
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DiscoveryFragmentViewModel(environment) as T
        }
    }
}
