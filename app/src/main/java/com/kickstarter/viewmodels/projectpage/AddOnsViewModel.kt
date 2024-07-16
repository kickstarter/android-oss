package com.kickstarter.viewmodels.projectpage

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.ProjectData
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
    val addOns: List<Reward> = listOf(),
    val isLoading: Boolean = false
)

class AddOnsViewModel(val environment: Environment, bundle: Bundle? = null) : ViewModel() {
    private val apolloClient = requireNotNull(environment.apolloClientV2())

    private var addOns: List<Reward> = listOf()
    private var currentUserReward: Reward = Reward.builder().build()
    var currentAddOnsSelections: MutableMap<Reward, Int> = mutableMapOf()
    private var projectData: ProjectData? = null
    private var errorAction: (message: String?) -> Unit = {}

    private var backedAddOns = emptyList<Reward>()
    var pledgeflowcontext: PledgeFlowContext? = null

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

    fun provideBundle(bundle: Bundle?) {
        bundle?.let {
            val pledgeData = it.getParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA) as PledgeData?

            pledgeData?.projectData()?.let { pData ->
                provideProjectData(pData)
            }

            pledgeData?.shippingRule()?.let { rule ->
                provideSelectedShippingRule(rule)
            }

            pledgeData?.pledgeFlowContext()?.let { pFContext ->
                pledgeflowcontext = pFContext
            }
        }
    }
    fun provideProjectData(projectData: ProjectData) {
        this.projectData = projectData
    }

    fun provideSelectedShippingRule(shippingRule: ShippingRule) {
        getAddOns(selectedShippingRule = shippingRule)
    }

    private fun getAddOns(selectedShippingRule: ShippingRule) {
        viewModelScope.launch {
            apolloClient
                .getProjectAddOns(
                    slug = projectData?.project()?.slug() ?: "",
                    locationId = selectedShippingRule.location() ?: LocationFactory.empty()
                ).asFlow()
                .onStart {
                    emitCurrentState(isLoading = true)
                }.map { addOns ->
                    if (!addOns.isNullOrEmpty()) {
                        backedAddOns.map {
                            currentAddOnsSelections[it] = it.quantity() ?: 0
                        }
                        this@AddOnsViewModel.addOns = addOns
                    }
                }.onCompletion {
                    emitCurrentState()
                }.catch {
                    errorAction.invoke(null)
                }.collect()
        }
    }

    private fun updateSelectionMap(backedAddOns: List<Reward>) {

    }

    /**
     * List of available addOns, but updated for those backed addOns with the Backed information
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
        }

        return holder.values.toList()
    }

    // UI events
    fun userRewardSelection(reward: Reward) {

        currentUserReward = reward

        viewModelScope.launch {
            emitCurrentState()
        }
    }

    fun onAddOnsAddedOrRemoved(currentAddOnsSelections: MutableMap<Reward, Int>) {
        this.currentAddOnsSelections = currentAddOnsSelections
        viewModelScope.launch {
            emitCurrentState()
        }
    }

    private suspend fun emitCurrentState(isLoading: Boolean = false) {
        mutableAddOnsUIState.emit(
            AddOnsUIState(
                addOns = addOns,
                isLoading = isLoading
            )
        )
    }

    class Factory(private val environment: Environment, private val bundle: Bundle? = null) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddOnsViewModel(environment, bundle) as T
        }
    }
}
