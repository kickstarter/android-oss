package com.kickstarter.features.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.rx2.asFlow
import kotlin.coroutines.EmptyCoroutineContext

data class HomeScreenUIState(
    val isLoggedInUser: Boolean = false,
    val userAvatarUrl: String = ""
)

@OptIn(FlowPreview::class)
class HomeScreenViewModel(
    private val environment: Environment,
    private val testDispatcher: CoroutineDispatcher? = null
) : ViewModel() {

    private val scope = viewModelScope + (testDispatcher ?: EmptyCoroutineContext)
    private val currentUserFlow = requireNotNull(environment.currentUserV2()?.observable())

    private val _homeUIState = MutableStateFlow(HomeScreenUIState())
    val homeUIState: StateFlow<HomeScreenUIState>
        get() = _homeUIState
            .asStateFlow()
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = HomeScreenUIState(
                    isLoggedInUser = false,
                    userAvatarUrl = ""
                )
            )

    init {
        scope.launch {
            currentUserFlow
                .asFlow()
                .collectLatest { userOpt ->
                    userOpt.getValue()?.let {
                        _homeUIState.emit(
                            HomeScreenUIState(
                                isLoggedInUser = true,
                                userAvatarUrl = it.avatar().medium()
                            )
                        )
                    }
                }
        }
    }

    class Factory(
        private val environment: Environment,
        private val testDispatcher: CoroutineDispatcher? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeScreenViewModel(environment, testDispatcher) as T
        }
    }
}
