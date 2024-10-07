package com.kickstarter.features.pledgedprojectsoverview.viewmodel

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.testing.TestPager
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.features.pledgedprojectsoverview.data.PPOCardFactory
import com.kickstarter.features.pledgedprojectsoverview.data.PledgedProjectsOverviewEnvelope
import com.kickstarter.features.pledgedprojectsoverview.data.PledgedProjectsOverviewQueryData
import com.kickstarter.libs.AnalyticEvents
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.MockTrackingClient
import com.kickstarter.libs.TrackingClientType
import com.kickstarter.libs.utils.EventName
import com.kickstarter.mock.MockCurrentConfigV2
import com.kickstarter.mock.MockFeatureFlagClient
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Project
import com.kickstarter.services.mutations.CreateOrUpdateBackingAddressData
import io.reactivex.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import rx.observers.TestSubscriber

@OptIn(ExperimentalCoroutinesApi::class)
class PledgedProjectsOverviewViewModelTest : KSRobolectricTestCase() {

    @Test
    fun `emits project when message creator called`() =
        runTest {
            val projectState = mutableListOf<Project>()
            val project = ProjectFactory.successfulProject()
            val viewModel = PledgedProjectsOverviewViewModel.Factory(
                ioDispatcher = UnconfinedTestDispatcher(testScheduler),
                environment = environment().toBuilder()
                    .apolloClientV2(object : MockApolloClientV2() {
                        override fun getProject(slug: String): Observable<Project> {
                            return Observable.just(project)
                        }
                    })
                    .build()
            ).create(PledgedProjectsOverviewViewModel::class.java)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.projectFlow.toList(projectState)
            }

            viewModel.onMessageCreatorClicked("test_project_slug", "projectID", "creatorID", listOf(PPOCardFactory.confirmAddressCard()), 10)

            assertEquals(
                projectState.last(),
                project
            )

