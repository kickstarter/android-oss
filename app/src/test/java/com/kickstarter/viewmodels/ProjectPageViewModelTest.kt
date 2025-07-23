package com.kickstarter.viewmodels

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Pair
import androidx.appcompat.widget.Toolbar
import androidx.test.core.app.ApplicationProvider
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.Either
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.ProjectPagerTabs
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.EventName
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.MockCurrentConfigV2
import com.kickstarter.mock.MockFeatureFlagClient
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.CheckoutDataFactory
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.AiDisclosure
import com.kickstarter.models.Backing
import com.kickstarter.models.EnvironmentalCommitment
import com.kickstarter.models.Project
import com.kickstarter.models.Urls
import com.kickstarter.models.User
import com.kickstarter.models.Web
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.activities.ProjectPageActivity
import com.kickstarter.ui.data.ActivityResult
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.MediaElement
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.helpers.createManagePledgeMenuOptions
import com.kickstarter.viewmodels.projectpage.PagerTabConfig
import com.kickstarter.viewmodels.projectpage.ProjectPageViewModel
import com.kickstarter.viewmodels.usecases.TPEventInputData
import io.mockk.coVerify
import io.mockk.slot
import io.mockk.spyk
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subscribers.TestSubscriber
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.mockito.Mockito
import org.robolectric.Robolectric
import java.math.RoundingMode
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class ProjectPageViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: ProjectPageViewModel.ProjectPageViewModel
    private val backingDetailsIsVisible = TestSubscriber<Boolean>()
    private val backingDetailsSubtitle = TestSubscriber<Either<String, Int>?>()
    private val backingDetailsTitle = TestSubscriber<Int>()
    private val expandPledgeSheet = TestSubscriber<Pair<Boolean, Boolean>>()
    private val goBack = TestSubscriber<Unit>()
    private val heartDrawableId = TestSubscriber<Int>()
    private val pledgeActionButtonColor = TestSubscriber<Int>()
    private val pledgeActionButtonContainerIsGone = TestSubscriber<Boolean>()
    private val pledgeActionButtonText = TestSubscriber<Int>()
    private val pledgeToolbarNavigationIcon = TestSubscriber<Int>()
    private val pledgeToolbarTitle = TestSubscriber<Int>()
    private val prelaunchUrl = TestSubscriber<String>()
    private val projectData = TestSubscriber<ProjectData>()
    private val reloadProjectContainerIsGone = TestSubscriber<Boolean>()
    private val reloadProgressBarIsGone = TestSubscriber<Boolean>()
    private val revealRewardsFragment = TestSubscriber<Unit>()
    private val savedTest = TestSubscriber<Boolean>()
    private val scrimIsVisible = TestSubscriber<Boolean>()
    private val setInitialRewardsContainerY = TestSubscriber<Unit>()
    private val showCancelPledgeFragment = TestSubscriber<Project>()
    private val showCancelPledgeSuccess = TestSubscriber<Unit>()
    private val showPledgeNotCancelableDialog = TestSubscriber<Unit>()
    private val showSavedPromptTest = TestSubscriber<Unit>()
    private val showShareSheet = TestSubscriber<Pair<String, String>>()
    private val showUpdatePledge = TestSubscriber<Pair<PledgeData, PledgeReason>>()
    private val showUpdatePledgeSuccess = TestSubscriber<Unit>()
    private val startRootCommentsActivity = TestSubscriber<ProjectData>()
    private val startRootCommentsForCommentsThreadActivity = TestSubscriber<Pair<String, ProjectData>>()
    private val startProjectUpdateActivity = TestSubscriber<Pair<Pair<String, Boolean>, Pair<Project, ProjectData>>>()
    private val startProjectUpdateToRepliesDeepLinkActivity = TestSubscriber<Pair<Pair<String, String>, Pair<Project, ProjectData>>>()
    private val startLoginToutActivity = TestSubscriber<Unit>()
    private val startMessagesActivity = TestSubscriber<Project>()
    private val startThanksActivity = TestSubscriber<Pair<CheckoutData, PledgeData>>()
    private val openBackingDetailsWebview = TestSubscriber<String>()
    private val openPledgeManagerWebview = TestSubscriber<String>()
    private val updateFragments = TestSubscriber<ProjectData>()
    private val projectMedia = BehaviorSubject.create<MediaElement>()
    private val playButtonIsVisible = TestSubscriber<Boolean>()
    private val backingViewGroupIsVisible = TestSubscriber<Boolean>()
    private val updateTabs = TestSubscriber<List<PagerTabConfig>>()
    private val hideVideoPlayer = TestSubscriber<Boolean>()
    private val onOpenVideoInFullScreen = TestSubscriber<kotlin.Pair<String, Long>>()
    private val updateVideoCloseSeekPosition = TestSubscriber<Long>()
    private val postCampaignPledgingEnabled = TestSubscriber<Boolean>()
    private val pledgeRedemptionIsVisible = TestSubscriber<Pair<Project, User>>()

    private val disposables = CompositeDisposable()

    @After
    fun clear() {
        disposables.clear()
    }

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ProjectPageViewModel.ProjectPageViewModel(environment)
        this.vm.outputs.backingDetailsIsVisible().subscribe { this.backingDetailsIsVisible.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.backingDetailsSubtitle().subscribe { this.backingDetailsSubtitle.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.backingDetailsTitle().subscribe { this.backingDetailsTitle.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.expandPledgeSheet().subscribe { this.expandPledgeSheet.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.goBack().subscribe { this.goBack.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.heartDrawableId().subscribe { this.heartDrawableId.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.pledgeActionButtonColor().subscribe { this.pledgeActionButtonColor.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.pledgeActionButtonContainerIsGone().subscribe { this.pledgeActionButtonContainerIsGone.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.pledgeActionButtonText().subscribe { this.pledgeActionButtonText.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.pledgeToolbarNavigationIcon().subscribe { this.pledgeToolbarNavigationIcon.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.pledgeToolbarTitle().subscribe { this.pledgeToolbarTitle.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.prelaunchUrl().subscribe { this.prelaunchUrl.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.projectData().subscribe { this.projectData.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.reloadProgressBarIsGone().subscribe { this.reloadProgressBarIsGone.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.reloadProjectContainerIsGone().subscribe { this.reloadProjectContainerIsGone.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.revealRewardsFragment().subscribe { this.revealRewardsFragment.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.scrimIsVisible().subscribe { this.scrimIsVisible.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.setInitialRewardsContainerY().subscribe { this.setInitialRewardsContainerY.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.showCancelPledgeFragment().subscribe { this.showCancelPledgeFragment.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.showCancelPledgeSuccess().subscribe { this.showCancelPledgeSuccess.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.showPledgeNotCancelableDialog().subscribe { this.showPledgeNotCancelableDialog.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.showSavedPrompt().subscribe { this.showSavedPromptTest.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.showShareSheet().subscribe { this.showShareSheet.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.showUpdatePledge().subscribe { this.showUpdatePledge.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.showUpdatePledgeSuccess().subscribe { this.showUpdatePledgeSuccess.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.startLoginToutActivity().subscribe { this.startLoginToutActivity.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.projectData().map { pD -> pD.project().isStarred() }.subscribe { this.savedTest.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.startRootCommentsActivity().subscribe { this.startRootCommentsActivity.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.startProjectUpdateActivity().subscribe { this.startProjectUpdateActivity.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.startMessagesActivity().subscribe { this.startMessagesActivity.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.startThanksActivity().subscribe { this.startThanksActivity.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.openBackingDetailsWebview().subscribe { this.openBackingDetailsWebview.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.openPledgeManagerWebview().subscribe { this.openPledgeManagerWebview.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.updateFragments().subscribe { this.updateFragments.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.startRootCommentsForCommentsThreadActivity().subscribe { this.startRootCommentsForCommentsThreadActivity.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.startProjectUpdateToRepliesDeepLinkActivity().subscribe { this.startProjectUpdateToRepliesDeepLinkActivity.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.updateTabs().subscribe { this.updateTabs.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.projectMedia().subscribe { this.projectMedia.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.playButtonIsVisible().subscribe { this.playButtonIsVisible.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.backingViewGroupIsVisible().subscribe { this.backingViewGroupIsVisible.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.hideVideoPlayer().subscribe { this.hideVideoPlayer.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.onOpenVideoInFullScreen().subscribe { this.onOpenVideoInFullScreen.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.updateVideoCloseSeekPosition().subscribe { this.updateVideoCloseSeekPosition.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.showLatePledgeFlow().subscribe { this.postCampaignPledgingEnabled.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.showPledgeRedemptionScreen().subscribe { this.pledgeRedemptionIsVisible.onNext(it) }.addToDisposable(disposables)
    }

    @Test
    fun testShowLatePledgeFlow_ProjectDisabled_WhenFFOff() {
        val project = ProjectFactory.project().toBuilder()
            .isInPostCampaignPledgingPhase(false)
            .postCampaignPledgingEnabled(false)
            .build()

        val mockFeatureFlagClient = object : MockFeatureFlagClient() {
            override fun getBoolean(FlagKey: FlagKey): Boolean {
                return false
            }
        }

        val environment = environment()
            .toBuilder()
            .featureFlagClient(mockFeatureFlagClient)
            .build()

        setUpEnvironment(environment)

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, project))
        this.postCampaignPledgingEnabled.assertNoValues()
    }

    @Test
    fun testShowLatePledgeFlow_ProjectDisabled() {
        val project = ProjectFactory.project().toBuilder()
            .isInPostCampaignPledgingPhase(false)
            .postCampaignPledgingEnabled(true)
            .build()

        val environment = environment()
            .toBuilder()
            .build()

        setUpEnvironment(environment)

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, project))
        this.postCampaignPledgingEnabled.assertNoValues()
    }

    @Test
    fun testShowLatePledgeFlow_ProjectEnabled() {
        val project = ProjectFactory.project().toBuilder()
            .isInPostCampaignPledgingPhase(true)
            .postCampaignPledgingEnabled(true)
            .build()

        val environment = environment()
            .toBuilder()
            .build()

        setUpEnvironment(environment)

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, project))
        this.postCampaignPledgingEnabled.assertValue(true)
    }

    @Test
    fun testProjectMedia_whenPhotoNull_shouldNotEmit() {
        val project = ProjectFactory.initialProject().toBuilder().photo(null).build()
        val currentUser = MockCurrentUserV2()
        val environment = environment()
            .toBuilder()
            .apolloClientV2(apolloClientSuccessfulGetProject())
            .currentConfig2(MockCurrentConfigV2())
            .currentUserV2(currentUser)
            .build()

        requireNotNull(environment.currentConfigV2()).config(ConfigFactory.configForUSUser())

        setUpEnvironment(environment)

        // Start the view model with an almost completed project
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, project))

        val media = projectMedia.value
        assertEquals(null, media)
    }

    @Test
    fun testProjectMedia_whenNotNull_shouldEmitPhoto() {
        val project = ProjectFactory.initialProject()
        val currentUser = MockCurrentUserV2()
        val environment = environment()
            .toBuilder()
            .apolloClientV2(apolloClientSuccessfulGetProject())
            .currentUserV2(currentUser)
            .currentConfig2(MockCurrentConfigV2())
            .build()

        requireNotNull(environment.currentConfigV2()).config(ConfigFactory.configForUSUser())

        setUpEnvironment(environment)

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, project))

        val media = projectMedia.value
        assertEquals(
            "https://i.kickstarter.com/assets/012/032/069/46817a8c099133d5bf8b64aad282a696_original.png?fit=crop&gravity=auto&height=873&origin=ugc&q=92&width=1552&sig=M0CDstmWdpd%2FPjzZc8qmMmfj2BiCgtKiH15ugun8qwk%3D",
            media?.thumbnailUrl
        )
        assertEquals(
            "https://ksr-video.imgix.net/projects/1657474/video-506369-h264_high.mp4",
            media?.videoModelElement?.sourceUrl
        )
        assertEquals(
            0L,
            media?.videoModelElement?.seekPosition
        )
    }

    @Test
    fun testProjectMedia_whenFullScreenOpened_shouldEmitOpenVideoInFullScreen() {
        val project = ProjectFactory.initialProject()
        val currentUser = MockCurrentUserV2()
        val environment = environment()
            .toBuilder()
            .apolloClientV2(apolloClientSuccessfulGetProject())
            .currentUserV2(currentUser)
            .currentConfig2(MockCurrentConfigV2())
            .build()

        requireNotNull(environment.currentConfigV2()).config(ConfigFactory.configForUSUser())

        setUpEnvironment(environment)

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, project))
        val videoInfo = kotlin.Pair("https://ksr-video.imgix.net/projects/1657474/video-506369-h264_high.mp4", 12L)
        this.vm.fullScreenVideoButtonClicked(videoInfo)

        onOpenVideoInFullScreen.assertValue(videoInfo)
    }

    @Test
    fun testProjectMedia_whenCloseFullScreenVideo_shouldEmitUpdateVideoCloseSeekPosition() {
        val project = ProjectFactory.initialProject()
        val currentUser = MockCurrentUserV2()
        val environment = environment()
            .toBuilder()
            .apolloClientV2(apolloClientSuccessfulGetProject())
            .currentConfig2(MockCurrentConfigV2())
            .currentUserV2(currentUser)
            .build()

        requireNotNull(environment.currentConfigV2()).config(ConfigFactory.configForUSUser())

        setUpEnvironment(environment)

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.closeFullScreenVideo(15L)

        updateVideoCloseSeekPosition.assertValue(15L)
    }

    @Test
    fun testMediaPlayButton_whenHasVideoFalse_shouldEmitFalse() {
        val project = ProjectFactory.initialProject().toBuilder().video(null).build()
        val currentUser = MockCurrentUserV2()
        val environment = environment()
            .toBuilder()
            .apolloClientV2(apolloClientSuccessfulGetProject())
            .currentUserV2(currentUser)
            .currentConfig2(MockCurrentConfigV2())
            .build()

        requireNotNull(environment.currentConfigV2()).config(ConfigFactory.configForUSUser())

        setUpEnvironment(environment)

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, project))

        playButtonIsVisible.assertValues(false, false)
    }

    @Test
    fun testMediaPlayButton_whenHasVideoTrue_shouldEmitTrue() {
        val project = ProjectFactory.initialProject()
        val currentUser = MockCurrentUserV2()
        val environment = environment()
            .toBuilder()
            .apolloClientV2(apolloClientSuccessfulGetProject())
            .currentConfig2(MockCurrentConfigV2())
            .currentUserV2(currentUser)
            .build()

        requireNotNull(environment.currentConfigV2()).config(ConfigFactory.configForUSUser())

        setUpEnvironment(environment)

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, project))

        playButtonIsVisible.assertValues(true, true)
    }

    @Test
    fun testBackingViewGroup_whenBacking_shouldEmitTrue() {
        val project = ProjectFactory.initialProject().toBuilder().isBacking(true).build()
        val currentUser = MockCurrentUserV2()
        val environment = environment()
            .toBuilder()
            .apolloClientV2(apolloClientSuccessfulGetProject())
            .currentUserV2(currentUser)
            .currentConfig2(MockCurrentConfigV2())
            .build()

        requireNotNull(environment.currentConfigV2()).config(ConfigFactory.configForUSUser())

        setUpEnvironment(environment)

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, project))

        backingViewGroupIsVisible.assertValues(true)
    }

    @Test
    fun testBackingViewGroup_whenPlayButtonClicked_shouldEmitFalse() {
        val project = ProjectFactory.initialProject().toBuilder().isBacking(true).build()
        val currentUser = MockCurrentUserV2()
        val environment = environment()
            .toBuilder()
            .apolloClientV2(apolloClientSuccessfulGetProject())
            .currentConfig2(MockCurrentConfigV2())
            .currentUserV2(currentUser)
            .build()

        requireNotNull(environment.currentConfigV2()).config(ConfigFactory.configForUSUser())

        setUpEnvironment(environment)

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, project))
        this.vm.inputs.onVideoPlayButtonClicked()

        backingViewGroupIsVisible.assertValues(true, false)
    }

    @Test
    fun testBackingViewGroup_whenNotBacking_shouldEmitFalse() {
        val project = ProjectFactory.initialProject().toBuilder().isBacking(false).build()
        val currentUser = MockCurrentUserV2()
        val environment = environment()
            .toBuilder()
            .apolloClientV2(apolloClientSuccessfulGetProject())
            .currentConfig2(MockCurrentConfigV2())
            .currentUserV2(currentUser)
            .build()

        requireNotNull(environment.currentConfigV2()).config(ConfigFactory.configForUSUser())

        setUpEnvironment(environment)

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, project))

        backingViewGroupIsVisible.assertValues(false)
    }

    @Test
    fun testUIOutputs_whenFetchProjectFromIntent_isSuccessful() {
        val initialProject = ProjectFactory.initialProject()
        val refreshedProject = ProjectFactory.project()
        val environment = environment()
            .toBuilder()
            .apolloClientV2(apolloClientSuccessfulGetProject(refreshedProject))
            .build()

        setUpEnvironment(environment)

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, initialProject))

        this.pledgeActionButtonContainerIsGone.assertValues(true, false)
        this.prelaunchUrl.assertNoValues()
        this.projectData.assertValues(ProjectDataFactory.project(refreshedProject))
        this.reloadProjectContainerIsGone.assertValue(true)
        this.reloadProgressBarIsGone.assertValues(false, true)
        this.updateFragments.assertValue(ProjectDataFactory.project(refreshedProject))
        this.segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName)
        assertEquals(null, this.vm.onThirdPartyEventSent.value)
    }

    @Test
    fun testUIOutputs_whenFetchProjectFromIntent_sendThirdPartyEvent_withFeatureFlag_on_isSuccessful() {
        val initialProject = ProjectFactory.initialProject()
        val refreshedProject =
            ProjectFactory.project().toBuilder().sendThirdPartyEvents(true).build()

        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return true
                }
            }

        val mockApolloClientV2: MockApolloClientV2 =
            object : MockApolloClientV2() {
                override fun getProject(project: Project): Observable<Project> {
                    return Observable.just(refreshedProject)
                }

                override fun triggerThirdPartyEvent(eventInput: TPEventInputData): Observable<Pair<Boolean, String>> {
                    return Observable.just(Pair(true, ""))
                }
            }

        var sharedPreferences: SharedPreferences = Mockito.mock(SharedPreferences::class.java)
        Mockito.`when`(sharedPreferences.getBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false)).thenReturn(true)

        val environment = environment()
            .toBuilder()
            .sharedPreferences(sharedPreferences)
            .currentUserV2(MockCurrentUserV2(UserFactory.user()))
            .featureFlagClient(mockFeatureFlagClient)
            .apolloClientV2(mockApolloClientV2)
            .build()

        setUpEnvironment(environment)

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, initialProject))

        this.segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName)
        assertEquals(true, this.vm.onThirdPartyEventSent.value)
    }

    @Test
    fun testUIOutputs_whenFetchProjectFromIntent_sendThirdPartyEvent_withConsentManagement_off_isFailed() {
        val initialProject = ProjectFactory.initialProject()
        val refreshedProject = ProjectFactory.project().toBuilder().sendThirdPartyEvents(true).build()
        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return true
                }
            }

        var sharedPreferences: SharedPreferences = Mockito.mock(SharedPreferences::class.java)
        Mockito.`when`(sharedPreferences.getBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false)).thenReturn(false)

        val environment = environment()
            .toBuilder()
            .sharedPreferences(sharedPreferences)
            .featureFlagClient(mockFeatureFlagClient)
            .currentUserV2(MockCurrentUserV2(UserFactory.user()))
            .apolloClientV2(apolloClientSuccessfulGetProject(refreshedProject))
            .build()

        setUpEnvironment(environment)

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, initialProject))

        this.segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName)
        assertEquals(null, this.vm.onThirdPartyEventSent.value)
    }

    @Test
    fun testUIOutputs_whenFetchProjectFromIntent_sendThirdPartyEvent_withProjectmNotHaveThirdPartyEnabled_isFailed() {
        val initialProject = ProjectFactory.initialProject()
        val refreshedProject = ProjectFactory.project().toBuilder().sendThirdPartyEvents(false).build()
        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return true
                }
            }

        var sharedPreferences: SharedPreferences = Mockito.mock(SharedPreferences::class.java)
        Mockito.`when`(sharedPreferences.getBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false)).thenReturn(true)

        val environment = environment()
            .toBuilder()
            .sharedPreferences(sharedPreferences)
            .featureFlagClient(mockFeatureFlagClient)
            .apolloClientV2(apolloClientSuccessfulGetProject(refreshedProject))
            .build()

        setUpEnvironment(environment)

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, initialProject))

        this.segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName)
        assertEquals(null, this.vm.onThirdPartyEventSent.value)
    }

    @Test
    fun testUIOutputs_whenFetchProjectWithoutEnVCommitment() {
        val initialProject = ProjectFactory.initialProject().toBuilder().envCommitments(
            emptyList
            ()
        ).build()

        setUpEnvironment(
            environment()
                .toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build()
        )

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, initialProject))

        val list = listOf(
            PagerTabConfig(ProjectPagerTabs.USE_OF_AI, false),
            PagerTabConfig(ProjectPagerTabs.ENVIRONMENTAL_COMMITMENT, false)
        )
        this.updateTabs.assertValue(list)
    }

    @Test
    fun testUIOutputs_whenFetchProjectWithAIDisclosure_WithoutEnVCommitment() {
        val initialProject = ProjectFactory
            .initialProject()
            .toBuilder()
            .envCommitments(emptyList())
            .aiDisclosure(
                AiDisclosure
                    .builder()
                    .generatedByAiDetails("Generated by AI details")
                    .build()
            )
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClientV2(apolloClientSuccessfulGetProject())
            .build()

        setUpEnvironment(environment)

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, initialProject))

        val list = listOf(
            PagerTabConfig(ProjectPagerTabs.USE_OF_AI, true),
            PagerTabConfig(ProjectPagerTabs.ENVIRONMENTAL_COMMITMENT, false)
        )

        this.updateTabs.assertValue(list)
    }

    @Test
    fun testUIOutputs_whenFetchProjectWithAIDisclosure_WithEnVCommitment() {
        val initialProject = ProjectFactory
            .initialProject()
            .toBuilder()
            .envCommitments(listOf(EnvironmentalCommitment.builder().build()))
            .aiDisclosure(
                AiDisclosure
                    .builder()
                    .otherAiDetails("Other details string here")
                    .build()
            )
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClientV2(apolloClientSuccessfulGetProject())
            .build()

        setUpEnvironment(environment)

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, initialProject))

        val list = listOf(
            PagerTabConfig(ProjectPagerTabs.USE_OF_AI, true),
            PagerTabConfig(ProjectPagerTabs.ENVIRONMENTAL_COMMITMENT, true)
        )

        this.updateTabs.assertValue(list)
    }

    @Test
    fun testUIOutputs_whenFetchProjectWithEnvCommitment() {
        val initialProject = ProjectFactory.project()
        val refreshedProject = ProjectFactory.project()
        val environment = environment()
            .toBuilder()
            .apolloClientV2(apolloClientSuccessfulGetProject(refreshedProject))
            .build()

        setUpEnvironment(environment)

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, initialProject))

        val list = listOf(
            PagerTabConfig(ProjectPagerTabs.USE_OF_AI, false),
            PagerTabConfig(ProjectPagerTabs.ENVIRONMENTAL_COMMITMENT, true)
        )
        this.updateTabs.assertValue(list)
    }

    @Test
    fun testUIOutputs_whenFetchProjectWithEnvCommitmentAndStoryTabFFDisabled() {
        val initialProject = ProjectFactory.project()
        val refreshedProject = ProjectFactory.project()

        val environment = environment()
            .toBuilder()
            .apolloClientV2(apolloClientSuccessfulGetProject(refreshedProject))
            .build()

        setUpEnvironment(environment)

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, initialProject))

        val list = listOf(
            PagerTabConfig(ProjectPagerTabs.USE_OF_AI, false),
            PagerTabConfig(ProjectPagerTabs.ENVIRONMENTAL_COMMITMENT, true)
        )
        this.updateTabs.assertValue(list)
    }

    @Test
    fun testUIOutputs_whenFetchProjectWithEnvCommitmentAndStoryTabFFEnabled() {
        val initialProject = ProjectFactory.project()
        val refreshedProject = ProjectFactory.project()

        val environment = environment()
            .toBuilder()
            .apolloClientV2(apolloClientSuccessfulGetProject(refreshedProject))
            .build()

        setUpEnvironment(environment)

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, initialProject))

        val list = listOf(
            PagerTabConfig(ProjectPagerTabs.USE_OF_AI, false),
            PagerTabConfig(ProjectPagerTabs.ENVIRONMENTAL_COMMITMENT, true)
        )
        this.updateTabs.assertValue(list)
    }

    @Test
    fun testUIOutputs_whenFetchProjectFromIntent_isUnsuccessful() {
        var error = true
        val initialProject = ProjectFactory.initialProject()
        val refreshedProject = ProjectFactory.project()

        val environment = environment()
            .toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getProject(project: Project): Observable<Project> {
                    val observable = when {
                        error -> Observable.error(Throwable("boop"))
                        else -> {
                            Observable.just(refreshedProject)
                        }
                    }
                    return observable
                }

                override fun getProject(slug: String): Observable<Project> {
                    val observable = when {
                        error -> Observable.error(Throwable("boop"))
                        else -> {
                            Observable.just(refreshedProject)
                        }
                    }
                    return observable
                }
            })
            .build()
        setUpEnvironment(environment)

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, initialProject))

        this.pledgeActionButtonContainerIsGone.assertValue(true)
        this.prelaunchUrl.assertNoValues()
        this.projectData.assertValue(ProjectDataFactory.project(initialProject))
        this.reloadProjectContainerIsGone.assertValue(false)
        this.reloadProgressBarIsGone.assertValues(false, true)
        this.updateFragments.assertNoValues()

        error = false
        this.vm.inputs.reloadProjectContainerClicked()

        this.pledgeActionButtonContainerIsGone.assertValues(true, false)
        this.prelaunchUrl.assertNoValues()
        this.projectData.assertValues(
            ProjectDataFactory.project(initialProject),
            ProjectDataFactory.project(refreshedProject)
        )
        this.reloadProjectContainerIsGone.assertValues(false, true, true)
        this.reloadProgressBarIsGone.assertValues(false, true, false, true)
        this.updateFragments.assertValue(ProjectDataFactory.project(refreshedProject))
        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testUIOutputs_whenFetchProjectFromDeepLink_isSuccessful() {
        val project = ProjectFactory.project()

        val environment = environment()
            .toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getProject(param: String): Observable<Project> {
                    return Observable.just(project)
                }

                override fun getProject(project: Project): Observable<Project> {
                    return Observable.just(project)
                }
            })
            .build()

        setUpEnvironment(environment)
        val intent = deepLinkIntent()
        this.vm.configureWith(intent)

        this.pledgeActionButtonContainerIsGone.assertValues(true, false)
        this.prelaunchUrl.assertNoValues()
        this.projectData.assertValue(ProjectDataFactory.project(project))
        this.reloadProgressBarIsGone.assertValues(false, true)
        this.updateFragments.assertValue(ProjectDataFactory.project(project))
        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testUIOutputs_whenSaveProjectFromDeepLink_isSuccessful() {
        val currentUser = MockCurrentUserV2()
        val project = ProjectFactory.successfulProject()
        val testScheduler = TestScheduler()

        setUpEnvironment(
            environment().toBuilder()
                .currentUserV2(currentUser)
                .apolloClientV2(apolloClientWithSuccessGetProjectFromSlug(project))
                .schedulerV2(testScheduler).build()
        )

        // Start the view model with a project.
        val intent = deepLinkIntent().apply {
            putExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_SAVE, true)
            putExtra(IntentKey.SAVE_FLAG_VALUE, true)
        }
        currentUser.refresh(UserFactory.user())
        this.vm.configureWith(intent)

        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        // The project should be saved, and a save prompt should NOT be shown.
        this.savedTest.assertValues(false, true)
        this.heartDrawableId.assertValues(R.drawable.icon__heart_outline, R.drawable.icon__heart)
        this.showSavedPromptTest.assertValueCount(0)
    }

    @Test
    fun testUIOutputs_whenFetchProjectFromDeepLink_isUnsuccessful() {
        var error = true
        val refreshedProject = ProjectFactory.project()

        val environment = environment()
            .toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getProject(slug: String): Observable<Project> {
                    val observable = when {
                        error -> Observable.error(Throwable("boop"))
                        else -> Observable.just(refreshedProject)
                    }
                    return observable
                }

                override fun getProject(project: Project): Observable<Project> {
                    return Observable.just(project)
                }
            })
            .build()
        setUpEnvironment(environment)

        this.vm.configureWith(deepLinkIntent())

        this.pledgeActionButtonContainerIsGone.assertNoValues()
        this.prelaunchUrl.assertNoValues()
        this.projectData.assertNoValues()
        this.reloadProgressBarIsGone.assertValues(false, true)
        this.reloadProjectContainerIsGone.assertValue(false)
        this.updateFragments.assertNoValues()

        error = false
        this.vm.inputs.reloadProjectContainerClicked()

        this.pledgeActionButtonContainerIsGone.assertValues(true, false)
        this.prelaunchUrl.assertNoValues()
        this.projectData.assertValue(ProjectDataFactory.project(refreshedProject))
        this.reloadProgressBarIsGone.assertValues(false, true, false, true)
        this.reloadProjectContainerIsGone.assertValues(false, true, true)
        this.updateFragments.assertValue(ProjectDataFactory.project(refreshedProject))
        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testUIOutputs_whenFetchProjectReturnsPrelaunchActivatedProject() {
        val url = "https://www.kickstarter.com/projects/1186238668/skull-graphic-tee"
        val project = ProjectFactory.prelaunchProject(url)

        setUpEnvironment(
            environment().toBuilder()
                .apolloClientV2(apolloClientWithSuccessGetProjectFromSlug(project))
                .build()
        )
        val uri = Uri.parse(url)
        this.vm.configureWith(Intent(Intent.ACTION_VIEW, uri))

        this.pledgeActionButtonContainerIsGone.assertNoValues()
        this.prelaunchUrl.assertValue(url)
        this.projectData.assertNoValues()
        this.reloadProgressBarIsGone.assertValues(false, true)
        this.reloadProjectContainerIsGone.assertNoValues()
        this.updateFragments.assertNoValues()
        this.segmentTrack.assertNoValues()
    }

    @Test
    fun testLoggedOutStarProjectFlow() {
        val currentUser = MockCurrentUserV2()
        val project = ProjectFactory.halfWayProject()
        val environment = environment().toBuilder()
            .currentUserV2(currentUser)
            .currentConfig2(MockCurrentConfigV2())
            .apolloClientV2(apolloClientSuccessfulGetProject())
            .build()
        requireNotNull(environment.currentConfigV2()).config(ConfigFactory.configForUSUser())

        setUpEnvironment(environment)

        // Start the view model with a project
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, project))

        this.savedTest.assertValues(false)
        this.heartDrawableId.assertValues(R.drawable.icon__heart_outline)

        // Try starring while logged out
        this.vm.inputs.heartButtonClicked()

        // The project shouldn't be saved, and a login prompt should be shown.
        this.savedTest.assertValues(false)
        this.heartDrawableId.assertValues(R.drawable.icon__heart_outline)
        this.showSavedPromptTest.assertValueCount(0)
        this.startLoginToutActivity.assertValueCount(1)

        // A koala event for starring should NOT be tracked

        // Login
        currentUser.refresh(UserFactory.user())

        // The project should be saved, and a star prompt should be shown.
        this.savedTest.assertValues(false, true)
        this.heartDrawableId.assertValues(R.drawable.icon__heart_outline, R.drawable.icon__heart)
        this.showSavedPromptTest.assertValueCount(1)

        this.segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testShowShareSheet() {
        val creator = UserFactory.creator()
        val slug = "best-project-2k19"
        val projectUrl = "https://www.kck.str/projects/" + creator.id().toString() + "/" + slug

        val webUrls = Web.builder()
            .project(projectUrl)
            .rewards("$projectUrl/rewards")
            .updates("$projectUrl/posts")
            .build()

        val project = ProjectFactory.project()
            .toBuilder()
            .name("Best Project 2K19")
            .urls(Urls.builder().web(webUrls).build())
            .build()

        setUpEnvironment(
            environment()
                .toBuilder()
                .apolloClientV2(apolloClientSuccessfulGetProject())
                .build()
        )
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.shareButtonClicked()
        val expectedName = "Best Project 2K19"
        val expectedShareUrl = "https://www.kck.str/projects/" + creator.id().toString() + "/" + slug + "?ref=android_project_share"

        this.vm.outputs.showShareSheet().subscribe {
            assertTrue(it.first == expectedName)
            assertTrue(it.second == expectedShareUrl)
        }.addToDisposable(disposables)

        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testStarProjectThatIsAlmostCompleted() {
        val currentUser = MockCurrentUserV2()
        val project = ProjectFactory.almostCompletedProject().toBuilder().isStarred(false).build()
        val environment = environment().toBuilder()
            .currentUserV2(currentUser)
            .currentConfig2(MockCurrentConfigV2())
            .apolloClientV2(apolloClientSuccessfulGetProject())
            .build()
        requireNotNull(environment.currentConfigV2()).config(ConfigFactory.configForUSUser())

        setUpEnvironment(environment)

        // Start the view model with a project
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, project))

        // Login
        currentUser.refresh(UserFactory.user())

        // Star the project
        this.vm.inputs.heartButtonClicked()

        // The project should be saved, and a save prompt should NOT be shown.
