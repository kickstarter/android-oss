package com.kickstarter.viewmodels

import TriggerThirdPartyEventMutation
import android.content.Intent
import android.content.SharedPreferences
import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.reduceToPreLaunchProject
import com.kickstarter.mock.MockFeatureFlagClient
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Project
import com.kickstarter.models.Urls
import com.kickstarter.models.Web
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.intentmappers.ProjectIntentMapper
import com.kickstarter.viewmodels.projectpage.PrelaunchProjectViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import okhttp3.ResponseBody
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.After
import org.junit.Test
import org.mockito.Mockito
import retrofit2.HttpException
import rx.subjects.BehaviorSubject
import type.TriggerThirdPartyEventInput
import java.util.concurrent.TimeUnit

class PrelaunchProjectViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: PrelaunchProjectViewModel.PrelaunchProjectViewModel

    private val project = BehaviorSubject.create<Project>()
    private val showShareSheet = BehaviorSubject.create<Pair<String, String>>()
    private val startLoginToutActivity = TestSubscriber.create<Unit>()
    private val showSavedPrompt = TestSubscriber.create<Unit>()
    private val startCreatorView = BehaviorSubject.create<Project>()
    private val resultTest = TestSubscriber.create<Project>()

    private val disposables = CompositeDisposable()

    val creator = UserFactory.creator()
    val slug = "best-project-2k19"

    val intent = Intent().putExtra(IntentKey.PROJECT_PARAM, slug)

    private val prelaunchProject by lazy {
        val projectUrl = "https://www.kck.str/projects/" + creator.id().toString() + "/" + slug

        val webUrls = Web.builder()
            .project(projectUrl)
            .rewards("$projectUrl/rewards")
            .updates("$projectUrl/posts")
            .build()

        ProjectFactory
            .prelaunchProject("https://www.kickstarter.com/projects/1186238668/skull-graphic-tee")
            .toBuilder()
            .name("Best Project 2K19")
            .sendThirdPartyEvents(true)
            .isStarred(false)
            .urls(Urls.builder().web(webUrls).build())
            .build()
    }

    private val mockApolloClientV2 = object : MockApolloClientV2() {

        override fun getProject(slug: String): Observable<Project> {
            return Observable
                .just(prelaunchProject)
        }

        override fun getProject(project: Project): Observable<Project> {
            return Observable
                .just(prelaunchProject)
        }

        override fun watchProject(project: Project): Observable<Project> {
            return Observable
                .just(project.toBuilder().isStarred(true).build())
        }

        override fun unWatchProject(project: Project): Observable<Project> {
            return Observable
                .just(project.toBuilder().isStarred(false).build())
        }

        override fun triggerThirdPartyEvent(triggerThirdPartyEventInput: TriggerThirdPartyEventInput): Observable<TriggerThirdPartyEventMutation.Data> {
            return Observable.just(
                TriggerThirdPartyEventMutation.Data(
                    TriggerThirdPartyEventMutation.TriggerThirdPartyEvent(
                        "", true
                    )
                )
            )
        }
    }

    private val testScheduler = io.reactivex.schedulers.TestScheduler()

    private val testEnvironment by lazy {
        environment()
            .toBuilder()
            .apolloClientV2(mockApolloClientV2)
            .schedulerV2(testScheduler)
            .build()
    }

    private fun setUpEnvironment(environment: Environment) {
        this.vm = PrelaunchProjectViewModel.PrelaunchProjectViewModel(environment)

        this.vm.outputs.project().subscribe { this.project.onNext(it) }
            .addToDisposable(disposables)

        this.vm.outputs
            .showShareSheet()
            .subscribe { this.showShareSheet.onNext(it) }
            .addToDisposable(disposables)

        this.vm.outputs
            .startLoginToutActivity().subscribe { this.startLoginToutActivity.onNext(it) }
            .addToDisposable(disposables)

        this.vm.outputs
            .showSavedPrompt().subscribe { this.showSavedPrompt.onNext(it) }
            .addToDisposable(disposables)

        this.vm.outputs
            .startCreatorView().subscribe { this.startCreatorView.onNext(it) }
            .addToDisposable(disposables)

        ProjectIntentMapper.project(intent, MockApolloClientV2()).subscribe {
            resultTest.onNext(prelaunchProject)
        }.addToDisposable(disposables)
    }

    @Test
    fun testProject_loadDeepLinkProject() {
        val user = UserFactory.germanUser().toBuilder().chosenCurrency("CAD").build()
        val deadline = DateTime(DateTimeZone.UTC).plusDays(10)

        val reducedProject = ProjectFactory.project().toBuilder()
            .watchesCount(10)
            .isStarred(true)
            .creator(user)
            .build()
            .reduceToPreLaunchProject().toBuilder().deadline(deadline).build()

        setUpEnvironment(testEnvironment)

        vm.inputs.configureWith(intent = intent.putExtra(IntentKey.PROJECT, reducedProject))
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)

        assertEquals(project.value, reducedProject)
    }

    @Test
    fun testProject_loadProject() {
        setUpEnvironment(
            environment()
                .toBuilder()
                .apolloClientV2(object : MockApolloClientV2() {

                    override fun getProject(slug: String): Observable<Project> {
                        return Observable
                            .error(
                                HttpException(
                                    retrofit2.Response.error<String>(
                                        426,
                                        ResponseBody.create(
                                            null,
                                            content = "Too Many Calls",
                                        ),
                                    ),
                                ),
                            )
                    }

                    override fun getProject(project: Project): Observable<Project> {
                        return Observable
                            .error(
                                HttpException(
                                    retrofit2.Response.error<String>(
                                        426,
                                        ResponseBody.create(
                                            null,
                                            content = "Too Many Calls",
                                        ),
                                    ),
                                ),
                            )
                    }
                })
                .schedulerV2(testScheduler)
                .build(),
        )

        vm.inputs.configureWith(intent = intent)

        assertEquals(project.value, null)
    }

    @Test
    fun testProject_loadProject_withError() {
        setUpEnvironment(testEnvironment)

        vm.inputs.configureWith(intent = intent)

        assertEquals(project.value, prelaunchProject)
    }

    @Test
    fun testToggleBookmark() {
        val currentUser = MockCurrentUser()
        val currentUserV2 = MockCurrentUserV2()

        val mockedEnv = testEnvironment.toBuilder().currentUser(currentUser)
            .currentUserV2(currentUserV2).build()
        setUpEnvironment(mockedEnv)

        vm.inputs.configureWith(intent = intent)

        assertEquals(project.value, prelaunchProject)

        currentUser.refresh(UserFactory.user())
        currentUserV2.refresh(UserFactory.user())

        vm.inputs.bookmarkButtonClicked()

        assertEquals(project.value.isStarred(), true)

        vm.inputs.bookmarkButtonClicked()

        assertEquals(project.value.isStarred(), false)
    }

    @Test
    fun testShowShareSheet() {
        setUpEnvironment(testEnvironment)

        val intent = Intent().putExtra(IntentKey.PROJECT_PARAM, "skull-graphic-tee")

        vm.inputs.configureWith(intent = intent)

        this.vm.inputs.shareButtonClicked()
        val expectedName = "Best Project 2K19"
        val expectedShareUrl = "https://www.kck.str/projects/" + creator.id().toString() + "/" + slug + "?ref=android_project_share"

        assertEquals(showShareSheet.value.first, expectedName)
        assertEquals(showShareSheet.value.second, expectedShareUrl)
    }

    @Test
    fun testLoggedOutStarProjectFlow() {
        val currentUser = MockCurrentUser()
        val currentUserV2 = MockCurrentUserV2()

        val mockedEnv = testEnvironment.toBuilder().currentUser(currentUser)
            .currentUserV2(currentUserV2).build()

        setUpEnvironment(mockedEnv)

        // Start the view model with a project
        vm.inputs.configureWith(intent = intent)

        assertEquals(project.value, prelaunchProject)

        // Try starring while logged out
        vm.inputs.bookmarkButtonClicked()

        assertEquals(project.value.isStarred(), false)
        this.showSavedPrompt.assertValueCount(0)
        this.startLoginToutActivity.assertValueCount(1)

        // Login
        currentUser.refresh(UserFactory.user())
        currentUserV2.refresh(UserFactory.user())

        vm.inputs.bookmarkButtonClicked()
        vm.inputs.bookmarkButtonClicked()
        assertEquals(true, project.value.isStarred())
        this.showSavedPrompt.assertValueCount(1)

        vm.inputs.bookmarkButtonClicked()
        assertEquals(false, project.value.isStarred())
        this.showSavedPrompt.assertValueCount(1)
    }

    @Test
    fun testCreatorDetailsClicked() {
        setUpEnvironment(testEnvironment)

        val intent = Intent().putExtra(IntentKey.PROJECT_PARAM, "skull-graphic-tee")

        vm.inputs.configureWith(intent = intent)
        this.vm.inputs.creatorInfoButtonClicked()

        assertEquals(project.value, prelaunchProject)
    }

    @Test
    fun testThirdPartyEventSent() {
        var sharedPreferences: SharedPreferences = Mockito.mock(SharedPreferences::class.java)
        Mockito.`when`(sharedPreferences.getBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false))
            .thenReturn(true)

        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return true
                }
            }

        val currentUser = MockCurrentUser()
        val currentUserV2 = MockCurrentUserV2()

        val mockedEnv = testEnvironment.toBuilder().currentUser(currentUser)
            .currentUserV2(currentUserV2)
            .featureFlagClient(mockFeatureFlagClient)
            .sharedPreferences(sharedPreferences)
            .build()

        setUpEnvironment(mockedEnv)

        val intent = Intent()
            .putExtra(IntentKey.PROJECT_PARAM, "skull-graphic-tee")
            .putExtra(IntentKey.PREVIOUS_SCREEN, ThirdPartyEventValues.ScreenName.DEEPLINK)

        vm.inputs.configureWith(intent = intent)

        assertEquals(true, this.vm.onThirdPartyEventSent.value)
    }

    @After
    fun cleanUp() {
        disposables.clear()
    }
}
