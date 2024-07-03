package com.kickstarter.viewmodels.usecases

import com.kickstarter.libs.Config
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.models.User
import com.kickstarter.services.ApolloClientTypeV2
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

data class ShippingRulesState(
    val shippingRules: List<ShippingRule> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)
class GetShippingRulesUseCase(
    private val apolloClient: ApolloClientTypeV2,
    private val rewardsList: List<Reward>,
    private val user: User,
    private val config: Config,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    // - Do not expose mutable states
    private val _mutableShippingRules =
        MutableStateFlow(ShippingRulesState())

    /**
     * Exposes result of this use case
     */
    val shippingRulesState: Flow<ShippingRulesState> = _mutableShippingRules

    operator fun invoke(viewModelScope: CoroutineScope) =
        viewModelScope.launch(defaultDispatcher) {
            // IO dispatcher for network operations to avoid blocking main thread
            apolloClient.getShippingRules(rewardsList.last())
                .asFlow()
                .onStart {
                    _mutableShippingRules.emit(ShippingRulesState(loading = true))
                }
                .catch { throwable ->
                    _mutableShippingRules.emit(
                        ShippingRulesState(
                            loading = false,
                            error = throwable.message
                        )
                    )
                }
                .collect { envelope ->
                    _mutableShippingRules.emit(
                        ShippingRulesState(
                            shippingRules = envelope.shippingRules(),
                            loading = false
                        )
                    )
                }
        }
}
// // On VM usage
// val useCase = SavedPaymentMethodsUseCase(apolloClient, user)
// useCase.invoke() // triger call
// SavedPaymentMethodsFlow.collect() // consult or operate with flow state from useCase
