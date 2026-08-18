package com.kickstarter.viewmodels.projectpage

import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Config
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.RewardViewUtils
import com.kickstarter.libs.utils.extensions.isBacked
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.models.Backing
import com.kickstarter.models.Location
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingCountryLocationsWrapper
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.usecases.GetShippingRulesUseCase
import com.kickstarter.viewmodels.usecases.ShippingRulesState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow
import timber.log.Timber
import kotlin.collections.first

data class RewardSelectionUIState(
    val selectedReward: Reward = Reward.builder().build(),
    val initialRewardIndex: Int = 0,
    val project: ProjectData = ProjectData.builder().build()
)

class RewardsSelectionViewModel(private val environment: Environment, private var shippingRulesUseCase: GetShippingRulesUseCase? = null) : ViewModel() {

    private val analytics = requireNotNull(environment.analytics())
    private val apolloClient = requireNotNull(environment.apolloClientV2())
    private val currentConfig = requireNotNull(environment.currentConfigV2()?.observable())

    private val currentUserV2 = requireNotNull(environment.currentUserV2())
    private val statsigClient = requireNotNull(environment.statsigClient())

    private lateinit var currentProjectData: ProjectData
    private var pReason: PledgeReason? = null
    private var previousUserBacking: Backing? = null
    private var previouslyBackedReward: Reward? = null
    private var indexOfBackedReward = 0
    private var newUserReward: Reward = Reward.builder().build()
    private var selectedShippingRule: ShippingRule = ShippingRuleFactory.emptyShippingRule()

    private var shippingRuleUseCaseDispatcher = Dispatchers.IO

