package com.kickstarter.features.pledgedprojectsoverview.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment

class PledgedProjectsOverviewViewModel(environment: Environment) : ViewModel() {

    class Factory(private val environment: Environment) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PledgedProjectsOverviewViewModel(environment) as T
        }
    }
}
