package com.kickstarter.viewmodels.usecases

import com.kickstarter.libs.Config
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.extensions.getDefaultLocationFrom
import com.kickstarter.libs.utils.extensions.isAllowedToPledge
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Location
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

data class ShippingRulesState(
    val shippingRules: List<ShippingRule> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val selectedShippingRule: ShippingRule = ShippingRule.builder().build(),
    val filteredRw: List<Reward> = emptyList()
)

/**
 * Will provide ShippingRulesState where:
 * `shippingRules` is the list of available shipping rules for a given project
 * `selectedShippingRule` will be the initial default shipping rule for a given configuration
 *  `error/loading` states for internal networking calls
 *
 *  Should be provided with:
 *  @param scope
 *  @param dispatcher
 *
 *  As the UseCase is lifecycle agnostic and is scoped to the class that uses it.
 */
class GetShippingRulesUseCase(
    private val project: Project,
    private val config: Config?,
    private val projectRewards: List<Reward> = emptyList(),
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val filteredRewards = mutableListOf<Reward>()
    private var defaultShippingRule = ShippingRule.builder().build()
    private var rewardsByShippingType: List<Reward>
    private val allAvailableRulesForProject = mutableMapOf<Long, ShippingRule>()

    init {

        // To avoid duplicates insert reward.id as key
        val rewardsToExtractLocation = mutableMapOf<Long, Reward>()

        // Get first reward with unrestricted shipping preference, when quering `getShippingRules` will return ALL available locations, no need to query more rewards locations
        projectRewards.filter { RewardUtils.shipsWorldwide(reward = it) }?.firstOrNull()?.let {
            rewardsToExtractLocation.put(it.id(), it)
        }

        // In case there is no unrestricted preference need to get restricted and local rewards, to query their specific locations
        if (rewardsToExtractLocation.isEmpty()) {
            projectRewards.filter {
                RewardUtils.shipsToRestrictedLocations(reward = it)
            }.forEach {
                rewardsToExtractLocation[it.id()] = it
            }
        }

        this.rewardsByShippingType = rewardsToExtractLocation.values.toList()
    }

    // - Do not expose mutable states
    private val _mutableShippingRules =
        MutableStateFlow(ShippingRulesState())

    /**
     * Exposes result of this use case
     */
    val shippingRulesState: Flow<ShippingRulesState>
        get() = _mutableShippingRules

    // - IO dispatcher for network operations to avoid blocking main thread
    operator fun invoke() {
        scope.launch(dispatcher) {
            val avShipMap = allAvailableRulesForProject
            emitCurrentState(isLoading = true)

            if (rewardsByShippingType.isNotEmpty() && project.isAllowedToPledge()) {
                rewardsByShippingType.forEachIndexed { index, reward ->

                    if (RewardUtils.shipsToRestrictedLocations(reward) || RewardUtils.shipsWorldwide(reward)) {
                        reward.shippingRules()?.map {
                            avShipMap.put(
                                requireNotNull(
                                    it.location()?.id()
                                ),
                                it
                            )
                        }
                    }

                    // - Filter rewards once all shipping rules have been collected
                    if (index == rewardsByShippingType.size - 1) {
                        defaultShippingRule = getDefaultShippingRule(
                            avShipMap,
                            project
                        )
                        filterRewardsByLocation(avShipMap, defaultShippingRule, projectRewards)
                    }
                }
            }
            // - all rewards digital
            if (rewardsByShippingType.isEmpty() && project.isAllowedToPledge()) {
                // - All rewards are digital, all rewards must be available
                filteredRewards.clear()
                filteredRewards.addAll(projectRewards)
                emitCurrentState(isLoading = false)
            }

            // - Just displaying all rewards available or not, project no collecting any longer
            if (!project.isAllowedToPledge()) {
                filteredRewards.clear()
                filteredRewards.addAll(project.rewards() ?: emptyList())
                emitCurrentState(isLoading = false)
            }
        }
    }

    fun getScope() = this.scope
    fun getDispatcher() = this.dispatcher

    fun filterBySelectedRule(shippingRule: ShippingRule) {
        scope.launch(dispatcher) {
            defaultShippingRule = shippingRule
            emitCurrentState(isLoading = true)
            delay(500) // Added delay due to the filtering happening too fast for the user to perceive the loading state
            filterRewardsByLocation(allAvailableRulesForProject, shippingRule, projectRewards)
        }
    }

    private suspend fun emitCurrentState(isLoading: Boolean, errorMessage: String? = null) {
        _mutableShippingRules.emit(
            ShippingRulesState(
                shippingRules = allAvailableRulesForProject.values.toList(),
                loading = isLoading,
                selectedShippingRule = defaultShippingRule,
                error = errorMessage,
                filteredRw = filteredRewards
            )
        )
    }

    /**
     * Check if the given @param rule is available in the list
     * of @param allAvailableShippingRules for this project.
     *
     * In case it is available, return only those rewards able to ship to
     * the selected rule
     */
    private suspend fun filterRewardsByLocation(
        allAvailableShippingRules: MutableMap<Long, ShippingRule>,
        rule: ShippingRule,
        rewards: List<Reward>
    ) {
        filteredRewards.clear()
        val locationId = rule.location()?.id() ?: 0
        val isIsValidRule = allAvailableShippingRules[locationId]

        rewards.map { rw ->
            if (RewardUtils.shipsWorldwide(rw) && rw.isAvailable()) {
                filteredRewards.add(rw)
            }

            if (RewardUtils.isNoReward(rw)) {
                filteredRewards.add(rw)
            }

            if (RewardUtils.isLocalPickup(rw) && rw.isAvailable()) {
                filteredRewards.add(rw)
            }

            if (RewardUtils.isDigital(rw) && rw.isAvailable()) {
                filteredRewards.add(rw)
            }

            // - If shipping is restricted, make sure the reward is able to ship to selected rule
            if (RewardUtils.shipsToRestrictedLocations(rw) && rw.isAvailable()) {
                if (isIsValidRule != null) {
                    rw.shippingRules()?.map {
                        if (it.location()?.id() == locationId) {
                            filteredRewards.add(rw)
                        }
                    }
                }
            }
        }

        emitCurrentState(isLoading = false)
    }

    /**
     * In case the project is backing, return the backed shippingRule
     * otherwise return the config default shippingRule
     */
    private fun getDefaultShippingRule(
        shippingRules: MutableMap<Long, ShippingRule>,
        project: Project
    ): ShippingRule =
        if (project.isBacking() && project.backing()?.location().isNotNull()) ShippingRule.builder()
            .apply {
                val backing = project.backing()
                val locationId = project.backing()?.locationId() ?: 0L
                this.id(locationId)
                backing?.reward()?.let {
                    if (RewardUtils.shipsToRestrictedLocations(it)) {
                        val rule = backing?.reward()?.shippingRules()
                            ?.first { it.location()?.id() == locationId }
                        this.location(rule?.location())
                        this.id(rule?.id())
                        this.cost(rule?.cost() ?: 0.0)
                    }
                    if (RewardUtils.shipsWorldwide(it)) {
                        val locationName = project.backing()?.locationName() ?: ""
                        this.location(Location.Builder().id(locationId).name(locationName).displayableName(locationName).build())
                        this.cost(it.shippingRules()?.first()?.cost() ?: 0.0)
                    }
                }
            }
            .build()
        else config?.getDefaultLocationFrom(shippingRules.values.toList()) ?: ShippingRule.builder()
            .build()
}
