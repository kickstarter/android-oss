package com.kickstarter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.models.CompleteOrderInput
import com.kickstarter.models.CompleteOrderPayload
import com.kickstarter.ui.activities.PlaygroundActivity
import com.kickstarter.viewmodels.projectpage.LatePledgeCheckoutViewModel
import com.stripe.android.paymentsheet.CreateIntentResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

class PlaygroundViewModel(environment: Environment) : ViewModel() {

    private val apolloClient = requireNotNull(environment.apolloClientV2())

    private var _payloadFlow = MutableStateFlow(CompleteOrderPayload())
    val payloadUIState: SharedFlow<CompleteOrderPayload> = _payloadFlow

    fun completeOrder(stripeId: String) {
        viewModelScope.launch {
            val input = CompleteOrderInput(
                projectId = "UHJvamVjdC01NzYyNDQ0OTk=",
                stripePaymentMethodId = stripeId
            )

            apolloClient.completeOrder(input).asFlow()
                .collect {
                    _payloadFlow.emit(it)
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


