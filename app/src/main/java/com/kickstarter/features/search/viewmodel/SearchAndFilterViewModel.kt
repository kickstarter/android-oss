package com.kickstarter.features.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.text.isNotBlank

data class SearchUIState(
    val isLoading: Boolean = false,
    val isErrored: Boolean = false,
    val popularProjectsList: List<Project> = emptyList(),
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

    // - Popular projects sorting selection
    private val popularDiscoveryParam = DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build()

    private val _params = MutableStateFlow(popularDiscoveryParam)
    val params: StateFlow<DiscoveryParams> = _params

    private val debouncePeriod = 300L
    private val _searchTerm = MutableStateFlow("")
    private val searchTerm: StateFlow<String> = _searchTerm

    init {
        scope.launch {
            searchTerm
                .debounce(debouncePeriod)
                .onEach { debouncedTerm ->
                    // - Reset to initial state in case of empty search term
                    if (debouncedTerm.isEmpty() || debouncedTerm.isBlank()) {
                        _params.emit(popularDiscoveryParam)
                    } else
                        _params.emit(DiscoveryParams.builder().term(debouncedTerm).build())
                }.collectLatest {
                    updateSearchResultsState()
                }
        }
    }

    /**
     * Update UIState with after executing Search query with latest params
     */
    private suspend fun updateSearchResultsState() {
        _searchUIState.emit(
            SearchUIState(
                isLoading = true,
            )
        )

        val searchEnvelopeResult = apolloClient.getSearchProjects(params.value)

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
                        popularProjectsList = if (params.value.term().isNull()) it else emptyList(),
                        searchList = if (params.value.term()?.isNotBlank().isTrue()) it else emptyList()
                    )
                )
            }
        }
    }

    fun updateSearchTerm(searchTerm: String) {
        scope.launch {
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