            segmentTrack.assertValue(EventName.CTA_CLICKED.eventName)
        }

    @Test
    fun `emits error when message creator called`() =
        runTest {
            var snackbarAction = 0
            val viewModel = PledgedProjectsOverviewViewModel.Factory(
                ioDispatcher = UnconfinedTestDispatcher(testScheduler),
                environment = environment().toBuilder()
                    .apolloClientV2(object : MockApolloClientV2() {
                        override fun getProject(slug: String): Observable<Project> {
                            return Observable.error(Throwable("error"))
                        }
                    }).build()
            ).create(PledgedProjectsOverviewViewModel::class.java)

            viewModel.provideSnackbarMessage { message, _ -> snackbarAction = message }
            viewModel.onMessageCreatorClicked("test_project_slug", "projectID", "creatorID", listOf(PPOCardFactory.confirmAddressCard()), 10)

            // Should equal error string id
            assertEquals(
                snackbarAction,
                R.string.Something_went_wrong_please_try_again
            )
        }

    @Test
    fun `emits error snackbar when confirms address errored`() =
        runTest {
            var snackbarAction = 0
            val viewModel = PledgedProjectsOverviewViewModel.Factory(
                ioDispatcher = UnconfinedTestDispatcher(testScheduler),
                environment = environment().toBuilder()
                    .apolloClientV2(object : MockApolloClientV2() {
                        override fun createOrUpdateBackingAddress(eventInput: CreateOrUpdateBackingAddressData): Observable<Boolean> {
                            return Observable.error(Throwable("error"))
                        }
                    }).build()
            ).create(PledgedProjectsOverviewViewModel::class.java)

            val uiState = mutableListOf<PledgedProjectsOverviewUIState>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.ppoUIState.toList(uiState)
            }

            viewModel.provideSnackbarMessage { message, _ -> snackbarAction = message }
            viewModel.confirmAddress("addressID", "backingID")

            assertEquals(
                snackbarAction,
                R.string.Something_went_wrong_please_try_again
            )

            assertEquals(
                uiState,
                listOf(
                    PledgedProjectsOverviewUIState(isLoading = false, isErrored = false),
                    PledgedProjectsOverviewUIState(isLoading = true, isErrored = false),
                    PledgedProjectsOverviewUIState(isLoading = false, isErrored = false)
                )
            )
        }

    @Test
    fun `emits error snackbar when confirms address response is false`() =
        runTest {
            var snackbarAction = 0
            val viewModel = PledgedProjectsOverviewViewModel.Factory(
                ioDispatcher = UnconfinedTestDispatcher(testScheduler),
                environment = environment().toBuilder()
                    .apolloClientV2(object : MockApolloClientV2() {
                        override fun createOrUpdateBackingAddress(eventInput: CreateOrUpdateBackingAddressData): Observable<Boolean> {
                            return Observable.just(false)
                        }
                    }).build()
            ).create(PledgedProjectsOverviewViewModel::class.java)

            val uiState = mutableListOf<PledgedProjectsOverviewUIState>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.ppoUIState.toList(uiState)
            }

            viewModel.provideSnackbarMessage { message, _ -> snackbarAction = message }
            viewModel.confirmAddress("addressID", "backingID")

            assertEquals(
                snackbarAction,
                R.string.Something_went_wrong_please_try_again
            )

            assertEquals(
                uiState,
                listOf(
                    PledgedProjectsOverviewUIState(isLoading = false, isErrored = false),
                    PledgedProjectsOverviewUIState(isLoading = true, isErrored = false),
                    PledgedProjectsOverviewUIState(isLoading = false, isErrored = false)
                )
            )
        }

    @Test
    fun `emits success snackbar when confirms address response is true`() =
        runTest {
            var snackbarAction = 0
            val viewModel = PledgedProjectsOverviewViewModel.Factory(
                ioDispatcher = UnconfinedTestDispatcher(testScheduler),
                environment = environment().toBuilder()
                    .apolloClientV2(object : MockApolloClientV2() {
                        override fun createOrUpdateBackingAddress(eventInput: CreateOrUpdateBackingAddressData): Observable<Boolean> {
                            return Observable.just(true)
                        }
                    }).build()
            ).create(PledgedProjectsOverviewViewModel::class.java)
            val uiState = mutableListOf<PledgedProjectsOverviewUIState>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.ppoUIState.toList(uiState)
            }
            viewModel.provideSnackbarMessage { message, _ -> snackbarAction = message }
            viewModel.confirmAddress("addressID", "backingID")

            assertEquals(
                snackbarAction,
                R.string.Address_confirmed_need_to_change_your_address_before_it_locks
            )

            assertEquals(
                uiState,
                listOf(
                    PledgedProjectsOverviewUIState(isLoading = false, isErrored = false),
                    PledgedProjectsOverviewUIState(isLoading = true, isErrored = false),
                    PledgedProjectsOverviewUIState(isLoading = false, isErrored = false),
                    PledgedProjectsOverviewUIState(isLoading = true, isErrored = false),
                    PledgedProjectsOverviewUIState(isLoading = false, isErrored = false)
                )
            )
        }

    @Test
    fun `pager result is errored when network response is errored`() {
        runTest {
            val mutableTotalAlerts = MutableStateFlow<Int>(0)
            val user = UserFactory.user()
            val trackingClient = MockTrackingClient(
                MockCurrentUserV2(user),
                MockCurrentConfigV2(),
                TrackingClientType.Type.SEGMENT,
                MockFeatureFlagClient()
            )

            val mockApolloClientV2 = object : MockApolloClientV2() {

                override fun getPledgedProjectsOverviewPledges(inputData: PledgedProjectsOverviewQueryData): Observable<PledgedProjectsOverviewEnvelope> {
                    return Observable.error(Throwable())
                }
            }
            val pagingSource = PledgedProjectsPagingSource(
                mockApolloClientV2,
                AnalyticEvents(listOf(trackingClient)),
                mutableTotalAlerts
            )

            val pager = TestPager(
                PagingConfig(
                    pageSize = 3,
                    prefetchDistance = 3,
                    enablePlaceholders = true,
                ),
                pagingSource
            )

            val result = pager.refresh()
            assertTrue(result is PagingSource.LoadResult.Error)

            val page = pager.getLastLoadedPage()
            assertNull(page)
        }
    }

    @Test
    fun `pager result returns list network call is successful`() {
        runTest {
            val mutableTotalAlerts = MutableStateFlow<Int>(0)
            val totalAlertsList = mutableListOf<Int>()
            val user = UserFactory.user()
            val trackingClient = MockTrackingClient(
                MockCurrentUserV2(user),
                MockCurrentConfigV2(),
                TrackingClientType.Type.SEGMENT,
                MockFeatureFlagClient()
            )
            val mockApolloClientV2 = object : MockApolloClientV2() {

                override fun getPledgedProjectsOverviewPledges(inputData: PledgedProjectsOverviewQueryData): Observable<PledgedProjectsOverviewEnvelope> {
                    return Observable.just(
                        PledgedProjectsOverviewEnvelope.builder().totalCount(10)
                            .pledges(listOf(PPOCardFactory.confirmAddressCard())).build()
                    )
                }
            }

            val segmentTrack: TestSubscriber<String> = TestSubscriber()
            trackingClient.eventNames.subscribe(segmentTrack)

            val pagingSource = PledgedProjectsPagingSource(
                mockApolloClientV2,
                AnalyticEvents(listOf(trackingClient)),
                mutableTotalAlerts,
            )

            var viewModel = PledgedProjectsOverviewViewModel.Factory(
                ioDispatcher = UnconfinedTestDispatcher(testScheduler),
                environment = environment()
            )
                .create(PledgedProjectsOverviewViewModel::class.java)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                mutableTotalAlerts.toList(totalAlertsList)
            }

            val pager = TestPager(
                PagingConfig(
                    pageSize = 3,
                    prefetchDistance = 3,
                    enablePlaceholders = true,
                ),
                pagingSource
            )
            viewModel.getPledgedProjects()

            val result = pager.refresh()
            assertTrue(result is PagingSource.LoadResult.Page)

            val page = pager.getLastLoadedPage()
            assert(page?.data?.size == 1)

            assertEquals(
                totalAlertsList,
                listOf(0, 10)
            )

            segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
        }
    }

