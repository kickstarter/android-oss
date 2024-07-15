package com.kickstarter.viewmodels.projectpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.models.Location
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
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
    var currentAddOnsSelection: MutableMap<Reward, Int> = mutableMapOf(),
    val isLoading: Boolean = false
)

class AddOnsViewModel(val environment: Environment) : ViewModel() {
    private val currentConfig = requireNotNull(environment.currentConfigV2())
    private val apolloClient = requireNotNull(environment.apolloClientV2())

    private val mutableAddOnsUIState = MutableStateFlow(AddOnsUIState())
    private var addOns: List<Reward> = listOf()
    private var currentUserReward: Reward = Reward.builder().build()
    private var currentAddOnsSelections: MutableMap<Reward, Int> = mutableMapOf()
    private lateinit var projectData: ProjectData
    private var errorAction: (message: String?) -> Unit = {}

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
                    slug = projectData.project().slug() ?: "",
                    locationId = selectedShippingRule.location() ?: Location.builder().build()
                ).asFlow()
                .onStart {
                    emitCurrentState(isLoading = true)
                }.map { addOns ->
                    if (!addOns.isNullOrEmpty()) {
                        this@AddOnsViewModel.addOns = addOns
                    }
                }.onCompletion {
                    emitCurrentState()
                }.catch {
                    errorAction.invoke(null)
                }.collect()
        }
    }

    // UI events
    fun userRewardSelection(reward: Reward) {
        // A new reward has been selected, so clear out any previous addons selection
        this.currentAddOnsSelections = mutableMapOf()

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
                currentAddOnsSelection = currentAddOnsSelections,
                addOns = addOns,
                isLoading = isLoading
            )
        )
    }

    class Factory(private val environment: Environment) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddOnsViewModel(environment) as T
        }
    }
}
