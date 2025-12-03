package com.kickstarter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.models.User
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.rx2.asFlow
import kotlin.coroutines.EmptyCoroutineContext

enum class WebViewEvent {
    SHOW_LOGIN,
    LOAD_WEBVIEW
}

class WebViewViewModel(
    val environment: Environment,
    testDispatcher: CoroutineDispatcher? = null
) : ViewModel() {

    private val currentUser = requireNotNull(environment.currentUserV2()).observable()
    private val scope = viewModelScope + (testDispatcher ?: EmptyCoroutineContext)

    private val _mutableWebViewUIState = MutableStateFlow(WebViewEvent.SHOW_LOGIN)
    val webViewUIState = _mutableWebViewUIState.asStateFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = WebViewEvent.SHOW_LOGIN
        )

    init {
        scope.launch {
            currentUser.asFlow()
                .collect {
                    emitCurrentState(it.getValue())
                }
        }
    }

    suspend fun emitCurrentState(user: User?) {
        if (user != null) {
            _mutableWebViewUIState.emit(WebViewEvent.LOAD_WEBVIEW)
        } else {
            _mutableWebViewUIState.emit(WebViewEvent.SHOW_LOGIN)
        }
    }

    class Factory(
        private val environment: Environment,
        private val testDispatcher: CoroutineDispatcher? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WebViewViewModel(environment, testDispatcher) as T
        }
    }
}
