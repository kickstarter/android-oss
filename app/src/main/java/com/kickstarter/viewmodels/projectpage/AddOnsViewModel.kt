package com.kickstarter.viewmodels.projectpage

import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.data.ProjectData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AddOnsUIState(
    var currentShippingRule: ShippingRule = ShippingRule.builder().build(),
    var shippingSelectorIsGone: Boolean = false,
    var currentAddOnsSelection: MutableMap<Reward, Int> = mutableMapOf()
)
class AddOnsViewModel(val environment: Environment) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val currentConfig = requireNotNull(environment.currentConfigV2())

    private val currentUserReward = PublishSubject.create<Reward>()
    private val shippingRules = BehaviorSubject.create<List<ShippingRule>>()
    private val defaultShippingRule = PublishSubject.create<ShippingRule>()
    private val shippingRuleSelected = PublishSubject.create<ShippingRule>()

    private val mutableAddOnsUIState = MutableStateFlow(AddOnsUIState())
    private var currentShippingRule: ShippingRule = ShippingRule.builder().build()
    private var shippingSelectorIsGone: Boolean = false
    private var currentAddOnsSelections: MutableMap<Reward, Int> = mutableMapOf()
    val addOnsUIState: StateFlow<AddOnsUIState>
        get() = mutableAddOnsUIState
            .asStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = AddOnsUIState()
            )

    private val mutableFlowUIRequest = MutableSharedFlow<FlowUIState>()
    val flowUIRequest: SharedFlow<FlowUIState>
        get() = mutableFlowUIRequest
            .asSharedFlow()

    init {
        currentUserReward
            .distinctUntilChanged()
            .subscribe {
                shippingSelectorIsGone = RewardUtils.isDigital(it) || !RewardUtils.isShippable(it) || RewardUtils.isLocalPickup(it)
                viewModelScope.launch {
                    mutableAddOnsUIState.emit(
                        AddOnsUIState(
                            currentShippingRule = currentShippingRule,
                            shippingSelectorIsGone = shippingSelectorIsGone,
                            currentAddOnsSelection = currentAddOnsSelections
                        )
                    )
                }
            }
            .addToDisposable(disposables)

        currentUserReward
            .filter {
                !RewardUtils.isDigital(it) && RewardUtils.isShippable(it) && !RewardUtils.isLocalPickup(it)
            }
            .compose<Pair<Reward, List<ShippingRule>>>(
                Transformers.combineLatestPair(
                    shippingRules
                )
            )
            .switchMap { getDefaultShippingRule(it.second) }
            .subscribe {
                defaultShippingRule.onNext(it)
                currentShippingRule = it
                viewModelScope.launch {
                    mutableAddOnsUIState.emit(
                        AddOnsUIState(
                            currentShippingRule = currentShippingRule,
                            shippingSelectorIsGone = shippingSelectorIsGone,
                            currentAddOnsSelection = currentAddOnsSelections
                        )
                    )
                }
            }.addToDisposable(disposables)

        val shippingRule = getSelectedShippingRule(defaultShippingRule, currentUserReward)

        shippingRule
            .distinctUntilChanged { rule1, rule2 ->
                rule1.location()?.id() == rule2.location()?.id() && rule1.cost() == rule2.cost()
            }
            .subscribe {
                currentShippingRule = it
                this.shippingRuleSelected.onNext(it)
            }
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
    private fun getSelectedShippingRule(
        defaultShippingRule: Observable<ShippingRule>,
        reward: Observable<Reward>
    ): Observable<ShippingRule> {
        return Observable.combineLatest(
            defaultShippingRule.startWith(ShippingRuleFactory.emptyShippingRule()),
            reward
        ) { defaultShipping, rw ->
            return@combineLatest chooseShippingRule(defaultShipping, rw)
        }
    }
    private fun chooseShippingRule(defaultShipping: ShippingRule, rw: Reward): ShippingRule =
        when {
            RewardUtils.isDigital(rw) || !RewardUtils.isShippable(rw) || RewardUtils.isLocalPickup(
                rw
            ) -> ShippingRuleFactory.emptyShippingRule()
            // sameReward -> backingShippingRule // TODO: When changing reward for manage pledge flow
            else -> defaultShipping
        }

    // UI events

    fun userRewardSelection(reward: Reward, shippingRules: List<ShippingRule>) {
        // A new reward has been selected, so clear out any previous addons selection
        this.currentAddOnsSelections = mutableMapOf()
        viewModelScope.launch {
            mutableAddOnsUIState.emit(
                AddOnsUIState(
                    currentShippingRule = currentShippingRule,
                    shippingSelectorIsGone = shippingSelectorIsGone,
                    currentAddOnsSelection = currentAddOnsSelections
                )
            )
        }

        this.currentUserReward.onNext(reward)
        this.shippingRules.onNext(shippingRules)
    }
    fun onShippingLocationChanged(shippingRule: ShippingRule) {
        shippingRuleSelected.onNext(shippingRule)
        currentShippingRule = shippingRule

        viewModelScope.launch {
            mutableAddOnsUIState.emit(
                AddOnsUIState(
                    currentShippingRule = shippingRule,
                    shippingSelectorIsGone = shippingSelectorIsGone,
                    currentAddOnsSelection = currentAddOnsSelections
                )
            )
        }
    }
    fun onAddOnsAddedOrRemoved(currentAddOnsSelections: MutableMap<Reward, Int>) {
        this.currentAddOnsSelections = currentAddOnsSelections
        viewModelScope.launch {
            mutableAddOnsUIState.emit(
                AddOnsUIState(
                    currentShippingRule = currentShippingRule,
                    shippingSelectorIsGone = shippingSelectorIsGone,
                    currentAddOnsSelection = currentAddOnsSelections
                )
            )
        }
    }
    fun onAddOnsContinueClicked() {
        viewModelScope.launch {
            // Go to confirm page
            mutableFlowUIRequest.emit(FlowUIState(currentPage = 2, expanded = true))
        }
    }

    class Factory(private val environment: Environment) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddOnsViewModel(environment) as T
        }
    }
}
