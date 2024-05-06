package com.kickstarter.viewmodels.projectpage

import android.util.Log
import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.libs.models.Country
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.models.CheckoutPayment
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.services.mutations.CreateCheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

data class ConfirmDetailsUIState(
    val rewardsAndAddOns: List<Reward> = listOf(),
    val initialBonusSupportAmount: Double = 0.0,
    val totalBonusSupportAmount: Double = 0.0,
    val shippingAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val minStepAmount: Double = 0.0,
    val maxPledgeAmount: Double = 0.0,
    val isLoading: Boolean = false
)

class ConfirmDetailsViewModel(val environment: Environment) : ViewModel() {

    private val apolloClient = requireNotNull(environment.apolloClientV2())

    private lateinit var projectData: ProjectData
    private lateinit var userSelectedReward: Reward
    private var rewardAndAddOns: List<Reward> = listOf()
    private var pledgeReason: PledgeReason? = null
    private lateinit var selectedShippingRule: ShippingRule
    private var initialBonusSupport = 0.0
    private var addedBonusSupport = 0.0
    private var shippingAmount: Double = 0.0
    private var totalAmount: Double = 0.0
    private var minStepAmount: Double = 0.0
    private var maxPledgeAmount: Double = 0.0
    private var errorAction: (message: String?) -> Unit = {}

