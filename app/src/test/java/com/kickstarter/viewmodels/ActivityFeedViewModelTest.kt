package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.utils.EventName
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.ActivityFactory.activity
import com.kickstarter.mock.factories.ActivityFactory.friendBackingActivity
import com.kickstarter.mock.factories.ActivityFactory.projectStateChangedActivity
import com.kickstarter.mock.factories.ActivityFactory.projectStateChangedPositiveActivity
import com.kickstarter.mock.factories.ActivityFactory.updateActivity
import com.kickstarter.mock.factories.SurveyResponseFactory.surveyResponse
import com.kickstarter.mock.factories.UserFactory.user
import com.kickstarter.mock.services.MockApiClientV2
import com.kickstarter.models.Activity
import com.kickstarter.models.ErroredBacking
import com.kickstarter.models.Project
import com.kickstarter.models.SurveyResponse
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.viewmodels.ActivityFeedViewModel.ActivityFeedViewModel
import com.kickstarter.viewmodels.ActivityFeedViewModel.Factory
import com.kickstarter.viewmodels.usecases.LoginUseCase
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class ActivityFeedViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ActivityFeedViewModel
    private val activityList = TestSubscriber<List<Activity>>()
    private val erroredBackings = TestSubscriber<List<ErroredBacking>>()
    private val goToDiscovery = TestSubscriber<Unit>()
    private val goToLogin = TestSubscriber<Unit>()
    private val goToProject = TestSubscriber<Project>()
    private val goToSurvey = TestSubscriber<SurveyResponse>()
    private val loggedOutEmptyStateIsVisible = TestSubscriber<Boolean>()
    private val loggedInEmptyStateIsVisible = TestSubscriber<Boolean>()
    private val startFixPledge = TestSubscriber<String>()
    private val startUpdateActivity = TestSubscriber<Activity>()
    private val surveys = TestSubscriber<List<SurveyResponse>>()
    private val user = TestSubscriber<User>()
    private val disposables = CompositeDisposable()

    private fun setUpEnvironment(environment: Environment) {
        vm = Factory(environment).create(ActivityFeedViewModel::class.java)
        vm.outputs.activityList().subscribe { activityList.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.erroredBackings().subscribe { erroredBackings.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.goToDiscovery().subscribe { goToDiscovery.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.goToLogin().subscribe { goToLogin.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.goToProject().subscribe { goToProject.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.goToSurvey().subscribe { goToSurvey.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.loggedOutEmptyStateIsVisible().subscribe { loggedOutEmptyStateIsVisible.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.loggedInEmptyStateIsVisible().subscribe { loggedInEmptyStateIsVisible.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.startFixPledge().subscribe { startFixPledge.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.startUpdateActivity().subscribe { startUpdateActivity.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.surveys().subscribe { surveys.onNext(it) }
            .addToDisposable(disposables)
    }

    @After
    fun cleanUp() {
        disposables.clear()
    }

    @Test
    fun testActivitiesEmit() {

        val environment = environment()
            .toBuilder()
            .apiClientV2(MockApiClientV2())
            .build()
        setUpEnvironment(environment)

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
        val currentUser: CurrentUserTypeV2 = MockCurrentUserV2()

        setUpEnvironment(
            environment()
                .toBuilder()
                .currentUserV2(currentUser)
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
        val currentUser: CurrentUserTypeV2 = MockCurrentUserV2()
        val currentUserV2 = MockCurrentUserV2()
        val initialUser = user()
        val updatedUser = user()

        val environment = environment().toBuilder()
            .apiClientV2(object : MockApiClientV2() {
                override fun fetchCurrentUser(): Observable<User> {
                    return Observable.just(updatedUser)
                }
            })
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
        val apiClient: ApiClientTypeV2 = MockApiClientV2()
        val currentUser: CurrentUserTypeV2 = MockCurrentUserV2()
        val environment = environment().toBuilder()
            .apiClientV2(apiClient)
            .currentUserV2(currentUser)
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
        val surveyResponses = listOf(
            surveyResponse(),
            surveyResponse()
        )

        val apiClient: MockApiClientV2 = object : MockApiClientV2() {
            override fun fetchUnansweredSurveys(): Observable<List<SurveyResponse>> {
                return Observable.just(surveyResponses)
            }
        }

        val currentUser: CurrentUserTypeV2 = MockCurrentUserV2()
        currentUser.logout()

        val environment = environment().toBuilder()
            .apiClientV2(apiClient)
            .currentUserV2(currentUser)
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

        // TODO
        // assertEquals(startFixPledge.onNextEvents[0], projectSlug)
    }

    @Test
    fun testStartFixPledge_shouldEmitToFixPledgeProjectPage() {
        val currentUser: CurrentUserTypeV2 = MockCurrentUserV2()

        setUpEnvironment(
            environment()
                .toBuilder()
                .currentUserV2(currentUser)
                .build()
        )

        val projectSlug = "slug"

        vm.inputs.managePledgeClicked(projectSlug)

        startFixPledge.assertValueCount(1)

        // TODO
        // assertTrue(startFixPledge.onNextEvents[0] === projectSlug)
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
        val currentUserV2 = MockCurrentUserV2()

        val environment = environment().toBuilder()
            .apiClientV2(MockApiClientV2())
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
        val initialUser = user().toBuilder().unseenActivityCount(3).build()

        val currentUserV2 = MockCurrentUserV2()

        val updatedUser = user()

        val environment = environment().toBuilder()
            .apiClientV2(object : MockApiClientV2() {
                override fun fetchCurrentUser(): Observable<User> {
                    return Observable.just(updatedUser)
                }
            })
            .currentUserV2(currentUserV2)
            .build()

        val loginUserCase = LoginUseCase(environment)

        loginUserCase.login(initialUser, "deadbeef")

        environment.currentUserV2()?.loggedInUser()?.subscribe { user.onNext(it) }
            ?.addToDisposable(disposables)

        setUpEnvironment(environment)

        surveys.assertValueCount(1)

        user.assertValues(initialUser, updatedUser)

        vm.inputs.refresh()

        surveys.assertValueCount(2)
        user.assertValues(initialUser, updatedUser)
    }

    @Test
    fun testUser_whenLoggedInAndResumedWithErroredBackings() {
        val currentUserV2 = MockCurrentUserV2()

        val initialUser = user()
            .toBuilder()
            .erroredBackingsCount(3)
            .build()

        val updatedUser = user()
        val environment = environment().toBuilder()
            .apiClientV2(object : MockApiClientV2() {
                override fun fetchCurrentUser(): Observable<User> {
                    return Observable.just(updatedUser)
                }
            })
            .currentUserV2(currentUserV2)
            .build()

        val loginUseCase = LoginUseCase(environment)
        loginUseCase.login(initialUser, "token")

        environment.currentUserV2()?.loggedInUser()?.subscribe { user.onNext(it) }
            ?.addToDisposable(disposables)

        setUpEnvironment(environment)

        user.assertValues(initialUser, updatedUser)

        vm.inputs.resume()
        user.assertValues(initialUser, updatedUser)
    }
}
