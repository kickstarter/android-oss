package com.kickstarter.features.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.models.Category
import com.kickstarter.models.Location
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import timber.log.Timber
import kotlin.coroutines.EmptyCoroutineContext

data class FilterMenuUIState(
    val isLoading: Boolean = false,
    val categoriesList: List<Category> = emptyList()
)

data class LocationsUIState(
    val isLoading: Boolean = false,
    val nearLocations: List<Location> = emptyList(),
    val searchedLocations: List<Location> = emptyList()
)

open class FilterMenuViewModel(
    private val environment: Environment,
    private val testDispatcher: CoroutineDispatcher? = null
) : ViewModel() {

    private val scope = viewModelScope + (testDispatcher ?: EmptyCoroutineContext)
    private val apolloClient = requireNotNull(environment.apolloClientV2())
    private var errorAction: (message: String?) -> Unit = {}

    private val _filterMenu = MutableStateFlow(FilterMenuUIState())
    val filterMenuUIState: StateFlow<FilterMenuUIState>
        get() = _filterMenu
            .asStateFlow()
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = FilterMenuUIState()
            )

    private val _locations = MutableStateFlow(LocationsUIState())
    val locationsUIState = _locations
        .asStateFlow()
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = LocationsUIState()
        )

    private var categoriesList = emptyList<Category>()

    private val _searchQuery = MutableStateFlow("")
    private var nearbyLocations = emptyList<Location>()
    private var suggestedLocations = emptyList<Location>()

    init {
        scope.launch {
            getLocations(default = true)

            scope.launch {
                _searchQuery
                    .debounce(300L)
                    .distinctUntilChanged()
                    .collectLatest { query ->
                        if (query.isNotBlank()) {
                            getLocations(default = false, term = query)
                        }
                    }
                    .runCatching {
                        errorAction.invoke(null)
                    }
            }
        }
    }

    fun updateQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearQuery() {
        _searchQuery.value = ""
        suggestedLocations = emptyList()
    }

    fun getRootCategories() {
        scope.launch {
            emitCurrentState(isLoading = true)
            val response = apolloClient.getCategories()

            if (response.isSuccess)
                categoriesList = response.getOrDefault(emptyList())
            else
                errorAction.invoke(response.exceptionOrNull()?.message)

            Timber.d("${this.javaClass} rootCategories: ${categoriesList.map { "${it.name()} id: ${it.id()}"}}")
            emitCurrentState(isLoading = false)
        }
    }

    suspend fun getLocations(default: Boolean, term: String? = null) {
        emitCurrentState(isLoading = true)

        val response = apolloClient.getLocations(useDefault = default, term = term)

        if (response.isSuccess) {
            if (default) nearbyLocations = response.getOrDefault(emptyList())
            if (!term.isNullOrEmpty()) suggestedLocations = response.getOrDefault(emptyList())
        } else
            errorAction.invoke(response.exceptionOrNull()?.message)

        emitLocationsCurrentState(isLoading = false, nearBy = nearbyLocations, searched = suggestedLocations)
    }

    fun provideErrorAction(errorAction: (message: String?) -> Unit) {
        this.errorAction = errorAction
    }

    private suspend fun emitCurrentState(isLoading: Boolean = false) {
        _filterMenu.emit(
            FilterMenuUIState(
                isLoading = isLoading,
                categoriesList = categoriesList
            )
        )
    }

    private suspend fun emitLocationsCurrentState(isLoading: Boolean = false, nearBy: List<Location>, searched: List<Location>) {
        _locations.emit(
            LocationsUIState(
                isLoading = isLoading,
                nearLocations = nearBy,
                searchedLocations = searched
            )
        )
    }

    class Factory(
        private val environment: Environment,
        private val testDispatcher: CoroutineDispatcher? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FilterMenuViewModel(environment, testDispatcher) as T
        }
    }
}
