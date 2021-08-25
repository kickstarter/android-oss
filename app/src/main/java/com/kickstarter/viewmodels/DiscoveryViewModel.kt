package com.kickstarter.viewmodels

import android.content.Intent
import android.net.Uri
import android.util.Pair
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.BuildCheck
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.CurrentConfigType
import com.kickstarter.libs.preferences.BooleanPreferenceType
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.BooleanUtils
import com.kickstarter.libs.utils.DiscoveryDrawerUtils
import com.kickstarter.libs.utils.DiscoveryUtils
import com.kickstarter.libs.utils.IntegerUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.UrlUtils.appendQueryParameter
import com.kickstarter.libs.utils.extensions.getTokenFromQueryParams
import com.kickstarter.libs.utils.extensions.isPresent
import com.kickstarter.libs.utils.extensions.isVerificationEmailUrl
import com.kickstarter.models.Category
import com.kickstarter.models.QualtricsIntercept
import com.kickstarter.models.QualtricsResult
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.WebClientType
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.services.apiresponses.InternalBuildEnvelope
import com.kickstarter.ui.activities.DiscoveryActivity
import com.kickstarter.ui.adapters.DiscoveryDrawerAdapter
import com.kickstarter.ui.adapters.DiscoveryPagerAdapter
import com.kickstarter.ui.adapters.data.NavigationDrawerData
import com.kickstarter.ui.intentmappers.DiscoveryIntentMapper
import com.kickstarter.ui.viewholders.discoverydrawer.ChildFilterViewHolder
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedInViewHolder
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedOutViewHolder
import com.kickstarter.ui.viewholders.discoverydrawer.ParentFilterViewHolder
import com.kickstarter.ui.viewholders.discoverydrawer.TopFilterViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface DiscoveryViewModel {
    interface Inputs : DiscoveryDrawerAdapter.Delegate, DiscoveryPagerAdapter.Delegate {
        /** Call when a new build is available.  */
        fun newerBuildIsAvailable(envelope: InternalBuildEnvelope)

        /** Call when you want to open or close the drawer.  */
        fun openDrawer(open: Boolean)

        /** Call when the users confirms they want to take the Qualtrics survey.  */
        fun qualtricsConfirmClicked()

        /** Call when the users dismisses the Qualtrics prompt.  */
        fun qualtricsDismissClicked()

        /** Call when you receive a [com.qualtrics.digital.TargetingResult] from Qualtrics.  */
        fun qualtricsResult(qualtricsResult: QualtricsResult)

        /** Call when the user selects a sort tab.  */
        fun sortClicked(sortPosition: Int)
    }

    interface Outputs {
        /** Emits a boolean that determines if the drawer is open or not.  */
        fun drawerIsOpen(): Observable<Boolean>

        /** Emits the drawable resource ID of the drawer menu icon.   */
        fun drawerMenuIcon(): Observable<Int>

        /** Emits a boolean that determines if the sort tab layout should be expanded/collapsed.  */
        fun expandSortTabLayout(): Observable<Boolean>

        /** Emits a boolean that determines if the Qualtrics prompt should be visible.  */
        fun qualtricsPromptIsGone(): Observable<Boolean>

        /** Emits when params change so that the tool bar can adjust accordingly.  */
        fun updateToolbarWithParams(): Observable<DiscoveryParams>

        /** Emits when the params of a particular page should be updated. The page will be responsible for
         * taking those params and creating paginating projects from it.  */
        fun updateParamsForPage(): Observable<DiscoveryParams>
        fun navigationDrawerData(): Observable<NavigationDrawerData>

        /** Emits the root categories and position. Position is used to determine the appropriate fragment
         * to pass the categories to.  */
        fun rootCategoriesAndPosition(): Observable<Pair<List<Category>, Int>>

        /** Emits a list of pages that should be cleared of all their content.  */
        fun clearPages(): Observable<List<Int>>

        /** Emits when we should set up [com.qualtrics.digital.Qualtrics] with first app session boolean.  */
        fun setUpQualtrics(): Observable<Boolean>

        /** Emits when a newer build is available and an alert should be shown.  */
        fun showBuildCheckAlert(): Observable<InternalBuildEnvelope>

        /** Start activity feed activity.  */
        fun showActivityFeed(): Observable<Void?>

        /** Start creator dashboard activity.  */
        fun showCreatorDashboard(): Observable<Void?>

        /** Start help activity.  */
        fun showHelp(): Observable<Void?>

        /** Start internal tools activity.  */
        fun showInternalTools(): Observable<Void?>

        /** Start login tout activity for result.  */
        fun showLoginTout(): Observable<Void?>

        /** Start [com.kickstarter.ui.activities.MessageThreadsActivity].  */
        fun showMessages(): Observable<Void?>

        /** Start profile activity.  */
        fun showProfile(): Observable<Void?>

        /** Start the [com.qualtrics.digital.QualtricsSurveyActivity] with the survey url.  */
        fun showQualtricsSurvey(): Observable<String>

        /** Start settings activity.  */
        fun showSettings(): Observable<Void?>

        /** Emits a [QualtricsIntercept] whose impression count property should be incremented.  */
        fun updateImpressionCount(): Observable<QualtricsIntercept>

        /** Emits the success message from verify endpoint  */
        fun showSuccessMessage(): Observable<String>

        /** Emits the error message from verify endpoint  */
        fun showErrorMessage(): Observable<String?>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<DiscoveryActivity?>(environment), Inputs, Outputs {
        val inputs = this
        val outputs = this

        private val apiClient: ApiClientType = environment.apiClient()
        private val buildCheck: BuildCheck = environment.buildCheck()
        private val currentUserType: CurrentUserType = environment.currentUser()
        private val currentConfigType: CurrentConfigType = environment.currentConfig()
        private val firstSessionPreference: BooleanPreferenceType = environment.firstSessionPreference()
        private val webClient: WebClientType = environment.webClient()

        private fun currentDrawerMenuIcon(user: User?): Int {
            if (ObjectUtils.isNull(user)) {
                return R.drawable.ic_menu
            }
            val erroredBackingsCount = IntegerUtils.intValueOrZero(user?.erroredBackingsCount())
            val unreadMessagesCount = IntegerUtils.intValueOrZero(user?.unreadMessagesCount())
            val unseenActivityCount = IntegerUtils.intValueOrZero(user?.unseenActivityCount())
            return if (!IntegerUtils.isZero(erroredBackingsCount)) {
                R.drawable.ic_menu_error_indicator
            } else if (!IntegerUtils.isZero(unreadMessagesCount + unseenActivityCount + erroredBackingsCount)) {
                R.drawable.ic_menu_indicator
            } else {
                R.drawable.ic_menu
            }
        }

        private val activityFeedClick = PublishSubject.create<Void?>()
        private val childFilterRowClick = PublishSubject.create<NavigationDrawerData.Section.Row?>()
        private val creatorDashboardClick = PublishSubject.create<Void?>()
        private val internalToolsClick = PublishSubject.create<Void?>()
        private val loggedOutLoginToutClick = PublishSubject.create<Void?>()
        private val loggedOutSettingsClick = PublishSubject.create<Void?>()
        private val messagesClick = PublishSubject.create<Void?>()
        private val newerBuildIsAvailable = PublishSubject.create<InternalBuildEnvelope>()
        private val openDrawer = PublishSubject.create<Boolean>()
        private val pagerSetPrimaryPage = PublishSubject.create<Int>()
        private val parentFilterRowClick = PublishSubject.create<NavigationDrawerData.Section.Row>()
        private val profileClick = PublishSubject.create<Void?>()
        private val qualtricsConfirmClicked = PublishSubject.create<Void?>()
        private val qualtricsDismissClicked = PublishSubject.create<Void?>()
        private val qualtricsResult = PublishSubject.create<QualtricsResult>()
        private val settingsClick = PublishSubject.create<Void?>()
        private val sortClicked = PublishSubject.create<Int>()
        private val topFilterRowClick = PublishSubject.create<NavigationDrawerData.Section.Row?>()
        private val clearPages = BehaviorSubject.create<List<Int>>()
        private val drawerIsOpen = BehaviorSubject.create<Boolean>()
        private val drawerMenuIcon = BehaviorSubject.create<Int>()
        private val expandSortTabLayout = BehaviorSubject.create<Boolean>()
        private val navigationDrawerData = BehaviorSubject.create<NavigationDrawerData>()
        private val qualtricsPromptIsGone = BehaviorSubject.create<Boolean>()
        private val rootCategoriesAndPosition = BehaviorSubject.create<Pair<List<Category>, Int>>()
        private val setUpQualtrics = BehaviorSubject.create<Boolean>()
        private val showActivityFeed: Observable<Void?>
        private val showBuildCheckAlert: Observable<InternalBuildEnvelope>
        private val showCreatorDashboard: Observable<Void?>
        private val showHelp: Observable<Void?>
        private val showInternalTools: Observable<Void?>
        private val showLoginTout: Observable<Void?>
        private val showMessages: Observable<Void?>
        private val showProfile: Observable<Void?>
        private val showQualtricsSurvey = PublishSubject.create<String>()
        private val showSettings: Observable<Void?>
        private val updateImpressionCount = PublishSubject.create<QualtricsIntercept>()
        private val updateParamsForPage = BehaviorSubject.create<DiscoveryParams>()
        private val updateToolbarWithParams = BehaviorSubject.create<DiscoveryParams>()
        private val successMessage = PublishSubject.create<String>()
        private val messageError = PublishSubject.create<String?>()

        init {
            buildCheck.bind(this, webClient)
            showActivityFeed = activityFeedClick
            showBuildCheckAlert = newerBuildIsAvailable
            showCreatorDashboard = creatorDashboardClick
            showHelp = loggedOutSettingsClick
            showInternalTools = internalToolsClick
            showLoginTout = loggedOutLoginToutClick
            showMessages = messagesClick
            showProfile = profileClick
            showSettings = settingsClick

            val currentUser = currentUserType.observable()

            val changedUser = currentUser
                .distinctUntilChanged()

            changedUser
                .compose(bindToLifecycle())
                .subscribe {
                    apiClient.config()
                        .compose(Transformers.neverError())
                        .subscribe { currentConfigType.config(it) }
                }

            // Seed params when we are freshly launching the app with no data.
            val paramsFromInitialIntent = intent()
                .take(1)
                .map { it.action }
                .filter { Intent.ACTION_MAIN == it }
                .compose(Transformers.combineLatestPair(changedUser))
                .map { DiscoveryParams.getDefaultParams(it.second) }
                .share()

            val uriFromVerification = intent()
                .map { it.data }
                .ofType(Uri::class.java)
                .filter { it.isVerificationEmailUrl() }

            val verification = uriFromVerification
                .map { it.getTokenFromQueryParams() }
                .filter { ObjectUtils.isNotNull(it) }
                .switchMap { apiClient.verifyEmail(it) }
                .materialize()
                .share()
                .distinctUntilChanged()

            verification
                .compose(Transformers.values())
                .map { it.message() }
                .compose(bindToLifecycle())
                .subscribe(successMessage)

            verification
                .compose(Transformers.errors())
                .map { ErrorEnvelope.fromThrowable(it) }
                .map { it?.errorMessage() }
                .filter { ObjectUtils.isNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe(messageError)

            val paramsFromIntent = intent()
                .flatMap { DiscoveryIntentMapper.params(it, apiClient) }

            val drawerParamsClicked = childFilterRowClick
                .mergeWith(topFilterRowClick)
                .map { it.params() }

            // Merge various param data sources.
            val params = Observable.merge(
                paramsFromInitialIntent,
                paramsFromIntent,
                drawerParamsClicked
            )

            val pagerSelectedPage = pagerSetPrimaryPage.distinctUntilChanged()

            // Combine params with the selected sort position.
            val paramsWithSort = Observable.combineLatest(
                params,
                pagerSelectedPage.map { DiscoveryUtils.sortFromPosition(it) }
            ) { p, s -> p.toBuilder().sort(s).build() }

            paramsWithSort
                .compose(bindToLifecycle())
                .subscribe(updateParamsForPage)

            paramsWithSort
                .compose(Transformers.takePairWhen(sortClicked.map { DiscoveryUtils.sortFromPosition(it) }))
                .map<Pair<DiscoveryParams.Sort, DiscoveryParams>> {
                    Pair.create(
                        it.first.sort(),
                        it.first.toBuilder().sort(it.second).build()
                    )
                }
                .compose(bindToLifecycle())
                .subscribe { analyticEvents.trackDiscoverSortCTA(it.first, it.second) }

            paramsWithSort
                .compose(Transformers.takeWhen(drawerParamsClicked))
                .compose(bindToLifecycle())
                .subscribe {
                    analyticEvents.trackDiscoverFilterCTA(it)
                }

            val categories = apiClient.fetchCategories()
                .compose(Transformers.neverError())
                .flatMapIterable { it }
                .toSortedList()
                .share()

            // Combine root categories with the selected sort position.
            Observable.combineLatest<List<Category>?, Int, Pair<List<Category>, Int>>(
                categories
                    .flatMapIterable { it }
                    .filter { it.isRoot }
                    .toList(),
                pagerSelectedPage
            ) { c, psp -> Pair.create(c, psp) }
                .compose(bindToLifecycle())
                .subscribe(rootCategoriesAndPosition)

            val drawerClickedParentCategory = parentFilterRowClick
                .map { it.params().category() }

            val expandedCategory = Observable.merge(
                topFilterRowClick.map { null },
                drawerClickedParentCategory
            )
                .scan(
                    null,
                    { previous: Category?, next: Category? ->
                        if (previous != null && next != null && previous == next) {
                            return@scan null
                        }
                        next
                    }
                )

            // Accumulate a list of pages to clear when the params or user changes,
            // to avoid displaying old data.
            pagerSelectedPage
                .compose(Transformers.takeWhen(params))
                .compose(Transformers.combineLatestPair(changedUser))
                .map { it.first }
                .flatMap {
                    Observable.from(DiscoveryParams.Sort.defaultSorts)
                        .map { sort: DiscoveryParams.Sort? -> DiscoveryUtils.positionFromSort(sort) }
                        .filter { sortPosition: Int -> sortPosition != it }
                        .toList()
                }
                .compose(bindToLifecycle())
                .subscribe(clearPages)

            params
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(updateToolbarWithParams)

            updateParamsForPage
                .map { true }
                .compose(bindToLifecycle())
                .subscribe(expandSortTabLayout)

            Observable.combineLatest<List<Category>, DiscoveryParams, Category?, User, NavigationDrawerData>(
                categories,
                params,
                expandedCategory,
                currentUser
            ) { c, s, ec, u -> DiscoveryDrawerUtils.deriveNavigationDrawerData(c, s, ec, u) }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(navigationDrawerData)

            val drawerOpenObservables = listOf(
                openDrawer,
                childFilterRowClick.map { false },
                topFilterRowClick.map { false },
                internalToolsClick.map { false },
                loggedOutLoginToutClick.map { false },
                loggedOutSettingsClick.map { false },
                activityFeedClick.map { false },
                messagesClick.map { false },
                creatorDashboardClick.map { false },
                profileClick.map { false },
                settingsClick.map { false }
            )

            Observable.merge(drawerOpenObservables)
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(drawerIsOpen)

            val drawerOpened = openDrawer
                .filter { bool: Boolean? -> BooleanUtils.isTrue(bool) }

            currentUser
                .map { currentDrawerMenuIcon(it) }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe { drawerMenuIcon.onNext(it) }

            Observable.just(firstSessionPreference)
                .map {
                    it.set(!it.isSet)
                    it.get()
                }
                .compose(bindToLifecycle())
                .subscribe { setUpQualtrics.onNext(it) }

            qualtricsResult
                .map { it.resultPassed() }
                .map { BooleanUtils.negate(it) }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(qualtricsPromptIsGone)

            Observable.merge(qualtricsConfirmClicked, qualtricsDismissClicked)
                .map { true }
                .compose(bindToLifecycle())
                .subscribe(qualtricsPromptIsGone)

            val passedQualtricsResult = qualtricsResult
                .filter { it.resultPassed() }
                .distinctUntilChanged()

            passedQualtricsResult
                .map { QualtricsIntercept.NATIVE_APP_FEEDBACK }
                .compose(bindToLifecycle())
                .subscribe(updateImpressionCount)

            passedQualtricsResult
                .map {
                    it.recordImpression()
                    it
                }
                .compose(Transformers.combineLatestPair(currentUser))
                .compose(Transformers.takeWhen(qualtricsConfirmClicked))
                .map {
                    val qualtricsResult = it.first
                    qualtricsResult.recordClick()
                    Pair.create(qualtricsResult.surveyUrl(), it.second)
                }
                .filter { ObjectUtils.isNotNull(it.first) }
                .filter { it.first.isPresent() }
                .map {
                    val surveyUrl = it.first
                    val user = it.second
                    val userLoggedIn = user != null
                    var url = appendQueryParameter(
                        surveyUrl,
                        "logged_in",
                        userLoggedIn.toString()
                    )
                    if (userLoggedIn) {
                        url = appendQueryParameter(
                            url,
                            "user_uid",
                            user.id().toString()
                        )
                    }
                    url
                }
                .compose(bindToLifecycle())
                .subscribe(showQualtricsSurvey)
        }

        override fun childFilterViewHolderRowClick(viewHolder: ChildFilterViewHolder, row: NavigationDrawerData.Section.Row) {
            childFilterRowClick.onNext(row)
        }
        override fun discoveryPagerAdapterSetPrimaryPage(adapter: DiscoveryPagerAdapter, position: Int) {
            pagerSetPrimaryPage.onNext(position)
        }
        override fun loggedInViewHolderActivityClick(viewHolder: LoggedInViewHolder) { activityFeedClick.onNext(null) }
        override fun loggedInViewHolderDashboardClick(viewHolder: LoggedInViewHolder) { creatorDashboardClick.onNext(null) }
        override fun loggedInViewHolderInternalToolsClick(viewHolder: LoggedInViewHolder) { internalToolsClick.onNext(null) }
        override fun loggedInViewHolderMessagesClick(viewHolder: LoggedInViewHolder) { messagesClick.onNext(null) }
        override fun loggedInViewHolderProfileClick(viewHolder: LoggedInViewHolder, user: User) { profileClick.onNext(null) }
        override fun loggedInViewHolderSettingsClick(viewHolder: LoggedInViewHolder, user: User) { settingsClick.onNext(null) }
        override fun loggedOutViewHolderActivityClick(viewHolder: LoggedOutViewHolder) { activityFeedClick.onNext(null) }
        override fun loggedOutViewHolderInternalToolsClick(viewHolder: LoggedOutViewHolder) { internalToolsClick.onNext(null) }
        override fun loggedOutViewHolderLoginToutClick(viewHolder: LoggedOutViewHolder) { loggedOutLoginToutClick.onNext(null) }
        override fun loggedOutViewHolderHelpClick(viewHolder: LoggedOutViewHolder) { loggedOutSettingsClick.onNext(null) }
        override fun topFilterViewHolderRowClick(viewHolder: TopFilterViewHolder, row: NavigationDrawerData.Section.Row) {
            topFilterRowClick.onNext(row)
        }

        // - Inputs
        override fun newerBuildIsAvailable(envelope: InternalBuildEnvelope) { newerBuildIsAvailable.onNext(envelope) }
        override fun openDrawer(open: Boolean) { openDrawer.onNext(open) }
        override fun parentFilterViewHolderRowClick(viewHolder: ParentFilterViewHolder, row: NavigationDrawerData.Section.Row) {
            parentFilterRowClick.onNext(row)
        }
        override fun qualtricsConfirmClicked() { qualtricsConfirmClicked.onNext(null) }
        override fun qualtricsDismissClicked() { qualtricsDismissClicked.onNext(null) }
        override fun qualtricsResult(qualtricsResult: QualtricsResult) { this.qualtricsResult.onNext(qualtricsResult) }
        override fun sortClicked(sortPosition: Int) { sortClicked.onNext(sortPosition) }

        // - Outputs
        override fun clearPages(): Observable<List<Int>> { return clearPages }
        override fun drawerIsOpen(): Observable<Boolean> { return drawerIsOpen }
        override fun drawerMenuIcon(): Observable<Int> { return drawerMenuIcon }
        override fun expandSortTabLayout(): Observable<Boolean> { return expandSortTabLayout }
        override fun navigationDrawerData(): Observable<NavigationDrawerData> { return navigationDrawerData }
        override fun qualtricsPromptIsGone(): Observable<Boolean> { return qualtricsPromptIsGone }
        override fun rootCategoriesAndPosition(): Observable<Pair<List<Category>, Int>> { return rootCategoriesAndPosition }
        override fun setUpQualtrics(): Observable<Boolean> { return setUpQualtrics }
        override fun showActivityFeed(): Observable<Void?> { return showActivityFeed }
        override fun showBuildCheckAlert(): Observable<InternalBuildEnvelope> { return showBuildCheckAlert }
        override fun showCreatorDashboard(): Observable<Void?> { return showCreatorDashboard }
        override fun showHelp(): Observable<Void?> { return showHelp }
        override fun showInternalTools(): Observable<Void?> { return showInternalTools }
        override fun showLoginTout(): Observable<Void?> { return showLoginTout }
        override fun showMessages(): Observable<Void?> { return showMessages }
        override fun showProfile(): Observable<Void?> { return showProfile }
        override fun showQualtricsSurvey(): Observable<String> { return showQualtricsSurvey }
        override fun showSettings(): Observable<Void?> { return showSettings }
        override fun updateImpressionCount(): Observable<QualtricsIntercept> { return updateImpressionCount }
        override fun updateParamsForPage(): Observable<DiscoveryParams> { return updateParamsForPage }
        override fun updateToolbarWithParams(): Observable<DiscoveryParams> { return updateToolbarWithParams }
        override fun showSuccessMessage(): Observable<String> { return successMessage }
        override fun showErrorMessage(): Observable<String?> { return messageError }
    }
}
