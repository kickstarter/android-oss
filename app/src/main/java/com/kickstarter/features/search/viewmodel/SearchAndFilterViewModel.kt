package com.kickstarter.features.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.text.isNotBlank
import kotlin.text.onEach

data class SearchUIState(
    val isLoading: Boolean = false,
    val isErrored: Boolean = false,
    val popularProjectsList: List<Project> = emptyList(), // TODO MBL-2135 popular & search lists could be potentially unified
    val searchList: List<Project> = emptyList()
)

@OptIn(FlowPreview::class)
class SearchAndFilterViewModel(
    private val environment: Environment,
    private val testDispatcher: CoroutineDispatcher? = null
) : ViewModel() {

    private val scope = viewModelScope + (testDispatcher ?: EmptyCoroutineContext)
    private val apolloClient = requireNotNull(environment.apolloClientV2())

    private val _searchUIState = MutableStateFlow(SearchUIState())
    val searchUIState: StateFlow<SearchUIState>
        get() = _searchUIState
            .asStateFlow()
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = SearchUIState()
            )

    // Popular projects sorting selection
    private val popularDiscoveryParam = DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build()
    // TODO Will be updated with the params used to call search with, private for now
    private val listOfSearchParams = listOf(popularDiscoveryParam)

    private val debouncePeriod = 300L
    private val _searchTerm = MutableStateFlow("")
    val searchTerm: StateFlow<String> = _searchTerm
    private val debouncedSearch = searchTerm
        .debounce(debouncePeriod)
        .filter { it.isNotBlank() }
        .onEach { debouncedTerm ->
            val params = DiscoveryParams.builder().term(debouncedTerm).build()

            val searchEnvelopeResult = search(params)

            if (searchEnvelopeResult.isFailure) {
                _searchUIState.emit(
                    SearchUIState(
                        isErrored = true,
                    )
                )
            }

            if (searchEnvelopeResult.isSuccess) {
                searchEnvelopeResult.getOrNull()?.projectList?.let {
                    _searchUIState.emit(
                        SearchUIState(
                            isErrored = false,
                            isLoading = false,
                            searchList = it,
                            popularProjectsList = emptyList()
                        )
                    )
                }
            }
        }
        .launchIn(scope)

    /**
     * Search screen will present the list of popular projects
     * as default when presenting SearchAndFilterActivity.
     */
    fun getPopularProjects() {
        scope.launch {
            // TODO trigger loading state UI will handle on MBL-2135
            _searchUIState.emit(
                SearchUIState(
                    isLoading = true,
                )
            )

            val searchEnvelopeResult = search(listOfSearchParams.toList().last())

            if (searchEnvelopeResult.isFailure) {
                // TODO trigger error state UI will handle on MBL-2135
                _searchUIState.emit(
                    SearchUIState(
                        isErrored = true,
                    )
                )
            }

            if (searchEnvelopeResult.isSuccess) {
                searchEnvelopeResult.getOrNull()?.projectList?.let {
                    _searchUIState.emit(
                        SearchUIState(
                            isErrored = false,
                            isLoading = false,
                            popularProjectsList = it,
                            searchList = emptyList()
                        )
                    )
                }
            }
        }
    }

    private suspend fun search(params: DiscoveryParams) = apolloClient.getSearchProjects(params)

    fun searchTerm(searchTerm: String) {
        scope.launch {
            _searchUIState.emit(
                SearchUIState(
                    isLoading = true,
                )
            )
            _searchTerm.emit(searchTerm)
        }
    }

    class Factory(
        private val environment: Environment,
        private val testDispatcher: CoroutineDispatcher? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchAndFilterViewModel(environment, testDispatcher) as T
        }
    }
}
