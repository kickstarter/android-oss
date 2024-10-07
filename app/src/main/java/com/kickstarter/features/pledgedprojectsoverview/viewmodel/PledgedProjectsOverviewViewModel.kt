package com.kickstarter.features.pledgedprojectsoverview.viewmodel

import androidx.compose.material.SnackbarDuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.kickstarter.R
import com.kickstarter.features.pledgedprojectsoverview.data.PPOCard
import com.kickstarter.features.pledgedprojectsoverview.data.PledgedProjectsOverviewQueryData
import com.kickstarter.libs.AnalyticEvents
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.models.Project
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope
import com.kickstarter.services.mutations.CreateOrUpdateBackingAddressData
import com.kickstarter.ui.compose.designsystem.KSSnackbarTypes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

private val PAGE_LIMIT = 25
class PledgedProjectsPagingSource(
    private val apolloClient: ApolloClientTypeV2,
    private val analyticEvents: AnalyticEvents,
    private var totalAlerts: MutableStateFlow<Int>,
    private val limit: Int = PAGE_LIMIT,

) : PagingSource<String, PPOCard>() {
    override fun getRefreshKey(state: PagingState<String, PPOCard>): String {
        return "" // - Default first page is empty string when paginating with graphQL
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, PPOCard> {
        return try {
            var ppoCardsList = emptyList<PPOCard>()
            var nextPageEnvelope: PageInfoEnvelope? = null
            var inputData = PledgedProjectsOverviewQueryData(limit, params.key ?: "")
            var result: LoadResult<String, PPOCard> = LoadResult.Error(Throwable())

            apolloClient.getPledgedProjectsOverviewPledges(
                inputData = inputData
            )
                .asFlow()
                .catch {
                    result = LoadResult.Error(it)
                }
                .collect { envelope ->
                    totalAlerts.emit(envelope.totalCount ?: 0)
                    analyticEvents.trackPledgedProjectsOverviewPageViewed(envelope.pledges() ?: emptyList(), envelope.totalCount() ?: 0)
                    ppoCardsList = envelope.pledges() ?: emptyList()
                    nextPageEnvelope = if (envelope.pageInfoEnvelope?.hasNextPage == true) envelope.pageInfoEnvelope else null
                    result = LoadResult.Page(
                        data = ppoCardsList,
                        prevKey = null,
                        nextKey = nextPageEnvelope?.endCursor
                    )
                }
            return result
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}

data class PledgedProjectsOverviewUIState(
    val isLoading: Boolean = false,
    val isErrored: Boolean = false,
)
class PledgedProjectsOverviewViewModel(
    environment: Environment,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val mutablePpoCards = MutableStateFlow<PagingData<PPOCard>>(PagingData.empty())
    private var mutableProjectFlow = MutableSharedFlow<Project>()
    private var snackbarMessage: (stringID: Int, type: String, duration: SnackbarDuration) -> Unit = { _, _, _ -> }
    private val apolloClient = requireNotNull(environment.apolloClientV2())
    private val analyticEvents = requireNotNull(environment.analytics())

    private val mutableTotalAlerts = MutableStateFlow<Int>(0)
    val totalAlertsState = mutableTotalAlerts.asStateFlow()

    private val mutablePPOUIState = MutableStateFlow(PledgedProjectsOverviewUIState())
    val ppoCardsState: StateFlow<PagingData<PPOCard>> = mutablePpoCards.asStateFlow()

    private var mutablePaymentRequiresAction = MutableSharedFlow<String>()
    val paymentRequiresAction: SharedFlow<String>
        get() = mutablePaymentRequiresAction.asSharedFlow()

    private var pagingSource = PledgedProjectsPagingSource(apolloClient = apolloClient, analyticEvents = analyticEvents, totalAlerts = mutableTotalAlerts, limit = PAGE_LIMIT)

    val ppoUIState: StateFlow<PledgedProjectsOverviewUIState>
        get() = mutablePPOUIState
            .asStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = PledgedProjectsOverviewUIState()
            )

    val projectFlow: SharedFlow<Project>
        get() = mutableProjectFlow
            .asSharedFlow()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
            )

    init {
        getPledgedProjects()
    }

    fun getPledgedProjects() {
        viewModelScope.launch(ioDispatcher) {
            try {
                Pager(
                    PagingConfig(
                        pageSize = PAGE_LIMIT,
                        prefetchDistance = 3,
                        enablePlaceholders = true,
                    )
                ) {
                    pagingSource
                }
                    .flow
                    .onStart {
                        emitCurrentState(isLoading = true)
                    }.catch {
                        emitCurrentState(isErrored = true)
                    }.collectLatest { pagingData ->
                        mutablePpoCards.value = pagingData
                        emitCurrentState()
                    }
            } catch (e: Exception) {
                emitCurrentState(isErrored = true)
            }
        }
    }

    fun confirmAddress(addressID: String, backingID: String) {
        val input = CreateOrUpdateBackingAddressData(backingID = backingID, addressID = addressID)
        viewModelScope
            .launch(ioDispatcher) {
                apolloClient
                    .createOrUpdateBackingAddress(input)
                    .asFlow()
                    .onStart {
                        emitCurrentState(isLoading = true)
                    }.map {
                        if (it.isTrue()) {
                            showHeadsUpSnackbar(R.string.Address_confirmed_need_to_change_your_address_before_it_locks)
                            getPledgedProjects()
                        } else {
                            showErrorSnackbar(R.string.Something_went_wrong_please_try_again)
                        }
                    }.catch {
                        showErrorSnackbar(R.string.Something_went_wrong_please_try_again)
                    }.onCompletion {
                        emitCurrentState()
                    }.collect()
            }
    }

    fun onMessageCreatorClicked(projectName: String, projectId: String, creatorID: String, ppoCards: List<PPOCard?>, totalAlerts: Int) {
        viewModelScope.launch(ioDispatcher) {
            analyticEvents.trackPPOMessageCreatorCTAClicked(projectID = projectId, ppoCards = ppoCards, totalCount = totalAlerts, creatorID = creatorID)
            apolloClient.getProject(
                slug = projectName,
            )
                .asFlow()
                .onStart {
                    emitCurrentState(isLoading = true)
                }.map { project ->
                    mutableProjectFlow.emit(project)
                }.catch {
                    showErrorSnackbar(R.string.Something_went_wrong_please_try_again)
                }.onCompletion {
                    emitCurrentState()
                }.collect()
        }
    }

    fun provideSnackbarMessage(snackBarMessage: (Int, String, SnackbarDuration) -> Unit) {
        this.snackbarMessage = snackBarMessage
    }

    suspend fun showLoadingState(isLoading: Boolean) {
        emitCurrentState(isLoading = isLoading)
    }

    private suspend fun emitCurrentState(isLoading: Boolean = false, isErrored: Boolean = false) {
        mutablePPOUIState.emit(
            PledgedProjectsOverviewUIState(
                isLoading = isLoading,
                isErrored = isErrored,
            )
        )
    }

    fun showHeadsUpSnackbar(messageId: Int, duration: SnackbarDuration = SnackbarDuration.Short) {
        snackbarMessage.invoke(messageId, KSSnackbarTypes.KS_HEADS_UP.name, duration)
    }

    fun showErrorSnackbar(messageId: Int, duration: SnackbarDuration = SnackbarDuration.Short) {
        snackbarMessage.invoke(messageId, KSSnackbarTypes.KS_ERROR.name, duration)
    }

    class Factory(
        private val environment: Environment,
        private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PledgedProjectsOverviewViewModel(environment, ioDispatcher) as T
        }
    }
}
