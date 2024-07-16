package com.kickstarter.viewmodels.projectpage

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.models.Project
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
    val addOns: List<Reward> = emptyList(),
    val totalCount: Int = 0,
    val isLoading: Boolean = true
)

class AddOnsViewModel(val environment: Environment, bundle: Bundle? = null) : ViewModel() {
    private val apolloClient = requireNotNull(environment.apolloClientV2())

    private var currentUserReward: Reward = Reward.builder().build()
    var project: Project = Project.builder().build()
    var shippingRule: ShippingRule = ShippingRule.builder().build()
    var pledgeflowcontext: PledgeFlowContext = PledgeFlowContext.NEW_PLEDGE

    private var addOns: List<Reward> = listOf()
    private var errorAction: (message: String?) -> Unit = {}
    val totalCount = mutableMapOf<Long, Int>()

    private var backedAddOns = emptyList<Reward>()

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

            pledgeData?.projectData()?.project()?.let {
                project
            }

            pledgeData?.shippingRule()?.let {
                shippingRule = it
                provideSelectedShippingRule(it)
            }

            pledgeData?.pledgeFlowContext()?.let { pFContext ->
                pledgeflowcontext = pFContext
            }
        }
    }

    fun provideProjectData(projectData: ProjectData) {
        projectData.project()?.let {
            project = it
        }
    }

    fun provideSelectedShippingRule(shippingRule: ShippingRule) {
        getAddOns(selectedShippingRule = shippingRule)
    }

    private fun getAddOns(selectedShippingRule: ShippingRule) {
        viewModelScope.launch {
           val newList = (1..10).map {
                Reward.builder()
                    .title("Item Number $it")
                    .description("This is a description for item $it")
                    .id((1..100).random().toLong())
                    .quantity(0)
                    .convertedMinimum((100 * (it + 1)).toDouble())
                    .isAvailable(it != 0)
                    .limit(if (it == 0) 1 else 10)
                    .build()
            }

            addOns = getUpdatedList(newList, listOf(newList.first().toBuilder().quantity(5).build()))
            emitCurrentState()
        }
//        viewModelScope.launch {
//            apolloClient
//                .getProjectAddOns(
//                    slug = project?.slug() ?: "",
//                    locationId = selectedShippingRule.location() ?: LocationFactory.empty()
//                )
//                .asFlow()
//                .map { addOns ->
//                    if (!addOns.isNullOrEmpty()) {
//                        this@AddOnsViewModel.addOns = getUpdatedList(addOns, backedAddOns)
//                    }
//                }.onCompletion {
//                    emitCurrentState()
//                }.catch {
//                    errorAction.invoke(null)
//                }.collect()
//        }
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
            totalCount[it.id()] = it.quantity() ?: 0
        }

        return holder.values.toList()
    }

    // UI events
    fun userRewardSelection(reward: Reward) {
//
//        currentUserReward = reward
//
//        viewModelScope.launch {
//            emitCurrentState()
//        }
    }

    fun onAddOnsAddedOrRemoved(currentAddOnsSelections: MutableMap<Reward, Int>) {
    }

    private suspend fun emitCurrentState(isLoading: Boolean = false) {
        mutableAddOnsUIState.emit(
            AddOnsUIState(
                addOns = addOns,
                isLoading = isLoading,
                totalCount = totalCount.values.sum()
            )
        )
    }

    fun start() {
        getAddOns(this.shippingRule)
    }

    fun updateSelection(rewardId: Long, quantity: Int) {
        viewModelScope.launch {
            totalCount[rewardId] = quantity
            emitCurrentState(isLoading = false)
        }
    }

    class Factory(private val environment: Environment, private val bundle: Bundle? = null) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddOnsViewModel(environment, bundle) as T
        }
    }
}
