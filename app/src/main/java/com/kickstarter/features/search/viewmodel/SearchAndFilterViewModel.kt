package com.kickstarter.features.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment

class SearchAndFilterViewModel(
    private val environment: Environment,
): ViewModel() {

    class Factory(
        private val environment: Environment,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchAndFilterViewModel(environment) as T
        }
    }
}