package com.kickstarter.features.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.models.Category
import com.kickstarter.models.Location
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import timber.log.Timber
import kotlin.coroutines.EmptyCoroutineContext

// TODO: Rename to CategoriesUIState maybe??
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
    private val testDispatcher: CoroutineDispatcher? = null,
    private val isInPreview: Boolean = false
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

    init {
        if (isInPreview) {
            scope.launch {
                getNearByLocations()
            }
        }
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

    suspend fun getNearByLocations() {
        emitCurrentState(isLoading = true)

        val locationsHardcodedList = listOf(LocationFactory.vancouver())
        emitLocationsCurrentState(
            isLoading = false,
            nearBy = locationsHardcodedList
        )
    }

    suspend fun getSearchedLocations(term: String) {
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

    // - the lists should probably be stored and not update every time, for now it's ok as it is hardcoded when real query is called change this
    private suspend fun emitLocationsCurrentState(isLoading: Boolean = false, nearBy: List<Location> = emptyList(), searched: List<Location> = emptyList()) {
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
