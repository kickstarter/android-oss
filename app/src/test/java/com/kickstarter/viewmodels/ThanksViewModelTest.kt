package com.kickstarter.viewmodels

import android.content.Intent
import android.content.SharedPreferences
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.RefTag.Companion.thanks
import com.kickstarter.libs.featureflag.FFKey
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.libs.preferences.MockBooleanPreference
import com.kickstarter.libs.utils.EventName
import com.kickstarter.mock.MockExperimentsClientType
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
import org.junit.Test
import org.mockito.Mockito
import rx.observers.TestSubscriber
import java.util.Arrays

class ThanksViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: ThanksViewModel.ViewModel
    private val adapterData = TestSubscriber<ThanksData>()
    private val finish = TestSubscriber<Void>()
    private val showGamesNewsletterDialogTest = TestSubscriber<Void>()
    private val showRatingDialogTest = TestSubscriber<Void>()
    private val showConfirmGamesNewsletterDialogTest = TestSubscriber.create<Void>()
    private val startDiscoveryTest = TestSubscriber<DiscoveryParams>()
    private val startProjectTest = TestSubscriber<android.util.Pair<Project, RefTag>>()
    private val showSavedPromptTest = TestSubscriber<Void>()

    private fun setUpEnvironment(environment: Environment) {
        vm = ThanksViewModel.ViewModel(environment)
        vm.outputs.adapterData().subscribe(adapterData)
        vm.outputs.finish().subscribe(finish)
        vm.outputs.showGamesNewsletterDialog().subscribe(showGamesNewsletterDialogTest)
        vm.outputs.showRatingDialog().subscribe(showRatingDialogTest)
        vm.outputs.showConfirmGamesNewsletterDialog().subscribe(
            showConfirmGamesNewsletterDialogTest
        )
        vm.outputs.startDiscoveryActivity().subscribe(startDiscoveryTest)
        vm.outputs.startProjectActivity().subscribe(startProjectTest)
        vm.outputs.showSavedPrompt().subscribe(showSavedPromptTest)
    }

    @Test
    fun testSaveProject() {
        val project = project()
            .toBuilder()
            .category(artCategory())
            .build()
        setUpEnvironment(environment())

        vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

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

        vm.intent(Intent().putExtra(IntentKey.PROJECT, project))
        adapterData.assertValueCount(1)
    }

    @Test
    fun testFinishEmits() {
        setUpEnvironment(environment())

        val intent = Intent()
            .putExtra(IntentKey.PROJECT, project())

        vm.intent(intent)
        vm.inputs.closeButtonClicked()
        finish.assertValueCount(1)
    }

    @Test
    fun testThanksViewModel_showRatingDialog() {
        val hasSeenAppRatingPreference = MockBooleanPreference(false)
        val hasSeenGamesNewsletterPreference = MockBooleanPreference(true)
        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FFKey: FFKey): Boolean {
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

        vm.intent(Intent().putExtra(IntentKey.PROJECT, project()))
        showRatingDialogTest.assertValueCount(0)
    }

    @Test
    fun testThanksViewModel_hideRatingDialog_if_feature_flag_disabled() {
        val hasSeenAppRatingPreference = MockBooleanPreference(false)
        val hasSeenGamesNewsletterPreference = MockBooleanPreference(true)
        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FFKey: FFKey): Boolean {
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

        vm.intent(Intent().putExtra(IntentKey.PROJECT, project()))
        showRatingDialogTest.assertValueCount(1)
    }

    @Test
    fun testThanksViewModel_dontShowRatingDialogIfAlreadySeen() {
        val hasSeenAppRatingPreference = MockBooleanPreference(true)
        val hasSeenGamesNewsletterPreference = MockBooleanPreference(true)
        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FFKey: FFKey): Boolean {
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

        vm.intent(Intent().putExtra(IntentKey.PROJECT, project()))
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
                override fun getBoolean(FFKey: FFKey): Boolean {
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

        vm.intent(Intent().putExtra(IntentKey.PROJECT, project))
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

        vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        showGamesNewsletterDialogTest.assertValueCount(1)
        assertEquals(Arrays.asList(false, true), hasSeenGamesNewsletterPreference.values())
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

        vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        showGamesNewsletterDialogTest.assertValueCount(0)
    }

    @Test
    fun testThanksViewModel_dontShowGamesNewsletterDialogIfUserHasAlreadySeen() {
        val hasSeenGamesNewsletterPreference = MockBooleanPreference(true)
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

        vm.intent(Intent().putExtra(IntentKey.PROJECT, project))
        showGamesNewsletterDialogTest.assertValueCount(0)
    }

    @Test
    fun testThanksViewModel_dontShowGamesNewsletterDialogIfUserHasAlreadySignedUp() {
        val hasSeenGamesNewsletterPreference = MockBooleanPreference(false)
        val user = user().toBuilder().gamesNewsletter(true).build()
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

        vm.intent(Intent().putExtra(IntentKey.PROJECT, project))
        showGamesNewsletterDialogTest.assertValueCount(0)
    }

    @Test
    fun testThanksViewModel_signupToGamesNewsletterOnClick() {
        val user = user().toBuilder().gamesNewsletter(false).build()
        val currentUser: CurrentUserType = MockCurrentUser(user)
        val environment = environment().toBuilder()
            .currentUser(currentUser)
            .build()

        setUpEnvironment(environment)

        val updateUserSettingsTest = TestSubscriber<User>()
        (environment.apiClient() as? MockApiClient)?.observable()
            ?.filter { "update_user_settings" == it.first }
            ?.map { it.second["user"] as? User }
            ?.subscribe(updateUserSettingsTest)

        val project = project()
            .toBuilder()
            .category(tabletopGamesCategory())
            .build()

        vm.intent(Intent().putExtra(IntentKey.PROJECT, project))
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
        val currentUser: CurrentUserType = MockCurrentUser(user)
        val environment = environment().toBuilder()
            .currentUser(currentUser)
            .build()

        setUpEnvironment(environment)

        val project = project().toBuilder().category(tabletopGamesCategory()).build()

        vm.intent(Intent().putExtra(IntentKey.PROJECT, project))
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

        vm.intent(intent)
        vm.inputs.projectCardViewHolderClicked(project)

        val projectPageParams = startProjectTest.onNextEvents[0]

        assertEquals(projectPageParams.first, project)
        assertEquals(projectPageParams.second, thanks())
        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
        assertEquals(null, this.vm.onCAPIEventSent.value)
    }

    @Test
    fun testSendCAPIEvent_whenBackedPRoject_sendCAPIEvent_withFeatureFlag__withConsentManagement_off_isFailed() {
        var sharedPreferences: SharedPreferences = Mockito.mock(SharedPreferences::class.java)
        Mockito.`when`(sharedPreferences.getBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false))
            .thenReturn(false)

        val mockExperimentsClientType: MockExperimentsClientType =
            object : MockExperimentsClientType() {
                override fun isFeatureEnabled(feature: OptimizelyFeature.Key): Boolean {
                    return true
                }
            }

        setUpEnvironment(
            environment().toBuilder()
                .sharedPreferences(sharedPreferences)
                .optimizely(mockExperimentsClientType)
                .build()
        )

        val project = project().toBuilder().sendMetaCapiEvents(true).build()
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

        vm.intent(intent)
        vm.inputs.projectCardViewHolderClicked(project)

        val projectPageParams = startProjectTest.onNextEvents[0]

        assertEquals(projectPageParams.first, project)
        assertEquals(projectPageParams.second, thanks())
        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
        assertEquals(null, this.vm.onCAPIEventSent.value)
    }
    @Test
    fun testSendCAPIEvent_whenBackedPRoject_sendCAPIEvent_withFeatureFlag_on_isSuccessful() {
        var sharedPreferences: SharedPreferences = Mockito.mock(SharedPreferences::class.java)
        Mockito.`when`(sharedPreferences.getBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false))
            .thenReturn(true)

        val mockExperimentsClientType: MockExperimentsClientType =
            object : MockExperimentsClientType() {
                override fun isFeatureEnabled(feature: OptimizelyFeature.Key): Boolean {
                    return true
                }
            }

        setUpEnvironment(
            environment().toBuilder()
                .sharedPreferences(sharedPreferences)
                .optimizely(mockExperimentsClientType)
                .build()
        )

        val project = project().toBuilder().sendMetaCapiEvents(true).build()
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

        vm.intent(intent)
        vm.inputs.projectCardViewHolderClicked(project)

        val projectPageParams = startProjectTest.onNextEvents[0]

        assertEquals(projectPageParams.first, project)
        assertEquals(projectPageParams.second, thanks())
        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
        assertEquals(true, this.vm.onCAPIEventSent.value)
    }

    @Test
    fun testThanksViewModel_whenFeatureFlagOn_shouldEmitProjectPage() {
        val user = MockCurrentUser()
        val mockExperimentsClientType: MockExperimentsClientType =
            object : MockExperimentsClientType() {
                override fun isFeatureEnabled(feature: OptimizelyFeature.Key): Boolean {
                    return true
                }
            }
        val environment = environment().toBuilder()
            .currentUser(user)
            .optimizely(mockExperimentsClientType)
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

        vm.intent(intent)
        vm.inputs.projectCardViewHolderClicked(project)

        val projectPageParams = startProjectTest.onNextEvents[0]
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

        vm.intent(intent)
        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testTracking_whenCheckoutDataAndPledgeDataExtrasNull() {
        setUpEnvironment(environment())

        val intent = Intent()
            .putExtra(IntentKey.PROJECT, project())

        vm.intent(intent)
        segmentTrack.assertNoValues()
    }
}
