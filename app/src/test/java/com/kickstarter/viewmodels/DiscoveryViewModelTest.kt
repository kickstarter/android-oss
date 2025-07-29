package com.kickstarter.viewmodels

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.EventName
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.fromJson
import com.kickstarter.libs.utils.extensions.positionFromSort
import com.kickstarter.mock.MockFeatureFlagClient
import com.kickstarter.mock.factories.ApiExceptionFactory
import com.kickstarter.mock.factories.CategoryFactory.artCategory
import com.kickstarter.mock.factories.CategoryFactory.musicCategory
import com.kickstarter.mock.factories.UserFactory.noRecommendations
import com.kickstarter.mock.factories.UserFactory.user
import com.kickstarter.mock.services.MockApiClientV2
import com.kickstarter.models.Category
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.apiresponses.EmailVerificationEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.adapters.DiscoveryPagerAdapter
import com.kickstarter.ui.adapters.data.NavigationDrawerData
import com.kickstarter.ui.viewholders.discoverydrawer.ChildFilterViewHolder
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedInViewHolder
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedOutViewHolder
import com.kickstarter.ui.viewholders.discoverydrawer.TopFilterViewHolder
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test
import org.mockito.Mockito

class DiscoveryViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: DiscoveryViewModel.DiscoveryViewModel
    private val clearPages = TestSubscriber<List<Int>>()
    private val drawerMenuIcon = TestSubscriber<Int>()
    private val expandSortTabLayout = TestSubscriber<Boolean>()
    private val navigationDrawerDataEmitted = TestSubscriber<Unit>()
    private val position = TestSubscriber<Int>()
    private val rootCategories = TestSubscriber<List<Category>>()
    private val rotatedExpandSortTabLayout = TestSubscriber<Boolean>()
    private val rotatedUpdatePage = TestSubscriber<Int>()
    private val rotatedUpdateParams = TestSubscriber<DiscoveryParams>()
    private val rotatedUpdateToolbarWithParams = TestSubscriber<DiscoveryParams>()
    private val showActivityFeed = TestSubscriber<Unit>()
    private val showHelp = TestSubscriber<Unit>()
    private val showInternalTools = TestSubscriber<Unit>()
    private val showLoginTout = TestSubscriber<Unit>()
    private val showMessages = TestSubscriber<Unit>()
    private val showProfile = TestSubscriber<Unit>()
    private val showSettings = TestSubscriber<Unit>()
    private val showPledgedProjects = TestSubscriber<Unit>()
    private val updatePage = TestSubscriber<Int>()
    private val updateParams = TestSubscriber<DiscoveryParams>()
    private val updateToolbarWithParams = TestSubscriber<DiscoveryParams>()
    private val showSuccessMessage = TestSubscriber<String>()
    private val showErrorMessage = TestSubscriber<String>()
    private val showNotifPermissionRequest = TestSubscriber<Unit>()
    private val showConsentManagementDialog = TestSubscriber<Unit>()
    private val showOnboardingFlow = TestSubscriber<Unit>()
    private val darkThemeEnabled = TestSubscriber<Boolean>()
    private val exceptions = TestSubscriber<Throwable>()
    private val disposables = CompositeDisposable()
    private fun setUpEnvironment(environment: Environment) {
        vm = DiscoveryViewModel.DiscoveryViewModel(environment)
    }
    // In the future, this can be dynamic and moved to the base class
    private fun setUpRxJavaPluginsErrorHandler() {
        RxJavaPlugins.setErrorHandler { t: Throwable ->
            exceptions.onNext(t)
        }
    }
    private fun tearDownRxJavaPluginsErrorHandler() {
        RxJavaPlugins.setErrorHandler(null)
    }

    @Test
    fun `test null Intent without error handling`() {
        setUpRxJavaPluginsErrorHandler()

        // Emulate V1 API deserialization of a response that does not adhere to expected schema
        val emailVerificationEnvelope = environment().gson()?.fromJson<EmailVerificationEnvelope>("{}")!!

        val url = "https://*.kickstarter.com/profile/verify_email"
        val intentWithUrl = Intent().setData(Uri.parse(url))
        val mockApiClient: MockApiClientV2 = object : MockApiClientV2() {
            override fun verifyEmail(token: String): Observable<EmailVerificationEnvelope> {
                return Observable.just(emailVerificationEnvelope)
            }
        }
        val mockedClientEnvironment = environment().toBuilder()
            .apiClientV2(mockApiClient)
            .build()
        setUpEnvironment(mockedClientEnvironment)

        vm.outputs.showSuccessMessage().subscribe { showSuccessMessage.onNext(it) }.addToDisposable(disposables)
        vm.provideIntent(intentWithUrl)
        showSuccessMessage.assertNoValues()

        exceptions.assertValueCount(1)

        tearDownRxJavaPluginsErrorHandler()
    }

    @Test
    fun `test Dark Mode disabled`() {
        val currentUser = MockCurrentUserV2()
        val env = environment().toBuilder().currentUserV2(currentUser).build()
        setUpEnvironment(env)

        vm.provideIntent(Intent(Intent.ACTION_MAIN))

        vm.outputs.darkThemeEnabled().subscribe { darkThemeEnabled.onNext(it) }.addToDisposable(disposables)

        vm.setDarkTheme(isDarkTheme = false)

        darkThemeEnabled.assertValues(false)
    }

    @Test
    fun `test Dark Mode enabled`() {
        val currentUser = MockCurrentUserV2()
        val env = environment().toBuilder().currentUserV2(currentUser).build()
        setUpEnvironment(env)

        vm.provideIntent(Intent(Intent.ACTION_MAIN))

        vm.outputs.darkThemeEnabled().subscribe { darkThemeEnabled.onNext(it) }.addToDisposable(disposables)

        vm.setDarkTheme(isDarkTheme = true)

        darkThemeEnabled.assertValues(true)
    }

    @Test
    fun testDrawerData() {
        val currentUser = MockCurrentUserV2()
        val env = environment().toBuilder().currentUserV2(currentUser).build()
        setUpEnvironment(env)
        vm.outputs.navigationDrawerData().compose(Transformers.ignoreValuesV2()).subscribe {
            navigationDrawerDataEmitted.onNext(it)
        }.addToDisposable(disposables)

        // Initialize activity.
        val intent = Intent(Intent.ACTION_MAIN)
        vm.provideIntent(intent)

        // Initial MAGIC page selected.
        vm.inputs.discoveryPagerAdapterSetPrimaryPage(
            Mockito.mock(
                DiscoveryPagerAdapter::class.java
            ),
            0
        )

        // Drawer data should emit. Drawer should be closed.
        navigationDrawerDataEmitted.assertValueCount(1)
        segmentTrack.assertNoValues()

        // Open drawer and click the top PWL filter.
        vm.inputs.topFilterViewHolderRowClick(
            Mockito.mock(
                TopFilterViewHolder::class.java
            ),
            NavigationDrawerData.Section.Row
                .builder()
                .params(DiscoveryParams.builder().staffPicks(true).build())
                .build()
        )

        // Drawer data should emit. Drawer should open, then close upon selection.
        navigationDrawerDataEmitted.assertValueCount(2)
        segmentTrack.assertValue(EventName.CTA_CLICKED.eventName)

        // Open drawer and click a child filter.
        vm.inputs.childFilterViewHolderRowClick(
            Mockito.mock(
                ChildFilterViewHolder::class.java
            ),
            NavigationDrawerData.Section.Row
                .builder()
                .params(
                    DiscoveryParams
                        .builder()
                        .category(artCategory())
                        .build()
                )
                .build()
        )

        // Drawer data should emit. Drawer should open, then close upon selection.
        navigationDrawerDataEmitted.assertValueCount(3)
        segmentTrack.assertValues(EventName.CTA_CLICKED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testUpdateInterfaceElementsWithParams() {
        setUpEnvironment(environment())
        vm.outputs.updateToolbarWithParams().subscribe { updateToolbarWithParams.onNext(it) }.addToDisposable(disposables)
        vm.outputs.expandSortTabLayout().subscribe { expandSortTabLayout.onNext(it) }.addToDisposable(disposables)

        // Initialize activity.
        val intent = Intent(Intent.ACTION_MAIN)
        vm.provideIntent(intent)

        // Initial MAGIC page selected.
        vm.inputs.discoveryPagerAdapterSetPrimaryPage(
            Mockito.mock(
                DiscoveryPagerAdapter::class.java
            ),
            0
        )

        // Sort tab should be expanded.
        expandSortTabLayout.assertValues(true, true)

        // Toolbar params should be loaded with initial params.
        updateToolbarWithParams.assertValues(
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).backed(-1).recommended(true).build()
        )

        // Select POPULAR sort.
        vm.inputs.sortClicked(1)
        vm.inputs.discoveryPagerAdapterSetPrimaryPage(
            Mockito.mock(
                DiscoveryPagerAdapter::class.java
            ),
            1
        )
        segmentTrack.assertValue(EventName.CTA_CLICKED.eventName)

        // Sort tab should be expanded.
        expandSortTabLayout.assertValues(true, true, true)

        // Unchanged toolbar params should not emit.
        updateToolbarWithParams.assertValues(
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).backed(-1).recommended(true).build()
        )

        // Select ALL PROJECTS filter from drawer.
        vm.inputs.topFilterViewHolderRowClick(
            Mockito.mock(
                TopFilterViewHolder::class.java
            ),
            NavigationDrawerData.Section.Row.builder()
                .params(DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build())
                .build()
        )

        // Sort tab should be expanded.
        expandSortTabLayout.assertValues(true, true, true, true, true)
        segmentTrack.assertValues(EventName.CTA_CLICKED.eventName, EventName.CTA_CLICKED.eventName)

        // Select ART category from drawer.
        vm.inputs.childFilterViewHolderRowClick(
            Mockito.mock(
                ChildFilterViewHolder::class.java
            ),
            NavigationDrawerData.Section.Row.builder()
                .params(
                    DiscoveryParams.builder().category(artCategory())
                        .sort(DiscoveryParams.Sort.POPULAR).build()
                )
                .build()
        )

        // Sort tab should be expanded.
        expandSortTabLayout.assertValues(true, true, true, true, true, true, true)
        segmentTrack.assertValues(
            EventName.CTA_CLICKED.eventName,
            EventName.CTA_CLICKED.eventName,
            EventName.CTA_CLICKED.eventName
        )

        // Simulate rotating the device and hitting initial getInputs() again.
        vm.outputs.updateToolbarWithParams().subscribe { rotatedUpdateToolbarWithParams.onNext(it) }.addToDisposable(disposables)
        vm.outputs.expandSortTabLayout().subscribe { rotatedExpandSortTabLayout.onNext(it) }.addToDisposable(disposables)

        // Simulate recreating and setting POPULAR fragment, the previous position before rotation.
        vm.inputs.discoveryPagerAdapterSetPrimaryPage(
            Mockito.mock(
                DiscoveryPagerAdapter::class.java
            ),
            1
        )

        // Sort tab and toolbar params should emit again with same params.
        rotatedExpandSortTabLayout.assertValues(true)
        rotatedUpdateToolbarWithParams.assertValues(
            DiscoveryParams.builder().category(artCategory()).sort(DiscoveryParams.Sort.POPULAR)
                .build()
        )
    }

    @Test
    fun testClickingInterfaceElements() {
        setUpEnvironment(environment())
        vm.outputs.showActivityFeed().subscribe { showActivityFeed.onNext(it) }.addToDisposable(disposables)
        vm.outputs.showHelp().subscribe { showHelp.onNext(it) }.addToDisposable(disposables)
        vm.outputs.showInternalTools().subscribe { showInternalTools.onNext(it) }.addToDisposable(disposables)
        vm.outputs.showLoginTout().subscribe { showLoginTout.onNext(it) }.addToDisposable(disposables)
        vm.outputs.showMessages().subscribe { showMessages.onNext(it) }.addToDisposable(disposables)
        vm.outputs.showProfile().subscribe { showProfile.onNext(it) }.addToDisposable(disposables)
        vm.outputs.showSettings().subscribe { showSettings.onNext(it) }.addToDisposable(disposables)
        vm.outputs.showPledgedProjects().subscribe { showPledgedProjects.onNext(it) }.addToDisposable(disposables)
        showActivityFeed.assertNoValues()
        showHelp.assertNoValues()
        showInternalTools.assertNoValues()
        showLoginTout.assertNoValues()
        showMessages.assertNoValues()
        showProfile.assertNoValues()
        showSettings.assertNoValues()
        showPledgedProjects.assertNoValues()
        vm.inputs.loggedInViewHolderActivityClick(
            Mockito.mock(
                LoggedInViewHolder::class.java
            )
        )
        vm.inputs.loggedOutViewHolderActivityClick(
            Mockito.mock(
                LoggedOutViewHolder::class.java
            )
        )
        vm.inputs.loggedOutViewHolderHelpClick(
            Mockito.mock(
                LoggedOutViewHolder::class.java
            )
        )
        vm.inputs.loggedInViewHolderInternalToolsClick(
            Mockito.mock(
                LoggedInViewHolder::class.java
            )
        )
        vm.inputs.loggedOutViewHolderLoginToutClick(
            Mockito.mock(
                LoggedOutViewHolder::class.java
            )
        )
        vm.inputs.loggedInViewHolderMessagesClick(
            Mockito.mock(
                LoggedInViewHolder::class.java
            )
        )
        vm.inputs.loggedInViewHolderProfileClick(
            Mockito.mock(
                LoggedInViewHolder::class.java
            ),
            user()
        )
        vm.inputs.loggedInViewHolderSettingsClick(
            Mockito.mock(
                LoggedInViewHolder::class.java
            ),
            user()
        )

        vm.inputs.loggedInViewHolderPledgedProjectsClick(
            Mockito.mock(
                LoggedInViewHolder::class.java
            )
        )

        showActivityFeed.assertValueCount(2)
        showHelp.assertValueCount(1)
        showInternalTools.assertValueCount(1)
        showLoginTout.assertValueCount(1)
        showMessages.assertValueCount(1)
        showProfile.assertValueCount(1)
        showSettings.assertValueCount(1)
        showPledgedProjects.assertValueCount(1)
    }

    @Test
    fun testInteractionBetweenParamsAndPageAdapter() {
        setUpEnvironment(environment())
        vm.outputs.updateParamsForPage().subscribe { updateParams.onNext(it) }.addToDisposable(disposables)
        vm.outputs.updateParamsForPage()
            .map { params: DiscoveryParams -> params.sort().positionFromSort() }
            .subscribe { updatePage.onNext(it) }.addToDisposable(disposables)

        // Start initial activity.
        val intent = Intent(Intent.ACTION_MAIN)
        vm.provideIntent(intent)

        // Initial MAGIC page selected.
        vm.inputs.discoveryPagerAdapterSetPrimaryPage(
            Mockito.mock(
                DiscoveryPagerAdapter::class.java
            ),
            0
        )

        // Initial params should emit. Page should not be updated yet.
        updateParams.assertValues(
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).backed(-1).recommended(true).build(),
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).backed(-1).recommended(true).build()
        )
        updatePage.assertValues(0, 0)

        // Select POPULAR sort position.
        vm.inputs.discoveryPagerAdapterSetPrimaryPage(
            Mockito.mock(
                DiscoveryPagerAdapter::class.java
            ),
            1
        )

        // Params and page should update with new POPULAR sort values.
        updateParams.assertValues(
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).backed(-1).recommended(true).build(),
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).backed(-1).recommended(true).build(),
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).backed(-1).recommended(true).build()
        )
        updatePage.assertValues(0, 0, 1)

        // Select ART category from the drawer.
        vm.inputs.childFilterViewHolderRowClick(
            Mockito.mock(
                ChildFilterViewHolder::class.java
            ),
            NavigationDrawerData.Section.Row.builder()
                .params(DiscoveryParams.builder().category(artCategory()).build())
                .build()
        )

        // Params should update with new category; page should remain the same.
        updateParams.assertValues(
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).backed(-1).recommended(true).build(),
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).backed(-1).recommended(true).build(),
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).backed(-1).recommended(true).build(),
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).category(artCategory())
                .build(),
            DiscoveryParams.builder().category(artCategory()).sort(DiscoveryParams.Sort.POPULAR)
                .build()
        )
        updatePage.assertValues(0, 0, 1, 1, 1)

        // Select MAGIC sort position.
        vm.inputs.discoveryPagerAdapterSetPrimaryPage(
            Mockito.mock(
                DiscoveryPagerAdapter::class.java
            ),
            0
        )

        // Params and page should update with new MAGIC sort value.
        updateParams.assertValues(
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).backed(-1).recommended(true).build(),
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).backed(-1).recommended(true).build(),
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).backed(-1).recommended(true).build(),
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).category(artCategory())
                .build(),
            DiscoveryParams.builder().category(artCategory()).sort(DiscoveryParams.Sort.POPULAR)
                .build(),
            DiscoveryParams.builder().category(artCategory()).sort(DiscoveryParams.Sort.MAGIC)
                .build()
        )
        updatePage.assertValues(0, 0, 1, 1, 1, 0)

        // Simulate rotating the device and hitting initial getInputs() again.
        vm.outputs.updateParamsForPage().subscribe { rotatedUpdateParams.onNext(it) }.addToDisposable(disposables)
        vm.outputs.updateParamsForPage()
            .map { params: DiscoveryParams -> params.sort().positionFromSort() }
            .subscribe { rotatedUpdatePage.onNext(it) }.addToDisposable(disposables)

        // Should emit again with same params.
        rotatedUpdateParams.assertValues(
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).category(artCategory())
                .build()
        )
        rotatedUpdatePage.assertValues(0)
    }

    @Test
    fun testDefaultParams_withUserLoggedOut() {
        setUpDefaultParamsTest(null)
        updateParams.assertValues(
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).backed(-1).recommended(true).build(),
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).backed(-1).recommended(true).build()
        )
    }

    @Test
    fun testDefaultParams_withUserLoggedIn_optedIn() {
        setUpDefaultParamsTest(user())
        updateParams.assertValues(
            DiscoveryParams.builder().recommended(true).backed(-1).sort(DiscoveryParams.Sort.MAGIC)
                .build(),
            DiscoveryParams.builder().recommended(true).backed(-1).sort(DiscoveryParams.Sort.MAGIC)
                .build()
        )
    }

    @Test
    fun testDefaultParams_withUserLoggedIn_optedOut() {
        setUpDefaultParamsTest(noRecommendations())
        updateParams.assertValues(
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build(),
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build()
        )
    }

    @Test
    fun testClearingPages() {
        setUpEnvironment(environment())
        vm.outputs.clearPages().subscribe { clearPages.onNext(it) }.addToDisposable(disposables)

        // Start initial activity.
        val intent = Intent(Intent.ACTION_MAIN)
        vm.provideIntent(intent)
        clearPages.assertNoValues()
        vm.inputs.discoveryPagerAdapterSetPrimaryPage(
            Mockito.mock(
                DiscoveryPagerAdapter::class.java
            ),
            1
        )
        clearPages.assertNoValues()
        vm.inputs.discoveryPagerAdapterSetPrimaryPage(
            Mockito.mock(
                DiscoveryPagerAdapter::class.java
            ),
            3
        )
        clearPages.assertNoValues()

        // Select ART category from the drawer.
        vm.inputs.childFilterViewHolderRowClick(
            Mockito.mock(
                ChildFilterViewHolder::class.java
            ),
            NavigationDrawerData.Section.Row.builder()
                .params(DiscoveryParams.builder().category(artCategory()).build())
                .build()
        )
        clearPages.assertValues(listOf(0, 1, 2))
        vm.inputs.discoveryPagerAdapterSetPrimaryPage(
            Mockito.mock(
                DiscoveryPagerAdapter::class.java
            ),
            1
        )

        // Select MUSIC category from the drawer.
        vm.inputs.childFilterViewHolderRowClick(
            Mockito.mock(
                ChildFilterViewHolder::class.java
            ),
            NavigationDrawerData.Section.Row.builder()
                .params(DiscoveryParams.builder().category(musicCategory()).build())
                .build()
        )
        clearPages.assertValues(listOf(0, 1, 2), listOf(0, 2, 3))
    }

    @Test
    fun testRootCategoriesEmitWithPosition() {
        setUpEnvironment(environment())

        vm.outputs.rootCategoriesAndPosition()
            .map { cp -> cp.first }
            .subscribe { rootCategories.onNext(it) }.addToDisposable(disposables)

        vm.outputs.rootCategoriesAndPosition()
            .map { cp -> cp.second }
            .subscribe { position.onNext(it) }.addToDisposable(disposables)

        // Start initial activity.
        vm.provideIntent(Intent(Intent.ACTION_MAIN))

        // Initial MAGIC page selected.
        vm.inputs.discoveryPagerAdapterSetPrimaryPage(
            Mockito.mock(
                DiscoveryPagerAdapter::class.java
            ),
            0
        )

        // Root categories should emit for the initial MAGIC sort this.position.
        rootCategories.assertValueCount(1)
        position.assertValues(0)

        // Select POPULAR sort position.
        vm.inputs.discoveryPagerAdapterSetPrimaryPage(
            Mockito.mock(
                DiscoveryPagerAdapter::class.java
            ),
            1
        )

        // Root categories should emit for the POPULAR sort position.
        rootCategories.assertValueCount(2)
        position.assertValues(0, 1)

        // Select ART category from the drawer.
        vm.inputs.childFilterViewHolderRowClick(
            Mockito.mock(
                ChildFilterViewHolder::class.java
            ),
            NavigationDrawerData.Section.Row.builder()
                .params(DiscoveryParams.builder().category(artCategory()).build())
                .build()
        )

        // Root categories should not emit again for the same position.
        rootCategories.assertValueCount(2)
        position.assertValues(0, 1)
    }

    @Test
    fun testDrawerMenuIcon_whenLoggedOut() {
        setUpEnvironment(environment())
        vm.outputs.drawerMenuIcon().subscribe { drawerMenuIcon.onNext(it) }.addToDisposable(disposables)
        vm.setDarkTheme(false)
        drawerMenuIcon.assertValue(R.drawable.ic_menu)
    }

    @Test
    fun testDrawerMenuIcon_afterLogInRefreshAndLogOut() {
        val currentUser = MockCurrentUserV2()
        setUpEnvironment(environment().toBuilder().currentUserV2(currentUser).build())
        vm.setDarkTheme(isDarkTheme = false)
        vm.outputs.drawerMenuIcon().subscribe { drawerMenuIcon.onNext(it) }.addToDisposable(disposables)
        drawerMenuIcon.assertValue(R.drawable.ic_menu)
        currentUser.refresh(user().toBuilder().unreadMessagesCount(4).build())
        vm.setDarkTheme(isDarkTheme = false)
        drawerMenuIcon.assertValues(R.drawable.ic_menu, R.drawable.ic_menu_indicator)
        currentUser.refresh(user().toBuilder().erroredBackingsCount(2).build())
        vm.setDarkTheme(isDarkTheme = false)
        drawerMenuIcon.assertValues(
            R.drawable.ic_menu,
            R.drawable.ic_menu_indicator,
            R.drawable.ic_menu_error_indicator
        )
        currentUser.refresh(
            user().toBuilder().unreadMessagesCount(4).unseenActivityCount(3).erroredBackingsCount(2)
                .build()
        )
        vm.setDarkTheme(isDarkTheme = false)
        drawerMenuIcon.assertValues(
            R.drawable.ic_menu,
            R.drawable.ic_menu_indicator,
            R.drawable.ic_menu_error_indicator
        )
        currentUser.logout()
        vm.setDarkTheme(isDarkTheme = false)
        drawerMenuIcon.assertValues(
            R.drawable.ic_menu, R.drawable.ic_menu_indicator, R.drawable.ic_menu_error_indicator,
            R.drawable.ic_menu
        )
    }

    @Test
    fun testDrawerMenuIcon_whenUserHasNoUnreadMessagesOrUnseenActivityOrErroredBackings() {
        val currentUser = MockCurrentUserV2(user())
        setUpEnvironment(
            environment()
                .toBuilder()
                .currentUserV2(currentUser)
                .build()
        )
        vm.outputs.drawerMenuIcon().subscribe { drawerMenuIcon.onNext(it) }.addToDisposable(disposables)
        vm.setDarkTheme(false)
        drawerMenuIcon.assertValue(R.drawable.ic_menu)
    }

    @Test
    fun testShowSnackBar_whenIntentFromDeepLinkSuccessResponse_showSuccessMessage() {
        val url = "https://*.kickstarter.com/profile/verify_email"
        val intentWithUrl = Intent().setData(Uri.parse(url))
        val mockApiClient: MockApiClientV2 = object : MockApiClientV2() {
            override fun verifyEmail(token: String): Observable<EmailVerificationEnvelope> {
                return Observable.just(
                    EmailVerificationEnvelope.builder()
                        .code(200)
                        .message("Success")
                        .build()
                )
            }
        }
        val mockedClientEnvironment = environment().toBuilder()
            .apiClientV2(mockApiClient)
            .build()
        setUpEnvironment(mockedClientEnvironment)
        vm.outputs.showSuccessMessage().subscribe { showSuccessMessage.onNext(it) }.addToDisposable(disposables)
        vm.outputs.showErrorMessage().subscribe { showErrorMessage.onNext(it) }.addToDisposable(disposables)
        vm.provideIntent(intentWithUrl)
        showSuccessMessage.assertValue("Success")
        showErrorMessage.assertNoValues()
    }

    @Test
    fun testShowSnackBar_whenIntentFromDeepLinkSuccessResponse_showErrorMessage() {
        val url = "https://*.kickstarter.com/profile/verify_email"
        val intentWithUrl = Intent().setData(Uri.parse(url))
        val errorEnvelope = ErrorEnvelope.builder()
            .httpCode(403).errorMessages(listOf("expired")).build()
        val apiException = ApiExceptionFactory.apiError(errorEnvelope)
        val mockApiClient: MockApiClientV2 = object : MockApiClientV2() {
            override fun verifyEmail(token: String): Observable<EmailVerificationEnvelope> {
                return Observable.error(apiException)
            }
        }
        val mockedClientEnvironment = environment().toBuilder()
            .apiClientV2(mockApiClient)
            .build()
        setUpEnvironment(mockedClientEnvironment)
        vm.outputs.showSuccessMessage().subscribe { showSuccessMessage.onNext(it) }.addToDisposable(disposables)
        vm.outputs.showErrorMessage().subscribe { showErrorMessage.onNext(it) }.addToDisposable(disposables)
        vm.provideIntent(intentWithUrl)
        showSuccessMessage.assertNoValues()
        showErrorMessage.assertValue("expired")
    }

    @Test
    fun testIntentWithUri_whenGivenSort_shouldEmitSort() {
        val url = "https://www.kickstarter.com/discover/advanced?sort=end_date"
        val intentWithUrl = Intent().setData(Uri.parse(url))
        setUpEnvironment(environment())
        vm.outputs.updateParamsForPage().subscribe { updateParams.onNext(it) }.addToDisposable(disposables)
        vm.provideIntent(intentWithUrl)
        updateParams.assertValue(
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.ENDING_SOON).build()
        )
    }

    @Test
    fun testNotificationPermissionRequest_whenUserNotLoggedIn_shouldNotEmit() {
        setUpEnvironment(environment())

        vm.outputs.showNotifPermissionsRequest().subscribe { showNotifPermissionRequest.onNext(it) }.addToDisposable(disposables)

        showNotifPermissionRequest.assertNoValues()
    }

    @Test
    fun testNotificationPermissionRequest_whenUserHasSeenRequest_shouldNotEmit() {
        val sharedPreferences: SharedPreferences = Mockito.mock(SharedPreferences::class.java)
        var user: User = user()
        val currentUser: CurrentUserTypeV2 = MockCurrentUserV2(user)

        Mockito.`when`(sharedPreferences.getBoolean(SharedPreferenceKey.HAS_SEEN_NOTIF_PERMISSIONS, false)).thenReturn(true)
        setUpEnvironment(
            environment()
                .toBuilder()
                .sharedPreferences(sharedPreferences)
                .currentUserV2(currentUser)
                .build()
        )

        vm.outputs.showNotifPermissionsRequest().subscribe { showNotifPermissionRequest.onNext(it) }.addToDisposable(disposables)

        showNotifPermissionRequest.assertNoValues()
    }

    @Test
    fun testNotificationPermissionRequest_whenLoggedInAndHasNotSeeRequest_shouldEmit() {
        val sharedPreferences: SharedPreferences = Mockito.mock(SharedPreferences::class.java)
        var user: User = user()
        val currentUser: CurrentUserTypeV2 = MockCurrentUserV2(user)

        Mockito.`when`(sharedPreferences.getBoolean(SharedPreferenceKey.HAS_SEEN_NOTIF_PERMISSIONS, false)).thenReturn(false)
        setUpEnvironment(
            environment()
                .toBuilder()
                .sharedPreferences(sharedPreferences)
                .currentUserV2(currentUser)
                .build()
        )

        vm.outputs.showNotifPermissionsRequest().subscribe { showNotifPermissionRequest.onNext(it) }.addToDisposable(disposables)

        showNotifPermissionRequest.assertValue(Unit)
    }

    @Test
    fun testConsentManagementDialog_whenPreferenceContainsKeyValue_shouldNotEmit() {
        val sharedPreferences: SharedPreferences = Mockito.mock(SharedPreferences::class.java)
        Mockito.`when`(sharedPreferences.contains(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE)).thenReturn(true)

        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return true
                }
            }

        setUpEnvironment(
            environment()
                .toBuilder()
                .sharedPreferences(sharedPreferences)
                .featureFlagClient(mockFeatureFlagClient)
                .build()
        )

        vm.outputs.showConsentManagementDialog().subscribe { showConsentManagementDialog.onNext(it) }.addToDisposable(disposables)

        showConsentManagementDialog.assertNoValues()
    }

    @Test
    fun testConsentManagementDialog_whenFFOff_shouldNotEmit() {
        val sharedPreferences: SharedPreferences = Mockito.mock(SharedPreferences::class.java)
        Mockito.`when`(sharedPreferences.contains(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE)).thenReturn(false)

        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return false
                }
            }

        setUpEnvironment(
            environment()
                .toBuilder()
                .sharedPreferences(sharedPreferences)
                .featureFlagClient(mockFeatureFlagClient)
                .build()
        )

        vm.outputs.showConsentManagementDialog().subscribe { showConsentManagementDialog.onNext(it) }.addToDisposable(disposables)

        showConsentManagementDialog.assertNoValues()
    }

    @Test
    fun testConsentManagementDialog_preferenceDoesNotContainKeyValueAndFFOn_shouldEmit() {
        val sharedPreferences: SharedPreferences = Mockito.mock(SharedPreferences::class.java)
        Mockito.`when`(sharedPreferences.contains(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE)).thenReturn(false)

        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    if (FlagKey == com.kickstarter.libs.featureflag.FlagKey.ANDROID_CONSENT_MANAGEMENT) {
                        return true
                    } else if (FlagKey == com.kickstarter.libs.featureflag.FlagKey.ANDROID_NATIVE_ONBOARDING_FLOW) {
                        return false
                    } else {
                        return false
                    }
                }
            }

        setUpEnvironment(
            environment()
                .toBuilder()
                .sharedPreferences(sharedPreferences)
                .featureFlagClient(mockFeatureFlagClient)
                .build()
        )

        vm.outputs.showConsentManagementDialog().subscribe { showConsentManagementDialog.onNext(it) }.addToDisposable(disposables)

        showConsentManagementDialog.assertValue(Unit)
    }

    @Test
    fun testOnboardingFlow_hasSeenPreferenceFalseNewUserTrueAndFFOn_shouldEmit() {
        val sharedPreferences: SharedPreferences = Mockito.mock(SharedPreferences::class.java)
        Mockito.`when`(sharedPreferences.getBoolean(SharedPreferenceKey.HAS_SEEN_ONBOARDING, false)).thenReturn(false)

        // New user heuristic: has not seen consent management or notification permissions
        Mockito.`when`(sharedPreferences.contains(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE)).thenReturn(true)
        Mockito.`when`(sharedPreferences.getBoolean(SharedPreferenceKey.HAS_SEEN_NOTIF_PERMISSIONS, false)).thenReturn(false)

        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return if (FlagKey == com.kickstarter.libs.featureflag.FlagKey.ANDROID_NATIVE_ONBOARDING_FLOW) {
                        true
                    } else if (FlagKey == com.kickstarter.libs.featureflag.FlagKey.ANDROID_CONSENT_MANAGEMENT) {
                        true
                    } else {
                        false
                    }
                }
            }

        setUpEnvironment(
            environment()
                .toBuilder()
                .sharedPreferences(sharedPreferences)
                .featureFlagClient(mockFeatureFlagClient)
                .build()
        )

        vm.outputs.showOnboardingFlow().subscribe { showOnboardingFlow.onNext(it) }.addToDisposable(disposables)

        showOnboardingFlow.assertValue(Unit)
    }

    @Test
    fun testOnboardingFlow_hasSeenPreferenceFalseNewUserFalseAndFFOn_shouldNotEmit() {
        val sharedPreferences: SharedPreferences = Mockito.mock(SharedPreferences::class.java)
        Mockito.`when`(sharedPreferences.getBoolean(SharedPreferenceKey.HAS_SEEN_ONBOARDING, false)).thenReturn(false)

        // NOT a new user, because user has seen both notification permissions and consent management
        Mockito.`when`(sharedPreferences.contains(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE)).thenReturn(true)
        Mockito.`when`(sharedPreferences.getBoolean(SharedPreferenceKey.HAS_SEEN_NOTIF_PERMISSIONS, false)).thenReturn(true)

        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return if (FlagKey == com.kickstarter.libs.featureflag.FlagKey.ANDROID_NATIVE_ONBOARDING_FLOW) {
                        true
                    } else if (FlagKey == com.kickstarter.libs.featureflag.FlagKey.ANDROID_CONSENT_MANAGEMENT) {
                        true
                    } else {
                        false
                    }
                }
            }

        setUpEnvironment(
            environment()
                .toBuilder()
                .sharedPreferences(sharedPreferences)
                .featureFlagClient(mockFeatureFlagClient)
                .build()
        )

        vm.outputs.showOnboardingFlow().subscribe { showOnboardingFlow.onNext(it) }.addToDisposable(disposables)

        showOnboardingFlow.assertNoValues()
    }

    @Test
    fun testOnboardingFlow_hasSeenPreferenceFalseAndFFOff_shouldNotEmit() {
        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return if (FlagKey == com.kickstarter.libs.featureflag.FlagKey.ANDROID_NATIVE_ONBOARDING_FLOW) {
                        false
                    } else {
                        false
                    }
                }
            }

        setUpEnvironment(
            environment()
                .toBuilder()
                .featureFlagClient(mockFeatureFlagClient)
                .build()
        )

        vm.outputs.showOnboardingFlow().subscribe { showOnboardingFlow.onNext(it) }.addToDisposable(disposables)

        showOnboardingFlow.assertNoValues()
    }

    @Test
    fun testOnboardingFlow_hasSeenPreferenceTrueAndFFOn_shouldNotEmit() {
        val sharedPreferences: SharedPreferences = Mockito.mock(SharedPreferences::class.java)
        Mockito.`when`(sharedPreferences.getBoolean(SharedPreferenceKey.HAS_SEEN_ONBOARDING, false)).thenReturn(true)

        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return if (FlagKey == com.kickstarter.libs.featureflag.FlagKey.ANDROID_NATIVE_ONBOARDING_FLOW) {
                        true
                    } else {
                        false
                    }
                }
            }

        setUpEnvironment(
            environment()
                .toBuilder()
                .sharedPreferences(sharedPreferences)
                .featureFlagClient(mockFeatureFlagClient)
                .build()
        )

        vm.outputs.showOnboardingFlow().subscribe { showOnboardingFlow.onNext(it) }.addToDisposable(disposables)

        showOnboardingFlow.assertNoValues()
    }

    private fun setUpDefaultParamsTest(user: User?) {
        val environmentBuilder = environment().toBuilder()

        if (user != null) {
            val currentUser = MockCurrentUserV2(user)
            environmentBuilder.currentUserV2(currentUser)
        }
        setUpEnvironment(environmentBuilder.build())
        vm.outputs.updateParamsForPage().subscribe { updateParams.onNext(it) }.addToDisposable(disposables)

        // Start initial activity.
        val intent = Intent(Intent.ACTION_MAIN)
        vm.provideIntent(intent)

        // Initial MAGIC page selected.
        vm.inputs.discoveryPagerAdapterSetPrimaryPage(
            Mockito.mock(
                DiscoveryPagerAdapter::class.java
            ),
            0
        )
    }

    @After
    fun cleanUp() {
        disposables.clear()
    }
}
