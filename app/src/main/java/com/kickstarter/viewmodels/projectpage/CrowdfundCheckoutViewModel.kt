package com.kickstarter.viewmodels.projectpage

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.checkoutTotalAmount
import com.kickstarter.libs.utils.extensions.pledgeAmountTotal
import com.kickstarter.libs.utils.extensions.rewardsAndAddOnsList
import com.kickstarter.libs.utils.extensions.shippingCostIfShipping
import com.kickstarter.models.Reward
import com.kickstarter.models.StoredCard
import com.kickstarter.models.User
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow
import type.CreditCardPaymentType

data class CheckoutUIState(
    val storeCards: List<StoredCard> = listOf(),
    val userEmail: String = "",
    val isLoading: Boolean = false,
    val selectedRewards: List<Reward> = emptyList(),
    val shippingAmount: Double = 0.0,
    val checkoutTotal: Double = 0.0,
    val isPledgeButtonEnabled: Boolean = true
)

class CrowdfundCheckoutViewModel(val environment: Environment, bundle: Bundle? = null) : ViewModel() {
    val analytics = requireNotNull(environment.analytics())
    val apolloClient = requireNotNull(environment.apolloClientV2())
    val currentUser = requireNotNull(environment.currentUserV2()?.loggedInUser()?.asFlow())

    private var pledgeData: PledgeData? = null
    private var checkoutData: CheckoutData? = null // TOD potentially needs to change with user card input
    private var pledgeReason: PledgeReason? = null
    private val storedCards = mutableListOf<StoredCard>()
    private var user: User? = null
    private var selectedRewards = emptyList<Reward>()
    private var isPledgeButtonEnabled = false

    private var errorAction: (message: String?) -> Unit = {}

    private var scope: CoroutineScope = viewModelScope
    private var dispatcher: CoroutineDispatcher = Dispatchers.IO

    private var _crowdfundCheckoutUIState = MutableStateFlow(CheckoutUIState())
    val crowdfundCheckoutUIState: StateFlow<CheckoutUIState>
        get() = _crowdfundCheckoutUIState
            .asStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = CheckoutUIState(isLoading = false)
            )

    /**
     * By default run in
     * scope: viewModelScope
     * dispatcher: Dispatchers.IO
     */
    fun provideScopeAndDispatcher(scope: CoroutineScope, dispatcher: CoroutineDispatcher) {
        this.scope = scope
        this.dispatcher = dispatcher
    }

    fun getPledgeData() = this.pledgeData

    fun getPledgeReason() = this.pledgeReason

    fun provideBundle(arguments: Bundle?) {
        val pData = arguments?.getParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA) as PledgeData?
        pledgeReason = arguments?.getSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON) as PledgeReason

        if (pData != null) {
            selectedRewards = pData.rewardsAndAddOnsList()
            pledgeData = pData
            checkoutData = CheckoutData.builder()
                .amount(pData.pledgeAmountTotal())
                .paymentType(CreditCardPaymentType.CREDIT_CARD)
                .bonusAmount(pData.bonusAmount())
                .shippingAmount(pData.shippingCostIfShipping())
                .build()

            collectUserInformation()
            sendPageViewedEvent()
        }
    }

    fun provideErrorAction(errorAction: (message: String?) -> Unit) {
        this.errorAction = errorAction
    }

    // TODO: can potentially be extracted to an UserUseCase
    private fun collectUserInformation() {
        scope.launch(dispatcher) {
            emitCurrentState(isLoading = true)
            currentUser.combine(apolloClient.userPrivacy().asFlow()) { cUser, privacy ->
                cUser.toBuilder()
                    .email(privacy.email)
                    .name(privacy.name)
                    .build()
            }.combine(apolloClient.getStoredCards().asFlow()) { updatedUser, cards ->
                user = updatedUser
                storedCards.addAll(cards)
            }.catch {
                errorAction.invoke(it.message)
                emitCurrentState(isLoading = false)
            }.collectLatest {
                emitCurrentState(isLoading = false)
            }
        }
    }

    private fun sendPageViewedEvent() {
        if (checkoutData != null && pledgeData != null) {
            analytics.trackCheckoutScreenViewed(checkoutData!!, pledgeData!!)
        }
    }

    private suspend fun emitCurrentState(isLoading: Boolean = false) {
        _crowdfundCheckoutUIState.emit(
            CheckoutUIState(
                storeCards = storedCards.toList(),
                userEmail = user?.email() ?: "",
                isLoading = isLoading,
                selectedRewards = selectedRewards,
                shippingAmount = this.pledgeData?.shippingCostIfShipping() ?: 0.0,
                checkoutTotal = this.pledgeData?.checkoutTotalAmount() ?: 0.0,
                isPledgeButtonEnabled = isLoading,
            )
        )
    }

    // TODO: call pledge mutation
    fun pledge() {
    }

    class Factory(private val environment: Environment, private val bundle: Bundle? = null) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CrowdfundCheckoutViewModel(environment, bundle) as T
        }
    }
}
