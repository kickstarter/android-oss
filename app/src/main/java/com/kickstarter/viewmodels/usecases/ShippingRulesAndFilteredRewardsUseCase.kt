package com.kickstarter.viewmodels.usecases

import com.kickstarter.libs.Config
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.extensions.getDefaultLocationFrom
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.models.Location
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.services.ApolloClientTypeV2
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

data class ShippingRulesState(
    val shippingRules: List<ShippingRule> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val selectedShippingRule: ShippingRule = ShippingRuleFactory.usShippingRule()
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
class ShippingRulesAndFilteredRewardsUseCase(
    private val apolloClient: ApolloClientTypeV2,
    private val project: Project,
    private val config: Config?,
    private val scope: CoroutineScope,
    // - IO dispatcher for network operations to avoid blocking main thread
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val allAvailableRulesForProject = mutableMapOf<Long, ShippingRule>()

    // - Will contain Limited shipping rewards + Only 1 reward that ships globally
    private var rewardsByShippingType: List<Reward>
    init {

        // To avoid duplicates insert reward.id as key
        val rewardsByShippingType = mutableMapOf<Long, Reward>()

        // Get first reward with unrestricted shipping preference, shipping rules for this type of rewards is All of them
        project.rewards()?.filter { RewardUtils.shipsWorldwide(reward = it) }?.firstOrNull()?.let {
            rewardsByShippingType.put(it.id(), it)
        }

        // In case there is no unrestricted preference need to get restricted and local rewards
        if (rewardsByShippingType.isEmpty()) {
            project.rewards()?.filter {
                RewardUtils.shipsToRestrictedLocations(reward = it)
            }?.forEach {
                rewardsByShippingType[it.id()] = it
            }
        }

        this.rewardsByShippingType = rewardsByShippingType.values.toList()
    }

    // - Do not expose mutable states
    private val _mutableShippingRules =
        MutableStateFlow(ShippingRulesState())

    // - Do not expose mutable states
    private val _filteredRewards =
        MutableStateFlow(emptyList<Reward>())

    /**
     * Exposes result of this use case
     */
    val shippingRulesState: Flow<ShippingRulesState>
        get() = _mutableShippingRules

    val filteredRewards: Flow<List<Reward>>
        get() = _filteredRewards


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
        val filteredRewards = mutableListOf<Reward>()
        val locationId = rule.location()?.id() ?: 0
        val isIsValidRule = allAvailableShippingRules[locationId]

        // Rule is available
        rewards.map { rw ->
            if (RewardUtils.shipsWorldwide(rw)) {
                filteredRewards.add(rw)
            }

            if (RewardUtils.isNoReward(rw)) {
                filteredRewards.add(rw)
            }

            if (RewardUtils.isLocalPickup(rw)) {
                filteredRewards.add(rw)
            }

            // - If shipping is restricted, make sure the reward is able to ship to selected rule
            if (RewardUtils.shipsToRestrictedLocations(rw)) {
                if (isIsValidRule != null) {
                    rw.shippingRules()?.map {
                        if (it.location()?.id() == locationId) {
                            filteredRewards.add(rw)
                        }
                    }
                }
            }
        }

        _filteredRewards.emit(filteredRewards.toList())
    }

    operator fun invoke() {
        if (rewardsByShippingType.isNotEmpty()) {
            scope.launch(dispatcher) {
                _mutableShippingRules.emit(ShippingRulesState(loading = true))
                rewardsByShippingType.forEachIndexed { index, reward ->

                    if (RewardUtils.shipsToRestrictedLocations(reward)) {
                        reward.shippingRules()?.map {
                            allAvailableRulesForProject.put(
                                requireNotNull(
                                    it.location()?.id()
                                ), it
                            )
                        }
                    }
                    if (RewardUtils.shipsWorldwide(reward)) {
                        getGlobalShippingRulesForReward(reward, allAvailableRulesForProject)
                    }

                    // - Filter if all shipping rules for all rewards have been collected
                    if (index == rewardsByShippingType.size - 1) {
                        val initialShippingRule = getDefaultShippingRule(
                            allAvailableRulesForProject,
                            project
                        )
                        _mutableShippingRules.emit(
                            ShippingRulesState(
                                shippingRules = allAvailableRulesForProject.values.toList(),
                                loading = false,
                                selectedShippingRule = initialShippingRule
                            )
                        )

                        filterRewardsByLocation(allAvailableRulesForProject, initialShippingRule, project.rewards() ?: emptyList())
                    }
                }
            }
        }
    }

    /**
     * Should be called every time the user selects a new shipping Rule
     */
    fun filterRewardsByLocation(rule: ShippingRule) {
        scope.launch(dispatcher) {
            filterRewardsByLocation(allAvailableRulesForProject, rule, project.rewards() ?: emptyList())
        }
    }

    private suspend fun getGlobalShippingRulesForReward(
        reward: Reward,
        shippingRules: MutableMap<Long, ShippingRule>
    ) {
        apolloClient.getShippingRules(reward)
            .asFlow()
            .map { rulesEnvelope ->
                rulesEnvelope.shippingRules()?.map { rule ->
                    rule?.let {
                        shippingRules.put(
                            requireNotNull(
                                it.location()?.id()
                            ), it
                        )
                    }
                }
            }
            .catch { throwable ->
                _mutableShippingRules.emit(
                    ShippingRulesState(
                        loading = false,
                        error = throwable.message
                    )
                )
            }.collect()
    }

    /**
     * In case the project is backing, return the backed shippingRule
     * otherwise return the config default shippingRule
     */
    private fun getDefaultShippingRule(
        shippingRules: MutableMap<Long, ShippingRule>,
        project: Project
    ): ShippingRule =
        if (project.isBacking()) ShippingRule.builder()
            .apply {
                val locationId = project.backing()?.locationId() ?: 0L
                val locationName = project.backing()?.locationName() ?: ""

                this.location(Location.Builder().id(locationId).name(locationName).displayableName(locationName).build())
            }
            .build()
        else config?.getDefaultLocationFrom(shippingRules.values.toList()) ?: ShippingRule.builder()
            .build()
}
