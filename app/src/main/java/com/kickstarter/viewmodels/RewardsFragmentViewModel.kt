package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.libs.utils.BackingUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.fragments.RewardsFragment
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class RewardsFragmentViewModel {
    interface Inputs {
        /** Configure with current [ProjectData]. */
        fun configureWith(projectData: ProjectData)

        /** Call when a reward is clicked.  */
        fun rewardClicked(reward: Reward)

        /** Call when the Alert button has been pressed  */
        fun alertButtonPressed()
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

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<RewardsFragment>(environment), Inputs, Outputs {

        private val projectDataInput = PublishSubject.create<ProjectData>()
        private val rewardClicked = PublishSubject.create<Pair<Reward, Boolean>>()
        private val alertButtonPressed = PublishSubject.create<Void>()

        private val backedRewardPosition = PublishSubject.create<Int>()
        private val projectData = BehaviorSubject.create<ProjectData>()
        private val rewardsCount = BehaviorSubject.create<Int>()
        private val showPledgeFragment = PublishSubject.create<Pair<PledgeData, PledgeReason>>()
        private val showAddOnsFragment = PublishSubject.create<Pair<PledgeData, PledgeReason>>()
        private val showAlert = PublishSubject.create<Pair<PledgeData, PledgeReason>>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            this.projectDataInput
                    .compose(bindToLifecycle())
                    .subscribe(this.projectData)

            val project = this.projectDataInput
                    .map { it.project() }
            
            project
                    .filter { it.isBacking }
                    .map { indexOfBackedReward(it) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.backedRewardPosition)

            val backedReward = project
                    .map { it.backing()?.let { backing -> getReward(backing) } }
                    .filter { ObjectUtils.isNotNull(it) }
                    .map { requireNotNull(it) }

            val defaultRewardClicked = Pair(Reward.builder().id(0).minimum(0.0).build(), false)

            Observable
                    .combineLatest(this.rewardClicked.startWith(defaultRewardClicked), this.projectDataInput) { rewardPair, projectData ->
                        if (!rewardPair.second) {
                            return@combineLatest null
                        } else {
                            return@combineLatest pledgeDataAndPledgeReason(projectData, rewardPair.first)
                        }
                    }
                    .filter { ObjectUtils.isNotNull(it) }
                    .map { requireNotNull(it) }
                    .compose(bindToLifecycle())
                    .subscribe {
                        val pledgeAndData = it
                        val newRw = it.first.reward()
                        val reason = it.second

                        when(reason) {
                            PledgeReason.PLEDGE -> {
                                if (newRw.hasAddons())
                                    this.showAddOnsFragment.onNext(pledgeAndData)
                                else
                                    this.showPledgeFragment.onNext(pledgeAndData)
                            }
                        }
                        this.rewardClicked.onNext(defaultRewardClicked)
                    }

            Observable
                    .combineLatest(this.rewardClicked.startWith(defaultRewardClicked), this.projectDataInput, backedReward) { rewardPair, projectData, backedReward ->
                        if (!rewardPair.second) {
                            return@combineLatest null
                        } else {
                            return@combineLatest Pair(pledgeDataAndPledgeReason(projectData, rewardPair.first), backedReward)
                        }
                    }
                    .filter { ObjectUtils.isNotNull(it) }
                    .map { requireNotNull(it) }
                    .compose(bindToLifecycle())
                    .subscribe {
                        val pledgeAndData = it.first
                        val newRw = it.first.first.reward()
                        val prevRw = it.second
                        val reason = it.first.second

                        when(reason) {
                            PledgeReason.UPDATE_REWARD -> {
                                if (prevRw.hasAddons() && !newRw.hasAddons())
                                    this.showAlert.onNext(pledgeAndData)

                                if (!prevRw.hasAddons() && !newRw.hasAddons())
                                    this.showPledgeFragment.onNext(pledgeAndData)

                                if (prevRw.hasAddons() && newRw.hasAddons()) {
                                    if (differentShippingTypes(prevRw, newRw)) this.showAlert.onNext(it.first)
                                    else this.showAddOnsFragment.onNext(pledgeAndData)
                                }

                                if (!prevRw.hasAddons() && newRw.hasAddons()) {
                                    this.showAddOnsFragment.onNext(pledgeAndData)
                                }
                            }
                        }
                        this.rewardClicked.onNext(defaultRewardClicked)
                    }

            project
                    .map { it.rewards()?.size?: 0 }
                    .compose(bindToLifecycle())
                    .subscribe(this.rewardsCount)

            this.showAlert
                    .compose<Pair<PledgeData, PledgeReason>>(takeWhen(alertButtonPressed))
                    .compose(bindToLifecycle())
                    .subscribe {
                        if (it.first.reward().hasAddons())
                            this.showAddOnsFragment.onNext(it)
                        else this.showPledgeFragment.onNext(it)
                    }
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
                newRW.shippingType()?.toLowerCase() ?: "" != backedRW.shippingType()?.toLowerCase() ?: ""
            }
        }

        private fun pledgeDataAndPledgeReason(projectData: ProjectData, reward: Reward): Pair<PledgeData, PledgeReason> {
            val pledgeReason = if (projectData.project().isBacking) PledgeReason.UPDATE_REWARD else PledgeReason.PLEDGE
            val pledgeData = PledgeData.with(PledgeFlowContext.forPledgeReason(pledgeReason), projectData, reward)
            return Pair(pledgeData, pledgeReason)
        }

        private fun indexOfBackedReward(project: Project): Int {
            project.rewards()?.run {
                for ((index, reward) in withIndex()) {
                    if (BackingUtils.isBacked(project, reward)) {
                        return index
                    }
                }
            }

            return 0
        }

        override fun configureWith(projectData: ProjectData) {
            this.projectDataInput.onNext(projectData)
        }

        override fun rewardClicked(reward: Reward) {
            this.rewardClicked.onNext(Pair(reward, true))
        }

        override fun alertButtonPressed() = this.alertButtonPressed.onNext(null)

        @NonNull
        override fun backedRewardPosition(): Observable<Int> = this.backedRewardPosition

        @NonNull
        override fun projectData(): Observable<ProjectData> = this.projectData

        @NonNull
        override fun rewardsCount(): Observable<Int> = this.rewardsCount

        @NonNull
        override fun showPledgeFragment(): Observable<Pair<PledgeData, PledgeReason>> = this.showPledgeFragment

        @NonNull
        override fun showAddOnsFragment(): Observable<Pair<PledgeData, PledgeReason>> = this.showAddOnsFragment

        @NonNull
        override fun showAlert(): Observable<Pair<PledgeData, PledgeReason>> = this.showAlert
    }
}
