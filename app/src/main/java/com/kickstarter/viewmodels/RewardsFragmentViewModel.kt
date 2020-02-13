package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.BackingUtils
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.data.*
import com.kickstarter.ui.fragments.RewardsFragment
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class RewardsFragmentViewModel {
    interface Inputs {
        /** Configure with current [ProjectData]. */
        fun configureWith(projectData: ProjectData)

        /** Call when a reward is clicked with its current screen location to snapshot.  */
        fun rewardClicked(screenLocation: ScreenLocation, reward: Reward)
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
    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<RewardsFragment>(environment), Inputs, Outputs {

        private val projectDataInput = PublishSubject.create<ProjectData>()
        private val rewardClicked = PublishSubject.create<Pair<ScreenLocation, Reward>>()

        private val backedRewardPosition = PublishSubject.create<Int>()
        private val projectData = BehaviorSubject.create<ProjectData>()
        private val rewardsCount = BehaviorSubject.create<Int>()
        private val showPledgeFragment = PublishSubject.create<Pair<PledgeData, PledgeReason>>()

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

            this.projectDataInput
                    .compose<Pair<ProjectData, Pair<ScreenLocation, Reward>>>(Transformers.takePairWhen(this.rewardClicked))
                    .map { pledgeDataAndPledgeReason(it.first, it.second.second, it.second.first) }
                    .compose(bindToLifecycle())
                    .subscribe(this.showPledgeFragment)

            project
                    .map { it.rewards()?.size?: 0 }
                    .compose(bindToLifecycle())
                    .subscribe(this.rewardsCount)
        }

        private fun pledgeDataAndPledgeReason(projectData: ProjectData, reward: Reward, screenLocation: ScreenLocation): Pair<PledgeData, PledgeReason> {
            val pledgeReason = if (projectData.project().isBacking) PledgeReason.UPDATE_REWARD else PledgeReason.PLEDGE
            val pledgeData = PledgeData.with(PledgeFlowContext.forPledgeReason(pledgeReason), projectData, reward, screenLocation)
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

        override fun rewardClicked(screenLocation: ScreenLocation, reward: Reward) {
            this.rewardClicked.onNext(Pair.create(screenLocation, reward))
        }

        @NonNull
        override fun backedRewardPosition(): Observable<Int> = this.backedRewardPosition

        @NonNull
        override fun projectData(): Observable<ProjectData> = this.projectData

        @NonNull
        override fun rewardsCount(): Observable<Int> = this.rewardsCount

        @NonNull
        override fun showPledgeFragment(): Observable<Pair<PledgeData, PledgeReason>> = this.showPledgeFragment
    }
}
