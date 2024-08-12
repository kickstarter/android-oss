package com.kickstarter.viewmodels.projectpage

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment

class CrowdfundCheckoutViewModel(val environment: Environment, bundle: Bundle? = null) : ViewModel() {

    class Factory(private val environment: Environment, private val bundle: Bundle? = null) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CrowdfundCheckoutViewModel(environment, bundle) as T
        }
    }
}