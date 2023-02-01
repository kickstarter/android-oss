package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.RefTag.Companion.search
import com.kickstarter.libs.RefTag.Companion.searchFeatured
import com.kickstarter.libs.RefTag.Companion.searchPopular
import com.kickstarter.libs.RefTag.Companion.searchPopularFeatured
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.libs.utils.EventName
import com.kickstarter.mock.MockExperimentsClientType
import com.kickstarter.mock.factories.DiscoverEnvelopeFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.ProjectFactory.allTheWayProject
import com.kickstarter.mock.factories.ProjectFactory.almostCompletedProject
import com.kickstarter.mock.factories.ProjectFactory.backedProject
import com.kickstarter.mock.factories.ProjectFactory.project
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.apiresponses.DiscoverEnvelope
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import rx.schedulers.TestScheduler
import java.util.Arrays
import java.util.concurrent.TimeUnit

class SearchViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: SearchViewModel.ViewModel

    private val goToProject = TestSubscriber<Project>()
    private val goToRefTag = TestSubscriber<RefTag>()
    private val popularProjects = TestSubscriber<List<Project>>()
    private val popularProjectsPresent = TestSubscriber<Boolean>()
    private val searchProjects = TestSubscriber<List<Project>>()
    private val searchProjectsPresent = TestSubscriber<Boolean>()
    private val startPreLaunchProjectActivity = TestSubscriber<Pair<Project, RefTag>>()

    private fun setUpEnvironment(environment: Environment) {
        vm = SearchViewModel.ViewModel(environment)
        vm.outputs.startProjectActivity()
            .map { it.first }
            .subscribe(
                goToProject
            )
        vm.outputs.startProjectActivity()
            .map { it.second }
            .subscribe(
                goToRefTag
            )
        vm.outputs.popularProjects().subscribe(popularProjects)
        vm.outputs.startPreLaunchProjectActivity().subscribe(startPreLaunchProjectActivity)
        vm.outputs.searchProjects().subscribe(searchProjects)
        vm.outputs.popularProjects().map { ps: List<Project?> -> ps.size > 0 }
            .subscribe(popularProjectsPresent)
        vm.outputs.searchProjects().map { ps: List<Project?> -> ps.size > 0 }
            .subscribe(searchProjectsPresent)
    }

    @Test
    fun testPopularProjectsLoadImmediately() {
        setUpEnvironment(environment())

        popularProjectsPresent.assertValues(true)
        searchProjectsPresent.assertNoValues()
    }

    @Test
    fun testSearchProjectsWhenEnterSearchTerm() {
        val scheduler = TestScheduler()
        val env = environment().toBuilder()
            .scheduler(scheduler)
            .build()
        setUpEnvironment(env)

        // Popular projects emit immediately.
        popularProjectsPresent.assertValues(true)
        searchProjectsPresent.assertNoValues()
        segmentTrack.assertValue(EventName.CTA_CLICKED.eventName)

        // Searching shouldn't emit values immediately
        vm.inputs.search("hello")
        searchProjectsPresent.assertNoValues()
        segmentTrack.assertValue(EventName.CTA_CLICKED.eventName)

        // Waiting a small amount time shouldn't emit values
        scheduler.advanceTimeBy(200, TimeUnit.MILLISECONDS)
        searchProjectsPresent.assertNoValues()
        segmentTrack.assertValue(EventName.CTA_CLICKED.eventName)

        // Waiting the rest of the time makes the search happen
        scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS)
        searchProjectsPresent.assertValues(false, true)
        scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS)
        segmentTrack.assertValues(EventName.CTA_CLICKED.eventName)

        // Typing more search terms doesn't emit more values
        vm.inputs.search("hello world!")

        searchProjectsPresent.assertValues(false, true)
        scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS)
        segmentTrack.assertValues(EventName.CTA_CLICKED.eventName, EventName.PAGE_VIEWED.eventName)

        // Waiting enough time emits search results
        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS)
        searchProjectsPresent.assertValues(false, true, false, true)

        // Clearing search terms brings back popular projects.
        vm.inputs.search("")
        searchProjectsPresent.assertValues(false, true, false, true, false)
        popularProjectsPresent.assertValues(true, false, true)
    }

    @Test
    fun testSearchPagination() {
        val scheduler = TestScheduler()
        val env = environment().toBuilder()
            .scheduler(scheduler)
            .build()

        setUpEnvironment(env)

        searchProjectsPresent.assertNoValues()
        segmentTrack.assertValues(EventName.CTA_CLICKED.eventName)

        vm.inputs.search("cats")

        scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS)
        searchProjectsPresent.assertValues(false, true)

        vm.inputs.nextPage()

        searchProjectsPresent.assertValues(false, true)
    }

    @Test
    fun testFeaturedSearchRefTags() {
        val scheduler = TestScheduler()
        val projects = Arrays.asList(
            allTheWayProject(),
            almostCompletedProject(),
            backedProject()
        )
        val apiClient: MockApiClient = object : MockApiClient() {
            override fun fetchProjects(params: DiscoveryParams): Observable<DiscoverEnvelope> {
                return Observable.just(DiscoverEnvelopeFactory.discoverEnvelope(projects))
            }
        }

        val env = environment().toBuilder()
            .scheduler(scheduler)
            .apiClient(apiClient)
            .build()

        setUpEnvironment(env)

        vm.inputs.search("cat")
        scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS)

        vm.inputs.projectClicked(projects[0])

        goToRefTag.assertValues(searchFeatured())
        goToProject.assertValues(projects[0])
    }

    @Test
    fun testSearchRefTags() {
        val scheduler = TestScheduler()
        val projects = Arrays.asList(
            allTheWayProject(),
            almostCompletedProject(),
            backedProject()
        )
        val apiClient: MockApiClient = object : MockApiClient() {
            override fun fetchProjects(params: DiscoveryParams): Observable<DiscoverEnvelope> {
                return Observable.just(DiscoverEnvelopeFactory.discoverEnvelope(projects))
            }
        }
        val env = environment().toBuilder()
            .scheduler(scheduler)
            .apiClient(apiClient)
            .build()
        setUpEnvironment(env)

        // populate search and overcome debounce
        vm.inputs.search("cat")
        scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS)

        vm.inputs.projectClicked(projects[1])

        goToRefTag.assertValues(search())
        goToProject.assertValues(projects[1])
    }

    @Test
    fun testFeaturedPopularRefTags() {
        val scheduler = TestScheduler()
        val projects = Arrays.asList(
            allTheWayProject(),
            almostCompletedProject(),
            backedProject()
        )
        val apiClient: MockApiClient = object : MockApiClient() {
            override fun fetchProjects(params: DiscoveryParams): Observable<DiscoverEnvelope> {
                return Observable.just(DiscoverEnvelopeFactory.discoverEnvelope(projects))
            }
        }
        val env = environment().toBuilder()
            .scheduler(scheduler)
            .apiClient(apiClient)
            .build()
        setUpEnvironment(env)

        // populate search and overcome debounce
        vm.inputs.search("")
        scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS)

        vm.inputs.projectClicked(projects[0])

        goToRefTag.assertValues(searchPopularFeatured())
        goToProject.assertValues(projects[0])
    }

    @Test
    fun testPopularRefTags() {
        val scheduler = TestScheduler()
        val projects = Arrays.asList(
            allTheWayProject(),
            almostCompletedProject(),
            backedProject()
        )
        val apiClient: MockApiClient = object : MockApiClient() {
            override fun fetchProjects(params: DiscoveryParams): Observable<DiscoverEnvelope> {
                return Observable.just(DiscoverEnvelopeFactory.discoverEnvelope(projects))
            }
        }
        val env = environment().toBuilder()
            .scheduler(scheduler)
            .apiClient(apiClient)
            .build()
        setUpEnvironment(env)

        // populate search and overcome debounce
        vm.inputs.search("")
        scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS)

        vm.inputs.projectClicked(projects[2])

        goToRefTag.assertValues(searchPopular())
        goToProject.assertValues(projects[2])
    }

    @Test
    fun testPopularRefTags_WithPreLaunchProject() {
        val scheduler = TestScheduler()
        val projects = Arrays.asList(
            allTheWayProject(),
            almostCompletedProject(),
            ProjectFactory.prelaunchProject("")
        )
        val apiClient: MockApiClient = object : MockApiClient() {
            override fun fetchProjects(params: DiscoveryParams): Observable<DiscoverEnvelope> {
                return Observable.just(DiscoverEnvelopeFactory.discoverEnvelope(projects))
            }
        }
        val env = environment().toBuilder()
            .scheduler(scheduler)
            .apiClient(apiClient)
            .build()
        setUpEnvironment(env)

        // populate search and overcome debounce
        vm.inputs.search("")
        scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS)

        vm.inputs.projectClicked(projects[2])

        goToRefTag.assertNoValues()
        goToProject.assertValueCount(0)
        startPreLaunchProjectActivity.assertValueCount(1)
        assertEquals(startPreLaunchProjectActivity.onNextEvents[0].first, projects[2])
    }

    @Test
    fun testProjectPage_whenFeatureFlagOn_shouldEmitProjectPage() {
        val user = MockCurrentUser()
        val mockExperimentsClientType: MockExperimentsClientType =
            object : MockExperimentsClientType() {
                override fun isFeatureEnabled(feature: OptimizelyFeature.Key): Boolean {
                    return true
                }
            }
        val scheduler = TestScheduler()
        val projects = listOf(
            allTheWayProject(),
            almostCompletedProject(),
            backedProject()
        )
        val apiClient: MockApiClient = object : MockApiClient() {
            override fun fetchProjects(params: DiscoveryParams): Observable<DiscoverEnvelope> {
                return Observable.just(DiscoverEnvelopeFactory.discoverEnvelope(projects))
            }
        }
        val env = environment().toBuilder()
            .currentUser(user)
            .optimizely(mockExperimentsClientType)
            .scheduler(scheduler)
            .apiClient(apiClient)
            .build()
        setUpEnvironment(env)

        // populate search and overcome debounce
        vm.inputs.search("")
        scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS)

        vm.inputs.projectClicked(projects[2])

        goToProject.assertValues(projects[2])
    }

    @Test
    fun testNoResults() {
        val scheduler = TestScheduler()
        val projects = Arrays.asList<Project>()
        val apiClient: MockApiClient = object : MockApiClient() {
            override fun fetchProjects(params: DiscoveryParams): Observable<DiscoverEnvelope> {
                return Observable.just(DiscoverEnvelopeFactory.discoverEnvelope(projects))
            }
        }
        val env = environment().toBuilder()
            .scheduler(scheduler)
            .apiClient(apiClient)
            .build()
        setUpEnvironment(env)

        // populate search and overcome debounce
        vm.inputs.search("__")
        scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS)

        searchProjects.assertValueCount(2)
    }

    @Test
    fun init_whenProjectCardClicked_shouldTrackProjectEvent() {
        val scheduler = TestScheduler()
        val env = environment().toBuilder()
            .scheduler(scheduler)
            .build()
        setUpEnvironment(env)
        vm.inputs.search("hello")

        segmentTrack.assertValues(EventName.CTA_CLICKED.eventName)

        vm.inputs.projectClicked(project())

        segmentTrack.assertValues(EventName.CTA_CLICKED.eventName, EventName.CTA_CLICKED.eventName)
    }
}
