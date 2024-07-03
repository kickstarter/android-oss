package com.kickstarter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.models.CompleteOrderInput
import com.kickstarter.models.CompleteOrderPayload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

class PlaygroundViewModel(environment: Environment) : ViewModel() {

    private val apolloClient = requireNotNull(environment.apolloClientV2())

    private var _payloadFlow = MutableStateFlow(CompleteOrderPayload())
    val payloadUIState: StateFlow<CompleteOrderPayload> = _payloadFlow.asStateFlow()

    private val _stripePaymentMethodId = MutableStateFlow<String>("")
    val stripePaymentMethodId: StateFlow<String> = _stripePaymentMethodId.asStateFlow()

    fun completeOrder(stripeId: String) {
        viewModelScope.launch {
            val input = CompleteOrderInput(
                projectId = "UHJvamVjdC01NzYyNDQ0OTk=",
                stripePaymentMethodId = stripeId,
            )

            apolloClient.completeOrder(input).asFlow()
                .collect {
                    val response = when (it.status) {
                        "requires_action" -> {
                            _payloadFlow.emit(
                                CompleteOrderPayload(
                                    status = it.status,
                                    clientSecret = it.clientSecret,
                                    trigger3ds = true,
                                    stripePaymentMethodId = stripeId
                                )
                            )
                        }

                        "succeeded" -> {
                            _payloadFlow.emit(
                                CompleteOrderPayload(
                                    status = it.status,
                                    clientSecret = it.clientSecret,
                                    trigger3ds = false,
                                    stripePaymentMethodId = stripeId
                                )
                            )
                        }

                        else -> {
                            _payloadFlow.emit(
                                CompleteOrderPayload(
                                    status = "error",
                                    clientSecret = it.clientSecret,
                                    trigger3ds = false,
                                    stripePaymentMethodId = stripeId
                                )
                            )
                        }
                    }
                }
        }
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlaygroundViewModel(environment) as T
        }
    }
}
