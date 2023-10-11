package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.ProgressBarUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.deadlineCountdownValue
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.mock.factories.ProjectDataFactory.project
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.CreatorDetails
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.ReportProjectViewModel.Companion.COMMUNITY_GUIDELINES
import com.kickstarter.viewmodels.ReportProjectViewModel.Companion.COMMUNITY_GUIDELINES_TAG
import com.kickstarter.viewmodels.ReportProjectViewModel.Companion.OUR_RULES
import com.kickstarter.viewmodels.ReportProjectViewModel.Companion.OUR_RULES_TAG
import com.kickstarter.viewmodels.projectpage.ProjectOverviewViewModel.ProjectOverviewViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.junit.After
import org.junit.Test
import type.FlaggingKind
import java.util.Arrays

class ProjectOverviewViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ProjectOverviewViewModel

    private val avatarPhotoUrl = TestSubscriber<String>()
    private val backersCountTextViewText = TestSubscriber<String>()
    private val blurbTextViewText = TestSubscriber<String>()
    private val categoryTextViewText = TestSubscriber<String>()
    private val commentsCountTextViewText = TestSubscriber<String>()
    private val conversionPledgedAndGoalText = TestSubscriber<Pair<String, String>>()
    private val conversionTextViewIsGone = TestSubscriber<Boolean>()
    private val creatorDetailsLoadingContainerIsVisible = TestSubscriber<Boolean>()
    private val creatorDetailsIsGone = TestSubscriber<Boolean>()
    private val creatorNameTextViewText = TestSubscriber<String>()
    private val deadlineCountdownTextViewText = TestSubscriber<String>()
    private val goalStringForTextView = TestSubscriber<String>()
    private val locationTextViewText = TestSubscriber<String>()
    private val percentageFundedProgress = TestSubscriber<Int>()
    private val percentageFundedProgressBarIsGone = TestSubscriber<Boolean>()
    private val pledgedTextViewText = TestSubscriber<String>()
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
    private val setCanceledProjectStateView = TestSubscriber<Unit>()
    private val setProjectSocialClickListener = TestSubscriber<Unit>()
    private val setSuccessfulProjectStateView = TestSubscriber<DateTime>()
    private val setSuspendedProjectStateView = TestSubscriber<Unit>()
    private val setUnsuccessfulProjectStateView = TestSubscriber<DateTime>()
    private val startProjectSocialActivity = TestSubscriber<Project>()
    private val updatesCountTextViewText = TestSubscriber<String>()
    private val startCreatorView = TestSubscriber<ProjectData>()
    private val startCommentsView = TestSubscriber<ProjectData>()
    private val startUpdatesView = TestSubscriber<ProjectData>()
    private val startReportProjectView = TestSubscriber<ProjectData>()
    private val startLoginView = TestSubscriber<Unit>()
    private val shouldShowReportProject = TestSubscriber<Boolean>()
    private val shouldShowProjectFlagged = TestSubscriber<Boolean>()
    private val urlToOpen = TestSubscriber<String>()

    private val disposables = CompositeDisposable()

    private fun setUpEnvironment(environment: Environment, projectData: ProjectData) {
        vm = ProjectOverviewViewModel.Factory(environment).create(ProjectOverviewViewModel::class.java)
        vm.outputs.avatarPhotoUrl().subscribe { avatarPhotoUrl.onNext(it) }.addToDisposable(disposables)
        vm.outputs.backersCountTextViewText().subscribe { backersCountTextViewText.onNext(it) }.addToDisposable(disposables)
        vm.outputs.blurbTextViewText().subscribe { blurbTextViewText.onNext(it) }.addToDisposable(disposables)
        vm.outputs.categoryTextViewText().subscribe { categoryTextViewText.onNext(it) }.addToDisposable(disposables)
        vm.outputs.commentsCountTextViewText().subscribe { commentsCountTextViewText.onNext(it) }.addToDisposable(disposables)
        vm.outputs.conversionPledgedAndGoalText().subscribe { conversionPledgedAndGoalText.onNext(it) }.addToDisposable(disposables)
        vm.outputs.conversionTextViewIsGone().subscribe { conversionTextViewIsGone.onNext(it) }.addToDisposable(disposables)
        vm.outputs.creatorDetailsLoadingContainerIsVisible().subscribe {
            creatorDetailsLoadingContainerIsVisible.onNext(it)
        }.addToDisposable(disposables)
        vm.outputs.creatorDetailsIsGone().subscribe { creatorDetailsIsGone.onNext(it) }.addToDisposable(disposables)
        vm.outputs.creatorNameTextViewText().subscribe { creatorNameTextViewText.onNext(it) }.addToDisposable(disposables)
        vm.outputs.deadlineCountdownTextViewText().subscribe { deadlineCountdownTextViewText.onNext(it) }.addToDisposable(disposables)
        vm.outputs.goalStringForTextView().subscribe { goalStringForTextView.onNext(it) }.addToDisposable(disposables)
        vm.outputs.locationTextViewText().subscribe { locationTextViewText.onNext(it) }.addToDisposable(disposables)
        vm.outputs.percentageFundedProgress().subscribe { percentageFundedProgress.onNext(it) }.addToDisposable(disposables)
        vm.outputs.percentageFundedProgressBarIsGone().subscribe { percentageFundedProgressBarIsGone.onNext(it) }.addToDisposable(disposables)
        vm.outputs.pledgedTextViewText().subscribe { pledgedTextViewText.onNext(it) }.addToDisposable(disposables)
        vm.outputs.projectDisclaimerGoalReachedDateTime().subscribe { projectDisclaimerGoalReachedDateTime.onNext(it) }.addToDisposable(disposables)
        vm.outputs.projectDisclaimerGoalNotReachedString().subscribe {
            projectDisclaimerGoalNotReachedString.onNext(it)
        }.addToDisposable(disposables)
        vm.outputs.projectDisclaimerTextViewIsGone().subscribe { projectDisclaimerTextViewIsGone.onNext(it) }.addToDisposable(disposables)
        vm.outputs.projectLaunchDate().subscribe { projectLaunchDate.onNext(it) }.addToDisposable(disposables)
        vm.outputs.projectLaunchDateIsGone().subscribe { projectLaunchDateIsGone.onNext(it) }.addToDisposable(disposables)
        vm.outputs.projectNameTextViewText().subscribe { projectNameTextViewText.onNext(it) }.addToDisposable(disposables)
        vm.outputs.projectOutput().subscribe { projectOutput.onNext(it) }.addToDisposable(disposables)
        vm.outputs.projectSocialImageViewIsGone().subscribe { projectSocialImageViewIsGone.onNext(it) }.addToDisposable(disposables)
        vm.outputs.projectSocialImageViewUrl().subscribe { projectSocialImageViewUrl.onNext(it) }.addToDisposable(disposables)
        vm.outputs.projectSocialTextViewFriends().subscribe { projectSocialTextViewFriends.onNext(it) }.addToDisposable(disposables)
        vm.outputs.projectSocialViewGroupIsGone().subscribe { projectSocialViewGroupIsGone.onNext(it) }.addToDisposable(disposables)
        vm.outputs.projectStateViewGroupBackgroundColorInt().subscribe {
            projectStateViewGroupBackgroundColorInt.onNext(it)
        }.addToDisposable(disposables)
        vm.outputs.projectStateViewGroupIsGone().subscribe { projectStateViewGroupIsGone.onNext(it) }.addToDisposable(disposables)
        vm.outputs.shouldSetDefaultStatsMargins().subscribe { shouldSetDefaultStatsMargins.onNext(it) }.addToDisposable(disposables)
        vm.outputs.setCanceledProjectStateView().subscribe { setCanceledProjectStateView.onNext(it) }.addToDisposable(disposables)
        vm.outputs.setProjectSocialClickListener().subscribe { setProjectSocialClickListener.onNext(it) }.addToDisposable(disposables)
        vm.outputs.setSuccessfulProjectStateView().subscribe { setSuccessfulProjectStateView.onNext(it) }.addToDisposable(disposables)
        vm.outputs.setSuspendedProjectStateView().subscribe { setSuspendedProjectStateView.onNext(it) }.addToDisposable(disposables)
        vm.outputs.setUnsuccessfulProjectStateView().subscribe { setUnsuccessfulProjectStateView.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startProjectSocialActivity().subscribe { startProjectSocialActivity.onNext(it) }.addToDisposable(disposables)
        vm.outputs.updatesCountTextViewText().subscribe { updatesCountTextViewText.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startUpdatesView().subscribe { startUpdatesView.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startCommentsView().subscribe { startCommentsView.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startCreatorView().subscribe { startCreatorView.onNext(it) }.addToDisposable(disposables)
        vm.outputs.shouldShowReportProject().subscribe { shouldShowReportProject.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startLoginView().subscribe { startLoginView.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startReportProjectView().subscribe { startReportProjectView.onNext(it) }.addToDisposable(disposables)
        vm.outputs.shouldShowProjectFlagged().subscribe { shouldShowProjectFlagged.onNext(it) }.addToDisposable(disposables)
        vm.outputs.openExternallyWithUrl().subscribe { urlToOpen.onNext(it) }.addToDisposable(disposables)
        vm.inputs.configureWith(projectData)
    }

    @After
    fun cleanUp() {
        disposables.clear()
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
    fun testCreatorDataEmits() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment(), project(project))
        avatarPhotoUrl.assertValues(project.creator().avatar().medium())
        creatorNameTextViewText.assertValues(project.creator().name())
    }

    @Test
    fun testCreatorDetailsIsVisible_whenCreatorDetailsQueryUnsuccessful() {
        setUpEnvironment(
            environmentWithUnsuccessfulCreatorDetailsQuery(),
            project(ProjectFactory.project())
        )
        creatorDetailsIsGone.assertValue(true)
    }

    @Test
    fun testCreatorDetailsVisible_whenCreatorDetailsQuerySuccessful() {
        setUpEnvironment(
            environmentWithSuccessfulCreatorDetailsQuery(),
            project(ProjectFactory.project())
        )
        creatorDetailsIsGone.assertNoValues()
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
            .currentUserV2(MockCurrentUserV2(creator))
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
    fun testShouldShowReportProject() {
        val env = environment()
            .toBuilder()
            .build()

        setUpEnvironment(env, project(ProjectFactory.project()))

        this.shouldShowReportProject.assertValue(true)
    }

    @Test
    fun testStartLoginFlow_when_NoUser() {
        setUpEnvironment(environment(), project(ProjectFactory.project()))

        // -  User hits report project
        vm.inputs.reportProjectButtonClicked()

        this.startLoginView.assertValueCount(1)
        this.startLoginView.assertValue(Unit)
    }

    @Test
    fun testProjectReport_when_NoUser() {
        val envWithUser = environment()
            .toBuilder()
            .currentUserV2(MockCurrentUserV2(UserFactory.germanUser()))
            .build()

        val projectData = project(ProjectFactory.project())
        setUpEnvironment(envWithUser, projectData)

        // -  User hits report project
        vm.inputs.reportProjectButtonClicked()

        this.startLoginView.assertValueCount(0)
        this.startLoginView.assertNoValues()

        this.startReportProjectView.assertValueCount(1)
        this.startReportProjectView.assertValue(projectData)
    }

    @Test
    fun testProjectReported() {
        val env = environment()
            .toBuilder()
            .build()

        val project = ProjectFactory.project().toBuilder()
            .isFlagged(true)
            .build()
        setUpEnvironment(env, project(project))

        vm.inputs.refreshFlaggedState(FlaggingKind.NOT_PROJECT.rawValue())
        this.shouldShowProjectFlagged.assertValues(true, true)
        this.shouldShowReportProject.assertValues(false, false)
    }

    @Test
    fun testProjectReportedClickedLink() {
        val env = environment()
            .toBuilder()
            .build()
        val project = ProjectFactory.project().toBuilder()
            .isFlagged(true)
            .build()
        setUpEnvironment(env, project(project))

        vm.inputs.linkClicked(OUR_RULES_TAG)
        vm.inputs.linkClicked(COMMUNITY_GUIDELINES_TAG)

        this.urlToOpen.assertValues(
            "${environment().webEndpoint()}$OUR_RULES",
            "${environment().webEndpoint()}$COMMUNITY_GUIDELINES"
        )
    }

    private fun environmentWithUnsuccessfulCreatorDetailsQuery(): Environment {
        return environment()
            .toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun creatorDetails(slug: String): Observable<CreatorDetails> {
                    return Observable.error(Throwable("failure"))
                }
            })
            .build()
    }

    private fun environmentWithSuccessfulCreatorDetailsQuery(): Environment {
        return environment()
            .toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun creatorDetails(slug: String): Observable<CreatorDetails> {
                    return Observable.just(CreatorDetails(1, 1))
                }
            })
            .build()
    }
}
