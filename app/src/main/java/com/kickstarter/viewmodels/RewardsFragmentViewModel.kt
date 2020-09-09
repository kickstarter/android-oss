package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.libs.utils.BackingUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.mock.factories.RewardFactory
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
        private val rewardClicked = PublishSubject.create<Reward>()
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

            val backedReward = project
                    .map { it.backing()?.reward() }
                    .filter { ObjectUtils.isNotNull(it) }
                    .map { requireNotNull(it) }

            val backedNoReward = project
                    .filter { it.backing() != null && it.backing()?.reward() == null }
                    .map { RewardFactory.noReward() }

            val backedAddOns = project
                    .map { it?.backing()?.addOns() }
                    .filter { ObjectUtils.isNotNull(it) }
                    .map { requireNotNull(it) }

            val hasAddOnsBacked = backedAddOns
                    .map { it.isNotEmpty() }

            project
                    .filter { it.isBacking }
                    .map { indexOfBackedReward(it) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.backedRewardPosition)

            // - If the selected Reward to update to do not have AddOns
            val pledgeDataAndReason = this.projectDataInput
                    .compose<Pair<ProjectData, Reward>>(Transformers.takePairWhen(this.rewardClicked))
                    .filter { !it.second.hasAddons() }
                    .map { pledgeDataAndPledgeReason(it.first, it.second) }

            pledgeDataAndReason
                    .filter { it.second == PledgeReason.PLEDGE}
                    .compose(bindToLifecycle())
                    .subscribe(this.showPledgeFragment)

            pledgeDataAndReason
                    .compose<Pair<Pair<PledgeData, PledgeReason>, Reward>>(combineLatestPair(backedReward))
                    .filter { !it.second.hasAddons() }
                    .map { it.first }
                    .compose(bindToLifecycle())
                    .subscribe(this.showPledgeFragment)

            pledgeDataAndReason
                    .filter { it.second == PledgeReason.UPDATE_REWARD }
                    .compose<Pair<Pair<PledgeData, PledgeReason>, Boolean>>(combineLatestPair(hasAddOnsBacked))
                    .compose(bindToLifecycle())
                    .subscribe {
                        if (!it.second && !it.first.first.reward().hasAddons())
                            this.showPledgeFragment.onNext(it.first)
                        else this.showAlert.onNext(it.first)
                    }

            // - If the selected Reward have AddOns
            val pledgeDataAndReasonWithAddOns = this.projectDataInput
                    .compose<Pair<ProjectData, Reward>>(Transformers.takePairWhen(this.rewardClicked))
                    .filter { it.second.hasAddons() }
                    .map { pledgeDataAndPledgeReason(it.first, it.second) }

            pledgeDataAndReasonWithAddOns
                    .filter { it.second == PledgeReason.PLEDGE }
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.showAddOnsFragment.onNext(it)
                    }

            val shouldShowAlert = pledgeDataAndReasonWithAddOns
                    .filter { it.second == PledgeReason.UPDATE_REWARD }
                    .map { it.first }
                    .compose<Pair<PledgeData, Reward>>(combineLatestPair(backedReward))
                    .map { differentShippingTypes(it.first.reward(), it.second) }

            // - In case choosing a reward with AddOns, from a NoReward Reward
            pledgeDataAndReasonWithAddOns
                    .filter { it.second == PledgeReason.UPDATE_REWARD }
                    .compose<Pair<Pair<PledgeData, PledgeReason>, Reward>>(combineLatestPair(backedNoReward))
                    .map { it.first }
                    .subscribe(this.showAddOnsFragment)

            pledgeDataAndReasonWithAddOns
                    .compose<Pair<Pair<PledgeData, PledgeReason>, Boolean>>(combineLatestPair(shouldShowAlert))
                    .compose(bindToLifecycle())
                    .subscribe { data ->
                        if (data.second) this.showAlert.onNext(data.first)
                        else this.showAddOnsFragment.onNext(data.first)
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
            this.rewardClicked.onNext(reward)
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