//        this.savedTest.assertValues(false, true)
//        this.heartDrawableId.assertValues(R.drawable.icon__heart_outline, R.drawable.icon__heart)
        this.showSavedPromptTest.assertValueCount(0)
        this.projectData.assertValues(
            ProjectDataFactory.project(project),
            ProjectDataFactory.project(project.toBuilder().isStarred(true).build()),
        )
    }

    @Test
    fun testSaveProjectThatIsSuccessful() {
        val project = ProjectFactory.successfulProject()
        val currentUser = MockCurrentUserV2()
        val environment = environment().toBuilder()
            .currentUserV2(currentUser)
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getProject(project: Project): Observable<Project> {
                    return Observable.just(project)
                }

                override fun getProject(slug: String): Observable<Project> {
                    return Observable.just(
                        ProjectFactory.project()
                            .toBuilder()
                            .slug(slug)
                            .build()
                    )
                }

                override fun unWatchProject(project: Project): Observable<Project> {
                    val proj = project.toBuilder().isStarred(false).build()
                    return Observable.just(proj)
                }

                override fun watchProject(project: Project): Observable<Project> {
                    val proj = project.toBuilder().isStarred(true).build()
                    return Observable.just(proj)
                }
            })
            .build()

        setUpEnvironment(environment)

        // Start the view model with a successful project
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, project))

        // Login the user
        currentUser.refresh(UserFactory.user())

        // Star the project
        this.vm.inputs.heartButtonClicked()

        // The project should be saved, and a save prompt should NOT be shown.
        this.savedTest.assertValues(false, true)
        this.heartDrawableId.assertValues(R.drawable.icon__heart_outline, R.drawable.icon__heart)
        this.showSavedPromptTest.assertValueCount(0)
    }

    @Test
    fun testUnStarProjectThatIsAlmostCompleted() {
        val project = ProjectFactory.almostCompletedProject().toBuilder().isStarred(true).build()

        val currentUser = MockCurrentUserV2()
        val environment = environment().toBuilder()
            .currentUserV2(currentUser)
            .apolloClientV2(apolloClientSuccessfulGetProject())
            .currentConfig2(MockCurrentConfigV2())
            .build()
        requireNotNull(environment.currentConfigV2()).config(ConfigFactory.configForUSUser())

        setUpEnvironment(environment)

        // Start the view model with an almost completed project
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, project))

        // Login
        currentUser.refresh(UserFactory.user())

        // Star the project
        this.vm.inputs.heartButtonClicked()

        // The project should be saved, and a save prompt should NOT be shown.
        this.savedTest.assertValues(true, false)
        this.heartDrawableId.assertValues(R.drawable.icon__heart, R.drawable.icon__heart_outline)
        this.showSavedPromptTest.assertValueCount(0)
        this.projectData.assertValues(
            ProjectDataFactory.project(project),
            ProjectDataFactory.project(project.toBuilder().isStarred(false).build())
        )
    }

    @Test
    fun testStartCommentsActivityFromDeepLink() {
        val project = ProjectFactory.project()
        val projectData = ProjectDataFactory.project(project)
        val projectAndData = Pair.create(project, projectData)
        val testScheduler = TestScheduler()

        setUpEnvironment(
            environment().toBuilder()
                .apolloClientV2(apolloClientSuccessfulGetProject())
                .schedulerV2(testScheduler).build()
        )

        // Start the view model with a project.
        val intent = Intent().apply {
            putExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_COMMENT, true)
            putExtra(IntentKey.PROJECT, project)
        }

        this.vm.configureWith(intent)

        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        this.startRootCommentsActivity.assertValues(projectData)
    }

    @Test
    fun testStartCommentsThreadActivityFromDeepLink() {
        val commentableId = "Q29tbWVudC0zMzU2MTY4Ng"
        val project = ProjectFactory.project()
        val projectData = ProjectDataFactory.project(project)
        val projectAndData = Pair.create(project, projectData)
        val deepLinkDate = Pair.create(commentableId, projectData)
        val testScheduler = TestScheduler()

        setUpEnvironment(
            environment().toBuilder()
                .apolloClientV2(apolloClientSuccessfulGetProject())
                .schedulerV2(testScheduler).build()
        )

        // Start the view model with a project.
        val intent = Intent().apply {
            putExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_COMMENT, true)
            putExtra(IntentKey.PROJECT, project)
            putExtra(IntentKey.COMMENT, commentableId)
        }

        this.vm.configureWith(intent)

        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        this.startRootCommentsForCommentsThreadActivity.assertValues(deepLinkDate)
    }

    @Test
    fun testStartUpdateActivityFromDeepLink() {
        val project = ProjectFactory.project()
        val projectData = ProjectDataFactory.project(project)
        val projectAndData = Pair.create(project, projectData)
        val postId = "3254626"
        val updateProjectAndData = Pair.create(Pair(postId, false), projectAndData)
        val testScheduler = TestScheduler()

        setUpEnvironment(
            environment().toBuilder()
                .apolloClientV2(apolloClientSuccessfulGetProject())
                .schedulerV2(testScheduler).build()
        )

        // Start the view model with a project.
        val intent = Intent().apply {
            putExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_UPDATE, postId)
            putExtra(IntentKey.PROJECT, project)
        }

        this.vm.configureWith(intent)

        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        this.startProjectUpdateActivity.assertValues(updateProjectAndData)
    }

    @Test
    fun testStartUpdateActivityFromDeepLinkToThreadActivity() {
        val commentableId = "Q29tbWVudC0zMzU2MTY4Ng"
        val project = ProjectFactory.project()
        val projectData = ProjectDataFactory.project(project)
        val projectAndData = Pair.create(project, projectData)
        val postId = "3254626"
        val updateProjectAndData = Pair.create(Pair(postId, commentableId), projectAndData)
        val testScheduler = TestScheduler()

        setUpEnvironment(
            environment().toBuilder()
                .apolloClientV2(apolloClientSuccessfulGetProject())
                .schedulerV2(testScheduler).build()
        )

        // Start the view model with a project.
        val intent = Intent().apply {
            putExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_UPDATE, postId)
            putExtra(IntentKey.PROJECT, project)
            putExtra(IntentKey.COMMENT, commentableId)
        }

        this.vm.configureWith(intent)

        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        this.startProjectUpdateToRepliesDeepLinkActivity.assertValues(updateProjectAndData)
    }

    @Test
    fun testStartUpdateActivityToCommentFromDeepLink() {
        val project = ProjectFactory.project()
        val projectData = ProjectDataFactory.project(project)
        val projectAndData = Pair.create(project, projectData)
        val postId = "3254626"
        val updateProjectAndData = Pair.create(Pair(postId, true), projectAndData)
        val testScheduler = TestScheduler()

        setUpEnvironment(
            environment().toBuilder()
                .apolloClientV2(apolloClientSuccessfulGetProject())
                .schedulerV2(testScheduler).build()
        )

        // Start the view model with a project.
        val intent = Intent().apply {
            putExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_UPDATE, postId)
            putExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_UPDATE_COMMENT, true)
            putExtra(IntentKey.PROJECT, project)
        }

        this.vm.configureWith(intent)

        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        this.startProjectUpdateActivity.assertValues(updateProjectAndData)
    }

    @Test
    fun testPledgeActionButtonUIOutputs_whenProjectIsLiveAndBacked() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.pledgeActionButtonColor.assertValue(R.color.button_pledge_manage)
        this.pledgeActionButtonText.assertValue(R.string.Manage)
    }

    @Test
    fun testPledgeActionButtonUIOutputs_projectIsLiveAndNotBacked() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.pledgeActionButtonColor.assertValue(R.color.button_pledge_live)
        this.pledgeActionButtonText.assertValue(R.string.Back_this_project)
    }

    @Test
    fun testPledgeActionButtonUIOutputs_projectIsLiveAndNotBacked_control() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.pledgeActionButtonColor.assertValue(R.color.button_pledge_live)
        this.pledgeActionButtonText.assertValue(R.string.Back_this_project)
    }

    @Test
    fun testPledgeActionButtonUIOutputs_whenProjectIsEndedAndBacked() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())
        val backedSuccessfulProject = ProjectFactory.backedProject()
            .toBuilder()
            .state(Project.STATE_SUCCESSFUL)
            .build()
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, backedSuccessfulProject))

        this.pledgeActionButtonColor.assertValue(R.color.button_pledge_ended)
        this.pledgeActionButtonText.assertValue(R.string.View_your_pledge)
    }

    @Test
    fun testPledgeActionButtonUIOutputs_whenProjectIsEndedAndNotBacked() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.successfulProject()))

        this.pledgeActionButtonColor.assertValue(R.color.button_pledge_ended)
        this.pledgeActionButtonText.assertValue(R.string.View_rewards)
    }

    @Test
    fun testPledgeActionButtonUIOutputs_whenCurrentUserIsProjectCreator() {
        val creator = UserFactory.creator()
        val creatorProject = ProjectFactory.project()
            .toBuilder()
            .creator(creator)
            .build()
        val environment = environment()
            .toBuilder()
            .apolloClientV2(apolloClientSuccessfulGetProject())
            .currentUserV2(MockCurrentUserV2(creator))
            .build()
        setUpEnvironment(environment)

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, creatorProject))

        this.pledgeActionButtonColor.assertValue(R.color.button_pledge_ended)
        this.pledgeActionButtonText.assertValues(R.string.View_your_rewards)
    }

    @Test
    fun testPledgeActionButtonUIOutputs_whenBackingIsErrored() {
        val backedSuccessfulProject = ProjectFactory.backedProject()
            .toBuilder()
            .backing(BackingFactory.backing(Backing.STATUS_ERRORED))
            .state(Project.STATE_SUCCESSFUL)
            .build()

        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, backedSuccessfulProject))

        this.pledgeActionButtonColor.assertValues(R.color.button_pledge_error)
        this.pledgeActionButtonText.assertValue(R.string.Manage)
    }

    @Test
    fun testPledgeToolbarNavigationIcon() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.pledgeToolbarNavigationIcon.assertValue(R.drawable.ic_arrow_down)

        this.vm.inputs.fragmentStackCount(1)

        this.pledgeToolbarNavigationIcon.assertValues(R.drawable.ic_arrow_down, R.drawable.ic_arrow_back)

        this.vm.inputs.fragmentStackCount(0)

        this.pledgeToolbarNavigationIcon.assertValues(R.drawable.ic_arrow_down, R.drawable.ic_arrow_back, R.drawable.ic_arrow_down)
    }

    @Test
    fun testPledgeToolbarTitle_whenProjectIsLiveAndUnbacked() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.pledgeToolbarTitle.assertValue(R.string.Back_this_project)
    }

    @Test
    fun testPledgeToolbarTitle_whenProjectIsLiveAndBacked() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.pledgeToolbarTitle.assertValue(R.string.Manage_your_pledge)
    }

    @Test
    fun testPledgeToolbarTitle_whenProjectIsEndedAndUnbacked() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.successfulProject()))

        this.pledgeToolbarTitle.assertValue(R.string.View_rewards)
    }

    @Test
    fun testPledgeToolbarTitle_whenProjectIsEndedAndBacked() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())

        val backedSuccessfulProject = ProjectFactory.backedProject()
            .toBuilder()
            .state(Project.STATE_SUCCESSFUL)
            .build()
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, backedSuccessfulProject))

        this.pledgeToolbarTitle.assertValue(R.string.View_your_pledge)
    }

    @Test
    fun testExpandPledgeSheet_whenCollapsingSheet() {
        val mockCurrentUser = MockCurrentUserV2().apply {
            login(UserFactory.user())
        }

        setUpEnvironment(
            environment()
                .toBuilder()
                .apolloClientV2(apolloClientSuccessfulGetProject())
                .currentUserV2(mockCurrentUser)
                .build()
        )

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.vm.inputs.nativeProjectActionButtonClicked()
        this.expandPledgeSheet.assertValue(Pair(true, true))

        this.vm.inputs.pledgeToolbarNavigationClicked()
        this.expandPledgeSheet.assertValues(Pair(true, true), Pair(false, true))
        this.goBack.assertNoValues()
        this.segmentTrack.assertValues(
            EventName.PAGE_VIEWED.eventName,
            EventName.CTA_CLICKED.eventName
        )
    }

    @Test
    fun testExpandPledgeSheet_whenProjectLiveAndNotBacked() {
        val mockCurrentUser = MockCurrentUserV2().apply {
            login(UserFactory.user())
        }

        setUpEnvironment(
            environment()
                .toBuilder()
                .apolloClientV2(apolloClientSuccessfulGetProject())
                .currentUserV2(mockCurrentUser)
                .build()
        )
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.vm.inputs.nativeProjectActionButtonClicked()

        this.expandPledgeSheet.assertValue(Pair(true, true))

        this.segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testExpandPledgeSheet_whenProjectLiveAndBacked() {
        val mockCurrentUser = MockCurrentUserV2().apply {
            login(UserFactory.user())
        }

        setUpEnvironment(
            environment()
                .toBuilder()
                .apolloClientV2(apolloClientSuccessfulGetProject())
                .currentUserV2(mockCurrentUser)
                .build()
        )
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.vm.inputs.nativeProjectActionButtonClicked()

        this.expandPledgeSheet.assertValue(Pair(true, true))
        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testExpandPledgeSheet_whenProjectBackedAndErrored() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())
        val backing = BackingFactory.backing(Backing.STATUS_ERRORED)
        val project = ProjectFactory.backedSuccessfulProject().toBuilder().backing(backing).build()
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.nativeProjectActionButtonClicked()
        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testExpandPledgeSheet_whenProjectEndedAndNotBacked() {
        val mockCurrentUser = MockCurrentUserV2().apply {
            login(UserFactory.user())
        }

        setUpEnvironment(
            environment()
                .toBuilder()
                .apolloClientV2(apolloClientSuccessfulGetProject())
                .currentUserV2(mockCurrentUser)
                .build()
        )
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.successfulProject()))

        this.vm.inputs.nativeProjectActionButtonClicked()

        this.expandPledgeSheet.assertValue(Pair(true, true))
        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testExpandPledgeSheet_whenProjectEndedAndBacked() {
        val mockCurrentUser = MockCurrentUserV2().apply {
            login(UserFactory.user())
        }

        setUpEnvironment(
            environment()
                .toBuilder()
                .apolloClientV2(apolloClientSuccessfulGetProject())
                .currentUserV2(mockCurrentUser)
                .build()
        )

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedSuccessfulProject()))

        this.vm.inputs.nativeProjectActionButtonClicked()

        this.expandPledgeSheet.assertValue(Pair(true, true))
        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testExpandPledgeSheet_whenComingBackFromProjectPage_OKResult() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.vm.activityResult(ActivityResult.create(ActivityRequestCodes.SHOW_REWARDS, Activity.RESULT_OK, null))

        this.expandPledgeSheet.assertValue(Pair(true, true))
        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testExpandPledgeSheet_whenComingBackFromProjectPage_CanceledResult() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.vm.activityResult(ActivityResult.create(ActivityRequestCodes.SHOW_REWARDS, Activity.RESULT_CANCELED, null))

        this.expandPledgeSheet.assertNoValues()
        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testExpandPledgeSheet_whenIntentExpandPledgeSheet_isTrue() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())
        val intent = Intent()
            .putExtra(IntentKey.PROJECT, ProjectFactory.project())
            .putExtra(IntentKey.EXPAND_PLEDGE_SHEET, true)
        this.vm.configureWith(intent)

        this.expandPledgeSheet.assertValues(Pair(true, true))
        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testExpandPledgeSheet_whenIntentExpandPledgeSheet_isFalse() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())
        val intent = Intent()
            .putExtra(IntentKey.PROJECT, ProjectFactory.project())
            .putExtra(IntentKey.EXPAND_PLEDGE_SHEET, false)
        this.vm.configureWith(intent)

        this.expandPledgeSheet.assertNoValues()
        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testExpandPledgeSheet_whenIntentExpandPledgeSheet_isNull() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())
        val intent = Intent()
            .putExtra(IntentKey.PROJECT, ProjectFactory.project())
        this.vm.configureWith(intent)

        this.expandPledgeSheet.assertNoValues()
        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testGoBack_whenFragmentBackStackIsEmpty() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.vm.inputs.pledgeToolbarNavigationClicked()
        this.goBack.assertNoValues()
    }

    @Test
    fun testGoBack_whenFragmentBackStackIsNotEmpty() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.vm.inputs.fragmentStackCount(3)
        this.vm.inputs.pledgeToolbarNavigationClicked()
        this.goBack.assertValueCount(1)

        this.vm.inputs.fragmentStackCount(2)
        this.vm.inputs.pledgeToolbarNavigationClicked()
        this.goBack.assertValueCount(2)
        this.expandPledgeSheet.assertNoValues()
    }

    @Test
    fun testSetInitialRewardsContainerY() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())
        this.vm.inputs.onGlobalLayout()
        this.setInitialRewardsContainerY.assertValueCount(1)
    }

    @Test
    fun testBackingDetails_whenProjectNotBacked() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))
        this.backingDetailsIsVisible.assertValue(false)
        this.backingDetailsSubtitle.assertNoValues()
        this.backingDetailsTitle.assertNoValues()
    }

    @Test
    fun testBackingDetails_whenShippableRewardBacked() {
        val environment = environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build()

        setUpEnvironment(environment)
        val reward = RewardFactory.reward()
            .toBuilder()
            .id(4)
            .build()

        val amount = 34.0
        val backing = BackingFactory.backing()
            .toBuilder()
            .amount(amount)
            .shippingAmount(4f)
            .rewardId(4)
            .build()

        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .backing(backing)
            .name("lednrgier")
            .rewards(listOf(reward))
            .build()

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, backedProject))
        this.backingDetailsIsVisible.assertValue(true)
        val expectedCurrency = expectedCurrency(environment, backedProject, amount)
        this.backingDetailsSubtitle.assertValue(Either.Left("$expectedCurrency • Digital Bundle"))
        this.backingDetailsTitle.assertValue(R.string.Youre_a_backer)
    }

    @Test
    fun testBackingDetails_whenDigitalReward() {
        val environment = environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build()

        setUpEnvironment(environment)
        val amount = 13.5
        val noRewardBacking = BackingFactory.backing()
            .toBuilder()
            .amount(amount)
            .reward(RewardFactory.noReward())
            .build()

        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .name("srjih234")
            .backing(noRewardBacking)
            .build()

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, backedProject))
        this.backingDetailsIsVisible.assertValue(true)
        val expectedCurrency = expectedCurrency(environment, backedProject, amount)
        this.backingDetailsSubtitle.assertValue(Either.Left(expectedCurrency))
        this.backingDetailsTitle.assertValue(R.string.Youre_a_backer)
    }

    @Test
    fun testBackingDetails_whenBackingIsErrored() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())

        val backedSuccessfulProject = ProjectFactory.backedProject()
            .toBuilder()
            .name("defrjngiejrhgie")
            .backing(BackingFactory.backing(Backing.STATUS_ERRORED))
            .state(Project.STATE_SUCCESSFUL)
            .build()

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, backedSuccessfulProject))
        this.backingDetailsIsVisible.assertValue(true)
        this.backingDetailsSubtitle.assertValue(Either.Right(R.string.We_cant_process_your_pledge))
        this.backingDetailsTitle.assertValue(R.string.Payment_failure)
    }

    @Test
    fun testScrimIsVisible_whenNotBackedProject() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.vm.inputs.fragmentStackCount(0)
        this.scrimIsVisible.assertValue(false)

        this.vm.inputs.fragmentStackCount(1)
        this.scrimIsVisible.assertValue(false)

        this.vm.inputs.fragmentStackCount(2)
        this.scrimIsVisible.assertValues(false)

        this.vm.inputs.fragmentStackCount(3)
        this.scrimIsVisible.assertValues(false)

        this.vm.inputs.fragmentStackCount(1)
        this.scrimIsVisible.assertValues(false)
    }

    @Test
    fun testScrimIsVisible_whenBackedProject() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.vm.inputs.fragmentStackCount(0)
        this.scrimIsVisible.assertValue(false)

        this.vm.inputs.fragmentStackCount(1)
        this.scrimIsVisible.assertValue(false)

        this.vm.inputs.fragmentStackCount(2)
        this.scrimIsVisible.assertValues(false)

        this.vm.inputs.fragmentStackCount(3)
        this.scrimIsVisible.assertValues(false)

        this.vm.inputs.fragmentStackCount(2)
        this.scrimIsVisible.assertValues(false)
    }

    @Test
    fun testCancelPledgeSuccess() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())

        // Start the view model with a backed project
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.projectData.assertValueCount(1)

        this.vm.inputs.pledgeSuccessfullyCancelled()
        this.expandPledgeSheet.assertValue(Pair(false, false))
        this.showCancelPledgeSuccess.assertValueCount(1)
        this.projectData.assertValueCount(2)
    }

    @Test
    fun testShowCancelPledgeFragment_whenBackingIsCancelable() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())

        val backing = BackingFactory.backing()
            .toBuilder()
            .cancelable(true)
            .build()

        // Start the view model with a backed project
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .backing(backing)
            .name("erfghuvwshd o231242")
            .build()
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.nativeProjectActionButtonClicked()
        this.vm.inputs.cancelPledgeClicked()

        this.showCancelPledgeFragment.assertValue(backedProject)
        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testShowCancelPledgeFragment_whenBackingIsNotCancelable() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())

        val backing = BackingFactory.backing()
            .toBuilder()
            .cancelable(false)
            .build()

        // Start the view model with a backed project
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .name("lsdfjnvesowrf")
            .backing(backing)
            .build()
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.nativeProjectActionButtonClicked()
        this.vm.inputs.cancelPledgeClicked()

        this.showCancelPledgeFragment.assertNoValues()
        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testShowConversation() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())

        // Start the view model with a backed project
        val backedProject = ProjectFactory.backedProject()
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.nativeProjectActionButtonClicked()
        this.vm.inputs.contactCreatorClicked()

        this.startMessagesActivity.assertValue(backedProject)
        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testShowPledgeNotCancelableDialog_whenBackingIsCancelable() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())
        val backing = BackingFactory.backing()
            .toBuilder()
            .cancelable(true)
            .build()

        // Start the view model with a backed project
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .name("djgnfbkdjnfslv")
            .backing(backing)
            .build()
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.cancelPledgeClicked()
        this.showPledgeNotCancelableDialog.assertNoValues()
    }

    @Test
    fun testShowPledgeNotCancelableDialog_whenBackingIsNotCancelable() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())

        val backing = BackingFactory.backing()
            .toBuilder()
            .cancelable(false)
            .build()

        // Start the view model with a backed project
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .name("fvdofjvos")
            .backing(backing)
            .build()
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.cancelPledgeClicked()
        this.showPledgeNotCancelableDialog.assertValueCount(1)
    }

    @Test
    fun testRevealRewardsFragment_whenBackedProjectLive() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())

        // Start the view model with a backed project
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.vm.inputs.nativeProjectActionButtonClicked()
        this.vm.inputs.viewRewardsClicked()

        this.revealRewardsFragment.assertValueCount(1)
        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testRevealRewardsFragment_whenBackedProjectEnded() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())

        // Start the view model with a backed project
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedSuccessfulProject()))

        this.vm.inputs.nativeProjectActionButtonClicked()
        this.vm.inputs.viewRewardsClicked()

        this.revealRewardsFragment.assertValueCount(1)

        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    // TODO this will be fixed in https://kickstarter.atlassian.net/browse/NT-1390
    /*
    @Test
    fun testShowUpdatePledge_whenFixingPaymentMethod() {
        setUpEnvironment(environment())

        // Start the view model with a backed project
        val reward = RewardFactory.reward()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(BackingFactory.backing()
                        .toBuilder()
                        .rewardId(reward.id())
                        .build())
                .rewards(listOf(RewardFactory.noReward(), reward))
                .build()

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.fixPaymentMethodButtonClicked()

        this.showUpdatePledge.assertValuesAndClear(Pair(PledgeData.builder()
                .pledgeFlowContext(PledgeFlowContext.FIX_ERRORED_PLEDGE)
                .reward(reward)
                .projectData(ProjectDataFactory.project(backedProject))
                .build(), PledgeReason.FIX_PLEDGE))
        this.koalaTest.assertValue("Project Page")
        this.lakeTest.assertValues("Project Page Viewed","Fix Pledge Button Clicked")
    }

    @Test
    fun testShowUpdatePledge_whenUpdatingPledge() {
        setUpEnvironment(environment())

        // Start the view model with a backed project
        val reward = RewardFactory.reward()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(BackingFactory.backing()
                        .toBuilder()
                        .rewardId(reward.id())
                        .build())
                .rewards(listOf(RewardFactory.noReward(), reward))
                .build()

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.updatePledgeClicked()

        this.showUpdatePledge.assertValuesAndClear(Pair(PledgeData.builder()
                .pledgeFlowContext(PledgeFlowContext.MANAGE_REWARD)
                .reward(reward)
                .projectData(ProjectDataFactory.project(backedProject))
                .build(), PledgeReason.UPDATE_PLEDGE))
        this.koalaTest.assertValues("Project Page", "Manage Pledge Option Clicked")
        this.lakeTest.assertValue("Project Page Viewed")
    }

    @Test
    fun testShowUpdatePledge_whenUpdatingPaymentMethod() {
        setUpEnvironment(environment())

        // Start the view model with a backed project
        val reward = RewardFactory.reward()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(BackingFactory.backing()
                        .toBuilder()
                        .rewardId(reward.id())
                        .build())
                .rewards(listOf(RewardFactory.noReward(), reward))
                .build()

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.updatePaymentClicked()

        this.showUpdatePledge.assertValuesAndClear(Pair(PledgeData.builder()
                .pledgeFlowContext(PledgeFlowContext.MANAGE_REWARD)
                .reward(reward)
                .projectData(ProjectDataFactory.project(backedProject))
                .build(), PledgeReason.UPDATE_PAYMENT))
        this.koalaTest.assertValues("Project Page", "Manage Pledge Option Clicked")
        this.lakeTest.assertValue("Project Page Viewed")
    }*/

    @Test
    fun testSendingAnalyticsEvents_whenUpdatingPaymentMethod() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())

        // Start the view model with a backed project
        val reward = RewardFactory.reward()
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .backing(
                BackingFactory.backing()
                    .toBuilder()
                    .rewardId(reward.id())
                    .build()
            )
            .name("rfgjoeidjrg")
            .rewards(listOf(RewardFactory.noReward(), reward))
            .build()

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.updatePaymentClicked()

        this.segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testShowUpdatePledgeSuccess_whenUpdatingPayment() {
        val initialBackedProject = ProjectFactory.backedProject()
        val refreshedProject = initialBackedProject.toBuilder()
            .id(9L)
            .build()
        val environment = environment()
            .toBuilder()
            .apolloClientV2(apolloClientWithSuccessGetProjectFromSlug(refreshedProject))
            .build()
        setUpEnvironment(environment)

        // Start the view model with a backed project
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, initialBackedProject))

        this.projectData.assertValues(ProjectDataFactory.project(initialBackedProject))
        this.showUpdatePledgeSuccess.assertNoValues()
        this.updateFragments.assertValue(ProjectDataFactory.project(initialBackedProject))

        this.vm.inputs.pledgePaymentSuccessfullyUpdated()
        this.projectData.assertValues(
            ProjectDataFactory.project(initialBackedProject),
            ProjectDataFactory.project(refreshedProject)
        )
        this.showUpdatePledgeSuccess.assertValueCount(1)
        this.updateFragments.assertValues(
            ProjectDataFactory.project(initialBackedProject),
            ProjectDataFactory.project(refreshedProject),
            ProjectDataFactory.project(refreshedProject)
        )
    }

    @Test
    fun testShowUpdatePledgeSuccess_whenUpdatingPledge() {
        val initialBackedProject = ProjectFactory.backedProject()
        val refreshedProject = initialBackedProject.toBuilder()
            .id(9L)
            .build()
        val environment = environment()
            .toBuilder()
            .apolloClientV2(apolloClientWithSuccessGetProjectFromSlug(refreshedProject))
            .build()
        setUpEnvironment(environment)

        // Start the view model with a backed project
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, initialBackedProject))

        this.projectData.assertValues(ProjectDataFactory.project(initialBackedProject))
        this.showUpdatePledgeSuccess.assertNoValues()
        this.updateFragments.assertValue(ProjectDataFactory.project(initialBackedProject))

        this.vm.inputs.pledgeSuccessfullyUpdated()
        this.projectData.assertValues(
            ProjectDataFactory.project(initialBackedProject),
            ProjectDataFactory.project(refreshedProject)
        )
        this.showUpdatePledgeSuccess.assertValueCount(1)
        this.updateFragments.assertValues(
            ProjectDataFactory.project(initialBackedProject),
            ProjectDataFactory.project(refreshedProject),
            ProjectDataFactory.project(refreshedProject)
        )
    }

    @Test
    fun testStartThanksActivity() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())

        // Start the view model with a unbacked project
        val project = ProjectFactory.project()
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, project))

        this.projectData.assertValueCount(1)

        val checkoutData = CheckoutDataFactory.checkoutData(3L, 20.0, 30.0)
        val pledgeData = PledgeData.with(PledgeFlowContext.NEW_PLEDGE, ProjectDataFactory.project(project), RewardFactory.reward())
        this.vm.inputs.pledgeSuccessfullyCreated(Pair(checkoutData, pledgeData))
        this.expandPledgeSheet.assertValue(Pair(false, false))
        this.startThanksActivity.assertValue(Pair(checkoutData, pledgeData))
        this.projectData.assertValueCount(2)
    }

    @Test
    fun `test open Backing Details Webview when order not null and feature flag on`() { // The testExpandPledgeSheet_ suite of tests covers projects that do not have PM checkout
        val mockFeatureFlagClient = object : MockFeatureFlagClient() {
            override fun getBoolean(FlagKey: FlagKey): Boolean {
                return true
            }
        }
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).featureFlagClient(mockFeatureFlagClient).build())

        // Start the view model with a backed project with PM checkout order
        val backedProject = ProjectFactory.backedProjectWithPMCheckoutOrder()
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.nativeProjectActionButtonClicked()

        val url = "https://ksr.com/backing/survey_responses"

        this.expandPledgeSheet.assertNoValues()
        this.openBackingDetailsWebview.assertValue(url)
        this.openPledgeManagerWebview.assertNoValues()
    }

    @Test
    fun `test open Pledge Manager Webview when project is actively accepting new backers to PM, and feature flag on`() {
        val mockFeatureFlagClient = object : MockFeatureFlagClient() {
            override fun getBoolean(FlagKey: FlagKey): Boolean {
                return true
            }
        }
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).featureFlagClient(mockFeatureFlagClient).build())

        // Start the view model with a project that is actively accepting new backers to PM
        val backedProject = ProjectFactory.projectWithActivePMForNewBackers()
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.nativeProjectActionButtonClicked()

        val url = "https://www.kickstarter.com/backing/redeem"

        this.expandPledgeSheet.assertNoValues()
        this.openBackingDetailsWebview.assertNoValues()
        this.openPledgeManagerWebview.assertValue(url)
    }

    @Test
    fun `test open pledge sheet when order not null but feature flag off`() {
        val mockFeatureFlagClient = object : MockFeatureFlagClient() {
            override fun getBoolean(flagKey: FlagKey): Boolean = false
        }

        val backedProject = ProjectFactory.backedProjectWithPMCheckoutOrder()
        val user = UserFactory.user()
        val mockCurrentUser = MockCurrentUserV2().apply { login(user) }

        setUpEnvironment(
            environment()
                .toBuilder()
                .apolloClientV2(apolloClientSuccessfulGetProject())
                .featureFlagClient(mockFeatureFlagClient)
                .currentUserV2(mockCurrentUser)
                .build()
        )

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.nativeProjectActionButtonClicked()

        this.openBackingDetailsWebview.assertNoValues()
        this.openPledgeManagerWebview.assertNoValues()
        this.expandPledgeSheet.assertValue(Pair(true, true))
    }

    @Test
    fun `test open pledge sheet when order null but feature flag on`() {
        val mockFeatureFlagClient = object : MockFeatureFlagClient() {
            override fun getBoolean(flagKey: FlagKey): Boolean = false
        }

        val backing = BackingFactory.backing().toBuilder().order(null).build()
        val backedProject = ProjectFactory.project()
            .toBuilder()
            .isBacking(true)
            .backing(backing)
            .build()

        val mockCurrentUser = MockCurrentUserV2().apply {
            login(UserFactory.user())
        }

        setUpEnvironment(
            environment()
                .toBuilder()
                .apolloClientV2(apolloClientSuccessfulGetProject())
                .featureFlagClient(mockFeatureFlagClient)
                .currentUserV2(mockCurrentUser)
                .build()
        )

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.nativeProjectActionButtonClicked()

        this.openBackingDetailsWebview.assertNoValues()
        this.expandPledgeSheet.assertValue(Pair(true, true))
    }

    @Test
    fun testProjectData_whenRefreshProjectIsCalled() {
        setUpEnvironment(environment().toBuilder().apolloClientV2(apolloClientSuccessfulGetProject()).build())

        // Start the view model with a backed project
        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject().toBuilder().name("fvkjn dxszhaouefndvlsdf").build()))

        this.projectData.assertValueCount(1)

        this.vm.inputs.refreshProject()
        this.projectData.assertValueCount(2)
    }

    @Test
    fun testProjectData_whenTabSelected() {
        val testScheduler = TestScheduler()

        setUpEnvironment(
            environment().toBuilder()
                .apolloClientV2(apolloClientSuccessfulGetProject())
                .schedulerV2(testScheduler).build()
        )

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))
        this.projectData.assertValueCount(1)

        // - the tab of the viewpager on position 1 has been pressed
        this.vm.inputs.tabSelected(1)
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)
        this.projectData.assertValueCount(2)
        this.segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.PAGE_VIEWED.eventName)

        // - the tab of the viewpager on position 0 has been pressed
        this.vm.inputs.tabSelected(0)
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)
        this.projectData.assertValueCount(3)
        this.segmentTrack.assertValues(
            EventName.PAGE_VIEWED.eventName,
            EventName.PAGE_VIEWED
                .eventName,
            EventName.PAGE_VIEWED.eventName
        )
    }

    @Test
    fun testHideVideoPlayer_whenOverviewSelected_returnFalse() {
        val testScheduler = TestScheduler()

        setUpEnvironment(
            environment().toBuilder()
                .apolloClientV2(apolloClientSuccessfulGetProject())
                .schedulerV2(testScheduler).build()
        )

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.vm.inputs.tabSelected(0)
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)

        this.hideVideoPlayer.assertValues(false)
    }

    @Test
    fun testHideVideoPlayer_whenOverviewNotSelected_returnTrue() {
        val testScheduler = TestScheduler()

        setUpEnvironment(
            environment().toBuilder()
                .apolloClientV2(apolloClientSuccessfulGetProject())
                .schedulerV2(testScheduler).build()
        )

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project().toBuilder().name("lrfnvoaiq1").build()))

        this.vm.inputs.tabSelected(1)
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)

        this.hideVideoPlayer.assertValues(true)
    }

    private fun apolloClientSuccessfulGetProject(refreshedProject: Project): MockApolloClientV2 {
        return object : MockApolloClientV2() {
            override fun getProject(project: Project): Observable<Project> {
                return Observable.just(refreshedProject)
            }
        }
    }

    private fun apolloClientSuccessfulGetProject(): MockApolloClientV2 {
        return object : MockApolloClientV2() {
            override fun getProject(project: Project): Observable<Project> {
                return Observable.just(project)
            }
        }
    }

    private fun apolloClientWithSuccessGetProjectFromSlug(refreshedProject: Project): MockApolloClientV2 {
        return object : MockApolloClientV2() {
            override fun getProject(slug: String): Observable<Project> {
                return Observable.just(refreshedProject)
            }

            override fun getProject(project: Project): Observable<Project> {
                return Observable.just(project)
            }
        }
    }

    @Test
    fun testUIOutputs_whenSaveProjectFromDeepLinkURI_isSuccessful() {
        val currentUser = MockCurrentUserV2()
        val project = ProjectFactory.successfulProject().toBuilder().name("wqeefcnvs dlp").build()
        val testScheduler = TestScheduler()

        setUpEnvironment(
            environment().toBuilder()
                .currentUserV2(currentUser)
                .apolloClientV2(apolloClientWithSuccessGetProjectFromSlug(project))
                .schedulerV2(testScheduler).build()
        )

        // Start the view model with a project.
        val intent = Intent().apply {
            data = Uri.parse("ksr://www.kickstarter.com/projects/1186238668/skull-graphic-tee?save=true")
        }
        currentUser.refresh(UserFactory.user())
        this.vm.configureWith(intent)

        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        // The project should be saved, and a save prompt should NOT be shown.
        this.savedTest.assertValues(false, true)
        this.heartDrawableId.assertValues(R.drawable.icon__heart_outline, R.drawable.icon__heart)
        this.showSavedPromptTest.assertValueCount(0)
    }

    @Test
    fun `Test Pledge Redemption button is visible for admin users when the project is backed and feature flag enabled`() {
        val user = UserFactory.user().toBuilder().isAdmin(true).build()
        val project = ProjectFactory.backedProject()
        val currentUserMock = MockCurrentUserV2(user)
        val mockFeatureFlagClient = object : MockFeatureFlagClient() {
            override fun getBoolean(FlagKey: FlagKey): Boolean {
                return true
            }
        }
        setUpEnvironment(
            environment().toBuilder()
                .currentUserV2(currentUserMock)
                .featureFlagClient(mockFeatureFlagClient)
                .build()
        )

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, project))

        pledgeRedemptionIsVisible.assertValueCount(2)
        this.vm.outputs.showPledgeRedemptionScreen().subscribe {
            assertEquals(it.first, project)
            assertEquals(it.second, user)
        }.addToDisposable(disposables)
    }

    @Test
    fun `Test Pledge Redemption button is NOT visible for admin users when the project is backed and feature flag disabled`() {
        val user = UserFactory.user().toBuilder().isAdmin(true).build()
        val project = ProjectFactory.backedProject()
        val currentUserMock = MockCurrentUserV2(user)
        val mockFeatureFlagClient = object : MockFeatureFlagClient() {
            override fun getBoolean(FlagKey: FlagKey): Boolean {
                return false
            }
        }
        setUpEnvironment(
            environment().toBuilder()
                .currentUserV2(currentUserMock)
                .featureFlagClient(mockFeatureFlagClient)
                .build()
        )

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, project))

        pledgeRedemptionIsVisible.assertNoValues()
    }

    @Test
    fun `Test Pledge Redemption button is NOT visible for users (not admin) when the project is backed and feature flag enabled`() {
        val user = UserFactory.user().toBuilder().isAdmin(false).build()
        val project = ProjectFactory.backedProject()
        val currentUserMock = MockCurrentUserV2(user)
        val mockFeatureFlagClient = object : MockFeatureFlagClient() {
            override fun getBoolean(FlagKey: FlagKey): Boolean {
                return true
            }
        }
        setUpEnvironment(
            environment().toBuilder()
                .currentUserV2(currentUserMock)
                .featureFlagClient(mockFeatureFlagClient)
                .build()
        )

        this.vm.configureWith(Intent().putExtra(IntentKey.PROJECT, project))

        pledgeRedemptionIsVisible.assertNoValues()
    }

    @Test
    fun addUserToSecretRewardGroup_isCalled_whenDeeplinkContainsSecretToken() {
        val secretToken = "secret_123"
        val user = UserFactory.user()
        val currentUserMock = MockCurrentUserV2(user)
        val deeplinkUri = Uri.parse("https://www.kickstarter.com/projects/cool-project?secret_reward_token=$secretToken")
        val project = ProjectFactory.project()
        val capturedProject = slot<Project>()
        val capturedToken = slot<String>()
        var timesCalled = 0
        val mockedApollo = object : MockApolloClientV2() {
            override suspend fun addUserToSecretRewardGroup(
                project: Project,
                secretRewardToken: String
            ): Result<Project> {
                timesCalled ++
                return Result.success(project)
            }
        }

        val mockApolloClient = spyk(mockedApollo, recordPrivateCalls = true)
        val environment = environment().toBuilder()
            .currentUserV2(currentUserMock)
            .apolloClientV2(mockApolloClient)
            .build()

        setUpEnvironment(environment)

        val intent = Intent().putExtra(IntentKey.PROJECT, project).setData(deeplinkUri)
        vm.configureWith(intent)

        runBlocking {
            delay(500)
        }

        coVerify {
            mockApolloClient.addUserToSecretRewardGroup(capture(capturedProject), capture(capturedToken))
        }

        assertTrue(timesCalled == 1)
        assertEquals(secretToken, capturedToken.captured)
    }

    @Test
    fun continuePledgeFlow_userNotLoggedInThenLogsIn_callbackIsInvoked() {
        val currentUser = MockCurrentUserV2() // logged out by default
        val environment = environment().toBuilder()
            .currentUserV2(currentUser)
            .build()

        setUpEnvironment(environment)

        val callbackInvoked = AtomicBoolean(false)

        // Start the pledge flow with a callback
        vm.inputs.continuePledgeFlow {
            callbackInvoked.set(true)
        }

        // Assert: login prompt triggered, but callback not yet run
        this.startLoginToutActivity.assertValueCount(1)
        assertFalse(callbackInvoked.get())

        // Simulate login
        currentUser.refresh(UserFactory.user())

        // Assert: callback now triggered
        assertTrue(callbackInvoked.get())
    }

    @Test
    fun continuePledgeFlow_userAlreadyLoggedIn_callbackIsInvokedImmediately() {
        val user = UserFactory.user()
        val currentUser = MockCurrentUserV2(user) // already logged in

        val environment = environment().toBuilder()
            .currentUserV2(currentUser)
            .build()

        setUpEnvironment(environment)

        val callbackInvoked = AtomicBoolean(false)

        // Start the pledge flow
        vm.inputs.continuePledgeFlow {
            callbackInvoked.set(true)
        }

        // Login prompt should NOT be shown
        this.startLoginToutActivity.assertNoValues()

        // Callback should be invoked immediately
        assertTrue(callbackInvoked.get())
    }

    @Test
    fun `manage pledge menu is inflated when project is backed`() {
        val project = ProjectFactory.backedProject()
        val intent = Intent(ApplicationProvider.getApplicationContext(), ProjectPageActivity::class.java)
        intent.putExtra(IntentKey.PROJECT, project)

        val activity = Robolectric.buildActivity(ProjectPageActivity::class.java, intent)
            .create()
            .start()
            .resume()
            .get()

        val toolbar = activity.findViewById<Toolbar>(R.id.pledge_toolbar)
        val menu = toolbar.menu

        assertTrue(menu.findItem(R.id.contact_creator)?.isVisible == true)
        assertTrue(menu.findItem(R.id.cancel_pledge)?.isVisible == true)
    }

    @Test
    fun `edit pledge is shown when feature flag is on and project is pledge over time`() {
        val user = UserFactory.user().toBuilder().isAdmin(false).build()
        val project = ProjectFactory.backedProject()
            .toBuilder()
            .isPledgeOverTimeAllowed(true)
            .build()

        val currentUserMock = MockCurrentUserV2(user)

        val mockFeatureFlagClient = object : MockFeatureFlagClient() {
            override fun getBoolean(flagKey: FlagKey): Boolean {
                return when (flagKey) {
                    FlagKey.ANDROID_PLEDGE_OVER_TIME,
                    FlagKey.ANDROID_PLOT_EDIT_PLEDGE -> true
                    else -> false
                }
            }
        }

        setUpEnvironment(
            environment().toBuilder()
                .currentUserV2(currentUserMock)
                .featureFlagClient(mockFeatureFlagClient)
                .build()
        )

        val intent = Intent(ApplicationProvider.getApplicationContext(), ProjectPageActivity::class.java)
        intent.putExtra(IntentKey.PROJECT, project)

        val activity = Robolectric.buildActivity(ProjectPageActivity::class.java, intent)
            .create()
            .start()
            .resume()
            .get()

        val toolbar = activity.findViewById<Toolbar>(R.id.pledge_toolbar)
        val menu = toolbar.menu

        assertTrue(menu.findItem(R.id.edit_pledge)?.isVisible == true)
    }

    @Test
    fun `showEditPledge is false when feature flag is off`() {
        val backing = BackingFactory.backing()
        val project = ProjectFactory.backedProject()
            .toBuilder()
            .isPledgeOverTimeAllowed(true)
            .backing(backing)
            .build()

        val mockFeatureFlagClient = object : MockFeatureFlagClient() {
            override fun getBoolean(flagKey: FlagKey): Boolean {
                return when (flagKey) {
                    FlagKey.ANDROID_PLEDGE_OVER_TIME -> true
                    FlagKey.ANDROID_PLOT_EDIT_PLEDGE -> false
                    else -> false
                }
            }
        }

        val options = createManagePledgeMenuOptions(project, mockFeatureFlagClient)
        assertFalse(options.showEditPledge)
    }

    @Test
    fun `choose another reward is shown when feature flag for edit pledge is off and standard pledge`() {
        val project = ProjectFactory.backedProject()
            .toBuilder()
            .isPledgeOverTimeAllowed(false)
            .build()

        val mockFeatureFlagClient = object : MockFeatureFlagClient() {
            override fun getBoolean(flagKey: FlagKey): Boolean {
                return when (flagKey) {
                    FlagKey.ANDROID_PLEDGE_OVER_TIME -> true
                    FlagKey.ANDROID_PLOT_EDIT_PLEDGE -> false
                    else -> false
                }
            }
        }

        val options = createManagePledgeMenuOptions(project, mockFeatureFlagClient)
        assertFalse(options.showEditPledge)
        assertTrue(options.showChooseAnotherReward)
    }

    @Test
    fun `choose another reward is hidden and edit pledge is shown when feature flag is on and project is pledge over time`() {
        val project = ProjectFactory.backedProject()
            .toBuilder()
            .isPledgeOverTimeAllowed(true)
            .build()

        val mockFeatureFlagClient = object : MockFeatureFlagClient() {
            override fun getBoolean(flagKey: FlagKey): Boolean {
                return when (flagKey) {
                    FlagKey.ANDROID_PLOT_EDIT_PLEDGE -> true
                    else -> false
                }
            }
        }

        setUpEnvironment(
            environment().toBuilder()
                .featureFlagClient(mockFeatureFlagClient)
                .build()
        )

        val options = createManagePledgeMenuOptions(project, mockFeatureFlagClient)
        assertTrue(options.showEditPledge)
        assertFalse(options.showChooseAnotherReward)
    }

    private fun deepLinkIntent(): Intent {
        val uri = Uri.parse("https://www.kickstarter.com/projects/1186238668/skull-graphic-tee")
        return Intent(Intent.ACTION_VIEW, uri)
    }

    private fun expectedCurrency(environment: Environment, project: Project, amount: Double): String =
        requireNotNull(environment.ksCurrency()).format(amount, project, RoundingMode.HALF_UP)

    private val mockApolloClientV2 = object : MockApolloClientV2() {

        override fun getProject(project: Project): Observable<Project> {
            return Observable
                .just(project)
        }

        override fun triggerThirdPartyEvent(eventInput: TPEventInputData): Observable<Pair<Boolean, String>> {
            return Observable.just(Pair(true, ""))
        }
    }
}
