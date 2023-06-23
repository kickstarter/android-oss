package com.kickstarter.viewmodels

import android.content.Intent
import android.content.SharedPreferences
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.RefTag.Companion.thanks
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.preferences.MockBooleanPreference
import com.kickstarter.libs.utils.EventName
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.MockFeatureFlagClient
import com.kickstarter.mock.factories.CategoryFactory.artCategory
import com.kickstarter.mock.factories.CategoryFactory.category
import com.kickstarter.mock.factories.CategoryFactory.ceramicsCategory
import com.kickstarter.mock.factories.CategoryFactory.tabletopGamesCategory
import com.kickstarter.mock.factories.CheckoutDataFactory.checkoutData
import com.kickstarter.mock.factories.LocationFactory.germany
import com.kickstarter.mock.factories.ProjectDataFactory.project
import com.kickstarter.mock.factories.ProjectFactory.project
import com.kickstarter.mock.factories.RewardFactory.reward
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.factories.UserFactory.user
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.DiscoveryParams.Companion.builder
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.adapters.data.ThanksData
import com.kickstarter.ui.data.PledgeData.Companion.with
import com.kickstarter.ui.data.PledgeFlowContext
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.Test
import org.mockito.Mockito

class ThanksViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: ThanksViewModel.ThanksViewModel
    private val adapterData = TestSubscriber<ThanksData>()
    private val finish = TestSubscriber<Unit>()
    private val showGamesNewsletterDialogTest = TestSubscriber<Unit>()
    private val showRatingDialogTest = TestSubscriber<Unit>()
    private val showConfirmGamesNewsletterDialogTest = TestSubscriber.create<Unit>()
    private val startDiscoveryTest = TestSubscriber<DiscoveryParams>()
    private val startProjectTest = TestSubscriber<android.util.Pair<Project, RefTag>>()
    private val showSavedPromptTest = TestSubscriber<Unit>()
    private val disposables = CompositeDisposable()

    private fun setUpEnvironment(environment: Environment) {
        vm = ThanksViewModel.ThanksViewModel(environment)
        vm.outputs.adapterData().subscribe { adapterData.onNext(it) }.addToDisposable(disposables)
        vm.outputs.finish().subscribe { finish.onNext(it) }.addToDisposable(disposables)
        vm.outputs.showGamesNewsletterDialog()
            .subscribe { showGamesNewsletterDialogTest.onNext(it) }.addToDisposable(disposables)
        vm.outputs.showRatingDialog().subscribe { showRatingDialogTest.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.showConfirmGamesNewsletterDialog().subscribe {
            showConfirmGamesNewsletterDialogTest.onNext(it)
        }.addToDisposable(disposables)
        vm.outputs.startDiscoveryActivity().subscribe { startDiscoveryTest.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.startProjectActivity().subscribe { startProjectTest.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.showSavedPrompt().subscribe { showSavedPromptTest.onNext(it) }
            .addToDisposable(disposables)
    }

    @Test
    fun testSaveProject() {
        val project = project()
            .toBuilder()
            .category(artCategory())
            .build()
        setUpEnvironment(environment())

        vm.provideIntent(Intent().putExtra(IntentKey.PROJECT, project))

        adapterData.assertValueCount(1)
        vm.inputs.onHeartButtonClicked(project)
        adapterData.assertValueCount(2)
        showSavedPromptTest.assertValueCount(1)
        segmentTrack.assertValues(EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testThanksViewModel_adapterData() {
        val project = project()
            .toBuilder()
            .category(artCategory())
            .build()

        setUpEnvironment(environment())

        vm.provideIntent(Intent().putExtra(IntentKey.PROJECT, project))
        adapterData.assertValueCount(1)
    }

    @Test
    fun testFinishEmits() {
        setUpEnvironment(environment())

        val intent = Intent()
            .putExtra(IntentKey.PROJECT, project())

        vm.provideIntent(intent)
        vm.inputs.closeButtonClicked()
        finish.assertValueCount(1)
    }

    @Test
    fun testThanksViewModel_showRatingDialog() {
        val hasSeenAppRatingPreference = MockBooleanPreference(false)
        val hasSeenGamesNewsletterPreference = MockBooleanPreference(true)
        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return true
                }
            }

        val environment = environment()
            .toBuilder()
            .featureFlagClient(mockFeatureFlagClient)
            .hasSeenAppRatingPreference(hasSeenAppRatingPreference)
            .hasSeenGamesNewsletterPreference(hasSeenGamesNewsletterPreference)
            .build()

        setUpEnvironment(environment)

        vm.provideIntent(Intent().putExtra(IntentKey.PROJECT, project()))
        showRatingDialogTest.assertValueCount(0)
    }

    @Test
    fun testThanksViewModel_hideRatingDialog_if_feature_flag_disabled() {
        val hasSeenAppRatingPreference = MockBooleanPreference(false)
        val hasSeenGamesNewsletterPreference = MockBooleanPreference(true)
        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return false
                }
            }

        val environment = environment()
            .toBuilder()
            .featureFlagClient(mockFeatureFlagClient)
            .hasSeenAppRatingPreference(hasSeenAppRatingPreference)
            .hasSeenGamesNewsletterPreference(hasSeenGamesNewsletterPreference)
            .build()

        setUpEnvironment(environment)

        vm.provideIntent(Intent().putExtra(IntentKey.PROJECT, project()))
        showRatingDialogTest.assertValueCount(1)
    }

    @Test
    fun testThanksViewModel_dontShowRatingDialogIfAlreadySeen() {
        val hasSeenAppRatingPreference = MockBooleanPreference(true)
        val hasSeenGamesNewsletterPreference = MockBooleanPreference(true)
        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return false
                }
            }

        val environment = environment()
            .toBuilder()
            .featureFlagClient(mockFeatureFlagClient)
            .hasSeenAppRatingPreference(hasSeenAppRatingPreference)
            .hasSeenGamesNewsletterPreference(hasSeenGamesNewsletterPreference)
            .build()

        setUpEnvironment(environment)

        vm.provideIntent(Intent().putExtra(IntentKey.PROJECT, project()))
        showRatingDialogTest.assertValueCount(0)
    }

    @Test
    fun testThanksViewModel_dontShowRatingDialogIfGamesNewsletterWillDisplay() {
        val hasSeenAppRatingPreference = MockBooleanPreference(false)
        val hasSeenGamesNewsletterPreference = MockBooleanPreference(false)
        val user = user().toBuilder().gamesNewsletter(false).build()
        val currentUser: CurrentUserType = MockCurrentUser(user)
        val project = project()
            .toBuilder()
            .category(tabletopGamesCategory())
            .build()

        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return false
                }
            }

        val environment = environment()
            .toBuilder()
            .currentUser(currentUser)
            .featureFlagClient(mockFeatureFlagClient)
            .hasSeenAppRatingPreference(hasSeenAppRatingPreference)
            .hasSeenGamesNewsletterPreference(hasSeenGamesNewsletterPreference)
            .build()

        setUpEnvironment(environment)

        vm.provideIntent(Intent().putExtra(IntentKey.PROJECT, project))
        showRatingDialogTest.assertValueCount(0)
    }

    @Test
    fun testThanksViewModel_showGamesNewsletterDialog() {
        val hasSeenGamesNewsletterPreference = MockBooleanPreference(false)
        val user = user().toBuilder().gamesNewsletter(false).build()
        val currentUser: CurrentUserType = MockCurrentUser(user)
        val environment = environment()
            .toBuilder()
            .currentUser(currentUser)
            .hasSeenGamesNewsletterPreference(hasSeenGamesNewsletterPreference)
            .build()

        setUpEnvironment(environment)

        val project = project()
            .toBuilder()
            .category(tabletopGamesCategory())
            .build()

        vm.provideIntent(Intent().putExtra(IntentKey.PROJECT, project))

        showGamesNewsletterDialogTest.assertValueCount(1)
        assertEquals(listOf(false, true), hasSeenGamesNewsletterPreference.values())
    }

    @Test
    fun testThanksViewModel_dontShowGamesNewsletterDialogIfRootCategoryIsNotGames() {
        val hasSeenGamesNewsletterPreference = MockBooleanPreference(false)
        val user = user().toBuilder().gamesNewsletter(false).build()
        val currentUser: CurrentUserType = MockCurrentUser(user)
        val environment = environment()
            .toBuilder()
            .currentUser(currentUser)
            .hasSeenGamesNewsletterPreference(hasSeenGamesNewsletterPreference)
            .build()

        setUpEnvironment(environment)

        val project = project()
            .toBuilder()
            .category(ceramicsCategory())
            .build()

        vm.provideIntent(Intent().putExtra(IntentKey.PROJECT, project))

        showGamesNewsletterDialogTest.assertValueCount(0)
    }

    @Test
    fun testThanksViewModel_dontShowGamesNewsletterDialogIfUserHasAlreadySeen() {
        val hasSeenGamesNewsletterPreference = MockBooleanPreference(true)
        val user = user().toBuilder().gamesNewsletter(false).build()
        val currentUser: CurrentUserTypeV2 = MockCurrentUserV2(user)
        val environment = environment()
            .toBuilder()
            .currentUserV2(currentUser)
            .hasSeenGamesNewsletterPreference(hasSeenGamesNewsletterPreference)
            .build()

        setUpEnvironment(environment)

        val project = project()
            .toBuilder()
            .category(tabletopGamesCategory())
            .build()

        vm.provideIntent(Intent().putExtra(IntentKey.PROJECT, project))
        showGamesNewsletterDialogTest.assertValueCount(0)
    }

    @Test
    fun testThanksViewModel_dontShowGamesNewsletterDialogIfUserHasAlreadySignedUp() {
        val hasSeenGamesNewsletterPreference = MockBooleanPreference(false)
        val user = user().toBuilder().gamesNewsletter(true).build()
        val currentUser: CurrentUserTypeV2 = MockCurrentUserV2(user)
        val environment = environment()
            .toBuilder()
            .currentUserV2(currentUser)
            .hasSeenGamesNewsletterPreference(hasSeenGamesNewsletterPreference)
            .build()

        setUpEnvironment(environment)

        val project = project()
            .toBuilder()
            .category(tabletopGamesCategory())
            .build()

        vm.provideIntent(Intent().putExtra(IntentKey.PROJECT, project))
        showGamesNewsletterDialogTest.assertValueCount(0)
    }

    @Test
    fun testThanksViewModel_signupToGamesNewsletterOnClick() {
        val user = user().toBuilder().gamesNewsletter(false).build()
        val currentUser: CurrentUserTypeV2 = MockCurrentUserV2(user)
        val environment = environment().toBuilder()
            .currentUserV2(currentUser)
            .build()

        setUpEnvironment(environment)

        val updateUserSettingsTest = TestSubscriber<User>()
        (environment.apiClient() as? MockApiClient)?.observable()
            ?.filter { "update_user_settings" == it.first }
            ?.map { it.second["user"] as? User }
            ?.subscribe { updateUserSettingsTest.onNext(it) }

        val project = project()
            .toBuilder()
            .category(tabletopGamesCategory())
            .build()

        vm.provideIntent(Intent().putExtra(IntentKey.PROJECT, project))
        vm.signupToGamesNewsletterClick()

        updateUserSettingsTest.assertValues(user.toBuilder().gamesNewsletter(true).build())
        showConfirmGamesNewsletterDialogTest.assertValueCount(0)
    }

    @Test
    fun testThanksViewModel_showNewsletterConfirmationPromptAfterSignupForGermanUser() {
        val user = user().toBuilder()
            .gamesNewsletter(false)
            .location(germany())
            .build()
        val currentUser: CurrentUserTypeV2 = MockCurrentUserV2(user)
        val environment = environment().toBuilder()
            .currentUserV2(currentUser)
            .build()

        setUpEnvironment(environment)

        val project = project().toBuilder().category(tabletopGamesCategory()).build()

        vm.provideIntent(Intent().putExtra(IntentKey.PROJECT, project))
        vm.signupToGamesNewsletterClick()
        showConfirmGamesNewsletterDialogTest.assertValueCount(1)
    }

    @Test
    fun testThanksViewModel_startDiscovery() {
        setUpEnvironment(environment())

        val category = category()

        vm.inputs.categoryViewHolderClicked(category)
        startDiscoveryTest.assertValues(builder().category(category).build())
    }

    @Test
    fun testThanksViewModel_startProject() {
        setUpEnvironment(environment())
        val project = project()
        val checkoutData = checkoutData(
            3L,
            20.0,
            30.0
        )
        val pledgeData = with(
            PledgeFlowContext.NEW_PLEDGE,
            project(project),
            reward(),
            emptyList(),
            null
        )
        val intent = Intent()
            .putExtra(IntentKey.CHECKOUT_DATA, checkoutData)
            .putExtra(IntentKey.PLEDGE_DATA, pledgeData)
            .putExtra(IntentKey.PROJECT, project)

        vm.provideIntent(intent)
        vm.inputs.projectCardViewHolderClicked(project)

        val projectPageParams = startProjectTest.values().first()

        assertEquals(projectPageParams.first, project)
        assertEquals(projectPageParams.second, thanks())
        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
        assertEquals(null, this.vm.onThirdPartyEventSent.value)
    }

    @Test
    fun testSendCAPIEvent_whenBackedPRoject_sendCAPIEvent_withFeatureFlag__withConsentManagement_off_isFailed() {
        var sharedPreferences: SharedPreferences = Mockito.mock(SharedPreferences::class.java)
        Mockito.`when`(
            sharedPreferences.getBoolean(
                SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE,
                false
            )
        )
            .thenReturn(false)

        setUpEnvironment(
            environment().toBuilder()
                .sharedPreferences(sharedPreferences)
                .build()
        )

        val project = project().toBuilder().sendThirdPartyEvents(true).build()
        val checkoutData = checkoutData(
            3L,
            20.0,
            30.0
        )
        val pledgeData = with(
            PledgeFlowContext.NEW_PLEDGE,
            project(project),
            reward(),
            emptyList(),
            null
        )
        val intent = Intent()
            .putExtra(IntentKey.CHECKOUT_DATA, checkoutData)
            .putExtra(IntentKey.PLEDGE_DATA, pledgeData)
            .putExtra(IntentKey.PROJECT, project)

        vm.provideIntent(intent)
        vm.inputs.projectCardViewHolderClicked(project)

        val projectPageParams = startProjectTest.values().first()

        assertEquals(projectPageParams.first, project)
        assertEquals(projectPageParams.second, thanks())
        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
        assertEquals(null, this.vm.onThirdPartyEventSent.value)
    }

    @Test
    fun testSendCAPIEvent_whenBackedPRoject_sendCAPIEvent_withFeatureFlag_on_isSuccessful() {
        var user = UserFactory.user()
        var sharedPreferences: SharedPreferences = Mockito.mock(SharedPreferences::class.java)
        Mockito.`when`(
            sharedPreferences.getBoolean(
                SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE,
                false
            )
        )
            .thenReturn(true)

        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return true
                }
            }

        setUpEnvironment(
            environment().toBuilder()
                .currentUser(MockCurrentUser(user))
                .sharedPreferences(sharedPreferences)
                .featureFlagClient(mockFeatureFlagClient)
                .build()
        )

        val project = project().toBuilder().sendThirdPartyEvents(true).build()
        val checkoutData = checkoutData(
            3L,
            20.0,
            30.0
        )
        val pledgeData = with(
            PledgeFlowContext.NEW_PLEDGE,
            project(project),
            reward(),
            emptyList(),
            ShippingRuleFactory.germanyShippingRule()
        )
        val intent = Intent()
            .putExtra(IntentKey.CHECKOUT_DATA, checkoutData)
            .putExtra(IntentKey.PLEDGE_DATA, pledgeData)
            .putExtra(IntentKey.PROJECT, project)

        vm.provideIntent(intent)
        vm.inputs.projectCardViewHolderClicked(project)

        val projectPageParams = startProjectTest.values().first()

        assertEquals(projectPageParams.first, project)
        assertEquals(projectPageParams.second, thanks())
        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
        assertEquals(true, this.vm.onThirdPartyEventSent.value)
    }

    @Test
    fun testThanksViewModel_whenFeatureFlagOn_shouldEmitProjectPage() {
        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return true
                }
            }
        val environment = environment().toBuilder()
            .featureFlagClient(mockFeatureFlagClient)
            .build()

        setUpEnvironment(environment)

        val project = project()
        val checkoutData = checkoutData(
            3L,
            20.0,
            30.0
        )
        val pledgeData = with(
            PledgeFlowContext.NEW_PLEDGE,
            project(project),
            reward(),
            emptyList(),
            null
        )
        val intent = Intent()
            .putExtra(IntentKey.CHECKOUT_DATA, checkoutData)
            .putExtra(IntentKey.PLEDGE_DATA, pledgeData)
            .putExtra(IntentKey.PROJECT, project)

        vm.provideIntent(intent)
        vm.inputs.projectCardViewHolderClicked(project)

        val projectPageParams = startProjectTest.values().first()
        assertEquals(projectPageParams.first, project)
        assertEquals(projectPageParams.second, thanks())
        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testTracking_whenCheckoutDataAndPledgeDataExtrasPresent() {
        setUpEnvironment(environment())

        val project = project()
        val checkoutData = checkoutData(
            3L,
            20.0,
            30.0
        )
        val pledgeData = with(
            PledgeFlowContext.NEW_PLEDGE,
            project(project),
            reward(),
            emptyList(),
            null
        )
        val intent = Intent()
            .putExtra(IntentKey.CHECKOUT_DATA, checkoutData)
            .putExtra(IntentKey.PLEDGE_DATA, pledgeData)
            .putExtra(IntentKey.PROJECT, project)

        vm.provideIntent(intent)
        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testTracking_whenCheckoutDataAndPledgeDataExtrasNull() {
        setUpEnvironment(environment())

        val intent = Intent()
            .putExtra(IntentKey.PROJECT, project())

        vm.provideIntent(intent)
        segmentTrack.assertNoValues()
    }
}
