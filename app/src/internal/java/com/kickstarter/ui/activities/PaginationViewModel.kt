package com.kickstarter.ui.activities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import com.kickstarter.libs.Environment
import com.kickstarter.models.Project
import com.kickstarter.models.Update
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

class UpdatesPagingSource(
    private val apolloClient: ApolloClientTypeV2,
    private val project: Project,
    private val limit: Int = 25
) : PagingSource<PageInfoEnvelope, Update>() {
    override fun getRefreshKey(state: PagingState<PageInfoEnvelope, Update>): PageInfoEnvelope? {
        return null
    }

    override suspend fun load(params: LoadParams<PageInfoEnvelope>): LoadResult<PageInfoEnvelope, Update> {
        return try {
//            var projectsList = emptyList<Project>()
//
//            val discoveryParams = params.key?.first
//
//            val currentPageUrl = params.key?.second
//            var nextPageUrl: String? = null
//
//            if (currentPageUrl.isNotNull()) {
//                // - Following pages will call the endpoint, with a paging URL.
//                // In an ideal implementation calling the network layer should be suspend functions, not converted to a flow Stream
//                apiClient.fetchProjects(requireNotNull(currentPageUrl)).asFlow().collect {
//                    projectsList = it.projects()
//                    nextPageUrl = it.urls()?.api()?.moreProjects()
//                }
//            } else {
//                // - First page requires discovery query params either the search one or the default ones
//                // In an ideal implementation calling the network layer should be suspend functions, not converted to a flow Stream
//                apiClient.fetchProjects(queryParams).asFlow().collect {
//                    projectsList = it.projects()
//                    nextPageUrl = it.urls()?.api()?.moreProjects()
//                }
//            }

            val currentPageEnvelope = params.key ?: PageInfoEnvelope.builder().build()
            var updatesList = emptyList<Update>()
            var nextPageEnvelope: PageInfoEnvelope? = null

            apolloClient.getProjectUpdates(
                slug = project.slug() ?: "",
                cursor = currentPageEnvelope.startCursor ?: "",
                limit = limit
            )
                .asFlow()
                .collect { envelope ->
                    updatesList = envelope.updates ?: emptyList()
                    nextPageEnvelope = if (envelope.pageInfoEnvelope?.hasNextPage == true) envelope.pageInfoEnvelope else null
                }

            return LoadResult.Page(
                data = updatesList,
                prevKey = null, // only forward pagination
                nextKey = nextPageEnvelope
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
class PaginationViewModel(
    private val environment: Environment,
) : ViewModel() {

    private val apolloClient = requireNotNull(environment.apolloClientV2())
    private val _uiState = MutableStateFlow<PagingData<Update>>(PagingData.empty())
    val projectUpdatesState: StateFlow<PagingData<Update>> = _uiState.asStateFlow()
    init {
        viewModelScope.launch {
            val project = Project.builder().slug("frosthaven").build()
            val limit = 25
            try {
                Pager(
                    PagingConfig(
                        pageSize = limit,
                        prefetchDistance = 3,
                        enablePlaceholders = true
                    )
                ) {
                    UpdatesPagingSource(apolloClient, project, limit)
                }
                    .flow
                    .cachedIn(viewModelScope)
                    .collectLatest { pagingData ->
                        _uiState.emit(pagingData)
                    }
            } catch (e: Exception) {
                // emit error
            }
        }
    }
}
class Factory(private val environment: Environment) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PaginationViewModel(environment) as T
    }
}
