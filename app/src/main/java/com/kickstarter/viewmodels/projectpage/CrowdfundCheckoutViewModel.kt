package com.kickstarter.viewmodels.projectpage

import android.os.Bundle
import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.utils.RefTagUtils
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.extensions.checkoutTotalAmount
import com.kickstarter.libs.utils.extensions.pledgeAmountTotal
import com.kickstarter.libs.utils.extensions.rewardsAndAddOnsList
import com.kickstarter.libs.utils.extensions.shippingCostIfShipping
import com.kickstarter.models.Checkout
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.StoredCard
import com.kickstarter.models.User
import com.kickstarter.models.extensions.getBackingData
import com.kickstarter.services.mutations.CreateBackingData
import com.kickstarter.services.mutations.UpdateBackingData
import com.kickstarter.services.mutations.getUpdateBackingData
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.viewmodels.getUpdateBackingData
import io.reactivex.Observable
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
import kotlinx.coroutines.flow.onStart
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
    val isPledgeButtonEnabled: Boolean = true,
    val selectedPaymentMethod: StoredCard = StoredCard.builder().build()
)

class CrowdfundCheckoutViewModel(val environment: Environment, bundle: Bundle? = null) : ViewModel() {
    val analytics = requireNotNull(environment.analytics())
    val apolloClient = requireNotNull(environment.apolloClientV2())
    val currentUser = requireNotNull(environment.currentUserV2()?.loggedInUser()?.asFlow())
    val cookieManager = requireNotNull(environment.cookieManager())
    val sharedPreferences = requireNotNull(environment.sharedPreferences())

    private var pledgeData: PledgeData? = null
    private var checkoutData: CheckoutData? = null // TOD potentially needs to change with user card input
    private var pledgeReason: PledgeReason? = null
    private var storedCards = emptyList<StoredCard>()
    private var project = Project.builder().build()
    private var user: User? = null
    private var selectedRewards = emptyList<Reward>()
    private var isPledgeButtonEnabled = false
    private var selectedPaymentMethod: StoredCard = StoredCard.builder().build()
    private var refTag: RefTag? = null

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

    // - CreateBacking/UpdateBacking Result States
    private var _checkoutResultState = MutableStateFlow(Checkout.builder().build())
    val checkoutResultState: StateFlow<Checkout>
        get() = _checkoutResultState
            .asStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = Checkout.builder().build()
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

    /**
     * PledgeData information that is given to the VM via
     * constructor on the bundle object.
     */
    fun getPledgeData() = this.pledgeData

    /**
     * PledgeReason information that is given to the VM via
     * constructor on the bundle object.
     */
    fun getPledgeReason() = this.pledgeReason

    fun provideBundle(arguments: Bundle?) {
        val pData = arguments?.getParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA) as PledgeData?
        pledgeReason = arguments?.getSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON) as PledgeReason?

        if (pData != null) {
            selectedRewards = pData.rewardsAndAddOnsList()
            pledgeData = pData
            project = pData.projectData().project()
            refTag = RefTagUtils.storedCookieRefTagForProject(project, cookieManager, sharedPreferences)

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
                selectedPaymentMethod = if (cards.isEmpty()) cards.first() else StoredCard.builder().build()
                storedCards = cards
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
                selectedPaymentMethod = selectedPaymentMethod
            )
        )
    }

    fun userChangedPaymentMethodSelected(paymentMethodSelected: StoredCard?) {
        paymentMethodSelected?.let {
            selectedPaymentMethod = it
        }
    }

    fun pledge() {
        scope.launch(dispatcher) {
            when (pledgeReason) {
                PledgeReason.PLEDGE -> createBacking()
                PledgeReason.UPDATE_PLEDGE,
                PledgeReason.UPDATE_REWARD,
                PledgeReason.UPDATE_PAYMENT -> updateBacking()
                else -> {
                    errorAction.invoke(null)
                }
            }
        }
    }

    private suspend fun createBacking() {
        val backingData = selectedPaymentMethod.getBackingData(
            proj = project,
            amount = pledgeData?.checkoutTotalAmount().toString(),
            locationId = pledgeData?.shippingRule()?.location()?.id()?.toString(),
            rewards = pledgeData?.rewardsAndAddOnsList() ?: emptyList<Reward>(),
            cookieRefTag = refTag
        )

        this.apolloClient.createBacking(backingData).asFlow()
            .onStart {
                isPledgeButtonEnabled = false
                emitCurrentState(isLoading = true)
            }.catch {
                errorAction.invoke(it.message)
                isPledgeButtonEnabled = true
                emitCurrentState(isLoading = false)
            }
            .collectLatest {
                _checkoutResultState.emit(it)
                emitCurrentState(isLoading = false)
            }
    }

    private suspend fun updateBacking() {
        val locationId = pledgeData?.shippingRule()?.location()?.id()?.toString()
        val amount = pledgeData?.checkoutTotalAmount().toString()
        val extendedList =
            RewardUtils.extendAddOns(pledgeData?.rewardsAndAddOnsList() ?: emptyList())
        if (project.isBacking()) {
            project.backing()?.let { backing ->
                val backingData =
                    if (backing.amount() == (pledgeData?.checkoutTotalAmount() ?: 0)) {
                        getUpdateBackingData(
                            backing,
                            null,
                            locationId,
                            extendedList,
                            selectedPaymentMethod
                        )
                    } else {
                        getUpdateBackingData(
                            backing,
                            amount,
                            locationId,
                            extendedList,
                            selectedPaymentMethod
                        )
                    }

                apolloClient.updateBacking(backingData).asFlow()
                    .onStart {
                        isPledgeButtonEnabled = false
                        emitCurrentState(isLoading = true)
                    }.catch {
                        errorAction.invoke(it.message)
                        isPledgeButtonEnabled = true
                        emitCurrentState(isLoading = false)
                    }
                    .collectLatest {
                        _checkoutResultState.emit(it)
                        emitCurrentState(isLoading = false)
                    }
            }

        }
    }


    class Factory(private val environment: Environment, private val bundle: Bundle? = null) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CrowdfundCheckoutViewModel(environment, bundle) as T
        }
    }
}
