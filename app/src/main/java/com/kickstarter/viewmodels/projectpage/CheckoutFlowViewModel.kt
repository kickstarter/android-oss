package com.kickstarter.viewmodels.projectpage

import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Location
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.data.ProjectData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FlowUIState(
    val currentPage: Int = 0,
    val expanded: Boolean = false
)

class CheckoutFlowViewModel(val environment: Environment) : ViewModel() {

    private val apolloClient = requireNotNull(environment.apolloClientV2())
    private val currentConfig = requireNotNull(environment.currentConfigV2())

    private val disposables = CompositeDisposable()

    val shippingRules = PublishSubject.create<List<ShippingRule>>()
    val addOns = PublishSubject.create<List<Reward>>()
    val defaultShippingRule = PublishSubject.create<ShippingRule>()
    val currentUserReward = PublishSubject.create<Reward>()
    val projectData = PublishSubject.create<ProjectData>()

    private lateinit var currentProjectData: ProjectData
    private lateinit var newUserReward: Reward

    private val mutableFlowUIState = MutableStateFlow(FlowUIState())
    val flowUIState: StateFlow<FlowUIState>
        get() = mutableFlowUIState
            .asStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = FlowUIState()
            )

    init {
        shippingRules
            .filter { it.isNotEmpty() }
            .compose<Pair<List<ShippingRule>, Reward>>(
                Transformers.combineLatestPair(
                    currentUserReward
                )
            )
            .filter {
                !RewardUtils.isDigital(it.second) && RewardUtils.isShippable(it.second) && !RewardUtils.isLocalPickup(
                    it.second
                )
            }
            .switchMap { getDefaultShippingRule(it.first) }
            .subscribe {
                defaultShippingRule.onNext(it)
            }.addToDisposable(disposables)
    }

    fun changePage(requestedFlowState: FlowUIState) {
        viewModelScope.launch {
            mutableFlowUIState.emit(requestedFlowState)
        }
    }

    fun provideProjectData(projectData: ProjectData) {
        currentProjectData = projectData
        viewModelScope.launch {
            projectData.project().rewards()?.let { rewards ->
                apolloClient.getShippingRules(
                    reward = rewards.first { theOne ->
                        !theOne.isAddOn() && theOne.isAvailable() && RewardUtils.isShippable(theOne)
                    }
                ).subscribe { shippingRulesEnvelope ->
                    if (shippingRulesEnvelope.isNotNull()) shippingRules.onNext(
                        shippingRulesEnvelope.shippingRules()
                    )
                }.addToDisposable(disposables)
            }
        }

        apolloClient
            .getProjectAddOns(
                projectData.project().slug() ?: "",
                projectData.project().location() ?: Location.builder().build()
            )
            .onErrorResumeNext(Observable.empty())
            .filter { it.isNotNull() }
            .subscribe { addOns.onNext(it) }
            .addToDisposable(disposables)
    }

    private fun getDefaultShippingRule(shippingRules: List<ShippingRule>): Observable<ShippingRule> {
        return this.currentConfig.observable()
            .map { it.countryCode() }
            .map { countryCode ->
                shippingRules.firstOrNull { it.location()?.country() == countryCode }
                    ?: shippingRules.first()
            }
    }

    fun userRewardSelection(reward: Reward) {
        viewModelScope.launch {
            currentUserReward.onNext(reward)
            newUserReward = reward
        }
    }

    fun onAddOnsContinueClicked() {
        viewModelScope.launch {
            // Show confirm page
            mutableFlowUIState.emit(FlowUIState(currentPage = 2, expanded = true))
        }
    }

    fun onBackPressed(currentPage: Int) {
        viewModelScope.launch {
            when (currentPage) {
                // From Checkout Screen
                3 -> {
                    // To Confirm Details
                    mutableFlowUIState.emit(FlowUIState(currentPage = 2, expanded = true))
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

    class Factory(private val environment: Environment) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CheckoutFlowViewModel(environment) as T
        }
    }
}
