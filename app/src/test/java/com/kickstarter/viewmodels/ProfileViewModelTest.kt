package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.utils.EventName
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.DiscoverEnvelopeFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApiClientV2
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.apiresponses.DiscoverEnvelope
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class ProfileViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: ProfileViewModel.ProfileViewModel
    private val avatarImageViewUrl = TestSubscriber<String>()
    private val backedCountTextViewHidden = TestSubscriber<Boolean>()
    private val backedCountTextViewText = TestSubscriber<String>()
    private val backedTextViewHidden = TestSubscriber<Boolean>()
    private val createdCountTextViewHidden = TestSubscriber<Boolean>()
    private val createdCountTextViewText = TestSubscriber<String>()
    private val createdTextViewHidden = TestSubscriber<Boolean>()
    private val dividerViewHidden = TestSubscriber<Boolean>()
    private val projectList = TestSubscriber<List<Project>>()
    private val resumeDiscoveryActivity = TestSubscriber<Unit>()
    private val startMessageThreadsActivity = TestSubscriber<Unit>()
    private val startProjectActivity = TestSubscriber<Project>()
    private val userNameTextViewText = TestSubscriber<String>()

    private val disposables = CompositeDisposable()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ProfileViewModel.ProfileViewModel(environment)

        this.vm.outputs.avatarImageViewUrl().subscribe { this.avatarImageViewUrl.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.backedCountTextViewHidden()
            .subscribe { this.backedCountTextViewHidden.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.backedCountTextViewText()
            .subscribe { this.backedCountTextViewText.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.backedTextViewHidden().subscribe { this.backedTextViewHidden.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.createdCountTextViewHidden()
            .subscribe { this.createdCountTextViewHidden.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.createdCountTextViewText()
            .subscribe { this.createdCountTextViewText.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.createdTextViewHidden().subscribe { this.createdTextViewHidden.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.dividerViewHidden().subscribe { this.dividerViewHidden.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.projectList().subscribe { this.projectList.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.resumeDiscoveryActivity()
            .subscribe { this.resumeDiscoveryActivity.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.startMessageThreadsActivity()
            .subscribe { this.startMessageThreadsActivity.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.startProjectActivity().subscribe { this.startProjectActivity.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.userNameTextViewText().subscribe { this.userNameTextViewText.onNext(it) }
            .addToDisposable(disposables)
    }

    @Test
    fun testProfileViewModel_EmitsBackedAndCreatedProjectsData() {
        val user = UserFactory.user().toBuilder()
            .backedProjectsCount(15)
            .createdProjectsCount(2)
            .build()

        val apiClient = object : MockApiClientV2() {
            override fun fetchCurrentUser(): Observable<User> {
                return Observable.just(user)
            }
        }

        setUpEnvironment(environment().toBuilder().apiClientV2(apiClient).build())

        // Backed text views are displayed.
        this.backedCountTextViewHidden.assertValues(false)
        this.backedCountTextViewText.assertValues(NumberUtils.format(user.backedProjectsCount()))
        this.backedTextViewHidden.assertValues(false)

        // Created text views are displayed.
        this.createdCountTextViewHidden.assertValues(false)
        this.createdCountTextViewText.assertValues(NumberUtils.format(user.createdProjectsCount()))
        this.createdTextViewHidden.assertValues(false)

        // Divider view is displayed.
        this.dividerViewHidden.assertValues(false)
    }

    @Test
    fun testProfileViewModel_EmitsBackedProjectsData() {
        val user = UserFactory.user().toBuilder()
            .backedProjectsCount(5)
            .createdProjectsCount(0)
            .build()

        val apiClient = object : MockApiClientV2() {
            override fun fetchCurrentUser(): Observable<User> {
                return Observable.just(user)
            }
        }

        setUpEnvironment(environment().toBuilder().apiClientV2(apiClient).build())

        // Backed text views are displayed.
        this.backedCountTextViewHidden.assertValues(false)
        this.backedCountTextViewText.assertValues(NumberUtils.format(user.backedProjectsCount()))
        this.backedTextViewHidden.assertValues(false)

        // Created text views are hidden.
        this.createdCountTextViewHidden.assertValues(true)
        this.createdCountTextViewText.assertNoValues()
        this.createdTextViewHidden.assertValues(true)

        // Divider view is hidden.
        this.dividerViewHidden.assertValues(true)
    }

    @Test
    fun testProfileViewModel_EmitsCreatedProjectsData() {
        val user = UserFactory.user().toBuilder()
            .backedProjectsCount(0)
            .createdProjectsCount(2)
            .build()

        val apiClient = object : MockApiClientV2() {
            override fun fetchCurrentUser(): Observable<User> {
                return Observable.just(user)
            }
        }

        setUpEnvironment(environment().toBuilder().apiClientV2(apiClient).build())

        // Backed text views are hidden.
        this.backedCountTextViewHidden.assertValues(true)
        this.backedCountTextViewText.assertNoValues()
        this.backedTextViewHidden.assertValues(true)

        // Created text views are displayed.
        this.createdCountTextViewHidden.assertValues(false)
        this.createdCountTextViewText.assertValues(NumberUtils.format(user.createdProjectsCount()))
        this.createdTextViewHidden.assertValues(false)

        // Divider view is hidden.
        this.dividerViewHidden.assertValues(true)
    }

    @Test
    fun testProfileViewModel_EmitsProjects() {
        val apiClient = object : MockApiClientV2() {
            override fun fetchProjects(params: DiscoveryParams): Observable<DiscoverEnvelope> {
                return Observable.just(
                    DiscoverEnvelopeFactory.discoverEnvelope(listOf(ProjectFactory.project()))
                )
            }
        }

        setUpEnvironment(environment().toBuilder().apiClientV2(apiClient).build())

        this.projectList.assertValueCount(1)
    }

    @Test
    fun testProfileViewModel_EmitsUserNameAndAvatar() {
        val user = UserFactory.user()
        val apiClient = object : MockApiClientV2() {
            override fun fetchCurrentUser(): Observable<User> {
                return Observable.just(user)
            }
        }

        setUpEnvironment(environment().toBuilder().apiClientV2(apiClient).build())

        this.avatarImageViewUrl.assertValues(user.avatar().medium())
        this.userNameTextViewText.assertValues(user.name())
    }

    @Test
    fun testProfileViewModel_ResumeDiscoveryActivity() {
        setUpEnvironment(environment())

        this.vm.inputs.exploreProjectsButtonClicked()
        this.resumeDiscoveryActivity.assertValueCount(1)
    }

    @Test
    fun testProfileViewModel_StartMessageThreadsActivity() {
        setUpEnvironment(environment())

        this.vm.inputs.messagesButtonClicked()
        this.startMessageThreadsActivity.assertValueCount(1)
    }

    @Test
    fun testProfileViewModel_StartProjectActivity() {
        setUpEnvironment(environment())

        val project = ProjectFactory.project()
        this.vm.inputs.projectCardClicked(project)
        this.startProjectActivity.assertValueCount(1)
        assertEquals(this.startProjectActivity.values().first(), project)
        this.segmentTrack.assertValue(EventName.CARD_CLICKED.eventName)
    }

    @Test
    fun testProfileViewModel_shouldEmitProjectPage() {
        val user = MockCurrentUser()

        val environment = environment().toBuilder()
            .currentUser(user)
            .build()

        setUpEnvironment(environment)

        val project = ProjectFactory.project()
        this.vm.inputs.projectCardClicked(project)
        this.startProjectActivity.assertValueCount(1)
        assertEquals(this.startProjectActivity.values().first(), project)
        this.segmentTrack.assertValue(EventName.CARD_CLICKED.eventName)
    }

    @After
    fun clear() {
        disposables.clear()
    }
}
