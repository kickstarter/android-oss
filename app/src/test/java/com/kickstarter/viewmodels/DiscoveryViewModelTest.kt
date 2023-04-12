package com.kickstarter.viewmodels

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.EventName
import com.kickstarter.libs.utils.extensions.positionFromSort
import com.kickstarter.mock.MockFeatureFlagClient
import com.kickstarter.mock.factories.ApiExceptionFactory
import com.kickstarter.mock.factories.CategoryFactory.artCategory
import com.kickstarter.mock.factories.CategoryFactory.musicCategory
import com.kickstarter.mock.factories.InternalBuildEnvelopeFactory.newerBuildAvailable
import com.kickstarter.mock.factories.UserFactory.noRecommendations
import com.kickstarter.mock.factories.UserFactory.user
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.models.Category
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.apiresponses.EmailVerificationEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.services.apiresponses.InternalBuildEnvelope
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.adapters.DiscoveryPagerAdapter
import com.kickstarter.ui.adapters.data.NavigationDrawerData
import com.kickstarter.ui.viewholders.discoverydrawer.ChildFilterViewHolder
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedInViewHolder
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedOutViewHolder
import com.kickstarter.ui.viewholders.discoverydrawer.TopFilterViewHolder
import org.junit.Test
import org.mockito.Mockito
import rx.Observable
import rx.observers.TestSubscriber

class DiscoveryViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: DiscoveryViewModel.ViewModel
    private val clearPages = TestSubscriber<List<Int>>()
    private val drawerIsOpen = TestSubscriber<Boolean>()
    private val drawerMenuIcon = TestSubscriber<Int>()
    private val expandSortTabLayout = TestSubscriber<Boolean>()
    private val navigationDrawerDataEmitted = TestSubscriber<Void>()
    private val position = TestSubscriber<Int>()
    private val rootCategories = TestSubscriber<List<Category>>()
    private val rotatedExpandSortTabLayout = TestSubscriber<Boolean>()
    private val rotatedUpdatePage = TestSubscriber<Int>()
    private val rotatedUpdateParams = TestSubscriber<DiscoveryParams>()
    private val rotatedUpdateToolbarWithParams = TestSubscriber<DiscoveryParams>()
    private val showActivityFeed = TestSubscriber<Void>()
    private val showBuildCheckAlert = TestSubscriber<InternalBuildEnvelope>()
    private val showCreatorDashboard = TestSubscriber<Void>()
    private val showHelp = TestSubscriber<Void>()
    private val showInternalTools = TestSubscriber<Void>()
    private val showLoginTout = TestSubscriber<Void>()
    private val showMessages = TestSubscriber<Void>()
    private val showProfile = TestSubscriber<Void>()
    private val showSettings = TestSubscriber<Void>()
    private val updatePage = TestSubscriber<Int>()
    private val updateParams = TestSubscriber<DiscoveryParams>()
    private val updateToolbarWithParams = TestSubscriber<DiscoveryParams>()
    private val showSuccessMessage = TestSubscriber<String>()
    private val showErrorMessage = TestSubscriber<String>()
    private val showNotifPermissionRequest = TestSubscriber<Void>()
    private val showConsentManagementDialog = TestSubscriber<Void>()

    private fun setUpEnvironment(environment: Environment) {
        vm = DiscoveryViewModel.ViewModel(environment)
    }

    @Test
    fun testBuildCheck() {
        setUpEnvironment(environment())
        val buildEnvelope = newerBuildAvailable()
        vm.outputs.showBuildCheckAlert().subscribe(showBuildCheckAlert)

        // Build check should not be shown.
        showBuildCheckAlert.assertNoValues()

        // Build check should be shown when newer build is available.
        vm.inputs.newerBuildIsAvailable(buildEnvelope)
        showBuildCheckAlert.assertValue(buildEnvelope)
    }

    @Test
    fun testDrawerData() {
        val currentUser = MockCurrentUser()
        val env = environment().toBuilder().currentUser(currentUser).build()
        setUpEnvironment(env)
        vm.outputs.navigationDrawerData().compose(Transformers.ignoreValues()).subscribe(
            navigationDrawerDataEmitted
        )
        vm.outputs.drawerIsOpen().subscribe(drawerIsOpen)

        // Initialize activity.
        val intent = Intent(Intent.ACTION_MAIN)
        vm.intent(intent)

        // Initial MAGIC page selected.
        vm.inputs.discoveryPagerAdapterSetPrimaryPage(
            Mockito.mock(
                DiscoveryPagerAdapter::class.java
            ),
            0
        )

        // Drawer data should emit. Drawer should be closed.
        navigationDrawerDataEmitted.assertValueCount(1)
        drawerIsOpen.assertNoValues()
        segmentTrack.assertNoValues()

        // Open drawer and click the top PWL filter.
        vm.inputs.openDrawer(true)
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
        drawerIsOpen.assertValues(true, false)
        segmentTrack.assertValue(EventName.CTA_CLICKED.eventName)

        // Open drawer and click a child filter.
        vm.inputs.openDrawer(true)
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
        drawerIsOpen.assertValues(true, false, true, false)
        segmentTrack.assertValues(EventName.CTA_CLICKED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testUpdateInterfaceElementsWithParams() {
        setUpEnvironment(environment())
        vm.outputs.updateToolbarWithParams().subscribe(updateToolbarWithParams)
        vm.outputs.expandSortTabLayout().subscribe(expandSortTabLayout)

        // Initialize activity.
        val intent = Intent(Intent.ACTION_MAIN)
        vm.intent(intent)

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
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build()
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
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build()
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
        vm.outputs.updateToolbarWithParams().subscribe(rotatedUpdateToolbarWithParams)
        vm.outputs.expandSortTabLayout().subscribe(rotatedExpandSortTabLayout)

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
        vm.outputs.showActivityFeed().subscribe(showActivityFeed)
        vm.outputs.showCreatorDashboard().subscribe(showCreatorDashboard)
        vm.outputs.showHelp().subscribe(showHelp)
        vm.outputs.showInternalTools().subscribe(showInternalTools)
        vm.outputs.showLoginTout().subscribe(showLoginTout)
        vm.outputs.showMessages().subscribe(showMessages)
        vm.outputs.showProfile().subscribe(showProfile)
        vm.outputs.showSettings().subscribe(showSettings)
        showActivityFeed.assertNoValues()
        showCreatorDashboard.assertNoValues()
        showHelp.assertNoValues()
        showInternalTools.assertNoValues()
        showLoginTout.assertNoValues()
        showMessages.assertNoValues()
        showProfile.assertNoValues()
        showSettings.assertNoValues()
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
        vm.inputs.loggedInViewHolderDashboardClick(
            Mockito.mock(
                LoggedInViewHolder::class.java
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
        showActivityFeed.assertValueCount(2)
        showCreatorDashboard.assertValueCount(1)
        showHelp.assertValueCount(1)
        showInternalTools.assertValueCount(1)
        showLoginTout.assertValueCount(1)
        showMessages.assertValueCount(1)
        showProfile.assertValueCount(1)
        showSettings.assertValueCount(1)
    }

    @Test
    fun testInteractionBetweenParamsAndPageAdapter() {
        setUpEnvironment(environment())
        vm.outputs.updateParamsForPage().subscribe(updateParams)
        vm.outputs.updateParamsForPage()
            .map { params: DiscoveryParams -> params.sort().positionFromSort() }
            .subscribe(updatePage)

        // Start initial activity.
        val intent = Intent(Intent.ACTION_MAIN)
        vm.intent(intent)

        // Initial MAGIC page selected.
        vm.inputs.discoveryPagerAdapterSetPrimaryPage(
            Mockito.mock(
                DiscoveryPagerAdapter::class.java
            ),
            0
        )

        // Initial params should emit. Page should not be updated yet.
        updateParams.assertValues(
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build(),
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build()
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
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build(),
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build(),
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build()
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
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build(),
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build(),
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build(),
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
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build(),
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build(),
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build(),
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).category(artCategory())
                .build(),
            DiscoveryParams.builder().category(artCategory()).sort(DiscoveryParams.Sort.POPULAR)
                .build(),
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).category(artCategory())
                .build()
        )
        updatePage.assertValues(0, 0, 1, 1, 1, 0)

        // Simulate rotating the device and hitting initial getInputs() again.
        vm.outputs.updateParamsForPage().subscribe(rotatedUpdateParams)
        vm.outputs.updateParamsForPage()
            .map { params: DiscoveryParams -> params.sort().positionFromSort() }
            .subscribe(rotatedUpdatePage)

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
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build(),
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build()
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
        vm.outputs.clearPages().subscribe(clearPages)

        // Start initial activity.
        val intent = Intent(Intent.ACTION_MAIN)
        vm.intent(intent)
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
            .subscribe(rootCategories)

        vm.outputs.rootCategoriesAndPosition()
            .map { cp -> cp.second }
            .subscribe(position)

        // Start initial activity.
        vm.intent(Intent(Intent.ACTION_MAIN))

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
        vm.outputs.drawerMenuIcon().subscribe(drawerMenuIcon)
        drawerMenuIcon.assertValue(R.drawable.ic_menu)
    }

    @Test
    fun testDrawerMenuIcon_afterLogInRefreshAndLogOut() {
        val currentUser = MockCurrentUser()
        setUpEnvironment(environment().toBuilder().currentUser(currentUser).build())
        vm.outputs.drawerMenuIcon().subscribe(drawerMenuIcon)
        drawerMenuIcon.assertValue(R.drawable.ic_menu)
        currentUser.refresh(user().toBuilder().unreadMessagesCount(4).build())
        drawerMenuIcon.assertValues(R.drawable.ic_menu, R.drawable.ic_menu_indicator)
        currentUser.refresh(user().toBuilder().erroredBackingsCount(2).build())
        drawerMenuIcon.assertValues(
            R.drawable.ic_menu,
            R.drawable.ic_menu_indicator,
            R.drawable.ic_menu_error_indicator
        )
        currentUser.refresh(
            user().toBuilder().unreadMessagesCount(4).unseenActivityCount(3).erroredBackingsCount(2)
                .build()
        )
        drawerMenuIcon.assertValues(
            R.drawable.ic_menu,
            R.drawable.ic_menu_indicator,
            R.drawable.ic_menu_error_indicator
        )
        currentUser.logout()
        drawerMenuIcon.assertValues(
            R.drawable.ic_menu, R.drawable.ic_menu_indicator, R.drawable.ic_menu_error_indicator,
            R.drawable.ic_menu
        )
    }

    @Test
    fun testDrawerMenuIcon_whenUserHasNoUnreadMessagesOrUnseenActivityOrErroredBackings() {
        val currentUser = MockCurrentUser(user())
        setUpEnvironment(
            environment()
                .toBuilder()
                .currentUser(currentUser)
                .build()
        )
        vm.outputs.drawerMenuIcon().subscribe(drawerMenuIcon)
        drawerMenuIcon.assertValue(R.drawable.ic_menu)
    }

    @Test
    fun testShowSnackBar_whenIntentFromDeepLinkSuccessResponse_showSuccessMessage() {
        val url = "https://*.kickstarter.com/profile/verify_email"
        val intentWithUrl = Intent().setData(Uri.parse(url))
        val mockApiClient: MockApiClient = object : MockApiClient() {
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
            .apiClient(mockApiClient)
            .build()
        setUpEnvironment(mockedClientEnvironment)
        vm.outputs.showSuccessMessage().subscribe(showSuccessMessage)
        vm.outputs.showErrorMessage().subscribe(showErrorMessage)
        vm.intent(intentWithUrl)
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
        val mockApiClient: MockApiClient = object : MockApiClient() {
            override fun verifyEmail(token: String): Observable<EmailVerificationEnvelope> {
                return Observable.error(apiException)
            }
        }
        val mockedClientEnvironment = environment().toBuilder()
            .apiClient(mockApiClient)
            .build()
        setUpEnvironment(mockedClientEnvironment)
        vm.outputs.showSuccessMessage().subscribe(showSuccessMessage)
        vm.outputs.showErrorMessage().subscribe(showErrorMessage)
        vm.intent(intentWithUrl)
        showSuccessMessage.assertNoValues()
        showErrorMessage.assertValue("expired")
    }

    @Test
    fun testIntentWithUri_whenGivenSort_shouldEmitSort() {
        val url = "https://www.kickstarter.com/discover/advanced?sort=end_date"
        val intentWithUrl = Intent().setData(Uri.parse(url))
        setUpEnvironment(environment())
        vm.outputs.updateParamsForPage().subscribe(updateParams)
        vm.intent(intentWithUrl)
        updateParams.assertValue(
            DiscoveryParams.builder().sort(DiscoveryParams.Sort.ENDING_SOON).build()
        )
    }

    @Test
    fun testNotificationPermissionRequest_whenUserNotLoggedIn_shouldNotEmit() {
        setUpEnvironment(environment())

        vm.outputs.showNotifPermissionsRequest().subscribe(showNotifPermissionRequest)

        showNotifPermissionRequest.assertNoValues()
    }

    @Test
    fun testNotificationPermissionRequest_whenUserHasSeenRequest_shouldNotEmit() {
        var sharedPreferences: SharedPreferences = Mockito.mock(SharedPreferences::class.java)
        var user: User = user()
        val currentUser: CurrentUserType = MockCurrentUser(user)

        Mockito.`when`(sharedPreferences.getBoolean(SharedPreferenceKey.HAS_SEEN_NOTIF_PERMISSIONS, false)).thenReturn(true)
        setUpEnvironment(
            environment()
                .toBuilder()
                .sharedPreferences(sharedPreferences)
                .currentUser(currentUser)
                .build()
        )

        vm.outputs.showNotifPermissionsRequest().subscribe(showNotifPermissionRequest)

        showNotifPermissionRequest.assertNoValues()
    }

    @Test
    fun testNotificationPermissionRequest_whenLoggedInAndHasNotSeeRequest_shouldEmit() {
        var sharedPreferences: SharedPreferences = Mockito.mock(SharedPreferences::class.java)
        var user: User = user()
        val currentUser: CurrentUserType = MockCurrentUser(user)

        Mockito.`when`(sharedPreferences.getBoolean(SharedPreferenceKey.HAS_SEEN_NOTIF_PERMISSIONS, false)).thenReturn(false)
        setUpEnvironment(
            environment()
                .toBuilder()
                .sharedPreferences(sharedPreferences)
                .currentUser(currentUser)
                .build()
        )

        vm.outputs.showNotifPermissionsRequest().subscribe(showNotifPermissionRequest)

        showNotifPermissionRequest.assertValue(null)
    }

    @Test
    fun testConsentManagementDialog_whenPreferenceContainsKeyValue_shouldNotEmit() {
        var sharedPreferences: SharedPreferences = Mockito.mock(SharedPreferences::class.java)
        Mockito.`when`(sharedPreferences.contains(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE)).thenReturn(true)

        val mockExperimentsClientType: MockExperimentsClientType =
            object : MockExperimentsClientType() {
                override fun isFeatureEnabled(feature: OptimizelyFeature.Key): Boolean {
                    return true
                }
            }
        setUpEnvironment(
            environment()
                .toBuilder()
                .sharedPreferences(sharedPreferences)
                .optimizely(mockExperimentsClientType)
                .build()
        )

        vm.outputs.showConsentManagementDialog().subscribe(showConsentManagementDialog)

        showConsentManagementDialog.assertNoValues()
    }

    @Test
    fun testConsentManagementDialog_whenFFOff_shouldNotEmit() {
        var sharedPreferences: SharedPreferences = Mockito.mock(SharedPreferences::class.java)
        Mockito.`when`(sharedPreferences.contains(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE)).thenReturn(false)

        val mockExperimentsClientType: MockExperimentsClientType =
            object : MockExperimentsClientType() {
                override fun isFeatureEnabled(feature: OptimizelyFeature.Key): Boolean {
                    return false
                }
            }

        setUpEnvironment(
            environment()
                .toBuilder()
                .sharedPreferences(sharedPreferences)
                .optimizely(mockExperimentsClientType)
                .build()
        )

        vm.outputs.showConsentManagementDialog().subscribe(showConsentManagementDialog)

        showConsentManagementDialog.assertNoValues()
    }

    @Test
    fun testConsentManagementDialog_preferenceDoesNotContainKeyValueAndFFOn_shouldEmit() {
        var sharedPreferences: SharedPreferences = Mockito.mock(SharedPreferences::class.java)

        Mockito.`when`(sharedPreferences.contains(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE)).thenReturn(false)
        val mockExperimentsClientType: MockExperimentsClientType =
            object : MockExperimentsClientType() {
                override fun isFeatureEnabled(feature: OptimizelyFeature.Key): Boolean {
                    return true
                }
            }

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
                .optimizely(mockExperimentsClientType)
                .featureFlagClient(mockFeatureFlagClient)
                .build()
        )

        vm.outputs.showConsentManagementDialog().subscribe(showConsentManagementDialog)

        showConsentManagementDialog.assertValue(null)
    }

    private fun setUpDefaultParamsTest(user: User?) {
        val environmentBuilder = environment().toBuilder()

        if (user != null) {
            val currentUser = MockCurrentUser(user)
            environmentBuilder.currentUser(currentUser)
        }
        setUpEnvironment(environmentBuilder.build())
        vm.outputs.updateParamsForPage().subscribe(updateParams)

        // Start initial activity.
        val intent = Intent(Intent.ACTION_MAIN)
        vm.intent(intent)

        // Initial MAGIC page selected.
        vm.inputs.discoveryPagerAdapterSetPrimaryPage(
            Mockito.mock(
                DiscoveryPagerAdapter::class.java
            ),
            0
        )
    }
}