    private val mutableConfirmDetailsUIState = MutableStateFlow(ConfirmDetailsUIState())
    val confirmDetailsUIState: StateFlow<ConfirmDetailsUIState>
        get() = mutableConfirmDetailsUIState
            .asStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = ConfirmDetailsUIState()
            )

    private val mutableCheckoutPayment =
        MutableStateFlow(CheckoutPayment(id = 0L, paymentUrl = null))
    val checkoutPayment: StateFlow<CheckoutPayment>
        get() = mutableCheckoutPayment
            .asStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = CheckoutPayment(id = 0L, paymentUrl = null)
            )

    fun provideProjectData(projectData: ProjectData) {
        this.projectData = projectData
        viewModelScope.launch {
            val country = Country.findByCurrencyCode(projectData.project().currency())
            country?.let {
                minStepAmount = it.minPledge.toDouble()
                maxPledgeAmount = it.maxPledge.toDouble()
            }
        }
    }

    fun onUserSelectedReward(reward: Reward) {
        this.userSelectedReward = reward
        if (RewardUtils.isNoReward(reward)) {
            rewardAndAddOns = listOf()
            initialBonusSupport = minStepAmount
        } else {
            rewardAndAddOns = listOf(userSelectedReward)
            initialBonusSupport = 0.0
        }
        if (::projectData.isInitialized) {
            pledgeReason = pledgeDataAndPledgeReason(projectData, reward).second
        }
        addedBonusSupport = 0.0

        updateShippingAmount()

        totalAmount = calculateTotal()

        viewModelScope.launch {
            emitCurrentState()
        }
    }

    fun onUserUpdatedAddOns(addOns: Map<Reward, Int>) {
        val rewardsAndAddOns = mutableListOf<Reward>()
        if (::userSelectedReward.isInitialized && !RewardUtils.isNoReward(userSelectedReward)) {
            rewardsAndAddOns.add(userSelectedReward)
        }

        addOns.forEach { rewardAndQuantity ->
            if (rewardAndQuantity.value > 0) {
                rewardsAndAddOns.add(
                    rewardAndQuantity.key.toBuilder().quantity(rewardAndQuantity.value).build()
                )
            }
        }

        rewardAndAddOns = rewardsAndAddOns

        updateShippingAmount()

        totalAmount = calculateTotal()

        viewModelScope.launch {
            emitCurrentState()
        }
    }

    fun provideErrorAction(errorAction: (message: String?) -> Unit) {
        this.errorAction = errorAction
    }

    private fun calculateTotal(): Double {
        var total = 0.0
        total += getRewardsTotalAmount(rewardAndAddOns)
        total += initialBonusSupport + addedBonusSupport
        if (::userSelectedReward.isInitialized) {
            total +=
                if (RewardUtils.isNoReward(userSelectedReward)) 0.0
                else if (RewardUtils.isShippable(userSelectedReward)) shippingAmount
                else 0.0
        }
        return total
    }

    private fun updateShippingAmount() {
        if (::selectedShippingRule.isInitialized) {
            shippingAmount = getShippingAmount(
                rule = selectedShippingRule,
                reason = pledgeReason,
                bShippingAmount = null,
                rewards = rewardAndAddOns
            )
        } else shippingAmount = 0.0
    }

    /**
     *  Calculate the shipping amount in case of shippable reward and reward + AddOns
     */
    private fun getShippingAmount(
        rule: ShippingRule,
        reason: PledgeReason? = null,
        bShippingAmount: Float? = null,
        rewards: List<Reward>
    ): Double {
        return when (reason) {
            PledgeReason.UPDATE_REWARD,
            PledgeReason.PLEDGE -> if (rewards.any { it.isAddOn() }) shippingCostForAddOns(
                rewards,
                rule
            ) + rule.cost() else rule.cost()

            else -> bShippingAmount?.toDouble() ?: rule.cost()
        }
    }

    private fun shippingCostForAddOns(listRw: List<Reward>, selectedRule: ShippingRule): Double {
        var shippingCost = 0.0
        listRw.filter {
            it.isAddOn()
        }.map { rw ->
            rw.shippingRules()?.filter { rule ->
                rule.location()?.id() == selectedRule.location()?.id()
            }?.map { rule ->
                shippingCost += rule.cost() * (rw.quantity() ?: 1)
            }
        }

        return shippingCost
    }

    private fun pledgeDataAndPledgeReason(
        projectData: ProjectData,
        reward: Reward
    ): Pair<PledgeData, PledgeReason> {
        val pledgeReason =
            if (projectData.project().isBacking()) PledgeReason.UPDATE_REWARD
            else PledgeReason.PLEDGE
        val pledgeData =
            PledgeData.with(PledgeFlowContext.forPledgeReason(pledgeReason), projectData, reward)
        return Pair(pledgeData, pledgeReason)
    }

    private fun getRewardsTotalAmount(rewards: List<Reward>): Double {
        var total = 0.0
        rewards.forEach { reward ->
            reward.quantity()?.let { quantity ->
                total += (reward.minimum() * quantity)
            } ?: run {
                total += reward.minimum()
            }
        }
        return total
    }

    fun incrementBonusSupport() {
        addedBonusSupport += minStepAmount
        totalAmount = calculateTotal()
        viewModelScope.launch {
            emitCurrentState()
        }
    }

    fun decrementBonusSupport() {
        if ((addedBonusSupport + initialBonusSupport) - minStepAmount >= initialBonusSupport) {
            addedBonusSupport -= minStepAmount
            totalAmount = calculateTotal()
            viewModelScope.launch {
                emitCurrentState()
            }
        }
    }

    fun inputBonusSupport(input: Double) {
        addedBonusSupport = input
        totalAmount = calculateTotal()
        viewModelScope.launch {
            emitCurrentState()
        }
    }

    fun onContinueClicked(defaultAction: () -> Unit) {
        if (projectData.project().postCampaignPledgingEnabled() == true && projectData.project()
            .isInPostCampaignPledgingPhase() == true
        ) {
            viewModelScope.launch {
                emitCurrentState(isLoading = true)
                apolloClient.createCheckout(
                    CreateCheckoutData(
                        project = projectData.project(),
                        amount = totalAmount.toString(),
                        locationId = if (::selectedShippingRule.isInitialized) selectedShippingRule.location()
                            ?.id()?.toString() else null,
                        rewardsIds = fullIdListForQuantities(rewardAndAddOns),
                        refTag = projectData.refTagFromIntent()
                    )
                )
                    .asFlow()
                    .map { checkoutPayment ->
                        mutableCheckoutPayment.emit(checkoutPayment)
                    }
                    .catch {
                        errorAction.invoke(null)
                    }
                    .onCompletion {
                        emitCurrentState()
                    }
                    .collect()
            }
        } else {
            defaultAction.invoke()
        }
    }

    private fun fullIdListForQuantities(flattenedList: List<Reward>): List<Reward> {
        val mutableList = mutableListOf<Reward>()

        flattenedList.map {
            if (!it.isAddOn()) mutableList.add(it)
            else {
                val q = it.quantity() ?: 1
                for (i in 1..q) {
                    mutableList.add(it)
                }
            }
        }

        return mutableList.toList()
    }

    fun provideCurrentShippingRule(shippingRule: ShippingRule) {
        selectedShippingRule = shippingRule
        updateShippingAmount()
        totalAmount = calculateTotal()

        viewModelScope.launch {
            emitCurrentState()
        }
    }

    private suspend fun emitCurrentState(isLoading: Boolean = false) {
        mutableConfirmDetailsUIState.emit(
            ConfirmDetailsUIState(
                rewardsAndAddOns = rewardAndAddOns,
                initialBonusSupportAmount = initialBonusSupport,
                totalBonusSupportAmount = initialBonusSupport + addedBonusSupport,
                shippingAmount = shippingAmount,
                totalAmount = totalAmount,
                minStepAmount = minStepAmount,
                maxPledgeAmount = maxPledgeAmount,
                isLoading = isLoading
            )
        )
    }

    class Factory(private val environment: Environment) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ConfirmDetailsViewModel(environment) as T
        }
    }
}
