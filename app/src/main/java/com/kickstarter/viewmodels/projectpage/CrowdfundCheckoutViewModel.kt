package com.kickstarter.viewmodels.projectpage

import android.os.Bundle
import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.RefTagUtils
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.extensions.checkoutTotalAmount
import com.kickstarter.libs.utils.extensions.pledgeAmountTotal
import com.kickstarter.libs.utils.extensions.rewardsAndAddOnsList
import com.kickstarter.libs.utils.extensions.shippingCostIfShipping
import com.kickstarter.models.Backing
import com.kickstarter.models.Checkout
import com.kickstarter.models.Location
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.models.StoredCard
import com.kickstarter.models.User
import com.kickstarter.models.extensions.getBackingData
import com.kickstarter.services.mutations.getUpdateBackingData
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.viewmodels.getUpdateBackingData
import com.kickstarter.viewmodels.usecases.SendThirdPartyEventUseCaseV2
import io.reactivex.Observable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onCompletion
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
    val selectedPaymentMethod: StoredCard = StoredCard.builder().build(),
    val bonusAmount: Double = 0.0,
    val shippingRule: ShippingRule? = null
)

class CrowdfundCheckoutViewModel(val environment: Environment, bundle: Bundle? = null) : ViewModel() {
    val analytics = requireNotNull(environment.analytics())
    val apolloClient = requireNotNull(environment.apolloClientV2())
    val currentUser = requireNotNull(environment.currentUserV2()?.loggedInUser()?.asFlow())
    val cookieManager = requireNotNull(environment.cookieManager())
    val sharedPreferences = requireNotNull(environment.sharedPreferences())
    val ffClient = requireNotNull(environment.featureFlagClient())

    private var pledgeData: PledgeData? = null
    private var checkoutData: CheckoutData? = null // TOD potentially needs to change with user card input
    private var pledgeReason: PledgeReason? = null
    private var storedCards = emptyList<StoredCard>()
    private var project = Project.builder().build()
    private var backing: Backing? = null
    private var user: User? = null
    private var selectedRewards = emptyList<Reward>()
    private var isPledgeButtonEnabled = false
    private var selectedPaymentMethod: StoredCard = StoredCard.builder().build()
    private var shippingRule: ShippingRule? = null
    private var refTag: RefTag? = null
    private var shippingAmount = 0.0
    private var totalAmount = 0.0
    private var bonusAmount = 0.0
    private var thirdPartyEventSent = Pair(false, "")

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
            pledgeData = pData
            project = pData.projectData().project()
            backing = project.backing()
            refTag = RefTagUtils.storedCookieRefTagForProject(
                project,
                cookieManager,
                sharedPreferences
            )

            if (backing == null) {
                selectedRewards = pData.rewardsAndAddOnsList()
                pledgeData = pData
                refTag = RefTagUtils.storedCookieRefTagForProject(
                    project,
                    cookieManager,
                    sharedPreferences
                )

                shippingRule = pData.shippingRule()
                shippingAmount = pData.shippingCostIfShipping()
                bonusAmount = pData.bonusAmount()
                totalAmount = pData.checkoutTotalAmount()

                checkoutData = CheckoutData.builder()
                    .amount(pData.pledgeAmountTotal())
                    .paymentType(CreditCardPaymentType.CREDIT_CARD)
                    .bonusAmount(bonusAmount)
                    .shippingAmount(pData.shippingCostIfShipping())
                    .build()
            } else {
                // TODO: explore make re-usable into a separate Utils/extension extracting function all information from backing code
                val list = mutableListOf<Reward>()
                backing?.reward()?.let {
                    list.add(it)
                }
                backing?.addOns()?.let {
                    list.addAll(it)
                }
                backing?.location()?.let {
                    shippingRule = ShippingRule.builder()
                        .location(it)
                        .build()
                }

                if (backing?.location() == null && backing?.locationName() != null && backing?.locationId() != null) {
                    val location = Location.builder()
                        .name(backing?.locationName())
                        .displayableName(backing?.locationName())
                        .id(backing?.locationId())
                        .build()
                    shippingRule = ShippingRule.builder()
                        .location(location)
                        .build()
                }

                selectedRewards = list.toList()

                shippingAmount = (backing?.shippingAmount() ?: 0.0).toDouble()

                bonusAmount = (backing?.bonusAmount() ?: 0.0).toDouble()
                val pAmount = (backing?.amount() ?: 0.0).toDouble()
                totalAmount = pAmount + bonusAmount + shippingAmount

                checkoutData = CheckoutData.builder()
                    .amount(totalAmount)
                    .paymentType(CreditCardPaymentType.CREDIT_CARD)
                    .bonusAmount(bonusAmount)
                    .shippingAmount(shippingAmount)
                    .build()
            }

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
            if (pledgeData?.pledgeFlowContext() == PledgeFlowContext.NEW_PLEDGE)
                analytics.trackCheckoutScreenViewed(requireNotNull(checkoutData), requireNotNull(pledgeData))
            else analytics.trackUpdatePledgePageViewed(requireNotNull(checkoutData), requireNotNull(pledgeData))
        }
    }

    private suspend fun emitCurrentState(isLoading: Boolean = false) {
        _crowdfundCheckoutUIState.emit(
            CheckoutUIState(
                storeCards = storedCards.toList(),
                userEmail = user?.email() ?: "",
                isLoading = isLoading,
                selectedRewards = selectedRewards,
                shippingAmount = shippingAmount,
                checkoutTotal = totalAmount,
                isPledgeButtonEnabled = isLoading,
                selectedPaymentMethod = selectedPaymentMethod,
                bonusAmount = bonusAmount,
                shippingRule = shippingRule
            )
        )
    }

    fun userChangedPaymentMethodSelected(paymentMethodSelected: StoredCard?) {
        paymentMethodSelected?.let {
            selectedPaymentMethod = it
        }

        // - Send event on background thread
        scope.launch(dispatcher) {
            SendThirdPartyEventUseCaseV2(sharedPreferences, ffClient)
                .sendThirdPartyEvent(
                    project = Observable.just(project),
                    currentUser = requireNotNull(environment.currentUserV2()),
                    apolloClient = apolloClient,
                    draftPledge = Pair(pledgeData?.pledgeAmountTotal(), shippingAmount),
                    checkoutAndPledgeData = Observable.just(Pair(checkoutData, pledgeData)),
                    eventName = ThirdPartyEventValues.EventName.ADD_PAYMENT_INFO
                ).asFlow().collect {
                    thirdPartyEventSent = it
                }
        }
    }

    fun isThirdPartyEventSent(): Pair<Boolean, String> = this.thirdPartyEventSent

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
        if (checkoutData != null && pledgeData != null) {
            analytics.trackPledgeSubmitCTA(requireNotNull(checkoutData), requireNotNull(pledgeData))
        }

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
                checkoutData = checkoutData?.toBuilder()?.id(it.id())?.build()
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
                    }.onCompletion {
                    }.collectLatest {
                        checkoutData = checkoutData?.toBuilder()?.id(it.id())?.build()
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