    private val mutableRewardSelectionUIState = MutableStateFlow(RewardSelectionUIState())
    val rewardSelectionUIState: StateFlow<RewardSelectionUIState>
        get() = mutableRewardSelectionUIState
            .asStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = RewardSelectionUIState(),
            )

    private val mutableShippingUIState = MutableStateFlow(ShippingRulesState())
    val shippingUIState: StateFlow<ShippingRulesState> = mutableShippingUIState.asStateFlow()

    private val mutableFlowUIRequest = MutableSharedFlow<FlowUIState>()
    val flowUIRequest: SharedFlow<FlowUIState>
        get() = mutableFlowUIRequest
            .asSharedFlow()

    fun provideProjectData(projectData: ProjectData) {
        val refreshData = if (::currentProjectData.isInitialized)
            currentProjectData.project().id() != projectData.project().id()
        else
            true

        /* In the future, if `refreshData` is false, we can probably just return here. */

        shippingRulesUseCase = null
        currentProjectData = projectData
        previousUserBacking =
            if (projectData.backing() != null) projectData.backing()
            else projectData.project().backing()
        previouslyBackedReward = getReward(previousUserBacking)
        indexOfBackedReward = indexOfBackedReward(project = projectData.project())
        pReason = when {
            previousUserBacking == null && projectData.project().isInPostCampaignPledgingPhase() == true -> PledgeReason.LATE_PLEDGE
            previousUserBacking != null -> PledgeReason.UPDATE_REWARD
            previousUserBacking == null && projectData.project().isInPostCampaignPledgingPhase() == false -> PledgeReason.PLEDGE
            else -> PledgeReason.PLEDGE
        }
        val project = projectData.project()
        val backing = projectData.backing() ?: projectData.project().backing()
        viewModelScope.launch {
            emitCurrentState()
        }

        if (!refreshData) return

        viewModelScope.launch(CoroutineExceptionHandler { _, throwable -> Timber.e(throwable, "CoroutineExceptionHandler") }) {
            mutableShippingUIState.update { previous ->
                previous.copy(loading = true)
            }

            val slug = project.slug() ?: ""
            val shouldFetchShippableCountries = slug.isNotBlank()

            Timber.d("RewardsSelectionViewModel: fetching rewards & shipping locations for project: $slug")
            val shippingLocationsDeferred = async { apolloClient.fetchShippingCountryLocations(shouldFetchShippableCountries, slug) }
            val rewardsDeferred = async { runCatching { apolloClient.getRewardsFromProject(slug).asFlow().first() } }

            val rewardsResult = rewardsDeferred.await()
            val rewards = rewardsResult
                .getOrElse { throwable ->
                    Timber.d(throwable, "Error fetching rewards for project: $slug")
                    /* There was previously no code path or user journey for dealing with a failure to fetch rewards here,
                      * so we will use an empty list of rewards in the interim. */
                    emptyList()
                }
                .let(RewardUtils::filterHasStarted)

            Timber.d("RewardsSelectionViewModel: fetched rewards for project $slug: ${rewards.joinToString(",") { "(${it.id()}, ${it.title()}, ${it.shippingPreference()})" }}")

            val itemizedRewards = rewards.filterNot { RewardUtils.isNoReward(it) }
            val allRewardsHaveRestrictedShipping =
                itemizedRewards.isNotEmpty() && itemizedRewards.all { RewardUtils.shipsToRestrictedLocations(it) }

            val shippingLocationsResult = shippingLocationsDeferred.await()
            val shippingLocationsWrapper = shippingLocationsResult.getOrElse { throwable ->
                Timber.d(throwable, "Error fetching shipping locations for project: $slug")
                ShippingCountryLocationsWrapper()
            }

            /* When fixed, we will use `shippingLocationsWrapper.shippableCountriesForProject` regardless. */
            val shippingLocations = if (allRewardsHaveRestrictedShipping) {
                Timber.d("RewardsSelectionViewModel: all rewards ship to restricted locations")
                Timber.d("RewardsSelectionViewModel: use extracted shipping locations from rewards")
                itemizedRewards.flatMap { it.shippingRules() ?: emptyList() }.mapNotNull { it.location() }.distinctBy { it.id() }
            } else {
                Timber.d("RewardsSelectionViewModel: not all rewards ship to restricted locations")
                Timber.d("RewardsSelectionViewModel: use `shippableCountriesForProject`")
                shippingLocationsWrapper.shippableCountriesForProject ?: shippingLocationsWrapper.shippingCountryLocations
            }

            Timber.d("RewardsSelectionViewModel: determined shipping locations for project $slug: ${shippingLocations.size} ${shippingLocations.joinToString(",") { "(${it.id()}, ${it.name()})" }}")

            val config = currentConfig.asFlow().first()

            Timber.d("RewardsSelectionViewModel: current config: ${config.countryCode()}")

            val defaultLocation = getDefaultLocation(config, project, shippingLocations)
            selectedShippingRule = ShippingRule.builder().location(defaultLocation).build()
            val sortedRewards = rewards.sortedByDescending { RewardViewUtils.isRewardSelectable(it, project, defaultLocation.id(), backing) }
            val repositionedRewards = repositionRewards(sortedRewards, project, defaultLocation.id(), backing)

            mutableShippingUIState.update { previous ->
                previous.copy(
                    loading = false,
                    shippingRules = shippingLocations.toShippingRules(),
                    selectedShippingRule = selectedShippingRule,
                    filteredRw = repositionedRewards
                )
            }
        }
    }

    fun onUserRewardSelection(reward: Reward) {
        viewModelScope.launch {
            pReason?.let {
                val pledgeData = PledgeData.with(
                    PledgeFlowContext.forPledgeReason(it),
                    currentProjectData,
                    reward
                )
                analytics.trackSelectRewardCTA(pledgeData)
            }
            newUserReward = reward
            emitCurrentState()

            // Show add-ons
            mutableFlowUIRequest.emit(FlowUIState(currentPage = 1, expanded = true))
        }
    }

    private fun getReward(backingObj: Backing?): Reward? {
        backingObj?.let { backing ->
            return backing.reward()?.let { reward ->
                if (backing.addOns().isNullOrEmpty()) reward
                else reward.toBuilder().hasAddons(true).build()
            } ?: RewardFactory.noReward()
        } ?: return null
    }

    private fun indexOfBackedReward(project: Project): Int {
        project.rewards()?.run {
            for ((index, reward) in withIndex()) {
                if (project.backing()?.isBacked(reward) == true) {
                    return index
                }
            }
        }
        return 0
    }

    fun sendEvent(expanded: Boolean, currentPage: Int = 0, projectData: ProjectData? = null) {
        if (expanded && currentPage == 0) {
            projectData?.let {
                analytics.trackRewardsCarouselViewed(projectData = projectData)
            } ?: {
                if (::currentProjectData.isInitialized) {
                    analytics.trackRewardsCarouselViewed(projectData = currentProjectData)
                }
            }
        }
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    private suspend fun emitShippingUIState() {
        // - collect useCase flow and update shippingUIState
        shippingRulesUseCase?.shippingRulesState?.collectLatest { shippingUseCase ->
            selectedShippingRule = shippingUseCase.selectedShippingRule
            mutableShippingUIState.emit(shippingUseCase)
        }
    }

    private suspend fun emitCurrentState() {
        mutableRewardSelectionUIState.emit(
            RewardSelectionUIState(
                initialRewardIndex = indexOfBackedReward,
                project = currentProjectData,
                selectedReward = newUserReward,
            )
        )
    }

    /**
     * The user has change the shipping location on the UI
     * @param shippingRule is the new selected location
     */
    fun selectedShippingRule(shippingRule: ShippingRule) {
        selectedShippingRule = shippingRule

        val selectedLocationId = shippingRule.location()?.id()
        val rewards = mutableShippingUIState.value.filteredRw
        val project = currentProjectData.project()
        val backing = currentProjectData.backing() ?: project.backing()
        val sortedRewards = rewards.sortedByDescending { RewardViewUtils.isRewardSelectable(it, project, selectedLocationId, backing) }
        val repositionedRewards = repositionRewards(sortedRewards, project, selectedLocationId, backing)

        mutableShippingUIState.update { previous ->
            previous.copy(
                selectedShippingRule = selectedShippingRule,
                filteredRw = repositionedRewards
            )
        }
    }

    fun getPledgeData(): Pair<PledgeData, PledgeReason>? {
        return this.currentProjectData.run {
            pReason?.let { pReason ->
                Pair(
                    PledgeData.with(
                        pledgeFlowContext = PledgeFlowContext.forPledgeReason(pReason),
                        projectData = this,
                        reward = newUserReward,
                        shippingRule = selectedShippingRule
                    ),
                    pReason
                )
            }
        }
    }

    /**
     * Used during Crowdfunding phase, while updating pledge
     * if User changes reward and had addOns backed before
     * display Alert
     */
    fun shouldShowAlert(): Boolean {
        val prevRw = previousUserBacking?.reward()
        prevRw?.let {
            if (pReason == PledgeReason.UPDATE_REWARD) {
                return !previousUserBacking?.addOns().isNullOrEmpty() && prevRw.id() != newUserReward.id()
            }
        }

        return false
    }

    /**
     * Used for testing purposes.
     *TODO:
     * Remove this method when refactoring the view model so no need to use shippingRulesUseCase = null on provideProjectData
     */
    @VisibleForTesting
    fun overrideShippingRulesUseCase(testUseCase: GetShippingRulesUseCase) {
        shippingRulesUseCase = testUseCase
        viewModelScope.launch {
            emitShippingUIState()
        }
    }

    @VisibleForTesting
    fun setShippingRuleUseCaseDispatcher(dispatcher: CoroutineDispatcher) {
        shippingRuleUseCaseDispatcher = dispatcher
    }

    private fun getDefaultLocation(
        config: Config,
        project: Project,
        shippingLocations: List<Location>,
    ): Location {
        val backingLocation =
            if (project.isBacking() && project.backing()?.locationId() != null) {
                /* While it's worth ensuring the backing location is in `shippingLocations`,
                 * we should probably just keep and use Backing.location() itself in GraphQLTransformers,
                 * instead of pulling out a separate `locationId` */
                val backingLocationId = project.backing()!!.locationId()!!
                shippingLocations.firstOrNull { it.id() == backingLocationId }
            } else {
                null
            }
        return backingLocation
            ?: shippingLocations.firstOrNull { it.country() == config.countryCode() }
            ?: shippingLocations.firstOrNull()
            ?: Location.builder().build()
    }

    private fun List<Location>.toShippingRules() =
        this.map { location -> ShippingRule.builder().location(location).build() }

    private fun indexOfFirstUnselectableReward(rewards: List<Reward>, project: Project, selectedLocationId: Long?, backing: Backing?): Int {
        return rewards.indexOfFirst { reward ->
            !RewardViewUtils.isRewardSelectable(reward, project, selectedLocationId, backing)
        }
    }

    private fun repositionRewards(rewards: List<Reward>, project: Project, selectedLocationId: Long?, backing: Backing?): List<Reward> {
        val noRewardOptionIndex = rewards.indexOfFirst { RewardUtils.isNoReward(it) }
        if (noRewardOptionIndex < 0) return rewards

        val mutableRewards = rewards.toMutableList()
        val noRewardOption = mutableRewards.removeAt(noRewardOptionIndex)
        val firstUnselectableRewardIndex =
            indexOfFirstUnselectableReward(mutableRewards, project, selectedLocationId, backing)
        if (firstUnselectableRewardIndex == -1) {
            mutableRewards.add(noRewardOption)
        } else {
            mutableRewards.add(firstUnselectableRewardIndex, noRewardOption)
        }
        return mutableRewards
    }
    
    class Factory(private val environment: Environment, private var shippingRulesUseCase: GetShippingRulesUseCase? = null) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RewardsSelectionViewModel(environment = environment, shippingRulesUseCase) as T
        }
    }

    companion object {
        private const val STATSIG_TIMEOUT = 500L
    }
}
