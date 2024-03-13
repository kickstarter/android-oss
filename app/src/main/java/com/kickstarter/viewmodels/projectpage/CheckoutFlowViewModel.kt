package com.kickstarter.viewmodels.projectpage

import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isBacked
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Backing
import com.kickstarter.models.Location
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
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
import java.util.Locale

data class FlowUIState(
    val currentPage: Int = 0,
    val expanded: Boolean = false
)

data class RewardSelectionUIState(
    val rewardList: List<Reward> = listOf(),
    val initialRewardIndex: Int = 0,
    val project: ProjectData = ProjectData.builder().build(),
    val showAlertDialog: Boolean = false
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

    lateinit var currentProjectData: ProjectData
    var previousUserBacking: Backing? = null
    var previouslyBackedReward: Reward? = null
    lateinit var newUserReward: Reward

    private val mutableRewardSelectionUIState = MutableStateFlow(RewardSelectionUIState())
    val rewardSelectionUIState: StateFlow<RewardSelectionUIState>
        get() = mutableRewardSelectionUIState
            .asStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = RewardSelectionUIState()
            )

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

    fun provideProjectData(projectData: ProjectData) {
        currentProjectData = projectData
        previousUserBacking = projectData.backing()
        previouslyBackedReward = getReward(previousUserBacking)
        val indexOfBackedReward = indexOfBackedReward(project = projectData.project())
        viewModelScope.launch {
            mutableRewardSelectionUIState.emit(
                RewardSelectionUIState(
                    rewardList = projectData.project().rewards() ?: listOf(),
                    initialRewardIndex = indexOfBackedReward,
                    project = projectData
                )
            )

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
            val pledgeDataAndReason = pledgeDataAndPledgeReason(currentProjectData, reward)
            newUserReward = pledgeDataAndReason.first.reward()

            when (pledgeDataAndReason.second) {
                PledgeReason.UPDATE_REWARD -> {
                    if (previouslyBackedReward?.hasAddons() == true && !newUserReward.hasAddons())
                    // Show warning to user
                        mutableRewardSelectionUIState.emit(
                            RewardSelectionUIState(
                                rewardList = currentProjectData.project().rewards() ?: listOf(),
                                project = currentProjectData,
                                showAlertDialog = true
                            )
                        )

                    if (previouslyBackedReward?.hasAddons() == false && !newUserReward.hasAddons())
                    // Go to confirm page
                        mutableFlowUIState.emit(FlowUIState(currentPage = 2, expanded = true))

                    if (previouslyBackedReward?.hasAddons() == true && newUserReward.hasAddons()) {
                        if (differentShippingTypes(previouslyBackedReward, newUserReward))
                        // Show warning to user
                            mutableRewardSelectionUIState.emit(
                                RewardSelectionUIState(
                                    rewardList = currentProjectData.project().rewards() ?: listOf(),
                                    project = currentProjectData,
                                    showAlertDialog = true
                                )
                            )
                        // Go to add-ons
                        else mutableFlowUIState.emit(FlowUIState(currentPage = 1, expanded = true))
                    }

                    if (previouslyBackedReward?.hasAddons() == false && newUserReward.hasAddons()) {
                        // Go to add-ons
                        mutableFlowUIState.emit(FlowUIState(currentPage = 1, expanded = true))
                    }
                }

                PledgeReason.PLEDGE -> {
                    if (newUserReward.hasAddons())
                    // Show add-ons
                        mutableFlowUIState.emit(FlowUIState(currentPage = 1, expanded = true))
                    else
                    // Show confirm page
                        mutableFlowUIState.emit(FlowUIState(currentPage = 2, expanded = true))
                }

                else -> {
                }
            }
        }
    }

    fun onRewardCarouselAlertClicked(wasPositive: Boolean) {
        viewModelScope.launch {
            mutableRewardSelectionUIState.emit(
                RewardSelectionUIState(
                    rewardList = currentProjectData.project().rewards() ?: listOf(),
                    project = currentProjectData,
                    showAlertDialog = false
                )
            )
            if (wasPositive) {
                if (newUserReward.hasAddons()) {
                    // Go to add-ons
                    mutableFlowUIState.emit(FlowUIState(currentPage = 1, expanded = true))
                } else {
                    // Show confirm page
                    mutableFlowUIState.emit(FlowUIState(currentPage = 2, expanded = true))
                }
            }
        }
    }

    fun onAddOnsContinueClicked() {
        viewModelScope.launch {
            // Show confirm page
            mutableFlowUIState.emit(FlowUIState(currentPage = 2, expanded = true))
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

    fun onBackPressed(currentPage: Int) {
        viewModelScope.launch {
            when (currentPage) {
                3 -> {
                    mutableFlowUIState.emit(FlowUIState(currentPage = 2, expanded = true))
                }

                2 -> {
                    if (newUserReward.hasAddons()) {
                        mutableFlowUIState.emit(FlowUIState(currentPage = 1, expanded = true))
                    } else {
                        mutableFlowUIState.emit(FlowUIState(currentPage = 0, expanded = true))
                    }
                }

                1 -> {
                    mutableFlowUIState.emit(FlowUIState(currentPage = 0, expanded = true))
                }

                0 -> {
                    mutableFlowUIState.emit(FlowUIState(currentPage = 0, expanded = false))
                }
            }
        }
    }

    fun onBackThisProjectClicked() {
        viewModelScope.launch {
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
