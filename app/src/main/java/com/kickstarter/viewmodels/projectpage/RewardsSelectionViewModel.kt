package com.kickstarter.viewmodels.projectpage

import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.utils.extensions.isBacked
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

data class RewardSelectionUIState(
    val rewardList: List<Reward> = listOf(),
    val initialRewardIndex: Int = 0,
    val project: ProjectData = ProjectData.builder().build(),
    val showAlertDialog: Boolean = false
)

class RewardsSelectionViewModel : ViewModel() {

    private lateinit var currentProjectData: ProjectData
    private var previousUserBacking: Backing? = null
    private var previouslyBackedReward: Reward? = null
    private lateinit var newUserReward: Reward
    private var indexOfBackedReward = 0

    private val mutableRewardSelectionUIState = MutableStateFlow(RewardSelectionUIState())
    val rewardSelectionUIState: StateFlow<RewardSelectionUIState>
        get() = mutableRewardSelectionUIState
            .asStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = RewardSelectionUIState(),
            )

    private val mutableFlowUIRequest = MutableSharedFlow<FlowUIState>()
    val flowUIRequest: SharedFlow<FlowUIState>
        get() = mutableFlowUIRequest
            .asSharedFlow()

    fun provideProjectData(projectData: ProjectData) {
        currentProjectData = projectData
        previousUserBacking = projectData.backing()
        previouslyBackedReward = getReward(previousUserBacking)
        indexOfBackedReward = indexOfBackedReward(project = projectData.project())
        viewModelScope.launch {
            emitCurrentState()
        }
    }

    fun onUserRewardSelection(reward: Reward) {
        viewModelScope.launch {
            val pledgeDataAndReason = pledgeDataAndPledgeReason(currentProjectData, reward)
            newUserReward = pledgeDataAndReason.first.reward()

            when (pledgeDataAndReason.second) {
                PledgeReason.UPDATE_REWARD -> {
                    if (previouslyBackedReward?.hasAddons() == true && !newUserReward.hasAddons())
                    // Show warning to user
                        emitCurrentState(showAlertDialog = true)

                    if (previouslyBackedReward?.hasAddons() == false && !newUserReward.hasAddons())
                    // Go to confirm page
                        mutableFlowUIRequest.emit(FlowUIState(currentPage = 2, expanded = true))

                    if (previouslyBackedReward?.hasAddons() == true && newUserReward.hasAddons()) {
                        if (differentShippingTypes(previouslyBackedReward, newUserReward))
                        // Show warning to user
                            emitCurrentState(showAlertDialog = true)
                        // Go to add-ons
                        else mutableFlowUIRequest.emit(FlowUIState(currentPage = 1, expanded = true))
                    }

                    if (previouslyBackedReward?.hasAddons() == false && newUserReward.hasAddons()) {
                        // Go to add-ons
                        mutableFlowUIRequest.emit(FlowUIState(currentPage = 1, expanded = true))
                    }
                }

                PledgeReason.PLEDGE -> {
                    if (newUserReward.hasAddons())
                    // Show add-ons
                        mutableFlowUIRequest.emit(FlowUIState(currentPage = 1, expanded = true))
                    else
                    // Show confirm page
                        mutableFlowUIRequest.emit(FlowUIState(currentPage = 2, expanded = true))
                }

                else -> {
                }
            }
        }
    }

    fun onRewardCarouselAlertClicked(wasPositive: Boolean) {
        viewModelScope.launch {
            emitCurrentState()
            if (wasPositive) {
                if (newUserReward.hasAddons()) {
                    // Go to add-ons
                    mutableFlowUIRequest.emit(FlowUIState(currentPage = 1, expanded = true))
                } else {
                    // Show confirm page
                    mutableFlowUIRequest.emit(FlowUIState(currentPage = 2, expanded = true))
                }
            }
        }
    }

    private fun pledgeDataAndPledgeReason(
        projectData: ProjectData,
        reward: Reward
    ): Pair<PledgeData, PledgeReason> {
        val pledgeReason =
            if (projectData.project().isBacking()) PledgeReason.UPDATE_REWARD
            else PledgeReason.PLEDGE
        val pledgeData =
            PledgeData.with(PledgeFlowContext.forPledgeReason(pledgeReason), projectData, reward)
        return Pair(pledgeData, pledgeReason)
    }

    private fun differentShippingTypes(newRW: Reward?, backedRW: Reward): Boolean {
        return if (newRW == null) false
        else if (newRW.id() == backedRW.id()) false
        else {
            (newRW.shippingType()?.lowercase(Locale.getDefault()) ?: "") != (backedRW.shippingType()?.lowercase(Locale.getDefault()) ?: "")
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

    private suspend fun emitCurrentState(
        showAlertDialog: Boolean = false
    ) {
        mutableRewardSelectionUIState.emit(
            RewardSelectionUIState(
                rewardList = currentProjectData.project().rewards() ?: listOf(),
                initialRewardIndex = indexOfBackedReward,
                project = currentProjectData,
                showAlertDialog = showAlertDialog,
            )
        )
    }

    class Factory :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RewardsSelectionViewModel() as T
        }
    }
}
