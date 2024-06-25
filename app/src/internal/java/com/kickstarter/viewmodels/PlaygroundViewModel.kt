package com.kickstarter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.models.CompleteOrderInput
import com.kickstarter.ui.activities.PlaygroundActivity
import com.kickstarter.viewmodels.projectpage.LatePledgeCheckoutViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

class PlaygroundViewModel(environment: Environment) : ViewModel() {

    private val apolloClient = requireNotNull(environment.apolloClientV2())

    fun completeOrder(stripeId: String) {
        val input = CompleteOrderInput(
            projectId = "UHJvamVjdC0zMTk5NDYzMDY=",
            stripePaymentMethodId = stripeId
        )

        viewModelScope.launch {
            apolloClient.completeOrder(input).asFlow()
                .collect { payload ->
                    payload.status
                    payload.clientSecret
                }
        }
    }
    class Factory(private val environment: Environment) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlaygroundViewModel(environment) as T
        }
    }
}