// TODO will add tests back after spike MBL-1638 completed
//    @Test
//    fun `emits_error_state_when_errored`() =
//        runTest {
//            val mockApolloClientV2 = object : MockApolloClientV2() {
//
//                override fun getPledgedProjectsOverviewPledges(inputData: PledgedProjectsOverviewQueryData): Observable<PledgedProjectsOverviewEnvelope> {
//                    return Observable.error(Throwable())
//                }
//            }
//
//            val environment = environment().toBuilder().apolloClientV2(mockApolloClientV2).build()
//
//            viewModel = PledgedProjectsOverviewViewModel.Factory(environment = environment)
//                .create(PledgedProjectsOverviewViewModel::class.java)
//
//            val uiState = mutableListOf<PledgedProjectsOverviewUIState>()
//
//            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
//                viewModel.ppoUIState.toList(uiState)
//            }
//
//            viewModel.getPledgedProjects()
//
//            assertEquals(
//                uiState,
//                listOf(
//                    PledgedProjectsOverviewUIState(isLoading = false, isErrored = false),
//                    PledgedProjectsOverviewUIState(isLoading = true, isErrored = false),
//                    PledgedProjectsOverviewUIState(isLoading = false, isErrored = true)
//                )
//            )
//        }
//
//    @Test
//    fun `emits_empty_state_when_no_pledges`() =
//        runTest {
//            val mockApolloClientV2 = object : MockApolloClientV2() {
//
//                override fun getPledgedProjectsOverviewPledges(inputData: PledgedProjectsOverviewQueryData): Observable<PledgedProjectsOverviewEnvelope> {
//                    return Observable.just(PledgedProjectsOverviewEnvelope.builder().totalCount(0).build())
//                }
//            }
//
//            viewModel = PledgedProjectsOverviewViewModel.Factory(environment = environment().toBuilder().apolloClientV2(mockApolloClientV2).build())
//                .create(PledgedProjectsOverviewViewModel::class.java)
//
//            val uiState = mutableListOf<PledgedProjectsOverviewUIState>()
//
//            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
//                viewModel.ppoUIState.toList(uiState)
//            }
//
//            viewModel.getPledgedProjects()
//
//            assertEquals(
//                uiState,
//                listOf(
//                    PledgedProjectsOverviewUIState(isLoading = false, isErrored = false),
//                    PledgedProjectsOverviewUIState(isLoading = true, isErrored = false),
//                    PledgedProjectsOverviewUIState(isLoading = false, isErrored = false)
//                )
//            )
//        }

//    @Test
//    fun `emits_loading_then_success_state_when_successful`() =
//        runTest {
//            val mockApolloClientV2 = object : MockApolloClientV2() {
//
//                override fun getPledgedProjectsOverviewPledges(inputData: PledgedProjectsOverviewQueryData): Observable<PledgedProjectsOverviewEnvelope> {
//                    return Observable.just(PledgedProjectsOverviewEnvelope.builder().totalCount(10).pledges(listOf(PPOCardFactory.confirmAddressCard())).build())
//                }
//            }
//
//            val environment = environment().toBuilder().apolloClientV2(mockApolloClientV2).build()
//
//            viewModel = PledgedProjectsOverviewViewModel.Factory(environment = environment)
//                .create(PledgedProjectsOverviewViewModel::class.java)
//
//            val uiState = mutableListOf<PledgedProjectsOverviewUIState>()
//
//            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
//                viewModel.ppoUIState.toList(uiState)
//            }
//
//            viewModel.getPledgedProjects()
//
//            assertEquals(
//                uiState,
//                listOf(
//                    PledgedProjectsOverviewUIState(isLoading = false, isErrored = false),
//                    PledgedProjectsOverviewUIState(isLoading = true, isErrored = false),
//                    PledgedProjectsOverviewUIState(isLoading = false, isErrored = false)
//                )
//            )
//        }
}
