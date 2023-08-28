package com.kickstarter.viewmodels

import android.content.Intent
import android.net.Uri
import android.util.Pair
import com.kickstarter.R
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.DiscoveryUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.extensions.deriveNavigationDrawerData
import com.kickstarter.libs.utils.extensions.getTokenFromQueryParams
import com.kickstarter.libs.utils.extensions.intValueOrZero
import com.kickstarter.libs.utils.extensions.isNonZero
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.isVerificationEmailUrl
import com.kickstarter.libs.utils.extensions.positionFromSort
import com.kickstarter.models.Category
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.services.apiresponses.InternalBuildEnvelope
import com.kickstarter.ui.SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE
import com.kickstarter.ui.SharedPreferenceKey.HAS_SEEN_NOTIF_PERMISSIONS
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
import java.util.Locale.filter

interface DiscoveryViewModel {
    interface Inputs : DiscoveryDrawerAdapter.Delegate, DiscoveryPagerAdapter.Delegate {
        /** Call when a new build is available.  */
        fun newerBuildIsAvailable(envelope: InternalBuildEnvelope)

        /** Call when you want to open or close the drawer.  */
        fun openDrawer(open: Boolean)

        /** Call when the user selects a sort tab.  */
        fun sortClicked(sortPosition: Int)

        /** Call when the user has seen the notifications permission request.  */
        fun hasSeenNotificationsPermission(hasShown: Boolean)
    }

    interface Outputs {
        /** Emits a boolean that determines if the drawer is open or not.  */
        fun drawerIsOpen(): Observable<Boolean>

        /** Emits the drawable resource ID of the drawer menu icon.   */
        fun drawerMenuIcon(): Observable<Int>

        /** Emits a boolean that determines if the sort tab layout should be expanded/collapsed.  */
        fun expandSortTabLayout(): Observable<Boolean>

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

        /** Emits when a newer build is available and an alert should be shown.  */
        fun showBuildCheckAlert(): Observable<InternalBuildEnvelope>

        /** Start activity feed activity.  */
        fun showActivityFeed(): Observable<Void?>

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

        /** Start settings activity.  */
        fun showSettings(): Observable<Void?>

        /** Emits the success message from verify endpoint  */
        fun showSuccessMessage(): Observable<String>

        /** Emits if the user should be shown the notification permission request  */
        fun showNotifPermissionsRequest(): Observable<Void?>

        /** Emits if the user should be shown the consent management dialog  */
        fun showConsentManagementDialog(): Observable<Void?>

