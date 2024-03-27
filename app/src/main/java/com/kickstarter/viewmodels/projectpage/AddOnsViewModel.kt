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
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.models.Location
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

data class AddOnsUIState(
    var currentShippingRule: ShippingRule = ShippingRule.builder().build(),
    var shippingSelectorIsGone: Boolean = false,
    var currentAddOnsSelection: MutableMap<Reward, Int> = mutableMapOf(),
    val addOns: List<Reward> = listOf(),
    val shippingRules: List<ShippingRule> = listOf()
)

class AddOnsViewModel(val environment: Environment) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val currentConfig = requireNotNull(environment.currentConfigV2())
    private val apolloClient = requireNotNull(environment.apolloClientV2())

    private val currentUserReward = PublishSubject.create<Reward>()
    private val shippingRulesObservable = BehaviorSubject.create<List<ShippingRule>>()
    private val defaultShippingRuleObservable = PublishSubject.create<ShippingRule>()

    private val mutableAddOnsUIState = MutableStateFlow(AddOnsUIState())
    private var addOns: List<Reward> = listOf()
    private var defaultShippingRule: ShippingRule = ShippingRule.builder().build()
    private var currentShippingRule: ShippingRule = ShippingRule.builder().build()
    private var shippingSelectorIsGone: Boolean = false
    private var currentAddOnsSelections: MutableMap<Reward, Int> = mutableMapOf()
    private var shippingRules: List<ShippingRule> = listOf()
    private lateinit var projectData: ProjectData

    val addOnsUIState: StateFlow<AddOnsUIState>
        get() = mutableAddOnsUIState
            .asStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = AddOnsUIState()
            )

    init {
        currentUserReward
            .compose<Pair<Reward, List<ShippingRule>>>(
                Transformers.combineLatestPair(
                    shippingRulesObservable
                )
            )
            .switchMap { getDefaultShippingRule(it.second) }
            .subscribe {
                defaultShippingRuleObservable.onNext(it)
                defaultShippingRule = it
                currentShippingRule = it
                getAddOns(noShippingRule = false)
                viewModelScope.launch {
                    emitCurrentState()
                }
            }.addToDisposable(disposables)

        val shippingRule = getSelectedShippingRule(defaultShippingRuleObservable, currentUserReward)

        shippingRule
            .distinctUntilChanged { rule1, rule2 ->
                rule1.location()?.id() == rule2.location()?.id() && rule1.cost() == rule2.cost()
            }
            .subscribe {
                currentShippingRule = it
            }
            .addToDisposable(disposables)
    }

    fun provideProjectData(projectData: ProjectData) {
        this.projectData = projectData

        projectData.project().rewards()?.let { rewards ->
            if (rewards.isNotEmpty()) {
                val reward = rewards.firstOrNull { theOne ->
                    !theOne.isAddOn() && theOne.isAvailable() && RewardUtils.isShippable(theOne)
                }
                reward?.let {
                    apolloClient.getShippingRules(
                        reward = reward
                    ).subscribe { shippingRulesEnvelope ->
                        if (shippingRulesEnvelope.isNotNull()) shippingRulesObservable.onNext(
                            shippingRulesEnvelope.shippingRules()
                        )
                        shippingRules = shippingRulesEnvelope.shippingRules()
                    }.addToDisposable(disposables)
                }
            }
        }
    }

    private fun getAddOns(noShippingRule: Boolean) {
        viewModelScope.launch {
            apolloClient
                .getProjectAddOns(
                    slug = projectData.project().slug() ?: "",
                    locationId = currentShippingRule.location() ?: defaultShippingRule.location() ?: Location.builder().build()
                ).asFlow()
                .map { addOns ->
                    if (!addOns.isNullOrEmpty()) {
                        if (noShippingRule) {
                            this@AddOnsViewModel.addOns = addOns.filter { !RewardUtils.isShippable(it) }
                        } else {
                            this@AddOnsViewModel.addOns = addOns
                        }
                    }
                    emitCurrentState()
                }.catch {
                    // Show some error
                }.collect()
        }
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

    fun userRewardSelection(reward: Reward) {
        // A new reward has been selected, so clear out any previous addons selection
        this.currentAddOnsSelections = mutableMapOf()
        shippingSelectorIsGone =
            RewardUtils.isDigital(reward) || !RewardUtils.isShippable(reward) || RewardUtils.isLocalPickup(reward)

        viewModelScope.launch {
            emitCurrentState()
        }

        if (shippingSelectorIsGone) getAddOns(noShippingRule = true)

        this.currentUserReward.onNext(reward)
    }

    fun onShippingLocationChanged(shippingRule: ShippingRule) {
        currentShippingRule = shippingRule
        // A new location has been selected, so clear out any previous addons selection
        this.currentAddOnsSelections = mutableMapOf()

        viewModelScope.launch {
            emitCurrentState()
        }

        getAddOns(noShippingRule = false)
    }

    fun onAddOnsAddedOrRemoved(currentAddOnsSelections: MutableMap<Reward, Int>) {
        this.currentAddOnsSelections = currentAddOnsSelections
        viewModelScope.launch {
            emitCurrentState()
        }
    }

    private suspend fun emitCurrentState() {
        mutableAddOnsUIState.emit(
            AddOnsUIState(
                currentShippingRule = currentShippingRule,
                shippingSelectorIsGone = shippingSelectorIsGone,
                currentAddOnsSelection = currentAddOnsSelections,
                addOns = addOns,
                shippingRules = shippingRules
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
