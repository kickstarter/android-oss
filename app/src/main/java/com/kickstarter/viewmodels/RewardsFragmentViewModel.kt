package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isBacked
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.usecases.SendThirdPartyEventUseCaseV2
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.Locale

class RewardsFragmentViewModel {
    interface Inputs {
        /** Configure with current [ProjectData]. */
        fun configureWith(projectData: ProjectData)

        /** Call when a reward is clicked.  */
        fun rewardClicked(reward: Reward)

        /** Call when the Alert button has been pressed  */
        fun alertButtonPressed()

        fun isExpanded(state: Boolean?)
    }

    interface Outputs {
        /**  Emits the position of the backed reward. */
        fun backedRewardPosition(): Observable<Int>

        /** Emits the current [ProjectData]. */
        fun projectData(): Observable<ProjectData>

        /** Emits the count of the current project's rewards. */
        fun rewardsCount(): Observable<Int>

        /** Emits when we should show the [com.kickstarter.ui.fragments.PledgeFragment].  */
        fun showPledgeFragment(): Observable<Pair<PledgeData, PledgeReason>>

        /** Emits when we should show the [com.kickstarter.ui.fragments.BackingAddOnsFragment].  */
        fun showAddOnsFragment(): Observable<Pair<PledgeData, PledgeReason>>

        /** Emits if we have to show the alert in case any AddOns selection could be lost. */
        fun showAlert(): Observable<Pair<PledgeData, PledgeReason>>
    }

    class RewardsFragmentViewModel(val environment: Environment) : ViewModel(), Inputs, Outputs {

        private val isExpanded = PublishSubject.create<Boolean>()
        private val projectDataInput = BehaviorSubject.create<ProjectData>()
        private val rewardClicked = PublishSubject.create<Pair<Reward, Boolean>>()
        private val alertButtonPressed = PublishSubject.create<Unit>()

        private val backedRewardPosition = PublishSubject.create<Int>()
        private val projectData = BehaviorSubject.create<ProjectData>()
        private val rewardsCount = BehaviorSubject.create<Int>()
        private val pledgeData = PublishSubject.create<Pair<PledgeData, PledgeReason>>()
        private val showPledgeFragment = PublishSubject.create<Pair<PledgeData, PledgeReason>>()
        private val showAddOnsFragment = PublishSubject.create<Pair<PledgeData, PledgeReason>>()
        private val showAlert = PublishSubject.create<Pair<PledgeData, PledgeReason>>()

        private val sharedPreferences = requireNotNull(environment.sharedPreferences())
        private val ffClient = requireNotNull(environment.featureFlagClient())
        private val apolloClient = requireNotNull(environment.apolloClientV2())
        private val currentUser = requireNotNull(environment.currentUserV2())
        private val analyticEvents = requireNotNull(environment.analytics())

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        val onThirdPartyEventSent = BehaviorSubject.create<Boolean>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val disposables = CompositeDisposable()

