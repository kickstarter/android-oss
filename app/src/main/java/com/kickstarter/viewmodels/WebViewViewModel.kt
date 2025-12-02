package com.kickstarter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment

class WebViewViewModel(val environment: Environment) : ViewModel() {
    val currentUser = requireNotNull(environment.currentUserV2()).observable()

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WebViewViewModel(environment) as T
        }
    }
}
