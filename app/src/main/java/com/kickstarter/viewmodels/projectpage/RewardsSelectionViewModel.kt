package com.kickstarter.viewmodels.projectpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.isBacked
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.usecases.GetShippingRulesUseCase
import com.kickstarter.viewmodels.usecases.ShippingRulesState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

data class RewardSelectionUIState(
    val selectedReward: Reward = Reward.builder().build(),
    val initialRewardIndex: Int = 0,
    val project: ProjectData = ProjectData.builder().build()
)

class RewardsSelectionViewModel(private val environment: Environment, private var shippingRulesUseCase: GetShippingRulesUseCase? = null) : ViewModel() {

    private val analytics = requireNotNull(environment.analytics())
    private val apolloClient = requireNotNull(environment.apolloClientV2())
    private val currentConfig = requireNotNull(environment.currentConfigV2()?.observable())

    private lateinit var currentProjectData: ProjectData
    private var pReason: PledgeReason? = null
    private var previousUserBacking: Backing? = null
    private var previouslyBackedReward: Reward? = null
    private var indexOfBackedReward = 0
    private var newUserReward: Reward = Reward.builder().build()
    private var selectedShippingRule: ShippingRule = ShippingRuleFactory.emptyShippingRule()

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
    val shippingUIState: StateFlow<ShippingRulesState>
        get() = mutableShippingUIState
            .asStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = ShippingRulesState(),
            )

    private val mutableFlowUIRequest = MutableSharedFlow<FlowUIState>()
    val flowUIRequest: SharedFlow<FlowUIState>
        get() = mutableFlowUIRequest
            .asSharedFlow()

    fun provideProjectData(projectData: ProjectData) {
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
        viewModelScope.launch {
            emitCurrentState()
            apolloClient.getRewardsFromProject(project.slug() ?: "")
                .asFlow()
                .combine(currentConfig.asFlow()) { rewardsList, config ->
                    if (shippingRulesUseCase == null) {
                        shippingRulesUseCase = GetShippingRulesUseCase(
                            project = projectData.project(),
                            config = config,
                            projectRewards = rewardsList,
                            viewModelScope,
                            Dispatchers.IO
                        )
                    }
                    shippingRulesUseCase?.invoke()
                    emitShippingUIState()
                }
                .catch { }
                .collect()
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
        viewModelScope.launch {
            shippingRulesUseCase?.filterBySelectedRule(shippingRule)
            emitShippingUIState()
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

    class Factory(private val environment: Environment, private var shippingRulesUseCase: GetShippingRulesUseCase? = null) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RewardsSelectionViewModel(environment = environment, shippingRulesUseCase) as T
        }
    }
}
