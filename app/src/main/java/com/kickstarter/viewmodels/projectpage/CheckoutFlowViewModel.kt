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

data class RewardSelectionUIState(
    val rewardList: List<Reward> = listOf(),
    val project: ProjectData = ProjectData.builder().build()
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

    private val mutableRewardSelectionUIState = MutableStateFlow(RewardSelectionUIState())
    val rewardSelectionUIState: StateFlow<RewardSelectionUIState>
        get() = mutableRewardSelectionUIState
            .asStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = RewardSelectionUIState()
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
                !RewardUtils.isDigital(it.second)
                        && RewardUtils.isShippable(it.second)
                        && !RewardUtils.isLocalPickup(
                    it.second
                )
            }
            .switchMap { getDefaultShippingRule(it.first) }
            .subscribe {
                defaultShippingRule.onNext(it)
            }.addToDisposable(disposables)
    }

    fun provideProjectData(projectData: ProjectData) {
        viewModelScope.launch {
            mutableRewardSelectionUIState.emit(
                RewardSelectionUIState(
                    rewardList = projectData.project().rewards() ?: listOf(),
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

    fun userCurrentRewardSelection(reward: Reward) {
        this.currentUserReward.onNext(reward)
    }

    private fun pledgeDataAndPledgeReason(projectData: ProjectData, reward: Reward): Pair<PledgeData, PledgeReason> {
        val pledgeReason = if (projectData.project().isBacking()) PledgeReason.UPDATE_REWARD else PledgeReason.PLEDGE
        val pledgeData = PledgeData.with(PledgeFlowContext.forPledgeReason(pledgeReason), projectData, reward)
        return Pair(pledgeData, pledgeReason)
    }

    class Factory(private val environment: Environment) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CheckoutFlowViewModel(environment) as T
        }
    }
}

