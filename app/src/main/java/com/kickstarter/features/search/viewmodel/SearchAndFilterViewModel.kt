package com.kickstarter.features.search.viewmodel

import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
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
    private val analyticEvents = requireNotNull(environment.analytics())

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

    private var errorAction: (message: String?) -> Unit = {}

    private var projectsList = emptyList<Project>()
    private var popularProjectsList = emptyList<Project>()

    init {
        scope.launch {
            searchTerm
                .debounce(debouncePeriod)
                .onEach { debouncedTerm ->
                    // - Reset to initial state in case of empty search term
                    if (debouncedTerm.isEmpty() || debouncedTerm.isBlank()) {
                        _params.emit(popularDiscoveryParam)
                    } else
                        _params.emit(
                            DiscoveryParams.builder()
                                .term(debouncedTerm)
                                .sort(DiscoveryParams.Sort.POPULAR) // TODO: update once sort option is ready MBL-2131, by default popular
                                .build()
                        )
                }.collectLatest {
                    updateSearchResultsState()
                }
        }
    }

    fun provideErrorAction(errorAction: (message: String?) -> Unit) {
        this.errorAction = errorAction
    }

    /**
     * Update UIState with after executing Search query with latest params
     */
    private suspend fun updateSearchResultsState() {
        analyticEvents.trackSearchCTAButtonClicked(params.value)

        emitCurrentState(isLoading = true)

        // - Result from API
        val searchEnvelopeResult = apolloClient.getSearchProjects(params.value)

        if (searchEnvelopeResult.isFailure) {
            errorAction.invoke(searchEnvelopeResult.exceptionOrNull()?.message)
        }

        if (searchEnvelopeResult.isSuccess) {
            searchEnvelopeResult.getOrNull()?.projectList?.let {
                if (params.value.term().isNull()) popularProjectsList = it
                if (params.value.term()?.isNotBlank().isTrue()) projectsList = it

                emitCurrentState(isLoading = false)

                analyticEvents.trackSearchResultPageViewed(
                    params.value,
                    1, // TODO: this will contain the page when pagination ready MBL-2139
                    params.value.sort() ?: DiscoveryParams.Sort.POPULAR
                )
            }
        }
    }

    private suspend fun emitCurrentState(isLoading: Boolean = false) {
        _searchUIState.emit(
            SearchUIState(
                isLoading = isLoading,
                popularProjectsList = popularProjectsList,
                searchList = projectsList
            )
        )
    }

    /**
     * Returns a project and its appropriate ref tag given its location in a list of popular projects or search results.
     *
     * @param searchTerm        The search term entered to determine list of search results.
     * @param projects          The list of popular or search result projects.
     * @param selectedProject   The project selected by the user.
     * @return The project and its appropriate ref tag.
     */
    private fun projectAndRefTag(
        searchTerm: String,
        projects: List<Project>,
        selectedProject: Project
    ): Pair<Project, RefTag> {
        val isFirstResult = if (projects.isEmpty()) false else selectedProject === projects[0]
        return if (searchTerm.isEmpty()) {
            if (isFirstResult) Pair.create(
                selectedProject,
                RefTag.searchPopularFeatured()
            ) else Pair.create(selectedProject, RefTag.searchPopular())
        } else {
            if (isFirstResult) Pair.create(
                selectedProject,
                RefTag.searchFeatured()
            ) else Pair.create(selectedProject, RefTag.search())
        }
    }

    fun updateSearchTerm(searchTerm: String) {
        scope.launch {
            _searchTerm.emit(searchTerm)
        }
    }

    fun getProjectAndRefTag(project: Project): Pair<Project, RefTag> {
        val allProjectsList = popularProjectsList.union(projectsList).toList()
        return projectAndRefTag(searchTerm.value, allProjectsList, project)
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
