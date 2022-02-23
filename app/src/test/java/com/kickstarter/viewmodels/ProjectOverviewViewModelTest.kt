package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.models.OptimizelyExperiment
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.libs.utils.EventName
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.ProgressBarUtils
import com.kickstarter.libs.utils.extensions.deadlineCountdownValue
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.MockExperimentsClientType
import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.mock.factories.ProjectDataFactory.Companion.project
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.models.CreatorDetails
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.projectpage.ProjectOverviewViewModel
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import java.util.Arrays

class ProjectOverviewViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ProjectOverviewViewModel.ViewModel

    private val avatarPhotoUrl = TestSubscriber<String>()
    private val backersCountTextViewText = TestSubscriber<String>()
    private val blurbTextViewText = TestSubscriber<String>()
    private val blurbVariantIsVisible = TestSubscriber<Boolean>()
    private val categoryTextViewText = TestSubscriber<String>()
    private val commentsCountTextViewText = TestSubscriber<String>()
    private val conversionPledgedAndGoalText = TestSubscriber<Pair<String, String>>()
    private val conversionTextViewIsGone = TestSubscriber<Boolean>()
    private val creatorBackedAndLaunchedProjectsCount = TestSubscriber<Pair<Int, Int>>()
    private val creatorDetailsLoadingContainerIsVisible = TestSubscriber<Boolean>()
    private val creatorDetailsVariantIsVisible = TestSubscriber<Boolean>()
    private val creatorNameTextViewText = TestSubscriber<String>()
    private val deadlineCountdownTextViewText = TestSubscriber<String>()
    private val goalStringForTextView = TestSubscriber<String>()
    private val locationTextViewText = TestSubscriber<String>()
    private val percentageFundedProgress = TestSubscriber<Int>()
    private val percentageFundedProgressBarIsGone = TestSubscriber<Boolean>()
    private val pledgedTextViewText = TestSubscriber<String>()
    private val projectDashboardButtonText = TestSubscriber<Int>()
    private val projectDashboardContainerIsGone = TestSubscriber<Boolean>()
    private val projectDisclaimerGoalReachedDateTime = TestSubscriber<DateTime>()
    private val projectDisclaimerGoalNotReachedString = TestSubscriber<Pair<String, DateTime>>()
    private val projectDisclaimerTextViewIsGone = TestSubscriber<Boolean>()
    private val projectLaunchDate = TestSubscriber<String>()
    private val projectLaunchDateIsGone = TestSubscriber<Boolean>()
    private val projectNameTextViewText = TestSubscriber<String>()
    private val projectOutput = TestSubscriber<Project>()
    private val projectSocialImageViewIsGone = TestSubscriber<Boolean>()
    private val projectSocialImageViewUrl = TestSubscriber<String>()
    private val projectSocialTextViewFriends = TestSubscriber<List<User>>()
    private val projectSocialViewGroupIsGone = TestSubscriber<Boolean>()
    private val projectStateViewGroupBackgroundColorInt = TestSubscriber<Int>()
    private val projectStateViewGroupIsGone = TestSubscriber<Boolean>()
    private val shouldSetDefaultStatsMargins = TestSubscriber<Boolean>()
    private val setCanceledProjectStateView = TestSubscriber<Void>()
    private val setProjectSocialClickListener = TestSubscriber<Void>()
    private val setSuccessfulProjectStateView = TestSubscriber<DateTime>()
    private val setSuspendedProjectStateView = TestSubscriber<Void>()
    private val setUnsuccessfulProjectStateView = TestSubscriber<DateTime>()
    private val startProjectSocialActivity = TestSubscriber<Project>()
    private val updatesCountTextViewText = TestSubscriber<String>()
    private val startCreatorView = TestSubscriber<ProjectData>()
    private val startCommentsView = TestSubscriber<ProjectData>()
    private val startUpdatesView = TestSubscriber<ProjectData>()
    private val startCampaignView = TestSubscriber<ProjectData>()
    private val startCreatorDashboard = TestSubscriber<ProjectData>()
    private val hideOldCampaignLink = TestSubscriber<Boolean>()

    private fun setUpEnvironment(environment: Environment, projectData: ProjectData) {
        vm = ProjectOverviewViewModel.ViewModel(environment)
        vm.outputs.avatarPhotoUrl().subscribe(avatarPhotoUrl)
        vm.outputs.backersCountTextViewText().subscribe(backersCountTextViewText)
        vm.outputs.blurbTextViewText().subscribe(blurbTextViewText)
        vm.outputs.blurbVariantIsVisible().subscribe(blurbVariantIsVisible)
        vm.outputs.categoryTextViewText().subscribe(categoryTextViewText)
        vm.outputs.commentsCountTextViewText().subscribe(commentsCountTextViewText)
        vm.outputs.conversionPledgedAndGoalText().subscribe(conversionPledgedAndGoalText)
        vm.outputs.conversionTextViewIsGone().subscribe(conversionTextViewIsGone)
        vm.outputs.creatorBackedAndLaunchedProjectsCount().subscribe(
            creatorBackedAndLaunchedProjectsCount
        )
        vm.outputs.creatorDetailsLoadingContainerIsVisible().subscribe(
            creatorDetailsLoadingContainerIsVisible
        )
        vm.outputs.creatorDetailsVariantIsVisible().subscribe(creatorDetailsVariantIsVisible)
        vm.outputs.creatorNameTextViewText().subscribe(creatorNameTextViewText)
        vm.outputs.deadlineCountdownTextViewText().subscribe(deadlineCountdownTextViewText)
        vm.outputs.goalStringForTextView().subscribe(goalStringForTextView)
        vm.outputs.locationTextViewText().subscribe(locationTextViewText)
        vm.outputs.percentageFundedProgress().subscribe(percentageFundedProgress)
        vm.outputs.percentageFundedProgressBarIsGone().subscribe(percentageFundedProgressBarIsGone)
        vm.outputs.pledgedTextViewText().subscribe(pledgedTextViewText)
        vm.outputs.projectDashboardButtonText().subscribe(projectDashboardButtonText)
        vm.outputs.projectDashboardContainerIsGone().subscribe(projectDashboardContainerIsGone)
        vm.outputs.projectDisclaimerGoalReachedDateTime()
            .subscribe(projectDisclaimerGoalReachedDateTime)
        vm.outputs.projectDisclaimerGoalNotReachedString().subscribe(
            projectDisclaimerGoalNotReachedString
        )
        vm.outputs.projectDisclaimerTextViewIsGone().subscribe(projectDisclaimerTextViewIsGone)
        vm.outputs.projectLaunchDate().subscribe(projectLaunchDate)
        vm.outputs.projectLaunchDateIsGone().subscribe(projectLaunchDateIsGone)
        vm.outputs.projectNameTextViewText().subscribe(projectNameTextViewText)
        vm.outputs.projectOutput().subscribe(projectOutput)
        vm.outputs.projectSocialImageViewIsGone().subscribe(projectSocialImageViewIsGone)
        vm.outputs.projectSocialImageViewUrl().subscribe(projectSocialImageViewUrl)
        vm.outputs.projectSocialTextViewFriends().subscribe(projectSocialTextViewFriends)
        vm.outputs.projectSocialViewGroupIsGone().subscribe(projectSocialViewGroupIsGone)
        vm.outputs.projectStateViewGroupBackgroundColorInt().subscribe(
            projectStateViewGroupBackgroundColorInt
        )
        vm.outputs.projectStateViewGroupIsGone().subscribe(projectStateViewGroupIsGone)
        vm.outputs.shouldSetDefaultStatsMargins().subscribe(shouldSetDefaultStatsMargins)
        vm.outputs.setCanceledProjectStateView().subscribe(setCanceledProjectStateView)
        vm.outputs.setProjectSocialClickListener().subscribe(setProjectSocialClickListener)
        vm.outputs.setSuccessfulProjectStateView().subscribe(setSuccessfulProjectStateView)
        vm.outputs.setSuspendedProjectStateView().subscribe(setSuspendedProjectStateView)
        vm.outputs.setUnsuccessfulProjectStateView().subscribe(setUnsuccessfulProjectStateView)
        vm.outputs.startProjectSocialActivity().subscribe(startProjectSocialActivity)
        vm.outputs.updatesCountTextViewText().subscribe(updatesCountTextViewText)
        vm.outputs.startUpdatesView().subscribe(startUpdatesView)
        vm.outputs.startCampaignView().subscribe(startCampaignView)
        vm.outputs.startCommentsView().subscribe(startCommentsView)
        vm.outputs.startCreatorView().subscribe(startCreatorView)
        vm.outputs.startCreatorDashboardView().subscribe(startCreatorDashboard)
        vm.outputs.hideOldCampaignLink().subscribe(hideOldCampaignLink)
        vm.inputs.configureWith(projectData)
    }

    @Test
    fun testCreatorDetailsClicked() {
        val projectData = project(ProjectFactory.project())
        setUpEnvironment(environment(), projectData)

        this.vm.inputs.creatorInfoButtonClicked()
        startCreatorView.assertValue(projectData)
    }

    @Test
    fun testUpdatesClicked() {
        val projectData = project(ProjectFactory.project())
        setUpEnvironment(environment(), projectData)

        this.vm.inputs.updatesButtonClicked()
        startUpdatesView.assertValue(projectData)
    }

    @Test
    fun testCommentsClicked() {
        val projectData = project(ProjectFactory.project())
        setUpEnvironment(environment(), projectData)

        this.vm.inputs.commentsButtonClicked()
        startCommentsView.assertValue(projectData)
    }

    @Test
    fun testCampaignClicked() {
        val projectData = project(ProjectFactory.project())
        setUpEnvironment(environment(), projectData)

        this.vm.inputs.campaignButtonClicked()
        startCampaignView.assertValue(projectData)
        this.segmentTrack.assertValue(EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testCreatorDashboardClicked() {
        val projectData = project(ProjectFactory.project())
        setUpEnvironment(environment(), projectData)

        this.vm.inputs.creatorDashboardClicked()
        startCreatorDashboard.assertValue(projectData)
        this.segmentTrack.assertValue(EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testBlurbVariantIsVisible_whenControl() {
        setUpEnvironment(environment(), project(ProjectFactory.project()))
        blurbVariantIsVisible.assertValue(false)
    }

    @Test
    fun testBlurbVariantIsVisible_whenVariant1() {
        setUpEnvironment(
            environmentForVariant(OptimizelyExperiment.Variant.VARIANT_1)!!,
            project(ProjectFactory.project())
        )
        blurbVariantIsVisible.assertValue(true)
    }

    @Test
    fun testBlurbVariantIsVisible_whenVariant2() {
        setUpEnvironment(
            environmentForVariant(OptimizelyExperiment.Variant.VARIANT_2)!!,
            project(ProjectFactory.project())
        )
        blurbVariantIsVisible.assertValue(true)
    }

    @Test
    fun testCreatorDataEmits() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment(), project(project))
        avatarPhotoUrl.assertValues(project.creator().avatar().medium())
        creatorNameTextViewText.assertValues(project.creator().name())
    }

    @Test
    fun testCreatorBackedAndLaunchedProjectsCount_whenFetchCreatorDetailsQuerySuccessful() {
        setUpEnvironment(environment(), project(ProjectFactory.project()))
        creatorBackedAndLaunchedProjectsCount.assertValue(Pair.create(3, 2))
    }

    @Test
    fun testCreatorBackedAndLaunchedProjectsCount_whenCreatorDetailsQueryUnsuccessful() {
        setUpEnvironment(
            environmentWithUnsuccessfulCreatorDetailsQuery()!!,
            project(ProjectFactory.project())
        )
        creatorBackedAndLaunchedProjectsCount.assertNoValues()
    }

    @Test
    fun testCreatorDetailsVariantIsVisible_whenCreatorDetailsQueryUnsuccessful() {
        setUpEnvironment(
            environmentWithUnsuccessfulCreatorDetailsQuery()!!,
            project(ProjectFactory.project())
        )
        creatorDetailsVariantIsVisible.assertValue(false)
    }

    @Test
    fun testCreatorDetailsVariantIsVisible_whenControl() {
        setUpEnvironment(environment(), project(ProjectFactory.project()))
        creatorDetailsVariantIsVisible.assertValue(false)
    }

    @Test
    fun testCreatorDetailsVariantIsVisible_whenVariant1() {
        setUpEnvironment(
            environmentForVariant(OptimizelyExperiment.Variant.VARIANT_1)!!,
            project(ProjectFactory.project())
        )
        creatorDetailsVariantIsVisible.assertValue(true)
    }

    @Test
    fun testCreatorDetailsLoadingContainerIsVisible_whenFetchCreatorDetailsQuerySuccessful() {
        setUpEnvironment(environment(), project(ProjectFactory.project()))
        creatorDetailsLoadingContainerIsVisible.assertValues(true, false)
    }

    @Test
    fun testCreatorDetailsLoadingContainerIsVisible_whenFetchCreatorDetailsQueryUnsuccessful() {
        setUpEnvironment(
            environmentWithUnsuccessfulCreatorDetailsQuery()!!,
            project(ProjectFactory.project())
        )
        creatorDetailsLoadingContainerIsVisible.assertValues(true, false)
    }

    @Test
    fun testProgressBar_Visible() {
        val project = ProjectFactory.project()
            .toBuilder()
            .state(Project.STATE_LIVE)
            .build()
        setUpEnvironment(environment(), project(project))
        percentageFundedProgress.assertValues(ProgressBarUtils.progress(project.percentageFunded()))
        percentageFundedProgressBarIsGone.assertValues(false)
    }

    @Test
    fun testProgressBar_Gone() {
        setUpEnvironment(environment(), project(ProjectFactory.successfulProject()))
        percentageFundedProgressBarIsGone.assertValues(true)
    }

    @Test
    fun testProjectDashboardButtonText_whenCurrentUserIsNotProjectCreator() {
        setUpEnvironment(environment(), project(ProjectFactory.project()))
        projectDashboardButtonText.assertNoValues()
    }

    @Test
    fun testProjectDashboardButtonText_whenCurrentUserIsProjectCreator_projectIsLive() {
        val creator = UserFactory.creator()
        val project = ProjectFactory.project()
            .toBuilder()
            .creator(creator)
            .build()
        val environment = environment()
            .toBuilder()
            .currentUser(MockCurrentUser(creator))
            .build()
        setUpEnvironment(environment, project(project))
        projectDashboardButtonText.assertValue(R.string.View_progress)
    }

    @Test
    fun testProjectDashboardButtonText_whenCurrentUserIsProjectCreator_projectIsNotLive() {
        val creator = UserFactory.creator()
        val project = ProjectFactory.successfulProject()
            .toBuilder()
            .creator(creator)
            .build()
        val environment = environment()
            .toBuilder()
            .currentUser(MockCurrentUser(creator))
            .build()
        setUpEnvironment(environment, project(project))
        projectDashboardButtonText.assertValue(R.string.View_dashboard)
    }

    @Test
    fun testProjectDashboardContainerIsGone_whenCurrentUserIsNotProjectCreator() {
        setUpEnvironment(environment(), project(ProjectFactory.project()))
        projectDashboardContainerIsGone.assertValue(true)
    }

    @Test
    fun testProjectDashboardContainerIsGone_whenCurrentUserIsProjectCreator() {
        val creator = UserFactory.creator()
        val project = ProjectFactory.successfulProject()
            .toBuilder()
            .creator(creator)
            .build()
        val environment = environment()
            .toBuilder()
            .currentUser(MockCurrentUser(creator))
            .build()
        setUpEnvironment(environment, project(project))
        projectDashboardContainerIsGone.assertValue(false)
    }

    @Test
    fun testProjectDataEmits() {
        val category = CategoryFactory.tabletopGamesCategory()
        val location = LocationFactory.unitedStates()
        val project = ProjectFactory.project()
            .toBuilder()
            .commentsCount(5000)
            .category(category)
            .location(location)
            .updatesCount(10)
            .build()
        setUpEnvironment(environment(), project(project))
        blurbTextViewText.assertValues(project.blurb())
        categoryTextViewText.assertValues(category.name())
        commentsCountTextViewText.assertValues("5,000")
        goalStringForTextView.assertValueCount(1)
        locationTextViewText.assertValues(location.displayableName())
        pledgedTextViewText.assertValueCount(1)
        projectNameTextViewText.assertValues(project.name())
        projectOutput.assertValues(project)
        updatesCountTextViewText.assertValues("10")
    }

    @Test
    fun testProjectDisclaimer_GoalReached() {
        val project = ProjectFactory.project()
            .toBuilder()
            .state(Project.STATE_LIVE)
            .goal(100.0)
            .pledged(500.0)
            .build()
        setUpEnvironment(environment(), project(project))
        projectDisclaimerGoalReachedDateTime.assertValueCount(1)
        projectDisclaimerTextViewIsGone.assertValues(false)
    }

    @Test
    fun testProjectDisclaimer_GoalNotReached() {
        val project = ProjectFactory.project()
            .toBuilder()
            .deadline(DateTime.now())
            .state(Project.STATE_LIVE)
            .goal(100.0)
            .pledged(50.0)
            .build()
        setUpEnvironment(environment(), project(project))
        projectDisclaimerGoalNotReachedString.assertValueCount(1)
        projectDisclaimerTextViewIsGone.assertValues(false)
    }

    @Test
    fun testProjectDisclaimer_NoDisclaimer() {
        setUpEnvironment(environment(), project(ProjectFactory.successfulProject()))

        // Disclaimer is not shown for completed projects.
        projectDisclaimerTextViewIsGone.assertValues(true)
    }

    @Test
    fun testProjectLaunchDate_whenLaunchedAtIsNull() {
        val project = ProjectFactory.project()
            .toBuilder()
            .launchedAt(null)
            .build()
        setUpEnvironment(environment(), project(project))
        projectLaunchDate.assertNoValues()
    }

    @Test
    fun testProjectLaunchDate_whenLaunchedAtIsNotNull() {
        val project = ProjectFactory.project()
            .toBuilder()
            .launchedAt(DateTime.parse("2019-11-05T14:21:42Z"))
            .build()
        setUpEnvironment(environment(), project(project))
        projectLaunchDate.assertValue("November 5, 2019")
    }

    @Test
    fun testProjectLaunchDateIsGone_whenCurrentUserIsProjectCreator() {
        val creator = UserFactory.creator()
        val project = ProjectFactory.project()
            .toBuilder()
            .creator(creator)
            .build()
        val environment = environment()
            .toBuilder()
            .currentUser(MockCurrentUser(creator))
            .build()
        setUpEnvironment(environment, project(project))
        projectLaunchDateIsGone.assertValue(false)
    }

    @Test
    fun testProjectLaunchDateIsGone_whenCurrentUserIsNotProjectCreator() {
        setUpEnvironment(environment(), project(ProjectFactory.project()))
        projectLaunchDateIsGone.assertValue(true)
    }

    @Test
    fun testProjectLaunchDateIsGone_whenLaunchedAtIsNull() {
        val project = ProjectFactory.project()
            .toBuilder()
            .launchedAt(null)
            .build()
        setUpEnvironment(environment(), project(project))
        projectLaunchDateIsGone.assertValue(true)
    }

    @Test
    fun testProjectSocialView_Clickable() {
        val myFriend = UserFactory.germanUser()
        val project = ProjectFactory.project()
            .toBuilder()
            .friends(Arrays.asList(myFriend, myFriend, myFriend))
            .build()
        setUpEnvironment(environment(), project(project))

        // On click listener should be set for view with > 2 friends.
        setProjectSocialClickListener.assertValueCount(1)
        projectSocialImageViewIsGone.assertValues(false)
        projectSocialImageViewUrl.assertValueCount(1)
        projectSocialTextViewFriends.assertValueCount(1)
        projectSocialViewGroupIsGone.assertValues(false)
        shouldSetDefaultStatsMargins.assertValues(false)
        vm.inputs.projectSocialViewGroupClicked()
        startProjectSocialActivity.assertValues(project)
    }

    @Test
    fun testProjectSocialView_NoSocial_LoggedIn() {
        val project = ProjectFactory.project()
            .toBuilder()
            .friends(emptyList())
            .build()
        setUpEnvironment(environment(), project(project))
        projectSocialImageViewIsGone.assertValues(true)
        projectSocialImageViewUrl.assertNoValues()
        projectSocialTextViewFriends.assertNoValues()
        projectSocialViewGroupIsGone.assertValues(true)
        shouldSetDefaultStatsMargins.assertValues(true)
        setProjectSocialClickListener.assertNoValues()
    }

    @Test
    fun testProjectSocialView_NoSocial_LoggedOut() {
        val project = ProjectFactory.project()
            .toBuilder()
            .friends(null)
            .build()
        setUpEnvironment(environment(), project(project))
        projectSocialImageViewIsGone.assertValues(true)
        projectSocialImageViewUrl.assertNoValues()
        projectSocialTextViewFriends.assertNoValues()
        projectSocialViewGroupIsGone.assertValues(true)
        shouldSetDefaultStatsMargins.assertValues(true)
        setProjectSocialClickListener.assertNoValues()
    }

    @Test
    fun testProjectSocialView_NotClickable() {
        val myFriend = UserFactory.germanUser()
        val project = ProjectFactory.project()
            .toBuilder()
            .friends(listOf(myFriend))
            .build()
        setUpEnvironment(environment(), project(project))

        // On click listener should be not set for view with < 2 friends.
        setProjectSocialClickListener.assertNoValues()
        projectSocialImageViewIsGone.assertValues(false)
        projectSocialImageViewUrl.assertValueCount(1)
        projectSocialTextViewFriends.assertValueCount(1)
        projectSocialViewGroupIsGone.assertValues(false)
        shouldSetDefaultStatsMargins.assertValues(false)
    }

    @Test
    fun testProjectState_Canceled() {
        val project = ProjectFactory.project()
            .toBuilder()
            .state(Project.STATE_CANCELED)
            .build()
        setUpEnvironment(environment(), project(project))
        projectStateViewGroupBackgroundColorInt.assertValues(R.color.kds_support_300)
        projectStateViewGroupIsGone.assertValues(false)
        setCanceledProjectStateView.assertValueCount(1)
    }

    @Test
    fun testProjectState_Live() {
        setUpEnvironment(environment(), project(ProjectFactory.project()))
        projectStateViewGroupBackgroundColorInt.assertNoValues()
        projectStateViewGroupIsGone.assertValues(true)
    }

    @Test
    fun testProjectState_Successful() {
        val stateChangedAt = DateTime.now()
        val project = ProjectFactory.project()
            .toBuilder()
            .state(Project.STATE_SUCCESSFUL)
            .stateChangedAt(stateChangedAt)
            .build()
        setUpEnvironment(environment(), project(project))
        projectStateViewGroupBackgroundColorInt.assertValues(R.color.green_alpha_50)
        projectStateViewGroupIsGone.assertValues(false)
        setSuccessfulProjectStateView.assertValues(stateChangedAt)
    }

    @Test
    fun testProjectState_Suspended() {
        val project = ProjectFactory.project()
            .toBuilder()
            .state(Project.STATE_SUSPENDED)
            .build()
        setUpEnvironment(environment(), project(project))
        projectStateViewGroupBackgroundColorInt.assertValues(R.color.kds_support_300)
        projectStateViewGroupIsGone.assertValues(false)
        setSuspendedProjectStateView.assertValueCount(1)
    }

    @Test
    fun testProjectState_Unsuccessful() {
        val stateChangedAt = DateTime.now()
        val project = ProjectFactory.project()
            .toBuilder()
            .state(Project.STATE_FAILED)
            .stateChangedAt(stateChangedAt)
            .build()
        setUpEnvironment(environment(), project(project))
        projectStateViewGroupBackgroundColorInt.assertValues(R.color.kds_support_300)
        projectStateViewGroupIsGone.assertValues(false)
        setUnsuccessfulProjectStateView.assertValues(stateChangedAt)
    }

    @Test
    fun testProjectStatsEmit() {
        DateTimeUtils.setCurrentMillisFixed(DateTime().millis)
        val project = ProjectFactory.project()
        setUpEnvironment(environment(), project(project))
        backersCountTextViewText.assertValues(NumberUtils.format(project.backersCount()))
        deadlineCountdownTextViewText.assertValues(
            NumberUtils.format(
                project.deadlineCountdownValue()
            )
        )
    }

    @Test
    fun testUsdConversionForNonUSProject() {
        // Use a CA project with a MX$ currency
        val project = ProjectFactory.mxCurrencyCAProject()
        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        // Set the current config for a US user. KSCurrency needs this config for conversions.
        setUpEnvironment(
            environment().toBuilder().ksCurrency(KSCurrency(currentConfig)).build(),
            project(project)
        )

        // USD conversion shown for non US project.
        conversionPledgedAndGoalText.assertValueCount(1)
        conversionTextViewIsGone.assertValue(false)
    }

    @Test
    fun testUsdConversionNotShownForUSProject() {
        val project = ProjectFactory.project()
            .toBuilder()
            .country("US")
            .build()
        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)
        setUpEnvironment(environment(), project(project))

        // USD conversion not shown for US project.
        conversionTextViewIsGone.assertValue(true)
    }

    @Test
    fun testOldLinkCampaignLinks_whenFeatureFlagOff_False() {
        val mockOptimizely = object : MockExperimentsClientType() {
            override fun isFeatureEnabled(feature: OptimizelyFeature.Key): Boolean {
                return true
            }
        }

        val project = ProjectFactory.project()

        val environment = environment()
            .toBuilder()
            .optimizely(mockOptimizely)
            .build()

        setUpEnvironment(environment, project(project))

        hideOldCampaignLink.assertValue(true)
    }

    @Test
    fun testOldLinkCampaignLinks_whenFeatureFlagOff_True() {
        val mockOptimizely = object : MockExperimentsClientType() {
            override fun isFeatureEnabled(feature: OptimizelyFeature.Key): Boolean {
                return false
            }
        }

        val project = ProjectFactory.project()

        val environment = environment()
            .toBuilder()
            .optimizely(mockOptimizely)
            .build()

        setUpEnvironment(environment, project(project))

        hideOldCampaignLink.assertValue(false)
    }

    private fun environmentForVariant(variant: OptimizelyExperiment.Variant): Environment? {
        return environment()
            .toBuilder()
            .optimizely(MockExperimentsClientType(variant))
            .build()
    }

    private fun environmentWithUnsuccessfulCreatorDetailsQuery(): Environment? {
        return environment()
            .toBuilder()
            .apolloClient(object : MockApolloClient() {
                override fun creatorDetails(slug: String): Observable<CreatorDetails> {
                    return Observable.error(Throwable("failure"))
                }
            })
            .build()
    }
}
