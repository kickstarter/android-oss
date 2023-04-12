package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.utils.EventName
import com.kickstarter.mock.factories.ActivityFactory.activity
import com.kickstarter.mock.factories.ActivityFactory.friendBackingActivity
import com.kickstarter.mock.factories.ActivityFactory.projectStateChangedActivity
import com.kickstarter.mock.factories.ActivityFactory.projectStateChangedPositiveActivity
import com.kickstarter.mock.factories.ActivityFactory.updateActivity
import com.kickstarter.mock.factories.SurveyResponseFactory.surveyResponse
import com.kickstarter.mock.factories.UserFactory.user
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.models.Activity
import com.kickstarter.models.ErroredBacking
import com.kickstarter.models.Project
import com.kickstarter.models.SurveyResponse
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.viewmodels.usecases.LoginUseCase
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import java.util.Arrays

class ActivityFeedViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ActivityFeedViewModel.ViewModel
    private val activityList = TestSubscriber<List<Activity>>()
    private val erroredBackings = TestSubscriber<List<ErroredBacking>>()
    private val goToDiscovery = TestSubscriber<Void>()
    private val goToLogin = TestSubscriber<Void>()
    private val goToProject = TestSubscriber<Project>()
    private val goToSurvey = TestSubscriber<SurveyResponse>()
    private val loggedOutEmptyStateIsVisible = TestSubscriber<Boolean>()
    private val loggedInEmptyStateIsVisible = TestSubscriber<Boolean>()
    private val startFixPledge = TestSubscriber<String>()
    private val startUpdateActivity = TestSubscriber<Activity>()
    private val surveys = TestSubscriber<List<SurveyResponse>>()
    private val user = TestSubscriber<User>()

    private fun setUpEnvironment(environment: Environment) {
        vm = ActivityFeedViewModel.ViewModel(environment)
        vm.outputs.activityList().subscribe(activityList)
        vm.outputs.erroredBackings().subscribe(erroredBackings)
        vm.outputs.goToDiscovery().subscribe(goToDiscovery)
        vm.outputs.goToLogin().subscribe(goToLogin)
        vm.outputs.goToProject().subscribe(goToProject)
        vm.outputs.goToSurvey().subscribe(goToSurvey)
        vm.outputs.loggedOutEmptyStateIsVisible().subscribe(loggedOutEmptyStateIsVisible)
        vm.outputs.loggedInEmptyStateIsVisible().subscribe(loggedInEmptyStateIsVisible)
        vm.outputs.startFixPledge().subscribe(startFixPledge)
        vm.outputs.startUpdateActivity().subscribe(startUpdateActivity)
        vm.outputs.surveys().subscribe(surveys)
    }

    @Test
    fun testActivitiesEmit() {
        setUpEnvironment(environment())

        // Swipe refresh.
        vm.inputs.refresh()

        // Activities should emit.
        activityList.assertValueCount(1)

        // Paginate.
        vm.inputs.nextPage()
        activityList.assertValueCount(1)
        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testClickingInterfaceElements() {
        setUpEnvironment(environment())
        goToDiscovery.assertNoValues()
        goToLogin.assertNoValues()
        goToProject.assertNoValues()
        startUpdateActivity.assertNoValues()

        // Empty activity feed clicks do not trigger events yet.
        vm.inputs.emptyActivityFeedDiscoverProjectsClicked(null)

        goToDiscovery.assertValueCount(1)

        vm.inputs.emptyActivityFeedLoginClicked(null)

        goToLogin.assertValueCount(1)

        vm.inputs.friendBackingClicked(null, friendBackingActivity())
        vm.inputs.projectStateChangedClicked(null, projectStateChangedActivity())
        vm.inputs.projectStateChangedPositiveClicked(null, projectStateChangedPositiveActivity())
        vm.inputs.projectUpdateProjectClicked(null, updateActivity())

        goToProject.assertValueCount(4)

        vm.inputs.projectUpdateClicked(null, activity())

        startUpdateActivity.assertValueCount(1)

        segmentTrack.assertValues(
            EventName.PAGE_VIEWED.eventName,
            EventName.CTA_CLICKED.eventName,
            EventName.CARD_CLICKED.eventName,
            EventName.CARD_CLICKED.eventName,
            EventName.CARD_CLICKED.eventName,
            EventName.CARD_CLICKED.eventName
        )
    }

    @Test
    fun testClickingInterfaceElements_shouldEmitProjectPage() {
        val currentUser: CurrentUserType = MockCurrentUser()
        val mockExperimentsClientType: MockExperimentsClientType =
            object : MockExperimentsClientType() {
                override fun isFeatureEnabled(feature: OptimizelyFeature.Key): Boolean {
                    return true
                }
            }

        setUpEnvironment(
            environment()
                .toBuilder()
                .currentUser(currentUser)
                .optimizely(mockExperimentsClientType)
                .build()
        )

        goToDiscovery.assertNoValues()
        goToLogin.assertNoValues()
        goToProject.assertNoValues()
        startUpdateActivity.assertNoValues()

        // Empty activity feed clicks do not trigger events yet.
        vm.inputs.emptyActivityFeedDiscoverProjectsClicked(null)

        goToDiscovery.assertValueCount(1)

        vm.inputs.emptyActivityFeedLoginClicked(null)

        goToLogin.assertValueCount(1)

        vm.inputs.friendBackingClicked(null, friendBackingActivity())
        vm.inputs.projectStateChangedClicked(null, projectStateChangedActivity())
        vm.inputs.projectStateChangedPositiveClicked(null, projectStateChangedPositiveActivity())
        vm.inputs.projectUpdateProjectClicked(null, updateActivity())

        goToProject.assertValueCount(4)

        // this.goToProject.assertValues(); TODO
        vm.inputs.projectUpdateClicked(null, activity())

        startUpdateActivity.assertValueCount(1)

        segmentTrack.assertValues(
            EventName.PAGE_VIEWED.eventName,
            EventName.CTA_CLICKED.eventName,
            EventName.CARD_CLICKED.eventName,
            EventName.CARD_CLICKED.eventName,
            EventName.CARD_CLICKED.eventName,
            EventName.CARD_CLICKED.eventName
        )
    }

    @Test
    fun testErroredBackings_whenLoggedIn() {
        val currentUser: CurrentUserType = MockCurrentUser()
        val currentUserV2 = MockCurrentUserV2()
        val initialUser = user()
        val updatedUser = user()

        val environment = environment().toBuilder()
            .apiClient(object : MockApiClient() {
                override fun fetchCurrentUser(): Observable<User> {
                    return Observable.just(updatedUser)
                }
            })
            .currentUser(currentUser)
            .currentUserV2(currentUserV2)
            .build()

        val loginUserCase = LoginUseCase(environment)

        loginUserCase.login(initialUser, "deadbeef")

        setUpEnvironment(environment)
        erroredBackings.assertValueCount(1)
        vm.inputs.refresh()
        erroredBackings.assertValueCount(2)
        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testErroredBackings_whenLoggedOut() {
        setUpEnvironment(environment())
        vm.inputs.resume()
        erroredBackings.assertNoValues()
        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testLoginFlow() {
        val apiClient: ApiClientType = MockApiClient()
        val currentUser: CurrentUserType = MockCurrentUser()
        val environment = environment().toBuilder()
            .apiClient(apiClient)
            .currentUser(currentUser)
            .build()
        setUpEnvironment(environment)

        // Empty activity feed with login button should be shown.
        loggedOutEmptyStateIsVisible.assertValue(true)

        // Login.
        vm.inputs.emptyActivityFeedLoginClicked(null)
        goToLogin.assertValueCount(1)
        currentUser.refresh(user())

        // Empty states are not shown when activities emit on successful login.
        activityList.assertValueCount(1)
        loggedOutEmptyStateIsVisible.assertValues(true, false)
        loggedInEmptyStateIsVisible.assertValue(false)
    }

    @Test
    fun testSurveys_LoggedOut() {
        val surveyResponses = Arrays.asList(
            surveyResponse(),
            surveyResponse()
        )

        val apiClient: MockApiClient = object : MockApiClient() {
            override fun fetchUnansweredSurveys(): Observable<List<SurveyResponse>> {
                return Observable.just(surveyResponses)
            }
        }

        val currentUser: CurrentUserType = MockCurrentUser()
        currentUser.logout()

        val environment = environment().toBuilder()
            .apiClient(apiClient)
            .currentUser(currentUser)
            .build()

        setUpEnvironment(environment)

        vm.inputs.resume()

        surveys.assertNoValues()
    }

    @Test
    fun testStartFixPledge() {
        setUpEnvironment(environment())

        val projectSlug = "slug"

        vm.inputs.managePledgeClicked(projectSlug)

        assertEquals(startFixPledge.onNextEvents[0], projectSlug)
    }

    @Test
    fun testStartFixPledge_shouldEmitToFixPledgeProjectPage() {
        val currentUser: CurrentUserType = MockCurrentUser()
        val mockExperimentsClientType: MockExperimentsClientType =
            object : MockExperimentsClientType() {
                override fun isFeatureEnabled(feature: OptimizelyFeature.Key): Boolean {
                    return true
                }
            }

        setUpEnvironment(
            environment()
                .toBuilder()
                .currentUser(currentUser)
                .optimizely(mockExperimentsClientType).build()
        )

        val projectSlug = "slug"

        vm.inputs.managePledgeClicked(projectSlug)

        startFixPledge.assertValueCount(1)

        assertTrue(startFixPledge.onNextEvents[0] === projectSlug)
    }

    @Test
    fun testStartUpdateActivity() {
        val activity = updateActivity()

        setUpEnvironment(environment())

        vm.inputs.projectUpdateClicked(null, activity)

        startUpdateActivity.assertValues(activity)
    }

    @Test
    fun testSurveys_LoggedIn_SwipeRefreshed() {
        val currentUser: CurrentUserType = MockCurrentUser()
        val currentUserV2 = MockCurrentUserV2()

        val environment = environment().toBuilder()
            .currentUser(currentUser)
            .currentUser(currentUser)
            .currentUserV2(currentUserV2)
            .build()

        val loginUserCase = LoginUseCase(environment)

        setUpEnvironment(environment)

        loginUserCase.login(user(), "deadbeef")

        surveys.assertValueCount(1)

        vm.inputs.refresh()
        surveys.assertValueCount(2)
    }

    @Test
    fun testUser_LoggedIn_SwipeRefreshed() {
        val currentUser: CurrentUserType = MockCurrentUser()
        val initialUser = user().toBuilder().unseenActivityCount(3).build()

        val currentUserV2 = MockCurrentUserV2()

        val updatedUser = user()

        val environment = environment().toBuilder()
            .apiClient(object : MockApiClient() {
                override fun fetchCurrentUser(): Observable<User> {
                    return Observable.just(updatedUser)
                }
            })
            .currentUser(currentUser)
            .currentUserV2(currentUserV2)
            .build()

        val loginUserCase = LoginUseCase(environment)

        loginUserCase.login(initialUser, "deadbeef")

        environment.currentUser()?.loggedInUser()?.subscribe(user)

        setUpEnvironment(environment)

        surveys.assertValueCount(1)

        user.assertValues(initialUser, updatedUser)

        vm.inputs.refresh()

        surveys.assertValueCount(2)
        user.assertValues(initialUser, updatedUser)
    }

    @Test
    fun testUser_whenLoggedInAndResumedWithErroredBackings() {
        val currentUser: CurrentUserType = MockCurrentUser()
        val currentUserV2 = MockCurrentUserV2()

        val initialUser = user()
            .toBuilder()
            .erroredBackingsCount(3)
            .build()

        val updatedUser = user()
        val environment = environment().toBuilder()
            .apiClient(object : MockApiClient() {
                override fun fetchCurrentUser(): Observable<User> {
                    return Observable.just(updatedUser)
                }
            })
            .currentUser(currentUser)
            .currentUserV2(currentUserV2)
            .build()

        val loginUseCase = LoginUseCase(environment)
        loginUseCase.login(initialUser, "token")

        environment.currentUser()?.loggedInUser()?.subscribe(user)

        setUpEnvironment(environment)

        user.assertValues(initialUser, updatedUser)

        vm.inputs.resume()
        user.assertValues(initialUser, updatedUser)
    }
}
