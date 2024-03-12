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
import kotlinx.coroutines.launch

class CheckoutFlowViewModel(val environment: Environment) : ViewModel() {

    private val apolloClient = requireNotNull(environment.apolloClientV2())
    private val currentConfig = requireNotNull(environment.currentConfigV2())

    private val disposables = CompositeDisposable()

    val shippingRules = PublishSubject.create<List<ShippingRule>>()
    val addOns = PublishSubject.create<List<Reward>>()
    val defaultShippingRule = PublishSubject.create<ShippingRule>()
    val currentUserReward = PublishSubject.create<Reward>()
    val projectData = PublishSubject.create<ProjectData>()

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
        this.projectData.onNext(projectData)
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

    fun userCurrentRewardSelection(reward: Reward) {
        this.currentUserReward.onNext(reward)
    }

    class Factory(private val environment: Environment) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CheckoutFlowViewModel(environment) as T
        }
    }
}

