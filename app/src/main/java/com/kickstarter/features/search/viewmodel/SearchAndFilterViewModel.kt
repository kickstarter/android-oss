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
    val searchList: List<Project> = emptyList(),
    val hasMore: Boolean = true // flag to load more items to the lists
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
    private val firstLoadParams = DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build()

    private val _params = MutableStateFlow(firstLoadParams)
    val params: StateFlow<DiscoveryParams> = _params

    val debouncePeriod = 300L
    private val _searchTerm = MutableStateFlow("")
    private val searchTerm: StateFlow<String> = _searchTerm
    private var persistedTerm: String? = null

    private var errorAction: (message: String?) -> Unit = {}

    private var projectsList = mutableListOf<Project>()
    private var popularProjectsList = mutableListOf<Project>()

    // Pagination variables
    private var nextPage: String? = null
    private var isLoadingMore = false

    init {
        scope.launch {
            val debounced = _searchTerm
                .debounce(debouncePeriod)

            _params
                .combine(debounced) { currentParams, debouncedTerm ->
                    // - Reset to initial state in case of empty search term
                    if (debouncedTerm.isEmpty() || debouncedTerm.isBlank()) {
                        persistedTerm = null
                        currentParams
                    } else {
                        persistedTerm = debouncedTerm
                        currentParams.toBuilder()
                            .term(debouncedTerm)
                            .build()
                    }
                }
                .collectLatest { params ->
                    resetPagination()
                    updateSearchResultsState(params)
                }
        }
    }

    fun provideErrorAction(errorAction: (message: String?) -> Unit) {
        this.errorAction = errorAction
    }

    fun updateParamsToSearchWith(category: Category? = null, projectSort: DiscoveryParams.Sort) {
        val update = params.value.toBuilder()
            .apply {
                this.category(category)
                this.sort(projectSort)
            }
            .build()

        scope.launch {
            _params.emit(update)
        }
    }

    fun loadMore() {
        if (!isLoadingMore && searchUIState.value.hasMore) {
            isLoadingMore = true
            scope.launch {
                val updatedParams = params.value.toBuilder().term(persistedTerm).build()
                updateSearchResultsState(updatedParams)
                isLoadingMore = false
            }
        }
    }

    private fun resetPagination() {
        isLoadingMore = false
        nextPage = null
        popularProjectsList = mutableListOf()
        projectsList = mutableListOf()
    }

    /**
     * Update UIState with after executing Search query with latest params
     */
    private suspend fun updateSearchResultsState(params: DiscoveryParams) {

        emitCurrentState(isLoading = true)

        // - Result from API
        Timber.d("${this.javaClass} params: $params")
        val searchEnvelopeResult = apolloClient.getSearchProjects(params, nextPage)

        if (searchEnvelopeResult.isFailure) {
            // - errorAction.invoke(searchEnvelopeResult.exceptionOrNull()?.message) to return API level message
            errorAction.invoke(null)
        }

        if (searchEnvelopeResult.isSuccess) {
            searchEnvelopeResult.getOrNull()?.projectList?.let {
                if (params.term().isNull()) popularProjectsList.addAll(it)
                if (params.term()?.isNotBlank().isTrue()) projectsList.addAll(it)

                val totalCount = searchEnvelopeResult.getOrNull()?.totalCount ?: 0

                Timber.d("${this.javaClass} popularProjectsList: ${popularProjectsList.size}")
                Timber.d("${this.javaClass} projectsList: ${projectsList.size}")

                // - Send analytic events only on first page load
                if (nextPage == null) {
                    analyticEvents.trackSearchCTAButtonClicked(params)
                    analyticEvents.trackSearchResultPageViewed(
                        params,
                        totalCount,
                        params.sort() ?: DiscoveryParams.Sort.MAGIC
                    )
                }

                // - pagination related stuff
                nextPage = searchEnvelopeResult.getOrNull()?.pageInfo?.endCursor
                val hasMore = searchEnvelopeResult.getOrNull()?.pageInfo?.hasNextPage ?: false

                // - update UI
                emitCurrentState(isLoading = false, hasMore = hasMore)
            }
        }
    }

    private suspend fun emitCurrentState(isLoading: Boolean = false, hasMore: Boolean = true) {
        _searchUIState.emit(
            SearchUIState(
                isLoading = isLoading,
                popularProjectsList = popularProjectsList.toList(),
                searchList = projectsList.toList(),
                hasMore = hasMore
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
