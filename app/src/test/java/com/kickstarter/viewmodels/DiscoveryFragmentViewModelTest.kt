package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.RefTag.Companion.collection
import com.kickstarter.libs.RefTag.Companion.discovery
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.libs.preferences.MockIntPreference
import com.kickstarter.libs.utils.EventName
import com.kickstarter.libs.utils.ExperimentData
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.mock.MockExperimentsClientType
import com.kickstarter.mock.factories.ActivityEnvelopeFactory.activityEnvelope
import com.kickstarter.mock.factories.ActivityFactory.activity
import com.kickstarter.mock.factories.ActivityFactory.updateActivity
import com.kickstarter.mock.factories.CategoryFactory.artCategory
import com.kickstarter.mock.factories.CategoryFactory.rootCategories
import com.kickstarter.mock.factories.DiscoverEnvelopeFactory
import com.kickstarter.mock.factories.ProjectFactory.project
import com.kickstarter.mock.factories.UserFactory.user
import com.kickstarter.mock.factories.UserFactory.userNeedPassword
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.models.Activity
import com.kickstarter.models.Project
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.ApolloClientType
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.DiscoveryParams.Companion.builder
import com.kickstarter.services.DiscoveryParams.Companion.getDefaultParams
import com.kickstarter.services.apiresponses.ActivityEnvelope
import com.kickstarter.services.apiresponses.DiscoverEnvelope
import com.kickstarter.ui.data.Editorial
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import rx.schedulers.TestScheduler
import rx.subjects.BehaviorSubject

class DiscoveryFragmentViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: DiscoveryFragmentViewModel.ViewModel
    val testScheduler = TestScheduler()
    private val activityTest = TestSubscriber<Activity?>()
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
    private val showSavedPromptTest = TestSubscriber<Void>()
    private val startSetPasswordActivity = TestSubscriber<String>()

    private fun setUpEnvironment(environment: Environment) {
        vm = DiscoveryFragmentViewModel.ViewModel(environment)

        vm.outputs.activity().subscribe(activityTest)
        vm.outputs.projectList()
            .map {
                ListUtils.nonEmpty(it)
            }.subscribe(
                hasProjects
            )

        vm.outputs.projectList()
            .filter {
                ListUtils.nonEmpty(it)
            }.subscribe(projects)

        vm.outputs.shouldShowEditorial().subscribe(shouldShowEditorial)
        vm.outputs.shouldShowEmptySavedView().subscribe(shouldShowEmptySavedView)
        vm.outputs.shouldShowOnboardingView().subscribe(shouldShowOnboardingViewTest)
        vm.outputs.showActivityFeed().subscribe(showActivityFeed)
        vm.outputs.showLoginTout().subscribe(showLoginTout)
        vm.outputs.startEditorialActivity().subscribe(startEditorialActivity)
        vm.outputs.startProjectActivity().subscribe(startProjectActivity)
        vm.outputs.startUpdateActivity().subscribe(startUpdateActivity)
        vm.outputs.startLoginToutActivityToSaveProject().subscribe(
            startLoginToutActivityToSaveProject
        )
        vm.outputs.scrollToSavedProjectPosition().subscribe(scrollToSavedProjectIndex)
        vm.outputs.showSavedPrompt().subscribe(showSavedPromptTest)
        vm.outputs.startSetPasswordActivity().subscribe(startSetPasswordActivity)
    }

    private fun setUpInitialHomeAllProjectsParams() {
        vm.inputs.paramsFromActivity(
            getDefaultParams(null).toBuilder().sort(DiscoveryParams.Sort.MAGIC).build()
        )
        vm.inputs.rootCategories(rootCategories())
    }

    @Test
    fun testRefresh() {
        setUpEnvironment(environment())

        // Load initial params and root categories from activity.
        setUpInitialHomeAllProjectsParams()

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
        setUpEnvironment(environment())

        // Load initial params and root categories from activity.
        setUpInitialHomeAllProjectsParams()

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
        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.PAGE_VIEWED.eventName)
        vm.inputs.clearPage()
        hasProjects.assertValues(true, true, true, false)
    }

    @Test
    fun testProjectsEmitWithNewSort() {
        setUpEnvironment(environment())

        // Initial load.
        setUpInitialHomeAllProjectsParams()
        projects.assertValueCount(1)
        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)

        // Popular tab clicked.
        vm.inputs.paramsFromActivity(builder().sort(DiscoveryParams.Sort.POPULAR).build())
        projects.assertValueCount(3)
        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testProjectsRefreshAfterLogin() {
        val currentUser: CurrentUserType = MockCurrentUser()
        val environment = environment().toBuilder()
            .currentUser(currentUser)
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
        shouldShowEditorial.assertValue(null)
    }

    @Test
    fun testShouldShowEditorial_defaultParams() {
        setUpEnvironment(environment())

        // Initial home all projects params.
        setUpInitialHomeAllProjectsParams()
        shouldShowEditorial.assertValue(null)
    }

    @Test
    fun testShouldShowEditorial_featureEnabled() {
        val user = MockCurrentUser()
        val mockExperimentsClientType: MockExperimentsClientType =
            object : MockExperimentsClientType() {
                override fun isFeatureEnabled(
                    feature: OptimizelyFeature.Key,
                    experimentData: ExperimentData
                ): Boolean {
                    return true
                }
            }
        val environment = environment().toBuilder()
            .currentUser(user)
            .optimizely(mockExperimentsClientType)
            .build()
        setUpEnvironment(environment)

        setUpInitialHomeAllProjectsParams()

        shouldShowEditorial.assertValue(Editorial.LIGHTS_ON)
    }

    @Test
    fun testShouldShowEditorial_featureDisabled() {
        val user = MockCurrentUser()
        val mockExperimentsClientType: MockExperimentsClientType =
            object : MockExperimentsClientType() {
                override fun isFeatureEnabled(
                    feature: OptimizelyFeature.Key,
                    experimentData: ExperimentData
                ): Boolean {
                    return false
                }
            }
        val environment = environment().toBuilder()
            .currentUser(user)
            .optimizely(mockExperimentsClientType)
            .build()
        setUpEnvironment(environment)

        setUpInitialHomeAllProjectsParams()

        shouldShowEditorial.assertValue(null)
    }

    @Test
    fun testShouldShowEditorial_whenOptimizelyInitializationDelay() {
        val environment = environment().toBuilder()
            .currentUser(MockCurrentUser())
            .optimizely(object : MockExperimentsClientType() {
                var enabledCount = 0
                override fun isFeatureEnabled(
                    feature: OptimizelyFeature.Key,
                    experimentData: ExperimentData
                ): Boolean {
                    return if (enabledCount == 0) {
                        enabledCount += 1
                        false
                    } else {
                        true
                    }
                }
            })
            .build()

        setUpEnvironment(environment)

        setUpInitialHomeAllProjectsParams()
        shouldShowEditorial.assertValue(null)
        vm.optimizelyReady()
        shouldShowEditorial.assertValues(null, Editorial.LIGHTS_ON)
    }

    @Test
    fun testShouldShowEmptySavedView_isFalse_whenUserHasSavedProjects() {
        val currentUser: CurrentUserType = MockCurrentUser()
        val environment = environment().toBuilder()
            .apolloClient(MockApolloClient())
            .currentUser(currentUser)
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
        val currentUser: CurrentUserType = MockCurrentUser()
        val apiClient: ApolloClientType = object : MockApolloClient() {
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
            .apolloClient(apiClient)
            .currentUser(currentUser)
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
        val currentUser: CurrentUserType = MockCurrentUser()
        val activity = activity()
        val apiClient: ApiClientType = object : MockApiClient() {
            override fun fetchActivities(): Observable<ActivityEnvelope> {
                return Observable.just(
                    activityEnvelope(listOf(activity))
                )
            }
        }
        val activitySamplePreference = MockIntPreference(987654321)
        val environment = environment().toBuilder()
            .activitySamplePreference(activitySamplePreference)
            .apiClient(apiClient)
            .currentUser(currentUser)
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

        // Activity sampler should be shown rather than onboarding view.
        shouldShowOnboardingViewTest.assertValues(true, false, false, false)
        activityTest.assertValues(null, activity)

        // Change params. Activity sampler should not be shown.
        vm.inputs.paramsFromActivity(builder().build())
        activityTest.assertValues(null, activity, null)
    }

    @Test
    fun testLoginToutToSaveProject() {
        val currentUser: CurrentUserType = MockCurrentUser()
        val environment = environment().toBuilder()
            .currentUser(currentUser)
            .scheduler(testScheduler)
            .build()
        setUpEnvironment(environment)
        val projects = BehaviorSubject.create<List<Pair<Project, DiscoveryParams>>>()
        vm.outputs.projectList().subscribe(projects)

        // Initial home all projects params.
        setUpInitialHomeAllProjectsParams()

        // Click on project save
        val project = projects.value[0].first
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
        val currentUser: CurrentUserType = MockCurrentUser()
        val environment = environment().toBuilder()
            .currentUser(currentUser)
            .scheduler(testScheduler)
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

        val mockExperimentsClientType: MockExperimentsClientType =
            object : MockExperimentsClientType() {
                override fun isFeatureEnabled(feature: OptimizelyFeature.Key): Boolean {
                    return true
                }
            }

        val mockApolloClient = object : MockApolloClient() {
            override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                return Observable.just(
                    UserPrivacyQuery.Data(
                        UserPrivacyQuery.Me(
                            "", "",
                            "rashad@test.com", true, true, false, false, ""
                        )
                    )
                )
            }
        }

        setUpEnvironment(
            environment().toBuilder().currentUser(currentUser)
                .optimizely(mockExperimentsClientType)
                .apolloClient(mockApolloClient)
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
        val currentUser: CurrentUserType = MockCurrentUser()
        val environment = environment().toBuilder()
            .currentUser(currentUser)
            .scheduler(testScheduler)
            .build()
        setUpEnvironment(environment)

        // Login.
        val user = user()
        currentUser.refresh(user)
        val projects = BehaviorSubject.create<List<Pair<Project, DiscoveryParams>>>()
        vm.outputs.projectList().subscribe(projects)

        // Initial home all projects params.
        setUpInitialHomeAllProjectsParams()

        // Click on project save
        val project = projects.value[0].first
        vm.inputs.onHeartButtonClicked(project)
        startLoginToutActivityToSaveProject.assertNoValues()
        this.projects.assertValueCount(2)
        assertTrue(projects.value[0].first.isStarred())
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
        setUpEnvironment(environment())

        // Load initial params and root categories from activity.
        setUpInitialHomeAllProjectsParams()

        // Click on editorial
        vm.inputs.editorialViewHolderClicked(Editorial.GO_REWARDLESS)
        vm.inputs.editorialViewHolderClicked(Editorial.LIGHTS_ON)
        startEditorialActivity.assertValues(Editorial.GO_REWARDLESS, Editorial.LIGHTS_ON)
        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testStartProjectActivity_whenViewingEditorial() {
        setUpEnvironment(environment())

        // Load editorial params and root categories from activity.
        val editorialParams = builder()
            .tagId(Editorial.GO_REWARDLESS.tagId)
            .sort(DiscoveryParams.Sort.MAGIC)
            .build()
        vm.inputs.paramsFromActivity(editorialParams)
        vm.inputs.rootCategories(rootCategories())

        // Click on project
        val project = project()
        vm.inputs.projectCardViewHolderClicked(project)
        startProjectActivity.assertValueCount(1)
        assertEquals(startProjectActivity.onNextEvents[0].first, project)
        assertEquals(startProjectActivity.onNextEvents[0].second, collection(518))
        segmentTrack.assertValues(
            EventName.PAGE_VIEWED.eventName,
            EventName.CARD_CLICKED.eventName,
            EventName.CTA_CLICKED.eventName
        )
    }

    @Test
    fun testStartProjectActivity_whenViewingFeatureFlagOn_shouldEmitProjectPageActivity() {
        val currentUser: CurrentUserType = MockCurrentUser()
        val mockExperimentsClientType: MockExperimentsClientType =
            object : MockExperimentsClientType() {
                override fun isFeatureEnabled(feature: OptimizelyFeature.Key): Boolean {
                    return true
                }
            }
        setUpEnvironment(
            environment().toBuilder().currentUser(currentUser).optimizely(mockExperimentsClientType)
                .build()
        )

        // Load editorial params and root categories from activity.
        val editorialParams = builder()
            .tagId(Editorial.GO_REWARDLESS.tagId)
            .sort(DiscoveryParams.Sort.MAGIC)
            .build()
        vm.inputs.paramsFromActivity(editorialParams)
        vm.inputs.rootCategories(rootCategories())

        // Click on project
        val project = project()
        vm.inputs.projectCardViewHolderClicked(project)
        startProjectActivity.assertValueCount(1)
        assertEquals(startProjectActivity.onNextEvents[0].first, project)
        assertEquals(startProjectActivity.onNextEvents[0].second, collection(518))
        segmentTrack.assertValues(
            EventName.PAGE_VIEWED.eventName,
            EventName.CARD_CLICKED.eventName,
            EventName.CTA_CLICKED.eventName
        )
    }

    @Test
    fun testStartProjectActivity_whenViewingAllProjects() {
        setUpEnvironment(environment())

        // Load initial params and root categories from activity.
        setUpInitialHomeAllProjectsParams()

        // Click on project
        val project = project()
        vm.inputs.projectCardViewHolderClicked(project)

        startProjectActivity.assertValueCount(1)
        assertEquals(startProjectActivity.onNextEvents[0].first, project)
        assertEquals(startProjectActivity.onNextEvents[0].second, discovery())
        segmentTrack.assertValues(
            EventName.PAGE_VIEWED.eventName,
            EventName.CARD_CLICKED.eventName,
            EventName.CTA_CLICKED.eventName
        )
    }

    @Test
    fun testStartProjectActivity_whenFeatureFlagEnabled_shouldEmitProjectPageActivity() {
        val currentUser: CurrentUserType = MockCurrentUser()
        val mockExperimentsClientType: MockExperimentsClientType =
            object : MockExperimentsClientType() {
                override fun isFeatureEnabled(feature: OptimizelyFeature.Key): Boolean {
                    return true
                }
            }
        setUpEnvironment(
            environment().toBuilder().currentUser(currentUser).optimizely(mockExperimentsClientType)
                .build()
        )

        // Load initial params and root categories from activity.
        setUpInitialHomeAllProjectsParams()

        // Click on project
        val project = project()
        vm.inputs.projectCardViewHolderClicked(project)
        startProjectActivity.assertValueCount(1)
        assertEquals(startProjectActivity.onNextEvents[0].first, project)
        assertEquals(startProjectActivity.onNextEvents[0].second, discovery())
        segmentTrack.assertValues(
            EventName.PAGE_VIEWED.eventName,
            EventName.CARD_CLICKED.eventName,
            EventName.CTA_CLICKED.eventName
        )
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
        val currentUser: CurrentUserType = MockCurrentUser()
        val throwableError = Throwable()
        val apiClient: MockApiClient = object : MockApiClient() {
            override fun fetchActivities(count: Int?): Observable<ActivityEnvelope> {
                return Observable.error(throwableError)
            }
        }
        val env = environment()
            .toBuilder()
            .currentUser(currentUser)
            .apiClient(apiClient)
            .build()
        setUpEnvironment(env)

        // Load initial params and root categories from activity.
        setUpInitialHomeAllProjectsParams()

        // Log in.
        logUserIn(currentUser)
        activityTest.assertValueCount(1)
        activityTest.assertValue(null)
    }

    @Test
    fun testSuccessResponseForFetchActivitiesWithCount() {
        val currentUser: CurrentUserType = MockCurrentUser()
        val activity = activity()
        val apiClient: MockApiClient = object : MockApiClient() {
            override fun fetchActivities(count: Int?): Observable<ActivityEnvelope> {
                return Observable.just(activityEnvelope(listOf(activity)))
            }
        }
        val env = environment()
            .toBuilder()
            .currentUser(currentUser)
            .apiClient(apiClient)
            .build()
        setUpEnvironment(env)

        // Load initial params and root categories from activity.
        setUpInitialHomeAllProjectsParams()

        // Log in.
        logUserIn(currentUser)
        activityTest.assertValueCount(2)
        activityTest.assertValues(null, activity)
    }

    private fun logUserIn(currentUser: CurrentUserType) {
        val user = user()
        currentUser.refresh(user)
        vm.inputs.paramsFromActivity(getDefaultParams(user))
    }
}
