package com.kickstarter.viewmodels.projectpage

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

data class AddOnsUIState(
    val addOns: List<Reward> = emptyList(),
    val totalCount: Int = 0,
    val isLoading: Boolean = true
)

class AddOnsViewModel(val environment: Environment, bundle: Bundle? = null) : ViewModel() {
    private val apolloClient = requireNotNull(environment.apolloClientV2())

    private var currentUserReward: Reward = Reward.builder().build()
    private var projectData: ProjectData? = null
    private var project: Project = Project.builder().build()
    private var shippingRule: ShippingRule = ShippingRule.builder().build()
    private var pledgeflowcontext: PledgeFlowContext = PledgeFlowContext.NEW_PLEDGE
    private var pReason: PledgeReason = PledgeReason.PLEDGE

    private var addOns: List<Reward> = listOf()
    private var errorAction: (message: String?) -> Unit = {}
    private val currentSelection = mutableMapOf<Long, Int>()

    private var backedAddOns = emptyList<Reward>()

    private var scope: CoroutineScope = viewModelScope
    private var dispatcher: CoroutineDispatcher = Dispatchers.IO

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
     * Used in Crowdfund checkout
     */
    fun provideBundle(bundle: Bundle?) {
        bundle?.let {
            val pledgeData = it.getParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA) as PledgeData?
            pReason = it.getSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON) as PledgeReason

            pledgeData?.projectData()?.let {
                projectData = it
            }

            // New pledge, selected reward
            pledgeData?.reward()?.let {
                currentUserReward = it
            }

            projectData?.project()?.let {
                project = it
            }

            pledgeData?.shippingRule()?.let {
                shippingRule = it
                provideSelectedShippingRule(it)
            }

            pledgeData?.pledgeFlowContext()?.let { pFContext ->
                pledgeflowcontext = pFContext
            }

            val backing = pledgeData?.projectData()?.backing() ?: project.backing()

            // - User backed a reward no reward
            if (backing != null && backing.reward() == null && backing.amount().isNotNull()) {
                currentUserReward = RewardFactory.noReward().toBuilder().pledgeAmount(backing.amount()).build()
            }

            // - User is backing
            backing?.reward()?.let {
                currentUserReward = it
            }

            backing?.addOns()?.let {
                backedAddOns = it
            }
        }
    }

    /**
     * Used in late pledges, does requires calling afterwards
     *  `provideSelectedShippingRule`
     */
    fun provideProjectData(projectData: ProjectData) {
        this.projectData = projectData
        this.projectData?.project()?.let {
            project = it
        }
    }

    /**
     * Used in late pledges
     */
    fun provideSelectedShippingRule(shippingRule: ShippingRule) {
        getAddOns(selectedShippingRule = shippingRule)
    }

    private fun getAddOns(selectedShippingRule: ShippingRule) {
        scope.launch(dispatcher) {
            apolloClient
                .getProjectAddOns(
                    slug = project?.slug() ?: "",
                    locationId = selectedShippingRule.location() ?: LocationFactory.empty()
                )
                .asFlow()
                .onStart {
                    emitCurrentState(isLoading = true)
                }
                .map { addOns ->
                    if (!addOns.isNullOrEmpty()) {
                        this@AddOnsViewModel.addOns = getUpdatedList(addOns, backedAddOns)
                    }
                }.onCompletion {
                    emitCurrentState()
                }.catch {
                    errorAction.invoke(null)
                }.collect()
        }
    }

    /**
     * List of available addOns, updated for those backed addOns with the Backed information
     * such as quantity backed.
     */
    private fun getUpdatedList(addOns: List<Reward>, backedAddOns: List<Reward>): List<Reward> {
        val holder = mutableMapOf<Long, Reward>()
        // First Store all addOns, Key should be the addOns ID
        addOns.map {
            holder[it.id()] = it
        }

        // Take the backed AddOns, update with the backed AddOn information which will contain the backed quantity
        backedAddOns.map {
            holder[it.id()] = it
            currentSelection[it.id()] = it.quantity() ?: 0
        }

        return holder.values.toList()
    }

    // UI events
    fun userRewardSelection(reward: Reward) {
        currentUserReward = reward
        currentSelection.clear()

        scope.launch {
            emitCurrentState()
        }
    }

    private suspend fun emitCurrentState(isLoading: Boolean = false) {
        mutableAddOnsUIState.emit(
            AddOnsUIState(
                addOns = addOns,
                isLoading = isLoading,
                totalCount = currentSelection.values.sum()
            )
        )
    }

    /**
     * Used in crowdfund
     */
    fun load() {
        getAddOns(this.shippingRule)
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
            if (amount != null) {
                selectedAddOns.add(it.toBuilder().quantity(amount).build())
            }
        }

        return projectData?.let {
            Pair(
                PledgeData.with(pledgeflowcontext, it, currentUserReward, selectedAddOns.toList(), shippingRule),
                pReason
            )
        }
    }

    fun getProject() = this.project

    class Factory(private val environment: Environment, private val bundle: Bundle? = null) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddOnsViewModel(environment, bundle) as T
        }
    }
}
