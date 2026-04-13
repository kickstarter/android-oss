package com.kickstarter.viewmodels

import android.util.Pair
import androidx.lifecycle.Lifecycle
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.RefTag.Companion.collection
import com.kickstarter.libs.RefTag.Companion.discovery
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.preferences.MockIntPreference
import com.kickstarter.libs.utils.EventName
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.MockFeatureFlagClient
import com.kickstarter.mock.factories.ActivityEnvelopeFactory.activityEnvelope
import com.kickstarter.mock.factories.ActivityFactory.activity
import com.kickstarter.mock.factories.ActivityFactory.updateActivity
import com.kickstarter.mock.factories.CategoryFactory.artCategory
import com.kickstarter.mock.factories.CategoryFactory.rootCategories
import com.kickstarter.mock.factories.DiscoverEnvelopeFactory
import com.kickstarter.mock.factories.ProjectFactory.prelaunchProject
import com.kickstarter.mock.factories.ProjectFactory.project
import com.kickstarter.mock.factories.UserFactory.user
import com.kickstarter.mock.factories.UserFactory.userNeedPassword
import com.kickstarter.mock.services.MockApiClientV2
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Activity
import com.kickstarter.models.Project
import com.kickstarter.models.UserPrivacy
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.DiscoveryParams.Companion.builder
import com.kickstarter.services.DiscoveryParams.Companion.getDefaultParams
import com.kickstarter.services.apiresponses.ActivityEnvelope
import com.kickstarter.services.apiresponses.DiscoverEnvelope
import com.kickstarter.ui.data.Editorial
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subscribers.TestSubscriber
import org.junit.Test
import java.util.concurrent.TimeUnit

class DiscoveryFragmentViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: DiscoveryFragmentViewModel.DiscoveryFragmentViewModel
    val testScheduler = TestScheduler()
    private val activityTest = TestSubscriber<Activity?>()
    private val clearActivitiesTest = TestSubscriber<Unit>()
    private val hasProjects = TestSubscriber<Boolean>()
    private val projects = TestSubscriber<List<Pair<Project, DiscoveryParams>>>()
    private val shouldShowEditorial = TestSubscriber<Editorial?>()
    private val shouldShowEmptySavedView = TestSubscriber<Boolean>()
    private val shouldShowOnboardingViewTest = TestSubscriber<Boolean>()
    private val showActivityFeed = TestSubscriber<Boolean>()
    private val showLoginTout = TestSubscriber<Boolean>()
    private val startEditorialActivity = TestSubscriber<Editorial>()
    private val startProjectActivity = TestSubscriber<Pair<Project, RefTag>>()
    private val startUpdateActivity = TestSubscriber<Activity>()
    private val startLoginToutActivityToSaveProject = TestSubscriber<Project>()
    private val scrollToSavedProjectIndex = TestSubscriber<Int>()
    private val showSavedPromptTest = TestSubscriber<Unit>()
    private val startSetPasswordActivity = TestSubscriber<String>()
    private val startPreLaunchProjectActivity = TestSubscriber<Pair<Project, RefTag>>()

    private val disposables = CompositeDisposable()

    private fun setUpEnvironment(environment: Environment) {
        vm = DiscoveryFragmentViewModel.DiscoveryFragmentViewModel(environment)

        vm.outputs.activity().subscribe { activityTest.onNext(it) }.addToDisposable(disposables)
        vm.outputs.clearActivities().subscribe { clearActivitiesTest.onNext(it) }.addToDisposable(disposables)
        vm.outputs.projectList()
            .map {
                ListUtils.nonEmpty(it)
            }.subscribe {
                hasProjects.onNext(it)
            }.addToDisposable(disposables)

        vm.outputs.projectList()
            .filter {
                ListUtils.nonEmpty(it)
            }.subscribe { projects.onNext(it) }.addToDisposable(disposables)

        vm.outputs.shouldShowEditorial().subscribe { shouldShowEditorial.onNext(it) }.addToDisposable(disposables)
        vm.outputs.shouldShowEmptySavedView().subscribe { shouldShowEmptySavedView.onNext(it) }.addToDisposable(disposables)
        vm.outputs.shouldShowOnboardingView().subscribe { shouldShowOnboardingViewTest.onNext(it) }.addToDisposable(disposables)
        vm.outputs.showActivityFeed().subscribe { showActivityFeed.onNext(it) }.addToDisposable(disposables)
        vm.outputs.showLoginTout().subscribe { showLoginTout.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startEditorialActivity().subscribe { startEditorialActivity.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startProjectActivity().subscribe { startProjectActivity.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startUpdateActivity().subscribe { startUpdateActivity.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startLoginToutActivityToSaveProject().subscribe {
            startLoginToutActivityToSaveProject.onNext(it)
        }.addToDisposable(disposables)
        vm.outputs.scrollToSavedProjectPosition().subscribe { scrollToSavedProjectIndex.onNext(it) }.addToDisposable(disposables)
        vm.outputs.showSavedPrompt().subscribe { showSavedPromptTest.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startSetPasswordActivity().subscribe { startSetPasswordActivity.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startPreLaunchProjectActivity().subscribe { startPreLaunchProjectActivity.onNext(it) }.addToDisposable(disposables)
    }

    private fun setUpInitialHomeAllProjectsParams() {
        vm.inputs.paramsFromActivity(
            getDefaultParams(null).toBuilder().sort(DiscoveryParams.Sort.MAGIC).build()
        )
        vm.inputs.rootCategories(rootCategories())
    }

    @Test
    fun testRefresh() {
        setUpEnvironment(environment().toBuilder().schedulerV2(testScheduler).build())

        // Load initial params and root categories from activity.
        setUpInitialHomeAllProjectsParams()
        vm.inputs.fragmentLifeCycle(Lifecycle.State.RESUMED)
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        // Should emit current fragment's projects.
        hasProjects.assertValues(true)
        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)

        // Page is refreshed
        vm.inputs.refresh()
        hasProjects.assertValues(true, true)
        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testProjectsEmitWithNewCategoryParams() {
        setUpEnvironment(environment().toBuilder().schedulerV2(testScheduler).build())

        // Load initial params and root categories from activity.
        setUpInitialHomeAllProjectsParams()
        vm.inputs.fragmentLifeCycle(Lifecycle.State.RESUMED)
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        // Should emit current fragment's projects.
        hasProjects.assertValues(true)
        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)

        // Select a new category.
        vm.inputs.paramsFromActivity(
            builder()
                .category(artCategory())
                .sort(DiscoveryParams.Sort.MAGIC)
                .build()
        )

        // New projects load with new params.
        hasProjects.assertValues(true, true, true)
        vm.inputs.fragmentLifeCycle(Lifecycle.State.RESUMED)
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)
        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.PAGE_VIEWED.eventName)
        vm.inputs.clearPage()
        hasProjects.assertValues(true, true, true, false)
    }

    @Test
    fun testProjectsEmitWithNewSort() {
        setUpEnvironment(environment().toBuilder().schedulerV2(testScheduler).build())

        // Initial load.
        setUpInitialHomeAllProjectsParams()
        vm.inputs.fragmentLifeCycle(Lifecycle.State.RESUMED)
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        projects.assertValueCount(1)
        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)

        // Popular tab clicked.
        vm.inputs.paramsFromActivity(builder().sort(DiscoveryParams.Sort.POPULAR).build())
        vm.inputs.fragmentLifeCycle(Lifecycle.State.RESUMED)
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)
        projects.assertValueCount(3)
        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testProjectsRefreshAfterLogin() {
        val currentUser: CurrentUserTypeV2 = MockCurrentUserV2()
        val environment = environment().toBuilder()
            .currentUserV2(currentUser)
            .build()
        setUpEnvironment(environment)

        // Initial load.
        setUpInitialHomeAllProjectsParams()
        hasProjects.assertValue(true)

        // Projects should emit.
        projects.assertValueCount(1)

        // Log in.
        logUserIn(currentUser)

        // Projects should emit again.
        projects.assertValueCount(4)
    }

    @Test
    fun testShouldShowEditorial_otherParams() {
        setUpEnvironment(environment())

        // Art projects params.
        vm.inputs.paramsFromActivity(
            builder()
                .category(artCategory())
                .sort(DiscoveryParams.Sort.MAGIC)
                .build()
        )
        shouldShowEditorial.assertNoValues()
    }

    @Test
    fun testShouldShowEditorial_defaultParams() {
        setUpEnvironment(environment())

        // Initial home all projects params.
        setUpInitialHomeAllProjectsParams()
        shouldShowEditorial.assertNoValues()
    }

    @Test
    fun testShouldShowEditorial_featureDisabled() {
        val user = MockCurrentUserV2()
        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return false
                }
            }
        val environment = environment().toBuilder()
            .currentUserV2(user)
            .featureFlagClient(mockFeatureFlagClient)
            .build()
        setUpEnvironment(environment)

        setUpInitialHomeAllProjectsParams()

        shouldShowEditorial.assertNoValues()
    }

    @Test
    fun testShouldShowEmptySavedView_isFalse_whenUserHasSavedProjects() {
        val currentUser: CurrentUserTypeV2 = MockCurrentUserV2()
        val environment = environment().toBuilder()
            .apolloClientV2(MockApolloClientV2())
            .currentUserV2(currentUser)
            .build()
        setUpEnvironment(environment)

        // Initial home all projects params.
        setUpInitialHomeAllProjectsParams()
        hasProjects.assertValue(true)
        shouldShowEmptySavedView.assertValue(false)

        // Login.
        logUserIn(currentUser)

        // New projects load.
        hasProjects.assertValues(true, true, true, true)
        shouldShowEmptySavedView.assertValues(false)

        // Saved projects params.
        vm.inputs.paramsFromActivity(builder().starred(1).build())

        // New projects load with updated params.
        hasProjects.assertValues(true, true, true, true, true, true)
        shouldShowEmptySavedView.assertValues(false)
    }

    @Test
    fun testShouldShowEmptySavedView_isTrue_whenUserHasNoSavedProjects() {
        val currentUser: CurrentUserTypeV2 = MockCurrentUserV2()
        val apiClient: ApolloClientTypeV2 = object : MockApolloClientV2() {
            override fun getProjects(
                params: DiscoveryParams,
                cursor: String?
            ): Observable<DiscoverEnvelope> {
                return if (params.isSavedProjects) {
                    Observable.just(DiscoverEnvelopeFactory.discoverEnvelope(ArrayList()))
                } else {
                    super.getProjects(params, cursor)
                }
            }
        }
        val environment = environment().toBuilder()
            .apolloClientV2(apiClient)
            .currentUserV2(currentUser)
            .build()
        setUpEnvironment(environment)

        // Initial home all projects params.
        setUpInitialHomeAllProjectsParams()
        hasProjects.assertValue(true)
        shouldShowEmptySavedView.assertValue(false)

        // Login.
        logUserIn(currentUser)

        // New projects load.
        hasProjects.assertValues(true, true, true, true)
        shouldShowEmptySavedView.assertValues(false)

        // Saved projects params.
        vm.inputs.paramsFromActivity(builder().starred(1).build())

        // Projects are cleared, new projects load with updated params.
        hasProjects.assertValues(true, true, true, true, false, false)
        shouldShowEmptySavedView.assertValues(false, true)
    }

    @Test
    fun testShowHeaderViews() {
        val currentUser: CurrentUserTypeV2 = MockCurrentUserV2()
        val activity = activity()
        val apiClient: ApiClientTypeV2 = object : MockApiClientV2() {
            override fun fetchActivities(): Observable<ActivityEnvelope> {
                return Observable.just(
                    activityEnvelope(listOf(activity))
                )
            }
        }
        val activitySamplePreference = MockIntPreference(987654321)
        val environment = environment().toBuilder()
            .activitySamplePreference(activitySamplePreference)
            .apiClientV2(apiClient)
            .currentUserV2(currentUser)
            .build()
        setUpEnvironment(environment)

        // Initial home all projects params.
        setUpInitialHomeAllProjectsParams()

        // Should show onboarding view.
        shouldShowOnboardingViewTest.assertValues(true)
        activityTest.assertNoValues()

        // Change params. Onboarding view should not be shown.
        vm.inputs.paramsFromActivity(builder().sort(DiscoveryParams.Sort.NEWEST).build())
        shouldShowOnboardingViewTest.assertValues(true, false)
        activityTest.assertNoValues()

        // Login.
        logUserIn(currentUser)

        clearActivitiesTest.assertValueCount(1)
        // Activity sampler should be shown rather than onboarding view.
        shouldShowOnboardingViewTest.assertValues(true, false, false, false)
        activityTest.assertValues(activity)
        clearActivitiesTest.assertValueCount(1)

        // Change params. Activity sampler should not be shown.
        vm.inputs.paramsFromActivity(builder().build())
        activityTest.assertValues(activity)
        clearActivitiesTest.assertValueCount(2)
    }

    @Test
    fun testLoginToutToSaveProject() {
        val currentUser: CurrentUserTypeV2 = MockCurrentUserV2()
        val environment = environment().toBuilder()
            .currentUserV2(currentUser)
            .schedulerV2(testScheduler)
            .build()
        setUpEnvironment(environment)
        val projects = BehaviorSubject.create<List<Pair<Project, DiscoveryParams>>>()
        vm.outputs.projectList().subscribe(projects)

        // Initial home all projects params.
        setUpInitialHomeAllProjectsParams()
        vm.inputs.fragmentLifeCycle(Lifecycle.State.RESUMED)
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        // Click on project save
        val project = projects.value?.get(0)?.first ?: Project.builder().build()
        vm.inputs.onHeartButtonClicked(project)
        startLoginToutActivityToSaveProject.assertValue(project)

        // Login.
        val user = user()
        currentUser.refresh(user)
        showSavedPromptTest.assertValueCount(1)
        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testStartSetPasswordActivity() {
        val currentUser: CurrentUserTypeV2 = MockCurrentUserV2()
        val environment = environment().toBuilder()
            .currentUserV2(currentUser)
            .schedulerV2(testScheduler)
            .build()

        setUpEnvironment(environment)

        val projects = BehaviorSubject.create<List<Pair<Project, DiscoveryParams>>>()
        vm.outputs.projectList().subscribe(projects)

        // Initial home all projects params.
        setUpInitialHomeAllProjectsParams()

        startSetPasswordActivity.assertValueCount(0)

        // Login.
        val user = user()
        currentUser.refresh(user)
        startSetPasswordActivity.assertValueCount(0)

        val mockFeatureFlagClientType: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return true
                }
            }

        val mockApolloClient = object : MockApolloClientV2() {
            override fun userPrivacy(): Observable<UserPrivacy> {
                return Observable.just(
                    UserPrivacy(
                        "",
                        "rashad@test.com",
                        true,
                        true,
                        false,
                        false,
                        "",
                        emptyList()
                    )
                )
            }
        }

        setUpEnvironment(
            environment().toBuilder().currentUserV2(currentUser)
                .featureFlagClient(mockFeatureFlagClientType)
                .apolloClientV2(mockApolloClient)
                .build()
        )

        vm.outputs.projectList().subscribe(projects)

        // Initial home all projects params.
        setUpInitialHomeAllProjectsParams()

        startSetPasswordActivity.assertValueCount(0)

        currentUser.refresh(user)
        startSetPasswordActivity.assertValueCount(0)

        currentUser.refresh(userNeedPassword())
        startSetPasswordActivity.assertValueCount(1)
        startSetPasswordActivity.assertValue("rashad@test.com")
    }

    @Test
    fun testSaveProject() {
        val currentUser: CurrentUserTypeV2 = MockCurrentUserV2()
        val environment = environment().toBuilder()
            .currentUserV2(currentUser)
            .schedulerV2(testScheduler)
            .build()
        setUpEnvironment(environment)

        // Login.
        val user = user()
        currentUser.refresh(user)
        val projects = BehaviorSubject.create<List<Pair<Project, DiscoveryParams>>>()
        vm.outputs.projectList().subscribe(projects)

        // Initial home all projects params.
        setUpInitialHomeAllProjectsParams()
        vm.inputs.fragmentLifeCycle(Lifecycle.State.RESUMED)
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        // Click on project save
        val project = projects.value?.get(0)?.first ?: Project.builder().build()
        vm.inputs.onHeartButtonClicked(project)
        startLoginToutActivityToSaveProject.assertNoValues()
        this.projects.assertValueCount(2)
        assertTrue(projects.value!![0].first.isStarred())
        showSavedPromptTest.assertValueCount(1)
        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testShowLoginTout() {
        setUpEnvironment(environment())

        // Clicking login on onboarding view should show login tout.
        vm.inputs.discoveryOnboardingViewHolderLoginToutClick(null)
        showLoginTout.assertValue(true)
        segmentTrack.assertValue(EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testStartEditorialActivity() {
        setUpEnvironment(environment().toBuilder().schedulerV2(testScheduler).build())

        // Load initial params and root categories from activity.
        setUpInitialHomeAllProjectsParams()

        // Click on editorial
        vm.inputs.fragmentLifeCycle(Lifecycle.State.RESUMED)
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        vm.inputs.editorialViewHolderClicked(Editorial.GO_REWARDLESS)
        vm.inputs.editorialViewHolderClicked(Editorial.LIGHTS_ON)
        startEditorialActivity.assertValues(Editorial.GO_REWARDLESS, Editorial.LIGHTS_ON)
        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testStartProjectActivity_whenViewingEditorial() {
        setUpEnvironment(
            environment().toBuilder()
                .schedulerV2(testScheduler).build()
        )

        // Load editorial params and root categories from activity.
        val editorialParams = builder()
            .tagId(Editorial.GO_REWARDLESS.tagId)
            .sort(DiscoveryParams.Sort.MAGIC)
            .build()
        vm.inputs.paramsFromActivity(editorialParams)
        vm.inputs.rootCategories(rootCategories())

        vm.inputs.fragmentLifeCycle(Lifecycle.State.RESUMED)
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        // Click on project
        val project = project()
        vm.inputs.projectCardViewHolderClicked(project)
        startProjectActivity.assertValueCount(1)
        assertEquals(startProjectActivity.values().first(), Pair(project, collection(518)))

        segmentTrack.assertValues(
            EventName.PAGE_VIEWED.eventName,
            EventName.CARD_CLICKED.eventName,
            EventName.CTA_CLICKED.eventName
        )
    }

    @Test
    fun testStartProjectActivity_whenViewingFeatureFlagOn_shouldEmitProjectPageActivity() {
        val currentUser: CurrentUserTypeV2 = MockCurrentUserV2()
        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return true
                }
            }
        setUpEnvironment(
            environment().toBuilder().currentUserV2(currentUser)
                .featureFlagClient(mockFeatureFlagClient)
                .schedulerV2(testScheduler)
                .build()
        )

        // Load editorial params and root categories from activity.
        val editorialParams = builder()
            .tagId(Editorial.GO_REWARDLESS.tagId)
            .sort(DiscoveryParams.Sort.MAGIC)
            .build()
        vm.inputs.paramsFromActivity(editorialParams)
        vm.inputs.rootCategories(rootCategories())
        vm.inputs.fragmentLifeCycle(Lifecycle.State.RESUMED)
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        // Click on project
        val project = project()
        vm.inputs.projectCardViewHolderClicked(project)
        startProjectActivity.assertValueCount(1)
        assertEquals(startProjectActivity.values().first(), Pair(project, collection(518)))
        segmentTrack.assertValues(
            EventName.PAGE_VIEWED.eventName,
            EventName.CARD_CLICKED.eventName,
            EventName.CTA_CLICKED.eventName
        )
    }

    @Test
    fun testStartProjectActivity_whenViewingAllProjects() {
        setUpEnvironment(environment().toBuilder().schedulerV2(testScheduler).build())

        // Load initial params and root categories from activity.
        setUpInitialHomeAllProjectsParams()
        vm.inputs.fragmentLifeCycle(Lifecycle.State.RESUMED)
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        // Click on project
        val project = project()
        vm.inputs.projectCardViewHolderClicked(project)

        startProjectActivity.assertValueCount(1)
        assertEquals(startProjectActivity.values().first(), Pair(project, discovery()))
        segmentTrack.assertValues(
            EventName.PAGE_VIEWED.eventName,
            EventName.CARD_CLICKED.eventName,
            EventName.CTA_CLICKED.eventName
        )
    }

    @Test
    fun testStartProjectActivity_whenFeatureFlagEnabled_shouldEmitProjectPageActivity() {
        val currentUser: CurrentUserTypeV2 = MockCurrentUserV2()
        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return true
                }
            }
        setUpEnvironment(
            environment().toBuilder().currentUserV2(currentUser)
                .featureFlagClient(mockFeatureFlagClient)
                .schedulerV2(testScheduler)
                .build()
        )

        // Load initial params and root categories from activity.
        setUpInitialHomeAllProjectsParams()
        vm.inputs.fragmentLifeCycle(Lifecycle.State.RESUMED)
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        // Click on project
        val project = project()
        vm.inputs.projectCardViewHolderClicked(project)
        startProjectActivity.assertValueCount(1)
        assertEquals(startProjectActivity.values().first(), Pair(project, discovery()))
        segmentTrack.assertValues(
            EventName.PAGE_VIEWED.eventName,
            EventName.CARD_CLICKED.eventName,
            EventName.CTA_CLICKED.eventName
        )
    }

    @Test
    fun testStartPelaunchProjectActivity_whenDisplayPelaunchEnabled_shouldEmitPelaunchProjectPageActivity() {
        val currentUser: CurrentUserTypeV2 = MockCurrentUserV2()

        setUpEnvironment(
            environment().toBuilder().currentUserV2(currentUser)
                .schedulerV2(testScheduler)
                .build()
        )

        // Load initial params and root categories from activity.
        setUpInitialHomeAllProjectsParams()
        vm.inputs.fragmentLifeCycle(Lifecycle.State.RESUMED)
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        // Click on project
        val project = prelaunchProject("")
        vm.inputs.projectCardViewHolderClicked(project)
        startProjectActivity.assertValueCount(0)
        startPreLaunchProjectActivity.assertValueCount(1)
        assertEquals(startPreLaunchProjectActivity.values().first().first, project)
        assertEquals(startPreLaunchProjectActivity.values().first().second, discovery())
    }

    @Test
    fun testClickingInterfaceElements() {
        setUpEnvironment(environment())

        // Clicking see activity feed button on sampler should show activity feed.
        showActivityFeed.assertNoValues()
        vm.inputs.activitySampleFriendBackingViewHolderSeeActivityClicked(null)
        showActivityFeed.assertValues(true)
        vm.inputs.activitySampleFriendFollowViewHolderSeeActivityClicked(null)
        showActivityFeed.assertValues(true, true)
        vm.inputs.activitySampleProjectViewHolderSeeActivityClicked(null)
        showActivityFeed.assertValues(true, true, true)

        // Clicking activity update on sampler should show activity update.
        startUpdateActivity.assertNoValues()
        vm.inputs.activitySampleProjectViewHolderUpdateClicked(null, updateActivity())
        startUpdateActivity.assertValueCount(1)
    }

    @Test
    fun testErroredResponseForFetchActivitiesWithCount() {
        val currentUser: CurrentUserTypeV2 = MockCurrentUserV2()
        val throwableError = Throwable()
        val apiClient: MockApiClientV2 = object : MockApiClientV2() {
            override fun fetchActivities(count: Int?): Observable<ActivityEnvelope> {
                return Observable.error(throwableError)
            }
        }
        val env = environment()
            .toBuilder()
            .currentUserV2(currentUser)
            .apiClientV2(apiClient)
            .build()
        setUpEnvironment(env)

        // Load initial params and root categories from activity.
        setUpInitialHomeAllProjectsParams()

        // Log in.
        logUserIn(currentUser)
        activityTest.assertNoValues()
    }

    @Test
    fun testSuccessResponseForFetchActivitiesWithCount() {
        val currentUser: CurrentUserTypeV2 = MockCurrentUserV2()
        val activity = activity()
        val apiClient: MockApiClientV2 = object : MockApiClientV2() {
            override fun fetchActivities(count: Int?): Observable<ActivityEnvelope> {
                return Observable.just(activityEnvelope(listOf(activity)))
            }
        }
        val env = environment()
            .toBuilder()
            .currentUserV2(currentUser)
            .apiClientV2(apiClient)
            .build()
        setUpEnvironment(env)

        // Load initial params and root categories from activity.
        setUpInitialHomeAllProjectsParams()

        // Log in.
        logUserIn(currentUser)
        activityTest.assertValueCount(1)
        activityTest.assertValues(activity)
    }

    private fun logUserIn(currentUser: CurrentUserTypeV2) {
        val user = user()
        currentUser.refresh(user)
        vm.inputs.paramsFromActivity(getDefaultParams(user))
    }
}
