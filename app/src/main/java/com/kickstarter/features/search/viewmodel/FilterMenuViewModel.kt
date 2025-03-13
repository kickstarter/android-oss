package com.kickstarter.features.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.models.Category
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

data class FilterMenuUIState(
    val isLoading: Boolean = false,
    val categoriesList: List<Category> = emptyList()
)

class FilterMenuViewModel(
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

    private var categoriesList = emptyList<Category>()

    fun getRootCategories() {
        scope.launch {
            emitCurrentState(isLoading = true)
            val response = apolloClient.getRootCategories()

            if (response.isSuccess)
                categoriesList = response.getOrDefault(emptyList())
            else
                errorAction.invoke(response.exceptionOrNull()?.message)

            Timber.d("${this.javaClass} rootCategories: ${categoriesList.map { "${it.name()} id: ${it.id()}"}}")
            emitCurrentState(isLoading = false)
        }
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

    class Factory(
        private val environment: Environment,
        private val testDispatcher: CoroutineDispatcher? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FilterMenuViewModel(environment, testDispatcher) as T
        }
    }
}
