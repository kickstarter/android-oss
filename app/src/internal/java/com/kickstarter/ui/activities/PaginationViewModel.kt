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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

class UpdatesPagingSource(
    private val apolloClient: ApolloClientTypeV2,
    private val project: Project,
    private val limit: Int = 25,
    private val _totalCount: MutableStateFlow<Int>
) : PagingSource<String, Update>() {
    override fun getRefreshKey(state: PagingState<String, Update>): String {
        return "" // - Default first page is empty string when paginating with graphQL
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Update> {
        return try {
            val currentPageEnvelope = params.key ?: "" // - Default first page is empty string when paginating with graphQL
            var updatesList = emptyList<Update>()
            var nextPageEnvelope: PageInfoEnvelope? = null

            apolloClient.getProjectUpdates(
                slug = project.slug() ?: "",
                cursor = currentPageEnvelope,
                limit = limit
            )
                .asFlow()
                .collect { envelope ->
                    _totalCount.emit(envelope.totalCount ?: 0)
                    updatesList = envelope.updates ?: emptyList()
                    nextPageEnvelope = if (envelope.pageInfoEnvelope?.hasNextPage == true) envelope.pageInfoEnvelope else null
                }

            return LoadResult.Page(
                data = updatesList, // - must be a list of whatever data type we need
                prevKey = null, // - only forward pagination
                nextKey = nextPageEnvelope?.startCursor // - If needed reversed pagination use endCursor
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
    private val _totalCount = MutableStateFlow<Int>(0)
    val totalItemsState = _totalCount.asStateFlow()
    val projectUpdatesState: StateFlow<PagingData<Update>> = _uiState.asStateFlow()
    init {
        loadUpdates()
    }

    fun loadUpdates() {
        viewModelScope.launch(Dispatchers.IO) {
            val project = Project.builder().slug("frosthaven").build()
            val limit = 25
            try {
                Pager(
                    PagingConfig(
                        pageSize = limit,
                        prefetchDistance = 3,
                        enablePlaceholders = true,
                    )
                ) {
                    UpdatesPagingSource(apolloClient, project, limit, _totalCount)
                }
                    .flow
                    .cachedIn(viewModelScope)
                    .collectLatest { pagingData ->
                        _uiState.value = pagingData
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
