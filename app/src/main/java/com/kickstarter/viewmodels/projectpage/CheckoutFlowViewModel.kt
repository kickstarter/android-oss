package com.kickstarter.viewmodels.projectpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.models.Reward
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

data class FlowUIState(
    val currentPage: Int = 0,
    val expanded: Boolean = false
)

class CheckoutFlowViewModel(val environment: Environment) : ViewModel() {

    private lateinit var newUserReward: Reward
    private val currentUser = requireNotNull(environment.currentUserV2())

    private val mutableFlowUIState = MutableStateFlow(FlowUIState())
    val flowUIState: StateFlow<FlowUIState>
        get() = mutableFlowUIState
            .asStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = FlowUIState()
            )

    fun changePage(requestedFlowState: FlowUIState) {
        viewModelScope.launch {
            mutableFlowUIState.emit(requestedFlowState)
        }
    }

    fun userRewardSelection(reward: Reward) {
        newUserReward = reward
    }

    fun onBackPressed(currentPage: Int) {
        viewModelScope.launch {
            when (currentPage) {
                // From Checkout Screen
                3 -> {
                    // To AddOns
                    mutableFlowUIState.emit(FlowUIState(currentPage = 1, expanded = true))
                }

                // From Confirm Details Screen
                2 -> {
                    if (newUserReward.hasAddons()) {
                        // To Add-ons
                        mutableFlowUIState.emit(FlowUIState(currentPage = 1, expanded = true))
                    } else {
                        // To Reward Carousel
                        mutableFlowUIState.emit(FlowUIState(currentPage = 0, expanded = true))
                    }
                }

                // From Add-ons Screen
                1 -> {
                    // To Rewards Carousel
                    mutableFlowUIState.emit(FlowUIState(currentPage = 0, expanded = true))
                }

                // From Rewards Carousel Screen
                0 -> {
                    // Leave flow
                    mutableFlowUIState.emit(FlowUIState(currentPage = 0, expanded = false))
                }
            }
        }
    }

    fun onBackThisProjectClicked() {
        viewModelScope.launch {
            // Open Flow
            mutableFlowUIState.emit(FlowUIState(currentPage = 0, expanded = true))
        }
    }

    fun onContinueClicked(logInCallback: () -> Unit, continueCallback: () -> Unit) {
        viewModelScope.launch {
            currentUser.isLoggedIn
                .asFlow()
                .collect { userLoggedIn ->
                    // - Show checkout page
                    if (userLoggedIn) {
                        mutableFlowUIState.emit(FlowUIState(currentPage = 4, expanded = true))
                        // - In case after login some action is required
                        continueCallback()
                    }
                    // - Trigger LoginFlow callback
                    else logInCallback()
                }
        }
    }

    fun onProjectSuccess() {
        viewModelScope.launch {
            // Open Flow
            mutableFlowUIState.emit(FlowUIState(currentPage = 0, expanded = false))
        }
    }

    fun onContinueClicked(continueCallback: () -> Unit) {
    }

    class Factory(private val environment: Environment) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CheckoutFlowViewModel(environment) as T
        }
    }
}
