package com.kickstarter.viewmodels.projectpage

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.pledgeAmountTotalPlusBonus
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Backing
import com.kickstarter.models.Location
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.rx2.asFlow
import kotlin.coroutines.EmptyCoroutineContext

data class AddOnsUIState(
    val addOns: List<Reward> = emptyList(),
    val totalCount: Int = 0,
    val isLoading: Boolean = false,
    val shippingRule: ShippingRule = ShippingRule.builder().build(),
    val totalPledgeAmount: Double = 0.0,
    val totalBonusAmount: Double = 0.0
)

class AddOnsViewModel(
    private val environment: Environment,
    bundle: Bundle? = null,
    private val testDispatcher: CoroutineDispatcher? = null
) : ViewModel() {

    private val apolloClient = requireNotNull(environment.apolloClientV2())
    private val currentUser = requireNotNull(environment.currentUserV2())
    private var isUserLoggedIn = false

    private var currentUserReward: Reward = Reward.builder().build()
    private var pledgeData: PledgeData? = null
    private var projectData: ProjectData? = null
    private var project: Project = Project.builder().build()
    private var shippingRule: ShippingRule = ShippingRule.builder().build()
    private var pledgeflowcontext: PledgeFlowContext = PledgeFlowContext.NEW_PLEDGE
    private var bonusAmount: Double = 0.0
    private var pReason: PledgeReason = PledgeReason.PLEDGE
    private var backing: Backing? = null

    private var addOns = mutableListOf<Reward>()
    private var errorAction: (message: String?) -> Unit = {}
    private val currentSelection = mutableMapOf<Long, Int>()

    private var backedAddOns = emptyList<Reward>()

    // Pagination variables
    private var nextPage: String? = null
    var hasMorePages = false

    private val scope = viewModelScope + (testDispatcher ?: EmptyCoroutineContext)

    private val mutableAddOnsUIState = MutableStateFlow(AddOnsUIState())
    val addOnsUIState: StateFlow<AddOnsUIState>
        get() = mutableAddOnsUIState
            .asStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = AddOnsUIState()
            )

    fun provideErrorAction(errorAction: (message: String?) -> Unit) {
        this.errorAction = errorAction
    }

    init {
        scope.launch {
            currentUser.observable().asFlow()
                .collectLatest {
                    isUserLoggedIn = it.isPresent()
                }
        }
    }

    /**
     * Used in Crowdfund checkout
     */
    fun provideBundle(bundle: Bundle?) {
        bundle?.let {
            pledgeData = it.getParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA) as PledgeData?

            // Send analytic event for crowdfund checkout
            this.sendEvent()

            pReason = it.getSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON) as PledgeReason

            pledgeData?.projectData()?.let {
                projectData = it
            }

            // New pledge, selected reward
            pledgeData?.reward()?.let { rw ->
                currentUserReward = rw
                bonusAmount = RewardUtils.minPledgeAmount(rw, project)
            }

            projectData?.project()?.let {
                project = it
            }

            pledgeData?.shippingRule()?.let {
                shippingRule = it
            }

            pledgeData?.pledgeFlowContext()?.let { pFContext ->
                pledgeflowcontext = pFContext
            }

            backing = pledgeData?.projectData()?.backing() ?: project.backing()

            if (pReason == PledgeReason.UPDATE_REWARD && backing?.reward()?.id() != currentUserReward.id()) {
                // Do nothing, user is selecting a different reward/addOns ...
            } else {
                // User is selecting a same reward with AddOns, might just adding/deleting addOns, bonus support ...
                backing?.let { b ->
                    // - backed a reward no reward
                    if (b.reward() == null && b.amount().isNotNull()) {
                        currentUserReward =
                            RewardFactory.noReward().toBuilder().pledgeAmount(b.amount()).build()
                        bonusAmount = b.amount()
                    } else {
                        backedAddOns = b.addOns() ?: emptyList()
                        bonusAmount = b.bonusAmount()
                    }
                }
            }

            scope.launch {
                getAddOns(shippingRule)
            }
        }
    }

    /**
     * Used in late pledges
     */
    fun provideProjectData(projectData: ProjectData) {
        this.projectData = projectData
        val isLatePledge = projectData.project().postCampaignPledgingEnabled() == true && projectData.project().isInPostCampaignPledgingPhase() == true
        val flowContext = if (isLatePledge) PledgeFlowContext.forPledgeReason(PledgeReason.LATE_PLEDGE) else PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)
        this.pledgeflowcontext = flowContext
        this.pledgeData = PledgeData.with(
            projectData = projectData,
            pledgeFlowContext = flowContext,
            reward = currentUserReward
        )
        this.projectData?.project()?.let {
            project = it
        }
    }

    /**
     * Used in late pledges
     */
    fun provideSelectedShippingRule(shippingRule: ShippingRule) {
        if (this.shippingRule != shippingRule) {
            this.shippingRule = shippingRule
            scope.launch {
                getAddOns(shippingRule)
            }
        }
    }

    fun loadMore() {
        scope.launch {
            getAddOns(shippingRule)
        }
    }

    private suspend fun getAddOns(selectedShippingRule: ShippingRule) {
        // - Do not execute call unless reward has addOns
        if (currentUserReward.hasAddons()) {
            emitCurrentState(isLoading = true)
            val envelopeResult = apolloClient.getRewardAllowedAddOns(
                rewardId = currentUserReward,
                locationId = selectedShippingRule.location() ?: Location.builder().build(),
                cursor = nextPage
            )

            if (envelopeResult.isSuccess) {
                val addOns = envelopeResult.getOrNull()?.addOnsList
                // - pagination related stuff
                nextPage = envelopeResult.getOrNull()?.pageInfo?.endCursor
                hasMorePages = envelopeResult.getOrNull()?.pageInfo?.hasNextPage ?: false
                if (!addOns.isNullOrEmpty()) {
                    val updatedList = getUpdatedList(
                        addOns,
                        backedAddOns,
                        selectedShippingRule.location() ?: Location.builder().build()
                    )

                    this@AddOnsViewModel.addOns.addAll(updatedList)
                }
                emitCurrentState(isLoading = false)
            }

            if (envelopeResult.isFailure) {
                errorAction.invoke(null)
            }
        } else {
            emitCurrentState(isLoading = false)
        }
    }

    /**
     * List of available addOns, updated for those backed addOns with the Backed information
     * such as quantity backed.
     */
    private fun getUpdatedList(addOns: List<Reward>, backedAddOns: List<Reward>, location: Location): List<Reward> {
        val holder = mutableMapOf<Long, Reward>()
        val locationId = location.id()

        val filteredAddOns = addOns.filter { reward ->
            when (reward.shippingPreference()) {
                "unrestricted", "none", "local" -> true
                else -> reward.shippingRules()!!.any { it.location()?.id() == locationId }
            }
        }
        // Store filtered addOns into holder
        filteredAddOns.forEach {
            holder[it.id()] = it
        }

        // Take the backed AddOns, update with matching addOn ID with the quantity information
        backedAddOns.forEach { backedAddOn ->
            val match = holder[backedAddOn.id()]
            if (match != null) {
                val updated = match.toBuilder().quantity(backedAddOn.quantity()).build()
                holder[backedAddOn.id()] = updated
            }
            currentSelection[backedAddOn.id()] = backedAddOn.quantity() ?: 0
        }

        return holder.values.toList()
    }

    // - User has selected a different reward clear previous states
    fun userRewardSelection(reward: Reward) {
        if (reward != currentUserReward) {
            currentUserReward = reward
            currentSelection.clear()
            bonusAmount = 0.0

            scope.launch {
                emitCurrentState()
            }
        }
    }

    private suspend fun emitCurrentState(isLoading: Boolean = false) {
        mutableAddOnsUIState.emit(
            AddOnsUIState(
                addOns = addOns.toList(),
                isLoading = isLoading,
                totalCount = currentSelection.values.sum(),
                shippingRule = shippingRule,
                totalPledgeAmount = calculateTotalPledgeAmount(),
                totalBonusAmount = bonusAmount
            )
        )
    }

    /**
     * Callback to update the selected addOns quantity
     */
    fun updateSelection(rewardId: Long, quantity: Int) {
        scope.launch {
            currentSelection[rewardId] = quantity
            emitCurrentState(isLoading = false)
        }
    }

    fun getPledgeDataAndReason(): Pair<PledgeData, PledgeReason>? {
        val selectedAddOns = mutableListOf<Reward>()
        addOns.forEach {
            val amount = currentSelection[it.id()]
            if (amount != null && amount > 0) {
                selectedAddOns.add(it.toBuilder().quantity(amount).build())
            }
        }

        return projectData?.let {
            Pair(
                PledgeData.with(pledgeflowcontext, it, currentUserReward, selectedAddOns.toList(), shippingRule, bonusAmount = bonusAmount),
                pReason
            )
        }
    }

    fun isUserLoggedIn(): Boolean = isUserLoggedIn
    fun getProject() = this.project
    fun getSelectedReward() = this.currentUserReward
    fun sendEvent() = this.pledgeData?.let {
        environment.analytics()?.trackAddOnsScreenViewed(it)
    }

    fun bonusAmountUpdated(bAmount: Double) {
        scope.launch {
            bonusAmount = bAmount
            emitCurrentState(isLoading = false)
        }
    }

    private fun calculateTotalPledgeAmount(): Double {
        return getPledgeDataAndReason()?.first?.pledgeAmountTotalPlusBonus() ?: 0.0
    }

    class Factory(
        private val environment: Environment,
        private val bundle: Bundle? = null,
        private val testDispatcher: CoroutineDispatcher? = null
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddOnsViewModel(environment, bundle, testDispatcher) as T
        }
    }
}