        /***
         * setState method brought from BaseFragment.java
         */
        fun setState(state: Boolean?) {
            state?.let {
                this.isExpanded.onNext(it)
            }
        }
        init {

            this.isExpanded
                .filter { it }
                .compose(combineLatestPair(this.projectDataInput))
                .filter { it.second.isNotNull() }
                .map { it.second }
                .subscribe {
                    this.analyticEvents.trackRewardsCarouselViewed(it)
                }
                .addToDisposable(disposables)

            this.projectDataInput
                .filter { sortAndFilterRewards(it).isNotNull() }
                .map { sortAndFilterRewards(it) }
                .subscribe { this.projectData.onNext(it) }
                .addToDisposable(disposables)

            val project = this.projectData
                .filter { it.project().isNotNull() }
                .map { it.project() }

            this.isExpanded
                .filter { it }
                .switchMap {
                    SendThirdPartyEventUseCaseV2(sharedPreferences, ffClient)
                        .sendThirdPartyEvent(
                            project = project,
                            apolloClient = apolloClient,
                            currentUser = currentUser,
                            eventName = ThirdPartyEventValues.EventName.SCREEN_VIEW,
                            firebaseScreen = ThirdPartyEventValues.ScreenName.REWARDS.value,
                            firebasePreviousScreen = ThirdPartyEventValues.ScreenName.PROJECT.value,
                        )
                }
                .compose(Transformers.neverErrorV2())
                .subscribe {
                    onThirdPartyEventSent.onNext(
                        it.first
                    )
                }
                .addToDisposable(disposables)

            project
                .filter { it.isBacking() && indexOfBackedReward(it).isNotNull() }
                .map { indexOfBackedReward(it) }
                .distinctUntilChanged()
                .subscribe { this.backedRewardPosition.onNext(it) }
                .addToDisposable(disposables)

            val backedReward = project
                .filter { it.isBacking() }
                .map { it.backing()?.let { backing -> getReward(backing) } }
                .filter { it.isNotNull() }
                .map { requireNotNull(it) }

            val defaultRewardClicked = Pair(Reward.builder().id(0L).minimum(0.0).build(), false)

            Observable
                .combineLatest(this.rewardClicked.startWith(defaultRewardClicked), this.projectDataInput) { rewardPair, projectData ->
                    if (!rewardPair.second) {
                        return@combineLatest Unit
                    } else {
                        return@combineLatest pledgeDataAndPledgeReason(projectData, rewardPair.first)
                    }
                }
                .filter { it.isNotNull() && it is Pair<*, *> && it.first is PledgeData && it.second is PledgeReason }
                .map { requireNotNull(it as Pair<PledgeData, PledgeReason>) }
                .subscribe {
                    val pledgeAndData = it
                    val newRw = it.first.reward()
                    val reason = it.second

                    when (reason) {
                        PledgeReason.PLEDGE -> {
                            if (newRw.hasAddons())
                                this.showAddOnsFragment.onNext(pledgeAndData)
                            else
                                this.pledgeData.onNext(pledgeAndData)
                        }
                        else -> {}
                    }
                    this.rewardClicked.onNext(defaultRewardClicked)
                }
                .addToDisposable(disposables)

            Observable
                .combineLatest(this.rewardClicked.startWith(defaultRewardClicked), this.projectDataInput, backedReward) { rewardPair, projectData, backedReward ->
                    if (!rewardPair.second) {
                        return@combineLatest Unit
                    } else {
                        return@combineLatest Pair(pledgeDataAndPledgeReason(projectData, rewardPair.first), backedReward)
                    }
                }
                .filter { it.isNotNull() && it is Pair<*, *> && it.first is Pair<*, *> && it.second is Reward } // todo extract to a function
                .map { requireNotNull(it as Pair<Pair<PledgeData, PledgeReason>, Reward>) }
                .subscribe {
                    val pledgeAndData = it.first
                    val newRw = it.first.first.reward()
                    val prevRw = it.second
                    val reason = it.first.second

                    when (reason) {
                        PledgeReason.UPDATE_REWARD -> {
                            if (prevRw.hasAddons() && !newRw.hasAddons())
                                this.showAlert.onNext(pledgeAndData)

                            if (!prevRw.hasAddons() && !newRw.hasAddons())
                                this.pledgeData.onNext(pledgeAndData)

                            if (prevRw.hasAddons() && newRw.hasAddons()) {
                                if (differentShippingTypes(prevRw, newRw)) this.showAlert.onNext(it.first)
                                else this.showAddOnsFragment.onNext(pledgeAndData)
                            }

                            if (!prevRw.hasAddons() && newRw.hasAddons()) {
                                this.showAddOnsFragment.onNext(pledgeAndData)
                            }
                        }
                        else -> {}
                    }
                    this.rewardClicked.onNext(defaultRewardClicked)
                }
                .addToDisposable(disposables)

            project
                .map { it.rewards()?.size ?: 0 }
                .subscribe { this.rewardsCount.onNext(it) }
                .addToDisposable(disposables)

            this.showAlert
                .compose<Pair<PledgeData, PledgeReason>>(takeWhenV2(alertButtonPressed))
                .subscribe {
                    if (it.first.reward().hasAddons())
                        this.showAddOnsFragment.onNext(it)
                    else this.pledgeData.onNext(it)
                }
                .addToDisposable(disposables)

            this.pledgeData
                .distinctUntilChanged()
                .subscribe {
                    this.showPledgeFragment.onNext(it)
                }
                .addToDisposable(disposables)
        }

        private fun sortAndFilterRewards(pData: ProjectData): ProjectData {
            val startedRewards = pData.project().rewards()?.filter { RewardUtils.hasStarted(it) }
            val sortedRewards = startedRewards?.filter { RewardUtils.isAvailable(pData.project(), it) }?.toMutableList() ?: mutableListOf()
            val unavailableRewards = startedRewards?.filter { !RewardUtils.isAvailable(pData.project(), it) }?.toMutableList()

            unavailableRewards?.let { sortedRewards.addAll(it) }

            val modifiedProject = pData.project().toBuilder().rewards(sortedRewards).build()
            return pData.toBuilder()
                .project(modifiedProject)
                .build()
        }

        private fun getReward(backingObj: Backing): Reward {
            return backingObj.reward()?.let { rw ->
                if (backingObj.addOns().isNullOrEmpty()) rw
                else rw.toBuilder().hasAddons(true).build()
            } ?: RewardFactory.noReward()
        }

        private fun differentShippingTypes(newRW: Reward, backedRW: Reward): Boolean {
            return if (newRW.id() == backedRW.id()) false
            else {
                (
                    newRW.shippingType()?.lowercase(Locale.getDefault())
                        ?: ""
                    ) != (
                    backedRW.shippingType()
                        ?.lowercase(Locale.getDefault()) ?: ""
                    )
            }
        }

        private fun pledgeDataAndPledgeReason(projectData: ProjectData, reward: Reward): Pair<PledgeData, PledgeReason> {
            val pledgeReason = if (projectData.project().isBacking()) PledgeReason.UPDATE_REWARD else PledgeReason.PLEDGE
            val pledgeData = PledgeData.with(PledgeFlowContext.forPledgeReason(pledgeReason), projectData, reward)
            return Pair(pledgeData, pledgeReason)
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

        override fun isExpanded(state: Boolean?) {
            state?.let {
                this.isExpanded.onNext(it)
            }
        }
        override fun configureWith(projectData: ProjectData) {
            this.projectDataInput.onNext(projectData)
        }

        override fun rewardClicked(reward: Reward) {
            this.rewardClicked.onNext(Pair(reward, true))
        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }

        override fun alertButtonPressed() = this.alertButtonPressed.onNext(Unit)

        override fun backedRewardPosition(): Observable<Int> = this.backedRewardPosition

        override fun projectData(): Observable<ProjectData> = this.projectData

        override fun rewardsCount(): Observable<Int> = this.rewardsCount

        override fun showPledgeFragment(): Observable<Pair<PledgeData, PledgeReason>> = this.showPledgeFragment

        override fun showAddOnsFragment(): Observable<Pair<PledgeData, PledgeReason>> = this.showAddOnsFragment

        override fun showAlert(): Observable<Pair<PledgeData, PledgeReason>> = this.showAlert
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RewardsFragmentViewModel(environment) as T
        }
    }
}
