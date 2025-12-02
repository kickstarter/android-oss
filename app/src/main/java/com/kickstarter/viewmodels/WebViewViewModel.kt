package com.kickstarter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.rx2.asFlow

sealed interface WebViewEvent {
    data object ShowLogin : WebViewEvent
    data object LoadWebPage : WebViewEvent
}

class WebViewViewModel(val environment: Environment) : ViewModel() {

    private val currentUser = requireNotNull(environment.currentUserV2()).observable()

    val events: StateFlow<WebViewEvent> =
        currentUser
            .asFlow()
            .map { user ->
                if (user.isPresent()) {
                    WebViewEvent.LoadWebPage
                } else {
                    WebViewEvent.ShowLogin
                }
            }
            .distinctUntilChanged()
            .stateIn( // Convert into hot stateflow
                viewModelScope,
                SharingStarted.WhileSubscribed(),
                initialValue = if (currentUser.blockingFirst().isPresent()) // blockingFirst: wait for the first value to be emitted and returned
                    WebViewEvent.LoadWebPage
                else
                    WebViewEvent.ShowLogin
            )

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WebViewViewModel(environment) as T
        }
    }
}
