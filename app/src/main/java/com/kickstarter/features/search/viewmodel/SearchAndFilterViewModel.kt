package com.kickstarter.features.search.viewmodel

import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.models.Category
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import timber.log.Timber
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
    private val firstLoadParams = DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build()

    private val _params = MutableStateFlow(firstLoadParams)
    val params: StateFlow<DiscoveryParams> = _params

    val debouncePeriod = 300L
    private val _searchTerm = MutableStateFlow("")
    private val searchTerm: StateFlow<String> = _searchTerm

    private var errorAction: (message: String?) -> Unit = {}

    private var projectsList = emptyList<Project>()
    private var popularProjectsList = emptyList<Project>()

    init {
        scope.launch {
            val debounced = _searchTerm
                .debounce(debouncePeriod)

            _params
                .combine(debounced) { currentParams, debouncedTerm ->
                    // - Reset to initial state in case of empty search term
                    if (debouncedTerm.isEmpty() || debouncedTerm.isBlank()) {
                        currentParams
                    } else {
                        currentParams.toBuilder()
                            .term(debouncedTerm)
                            .build()
                    }
                }
                .collectLatest { params ->
                    updateSearchResultsState(params)
                }
        }
    }

    fun provideErrorAction(errorAction: (message: String?) -> Unit) {
        this.errorAction = errorAction
    }

    fun updateParamsToSearchWith(category: Category? = null, projectSort: DiscoveryParams.Sort = DiscoveryParams.Sort.POPULAR) {
        val update = params.value.toBuilder()
            .apply {
                this.category(category)
                this.sort(projectSort) // - Default sorting is popular
            }
            .build()

        scope.launch {
            _params.emit(update)
        }
    }

    /**
     * Update UIState with after executing Search query with latest params
     */
    private suspend fun updateSearchResultsState(params: DiscoveryParams) {
        analyticEvents.trackSearchCTAButtonClicked(params)

        emitCurrentState(isLoading = true)

        // - Result from API
        Timber.d("${this.javaClass} params: $params")
        val searchEnvelopeResult = apolloClient.getSearchProjects(params)

        if (searchEnvelopeResult.isFailure) {
            // - errorAction.invoke(searchEnvelopeResult.exceptionOrNull()?.message) to return API level message
            errorAction.invoke(null)
        }

        if (searchEnvelopeResult.isSuccess) {
            searchEnvelopeResult.getOrNull()?.projectList?.let {
                if (params.term().isNull()) popularProjectsList = it
                if (params.term()?.isNotBlank().isTrue()) projectsList = it

                emitCurrentState(isLoading = false)

                analyticEvents.trackSearchResultPageViewed(
                    params,
                    1, // TODO: this will contain the page when pagination ready MBL-2139
                    params.sort() ?: DiscoveryParams.Sort.POPULAR
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
