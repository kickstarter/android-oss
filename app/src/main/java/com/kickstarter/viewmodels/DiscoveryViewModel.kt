package com.kickstarter.viewmodels

import android.content.Intent
import android.net.Uri
import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.DiscoveryUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.deriveNavigationDrawerData
import com.kickstarter.libs.utils.extensions.getTokenFromQueryParams
import com.kickstarter.libs.utils.extensions.intValueOrZero
import com.kickstarter.libs.utils.extensions.isNonZero
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.libs.utils.extensions.isVerificationEmailUrl
import com.kickstarter.libs.utils.extensions.positionFromSort
import com.kickstarter.models.Category
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.ui.SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE
import com.kickstarter.ui.SharedPreferenceKey.HAS_SEEN_NOTIF_PERMISSIONS
import com.kickstarter.ui.adapters.DiscoveryDrawerAdapter
import com.kickstarter.ui.adapters.DiscoveryPagerAdapter
import com.kickstarter.ui.adapters.data.NavigationDrawerData
import com.kickstarter.ui.intentmappers.DiscoveryIntentMapper
import com.kickstarter.ui.viewholders.discoverydrawer.ChildFilterViewHolder
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedInViewHolder
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedOutViewHolder
import com.kickstarter.ui.viewholders.discoverydrawer.ParentFilterViewHolder
import com.kickstarter.ui.viewholders.discoverydrawer.TopFilterViewHolder
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface DiscoveryViewModel {
    interface Inputs : DiscoveryDrawerAdapter.Delegate, DiscoveryPagerAdapter.Delegate {

        /** Call when the user selects a sort tab.  */
        fun sortClicked(sortPosition: Int)

        /** Call when the user has seen the notifications permission request.  */
        fun hasSeenNotificationsPermission(hasShown: Boolean)
    }

    interface Outputs {
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

        /** Start activity feed activity.  */
        fun showActivityFeed(): Observable<Unit>

        /** Start help activity.  */
        fun showHelp(): Observable<Unit>

        /** Start internal tools activity.  */
        fun showInternalTools(): Observable<Unit>

        /** Start login tout activity for result.  */
        fun showLoginTout(): Observable<Unit>

        /** Start [com.kickstarter.ui.activities.MessageThreadsActivity].  */
        fun showMessages(): Observable<Unit>

        /** Start profile activity.  */
        fun showProfile(): Observable<Unit>

        /** Start pledged projects overview activity.  */
        fun showPledgedProjects(): Observable<Unit>

        /** Start settings activity.  */
        fun showSettings(): Observable<Unit>

        /** Emits the success message from verify endpoint  */
        fun showSuccessMessage(): Observable<String>

        /** Emits if the user should be shown the notification permission request  */
        fun showNotifPermissionsRequest(): Observable<Unit>

        /** Emits if the user should be shown the consent management dialog  */
        fun showConsentManagementDialog(): Observable<Unit>

        /** Emits the error message from verify endpoint  */
        fun showErrorMessage(): Observable<String>
    }

    class DiscoveryViewModel(environment: Environment) : ViewModel(), Inputs, Outputs {
        val inputs = this
        val outputs = this

        private val apiClient = requireNotNull(environment.apiClientV2())
        private val apolloClient = requireNotNull(environment.apolloClientV2())
        private val currentUserType = requireNotNull(environment.currentUserV2())
        private val currentConfigType = requireNotNull(environment.currentConfigV2())
        private val sharedPreferences = requireNotNull(environment.sharedPreferences())
        private val analyticEvents = requireNotNull(environment.analytics())
        private val ffClient = environment.featureFlagClient()

        private fun currentDrawerMenuIcon(user: User?): Int {
            if (user.isNull()) {
                return if (isDarkTheme) R.drawable.ic_menu_dark else R.drawable.ic_menu
            }
            val erroredBackingsCount = user?.erroredBackingsCount().intValueOrZero()
            val unreadMessagesCount = user?.unreadMessagesCount().intValueOrZero()
            val unseenActivityCount = user?.unseenActivityCount().intValueOrZero()

            val ppoHasActions = when (user?.ppoHasAction()) {
                true -> 1
                false, null -> 0
            }

            return when {
                (erroredBackingsCount.isNonZero() || ppoHasActions.isNonZero()) -> {
                    if (isDarkTheme) R.drawable.ic_menu_error_indicator_dark else R.drawable.ic_menu_error_indicator
                }

                (unreadMessagesCount + unseenActivityCount + erroredBackingsCount + ppoHasActions).isNonZero() -> {
                    if (isDarkTheme) R.drawable.ic_menu_indicator_dark else R.drawable.ic_menu_indicator
                }

                else -> if (isDarkTheme) R.drawable.ic_menu_dark else R.drawable.ic_menu
            }
        }

        private val activityFeedClick = PublishSubject.create<Unit>()
        private val childFilterRowClick = PublishSubject.create<NavigationDrawerData.Section.Row>()
        private val internalToolsClick = PublishSubject.create<Unit>()
        private val loggedOutLoginToutClick = PublishSubject.create<Unit>()
        private val loggedOutHelpClick = PublishSubject.create<Unit>()
        private val messagesClick = PublishSubject.create<Unit>()
        private val pagerSetPrimaryPage = PublishSubject.create<Int>()
        private val parentFilterRowClick = PublishSubject.create<NavigationDrawerData.Section.Row>()
        private val profileClick = PublishSubject.create<Unit>()
        private val showNotifPermissionRequest = BehaviorSubject.create<Unit>()
        private val showConsentManagementDialog = BehaviorSubject.create<Unit>()
        private val settingsClick = PublishSubject.create<Unit>()
        private val pledgedProjectsClick = PublishSubject.create<Unit>()
        private val sortClicked = PublishSubject.create<Int>()
        private val hasSeenNotificationsPermission = PublishSubject.create<Boolean>()
        private val topFilterRowClick = PublishSubject.create<NavigationDrawerData.Section.Row>()
        private val clearPages = BehaviorSubject.create<List<Int>>()
        private val drawerMenuIcon = BehaviorSubject.create<Int>()
        private val expandSortTabLayout = BehaviorSubject.create<Boolean>()
        private val navigationDrawerData = BehaviorSubject.create<NavigationDrawerData>()
        private val rootCategoriesAndPosition = BehaviorSubject.create<Pair<List<Category>, Int>>()
        private val showActivityFeed: Observable<Unit>
        private val showHelp: Observable<Unit>
        private val showInternalTools: Observable<Unit>
        private val showLoginTout: Observable<Unit>
        private val showMessages: Observable<Unit>
        private val showProfile: Observable<Unit>
        private val showSettings: Observable<Unit>
        private val showPledgedProjects: Observable<Unit>
        private val updateParamsForPage = BehaviorSubject.create<DiscoveryParams>()
        private val updateToolbarWithParams = BehaviorSubject.create<DiscoveryParams>()
        private val successMessage = PublishSubject.create<String>()
        private val messageError = PublishSubject.create<String>()
        private val darkThemeEnabled = BehaviorSubject.create<Boolean>()
        private val intent = PublishSubject.create<Intent>()
        private val closeDrawer = PublishSubject.create<Unit>()
        private var isDarkTheme = false
        private val disposables = CompositeDisposable()

        init {
            showActivityFeed = activityFeedClick
            showHelp = loggedOutHelpClick
            showInternalTools = internalToolsClick
            showLoginTout = loggedOutLoginToutClick
            showMessages = messagesClick
            showProfile = profileClick
            showSettings = settingsClick
            showPledgedProjects = pledgedProjectsClick

            val currentUser = currentUserType.observable()
                .map { it.getValue() ?: User.builder().build() }
                .filter { it.isNotNull() }
                .map { it }

            val changedUser = currentUser
                .filter { it.isNotNull() }
                .map { it }
                .distinctUntilChanged()

            changedUser
                .subscribe {
                    apiClient.config()
                        .compose(Transformers.neverErrorV2())
                        .subscribe { currentConfigType.config(it) }
                        .addToDisposable(disposables)
                }
                .addToDisposable(disposables)

            val intentObservable = intent.share()

            // Seed params when we are freshly launching the app with no data.
            val paramsFromInitialIntent = intentObservable
                .take(1)
                .filter { it.action.isNotNull() }
                .map { it.action }
                .filter { Intent.ACTION_MAIN == it }
                .compose(Transformers.combineLatestPair(changedUser))
                .map { DiscoveryParams.getDefaultParams(it.second) }
                .share()

            val uriFromVerification = intentObservable
                .filter { it.data.isNotNull() }
                .map { it.data }
                .ofType(Uri::class.java)
                .filter { it.isVerificationEmailUrl() }

            val paramsFromIntent = intentObservable
                .map { it }
                .flatMap { DiscoveryIntentMapper.params(it, apiClient, apolloClient) }

            val verification = uriFromVerification
                .map { it.getTokenFromQueryParams() }
                .filter { it.isNotNull() }
                .switchMap { apiClient.verifyEmail(it) }
                .materialize()
                .share()
                .distinctUntilChanged()

            verification
                .compose(Transformers.valuesV2())
                .map { it.message() }
                .subscribe { successMessage.onNext(it) }
                .addToDisposable(disposables)

            verification
                .compose(Transformers.errorsV2())
                .map { ErrorEnvelope.fromThrowable(it) }
                .map { it.errorMessage() }
                .filter { it.isNotNull() }
                .map { it }
                .subscribe { messageError.onNext(it) }
                .addToDisposable(disposables)

            currentUserType.isLoggedIn
                .filter { it }
                .distinctUntilChanged()
                .take(1)
                .filter { !sharedPreferences.getBoolean(HAS_SEEN_NOTIF_PERMISSIONS, false) }
                .subscribe { showNotifPermissionRequest.onNext(Unit) }
                .addToDisposable(disposables)

            hasSeenNotificationsPermission
                .subscribe { sharedPreferences.edit().putBoolean(HAS_SEEN_NOTIF_PERMISSIONS, it).apply() }
                .addToDisposable(disposables)

            Observable.just(sharedPreferences.contains(CONSENT_MANAGEMENT_PREFERENCE))
                .filter { !it }
                .filter { ffClient?.getBoolean(FlagKey.ANDROID_CONSENT_MANAGEMENT) ?: false }
                .subscribe { showConsentManagementDialog.onNext(Unit) }
                .addToDisposable(disposables)

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
                params.filter { it.sort().isNotNull() }.map { it.sort() }
            ).filter { it.isNotNull() }
                .map { it }

            // Combine params with the selected sort position.
            val paramsWithSort = Observable.combineLatest(
                params,
                sortToTabOpen
            ) { p, s -> p.toBuilder().sort(s).build() }

            paramsWithSort
                .subscribe { updateParamsForPage.onNext(it) }
                .addToDisposable(disposables)

            paramsWithSort
                .compose(Transformers.takePairWhenV2(sortClicked.map { DiscoveryUtils.sortFromPosition(it) }))
                .map<Pair<DiscoveryParams.Sort, DiscoveryParams>> {
                    Pair.create(
                        it.first.sort(),
                        it.first.toBuilder().sort(it.second).build()
                    )
                }
                .subscribe { analyticEvents.trackDiscoverSortCTA(it.first, it.second) }
                .addToDisposable(disposables)

            paramsWithSort
                .compose(Transformers.takeWhenV2(drawerParamsClicked))
                .subscribe {
                    analyticEvents.trackDiscoverFilterCTA(it)
                }
                .addToDisposable(disposables)

            val categories = apolloClient.fetchCategories()
                .compose(Transformers.neverErrorV2())
                .flatMapIterable { it }
                .toSortedList()
                .toObservable()
                .share()

            // Combine root categories with the selected sort position.
            Observable.combineLatest<List<Category>?, Int, Pair<List<Category>, Int>>(
                categories
                    .flatMapIterable { it }
                    .filter { it.isRoot }
                    .toList().toObservable(),
                pagerSelectedPage
            ) { c, psp -> Pair.create(c, psp) }
                .subscribe { rootCategoriesAndPosition.onNext(it) }
                .addToDisposable(disposables)

            val drawerClickedParentCategory = parentFilterRowClick
                .map { it.params().category() ?: Category.builder().build() }
                .startWith(Category.builder().build())

            val expandedCategory = Observable.merge(
                topFilterRowClick.map { Category.builder().build() },
                drawerClickedParentCategory
            ).scan { previous: Category, next: Category ->
                if (previous == next) {
                    return@scan Category.builder().build()
                }
                next
            }

            // Accumulate a list of pages to clear when the params or user changes,
            // to avoid displaying old data.
            pagerSelectedPage
                .compose(Transformers.takeWhenV2(params))
                .compose(Transformers.combineLatestPair(changedUser))
                .map { it.first }
                .flatMap { pageInt ->
                    Observable.just(
                        DiscoveryParams.Sort.defaultSorts
                            .map { it.positionFromSort() }
                            .filter { it != pageInt }
                    )
                }
                .subscribe { clearPages.onNext(it) }
                .addToDisposable(disposables)

            params
                .distinctUntilChanged()
                .subscribe { updateToolbarWithParams.onNext(it) }
                .addToDisposable(disposables)

            updateParamsForPage
                .map { true }
                .subscribe { expandSortTabLayout.onNext(it) }
                .addToDisposable(disposables)

            Observable.combineLatest(
                categories,
                params,
                expandedCategory,
                currentUser
            ) { c, s, ec, u -> s.deriveNavigationDrawerData(c, ec, u) }
                .distinctUntilChanged()
                .subscribe { navigationDrawerData.onNext(it) }
                .addToDisposable(disposables)

            val drawerOpenObservables = listOf(
                childFilterRowClick.map { },
                topFilterRowClick.map { },
                internalToolsClick.map { },
                loggedOutLoginToutClick.map { },
                loggedOutHelpClick.map { },
                activityFeedClick.map { },
                messagesClick.map { },
                profileClick.map { },
                settingsClick.map { },
                pledgedProjectsClick.map { }
            )

            Observable.merge(drawerOpenObservables)
                .subscribe { closeDrawer.onNext(Unit) }
                .addToDisposable(disposables)

            currentUser
                .compose(Transformers.takeWhenV2(darkThemeEnabled))
                .map { currentDrawerMenuIcon(it) }
                .distinctUntilChanged()
                .subscribe { drawerMenuIcon.onNext(it) }
                .addToDisposable(disposables)
        }

        override fun childFilterViewHolderRowClick(viewHolder: ChildFilterViewHolder, row: NavigationDrawerData.Section.Row) {
            childFilterRowClick.onNext(row)
        }
        override fun discoveryPagerAdapterSetPrimaryPage(adapter: DiscoveryPagerAdapter, position: Int) {
            pagerSetPrimaryPage.onNext(position)
        }
        override fun loggedInViewHolderActivityClick(viewHolder: LoggedInViewHolder) { activityFeedClick.onNext(Unit) }
        override fun loggedInViewHolderInternalToolsClick(viewHolder: LoggedInViewHolder) { internalToolsClick.onNext(Unit) }
        override fun loggedInViewHolderMessagesClick(viewHolder: LoggedInViewHolder) { messagesClick.onNext(Unit) }
        override fun loggedInViewHolderProfileClick(viewHolder: LoggedInViewHolder, user: User) { profileClick.onNext(Unit) }
        override fun loggedInViewHolderSettingsClick(viewHolder: LoggedInViewHolder, user: User) { settingsClick.onNext(Unit) }
        override fun loggedInViewHolderPledgedProjectsClick(viewHolder: LoggedInViewHolder) { pledgedProjectsClick.onNext(Unit) }
        override fun loggedOutViewHolderActivityClick(viewHolder: LoggedOutViewHolder) { activityFeedClick.onNext(Unit) }
        override fun loggedOutViewHolderInternalToolsClick(viewHolder: LoggedOutViewHolder) { internalToolsClick.onNext(Unit) }
        override fun loggedOutViewHolderLoginToutClick(viewHolder: LoggedOutViewHolder) { loggedOutLoginToutClick.onNext(Unit) }
        override fun loggedOutViewHolderHelpClick(viewHolder: LoggedOutViewHolder) { loggedOutHelpClick.onNext(Unit) }
        override fun loggedOutViewHolderSettingsClick(viewHolder: LoggedOutViewHolder) { settingsClick.onNext(Unit) }
        override fun topFilterViewHolderRowClick(viewHolder: TopFilterViewHolder, row: NavigationDrawerData.Section.Row) {
            topFilterRowClick.onNext(row)
        }

        // - Inputs
        override fun parentFilterViewHolderRowClick(viewHolder: ParentFilterViewHolder, row: NavigationDrawerData.Section.Row) {
            parentFilterRowClick.onNext(row)
        }
        override fun sortClicked(sortPosition: Int) { sortClicked.onNext(sortPosition) }
        override fun hasSeenNotificationsPermission(hasShown: Boolean) { hasSeenNotificationsPermission.onNext(hasShown) }

        // - Outputs
        override fun clearPages(): Observable<List<Int>> { return clearPages }
        override fun drawerMenuIcon(): Observable<Int> { return drawerMenuIcon }
        override fun expandSortTabLayout(): Observable<Boolean> { return expandSortTabLayout }
        override fun navigationDrawerData(): Observable<NavigationDrawerData> { return navigationDrawerData }
        override fun rootCategoriesAndPosition(): Observable<Pair<List<Category>, Int>> { return rootCategoriesAndPosition }
        override fun showActivityFeed(): Observable<Unit> { return showActivityFeed }
        override fun showHelp(): Observable<Unit> { return showHelp }
        override fun showInternalTools(): Observable<Unit> { return showInternalTools }
        override fun showLoginTout(): Observable<Unit> { return showLoginTout }
        override fun showMessages(): Observable<Unit> { return showMessages }
        override fun showProfile(): Observable<Unit> { return showProfile }
        override fun showPledgedProjects(): Observable<Unit> { return showPledgedProjects }
        override fun showSettings(): Observable<Unit> { return showSettings }
        override fun updateParamsForPage(): Observable<DiscoveryParams> { return updateParamsForPage }
        override fun updateToolbarWithParams(): Observable<DiscoveryParams> { return updateToolbarWithParams }
        override fun showSuccessMessage(): Observable<String> { return successMessage }
        override fun showErrorMessage(): Observable<String> { return messageError }
        override fun showNotifPermissionsRequest(): Observable<Unit> { return showNotifPermissionRequest }
        override fun showConsentManagementDialog(): Observable<Unit> { return showConsentManagementDialog }
        override fun darkThemeEnabled(): Observable<Boolean> { return darkThemeEnabled }
        fun closeDrawer(): Observable<Unit> { return closeDrawer }

        fun setDarkTheme(isDarkTheme: Boolean) {
            this.isDarkTheme = isDarkTheme
            darkThemeEnabled.onNext(isDarkTheme)
        }

        fun provideIntent(intent: Intent) {
            this.intent.onNext(intent)
        }

        override fun onCleared() {
            apolloClient.cleanDisposables()
            disposables.clear()
            super.onCleared()
        }
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DiscoveryViewModel(environment) as T
        }
    }
}
