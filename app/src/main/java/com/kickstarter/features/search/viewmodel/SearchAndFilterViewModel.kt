package com.kickstarter.features.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SearchUIState(
    val isLoading: Boolean = false,
    val isErrored: Boolean = false,
    val popularProjectsList: List<Project> = emptyList(), // TODO MBL-2135 popular & search lists could be potentially unified
    val searchList: List<Project> = emptyList()
)

class SearchAndFilterViewModel(
    private val environment: Environment,
    private val dispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val apolloClient = requireNotNull(environment.apolloClientV2())

    private val _searchUIState = MutableStateFlow(SearchUIState())
    val searchUIState: StateFlow<SearchUIState>
        get() = _searchUIState
            .asStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = SearchUIState()
            )

    // Popular projects sorting selection
    private val popularDiscoveryParam = DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build()
    // TODO Will be updated with the params used to call search with, private for now
    private val listOfSearchParams = listOf(popularDiscoveryParam)

    /**
     * Search screen will present the list of popular projects
     * as default when presenting SearchAndFilterActivity.
     */
    fun getPopularProjects() {
        viewModelScope.launch(dispatcher) {
            // TODO trigger loading state UI will handle on MBL-2135
            _searchUIState.emit(
                SearchUIState(
                    isLoading = true,
                )
            )

            val searchEnvelopeResult = search(listOfSearchParams.last())

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
                            popularProjectsList = it
                        )
                    )
                }
            }
        }
    }

    private suspend fun search(params: DiscoveryParams) = apolloClient.getSearchProjects(params)

    class Factory(
        private val environment: Environment,
        private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchAndFilterViewModel(environment, dispatcher) as T
        }
    }
}