        /** Emits the error message from verify endpoint  */
        fun showErrorMessage(): Observable<String?>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<DiscoveryActivity?>(environment), Inputs, Outputs {
        val inputs = this
        val outputs = this

        private val apiClient = requireNotNull(environment.apiClient())
        private val apolloClient = requireNotNull(environment.apolloClient())
        private val buildCheck = requireNotNull(environment.buildCheck())
        private val currentUserType = requireNotNull(environment.currentUser())
        private val currentConfigType = requireNotNull(environment.currentConfig())
        private val sharedPreferences = requireNotNull(environment.sharedPreferences())
        private val webClient = requireNotNull(environment.webClient())
        private val ffClient = environment.featureFlagClient()

        private fun currentDrawerMenuIcon(user: User?): Int {
            if (user.isNull()) {
                return R.drawable.ic_menu
            }
            val erroredBackingsCount = user?.erroredBackingsCount().intValueOrZero()
            val unreadMessagesCount = user?.unreadMessagesCount().intValueOrZero()
            val unseenActivityCount = user?.unseenActivityCount().intValueOrZero()
            return when {
                erroredBackingsCount.isNonZero() -> R.drawable.ic_menu_error_indicator
                (unreadMessagesCount + unseenActivityCount + erroredBackingsCount).isNonZero() -> R.drawable.ic_menu_indicator
                else -> R.drawable.ic_menu
            }
        }

        private val activityFeedClick = PublishSubject.create<Void?>()
        private val childFilterRowClick = PublishSubject.create<NavigationDrawerData.Section.Row?>()
        private val internalToolsClick = PublishSubject.create<Void?>()
        private val loggedOutLoginToutClick = PublishSubject.create<Void?>()
        private val loggedOutSettingsClick = PublishSubject.create<Void?>()
        private val messagesClick = PublishSubject.create<Void?>()
        private val newerBuildIsAvailable = PublishSubject.create<InternalBuildEnvelope>()
        private val openDrawer = PublishSubject.create<Boolean>()
        private val pagerSetPrimaryPage = PublishSubject.create<Int>()
        private val parentFilterRowClick = PublishSubject.create<NavigationDrawerData.Section.Row>()
        private val profileClick = PublishSubject.create<Void?>()
        private val showNotifPermissionRequest = BehaviorSubject.create<Void?>()
        private val showConsentManagementDialog = BehaviorSubject.create<Void?>()
        private val settingsClick = PublishSubject.create<Void?>()
        private val sortClicked = PublishSubject.create<Int>()
        private val hasSeenNotificationsPermission = PublishSubject.create<Boolean>()
        private val topFilterRowClick = PublishSubject.create<NavigationDrawerData.Section.Row?>()
        private val clearPages = BehaviorSubject.create<List<Int>>()
        private val drawerIsOpen = BehaviorSubject.create<Boolean>()
        private val drawerMenuIcon = BehaviorSubject.create<Int>()
        private val expandSortTabLayout = BehaviorSubject.create<Boolean>()
        private val navigationDrawerData = BehaviorSubject.create<NavigationDrawerData>()
        private val rootCategoriesAndPosition = BehaviorSubject.create<Pair<List<Category>, Int>>()
        private val showActivityFeed: Observable<Void?>
        private val showBuildCheckAlert: Observable<InternalBuildEnvelope>
        private val showHelp: Observable<Void?>
        private val showInternalTools: Observable<Void?>
        private val showLoginTout: Observable<Void?>
        private val showMessages: Observable<Void?>
        private val showProfile: Observable<Void?>
        private val showSettings: Observable<Void?>
        private val updateParamsForPage = BehaviorSubject.create<DiscoveryParams>()
        private val updateToolbarWithParams = BehaviorSubject.create<DiscoveryParams>()
        private val successMessage = PublishSubject.create<String>()
        private val messageError = PublishSubject.create<String?>()

        init {
            buildCheck.bind(this, webClient)
            showActivityFeed = activityFeedClick
            showBuildCheckAlert = newerBuildIsAvailable
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

            currentUserType.isLoggedIn
                .filter { it }
                .distinctUntilChanged()
                .take(1)
                .filter { !sharedPreferences.getBoolean(HAS_SEEN_NOTIF_PERMISSIONS, false) }
                .compose(bindToLifecycle())
                .subscribe { showNotifPermissionRequest.onNext(null) }

            hasSeenNotificationsPermission
                .compose(bindToLifecycle())
                .subscribe { sharedPreferences.edit().putBoolean(HAS_SEEN_NOTIF_PERMISSIONS, it).apply() }

            Observable.just(sharedPreferences.contains(CONSENT_MANAGEMENT_PREFERENCE))
                .filter { !it }
                .filter { ffClient?.getBoolean(FlagKey.ANDROID_CONSENT_MANAGEMENT) }
                .compose(bindToLifecycle())
                .subscribe { showConsentManagementDialog.onNext(null) }

            val paramsFromIntent = intent()
                .flatMap { DiscoveryIntentMapper.params(it, apiClient, apolloClient) }

            val pagerSelectedPage = pagerSetPrimaryPage.distinctUntilChanged()

            val drawerParamsClicked = childFilterRowClick
                .mergeWith(topFilterRowClick)
                .withLatestFrom(
                    pagerSelectedPage.map { DiscoveryUtils.sortFromPosition(it) }
                ) { drawerClickParams, currentParams ->
                    if (drawerClickParams.params().sort() == null)
                        drawerClickParams.params().toBuilder().sort(currentParams).build()
                    else drawerClickParams.params()
                }

            // Merge various param data sources.
            val params = Observable.merge(
                paramsFromInitialIntent,
                paramsFromIntent,
                drawerParamsClicked
            )

            val sortToTabOpen = Observable.merge(
                pagerSelectedPage.map { DiscoveryUtils.sortFromPosition(it) },
                params.map { it.sort() }
            )
                .filter { ObjectUtils.isNotNull(it) }

            // Combine params with the selected sort position.
            val paramsWithSort = Observable.combineLatest(
                params,
                sortToTabOpen
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

            val categories = apolloClient.fetchCategories()
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
                    null
                ) { previous: Category?, next: Category? ->
                    if (previous != null && next != null && previous == next) {
                        return@scan null
                    }
                    next
                }

            // Accumulate a list of pages to clear when the params or user changes,
            // to avoid displaying old data.
            pagerSelectedPage
                .compose(Transformers.takeWhen(params))
                .compose(Transformers.combineLatestPair(changedUser))
                .map { it.first }
                .flatMap {
                    Observable.from(DiscoveryParams.Sort.defaultSorts)
                        .map { sort: DiscoveryParams.Sort? -> sort.positionFromSort() }
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
            ) { c, s, ec, u -> s.deriveNavigationDrawerData(c, ec, u) }
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
                profileClick.map { false },
                settingsClick.map { false }
            )

            Observable.merge(drawerOpenObservables)
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(drawerIsOpen)

            val drawerOpened = openDrawer
                .filter { bool: Boolean? -> bool.isTrue() }

            currentUser
                .map { currentDrawerMenuIcon(it) }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe { drawerMenuIcon.onNext(it) }
        }

        override fun childFilterViewHolderRowClick(viewHolder: ChildFilterViewHolder, row: NavigationDrawerData.Section.Row) {
            childFilterRowClick.onNext(row)
        }
        override fun discoveryPagerAdapterSetPrimaryPage(adapter: DiscoveryPagerAdapter, position: Int) {
            pagerSetPrimaryPage.onNext(position)
        }
        override fun loggedInViewHolderActivityClick(viewHolder: LoggedInViewHolder) { activityFeedClick.onNext(null) }
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
        override fun sortClicked(sortPosition: Int) { sortClicked.onNext(sortPosition) }
        override fun hasSeenNotificationsPermission(hasShown: Boolean) { hasSeenNotificationsPermission.onNext(hasShown) }

        // - Outputs
        override fun clearPages(): Observable<List<Int>> { return clearPages }
        override fun drawerIsOpen(): Observable<Boolean> { return drawerIsOpen }
        override fun drawerMenuIcon(): Observable<Int> { return drawerMenuIcon }
        override fun expandSortTabLayout(): Observable<Boolean> { return expandSortTabLayout }
        override fun navigationDrawerData(): Observable<NavigationDrawerData> { return navigationDrawerData }
        override fun rootCategoriesAndPosition(): Observable<Pair<List<Category>, Int>> { return rootCategoriesAndPosition }
        override fun showActivityFeed(): Observable<Void?> { return showActivityFeed }
        override fun showBuildCheckAlert(): Observable<InternalBuildEnvelope> { return showBuildCheckAlert }
        override fun showHelp(): Observable<Void?> { return showHelp }
        override fun showInternalTools(): Observable<Void?> { return showInternalTools }
        override fun showLoginTout(): Observable<Void?> { return showLoginTout }
        override fun showMessages(): Observable<Void?> { return showMessages }
        override fun showProfile(): Observable<Void?> { return showProfile }
        override fun showSettings(): Observable<Void?> { return showSettings }
        override fun updateParamsForPage(): Observable<DiscoveryParams> { return updateParamsForPage }
        override fun updateToolbarWithParams(): Observable<DiscoveryParams> { return updateToolbarWithParams }
        override fun showSuccessMessage(): Observable<String> { return successMessage }
        override fun showErrorMessage(): Observable<String?> { return messageError }
        override fun showNotifPermissionsRequest(): Observable<Void?> { return showNotifPermissionRequest }
        override fun showConsentManagementDialog(): Observable<Void?> { return showConsentManagementDialog }
    }
}
