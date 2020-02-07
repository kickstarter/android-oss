package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.BackingUtils
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectTracking
import com.kickstarter.ui.data.ScreenLocation
import com.kickstarter.ui.fragments.RewardsFragment
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class RewardsFragmentViewModel {
    interface Inputs {
        /** Configure with current project.  */
        fun project(projectTracking: ProjectTracking)

        /** Call when a reward is clicked with its current screen location to snapshot.  */
        fun rewardClicked(screenLocation: ScreenLocation, reward: Reward)
    }

    interface Outputs {
        /**  Emits the position of the backed reward. */
        fun backedRewardPosition(): Observable<Int>

        /** Emits the current project. */
        fun project(): Observable<ProjectTracking>

        /** Emits the count of the current project's rewards. */
        fun rewardsCount(): Observable<Int>

        /** Emits when we should show the [com.kickstarter.ui.fragments.PledgeFragment].  */
        fun showPledgeFragment(): Observable<Pair<PledgeData, PledgeReason>>
    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<RewardsFragment>(environment), Inputs, Outputs {

        private val projectTrackingInput = PublishSubject.create<ProjectTracking>()
        private val rewardClicked = PublishSubject.create<Pair<ScreenLocation, Reward>>()

        private val backedRewardPosition = PublishSubject.create<Int>()
        private val project = BehaviorSubject.create<ProjectTracking>()
        private val rewardsCount = BehaviorSubject.create<Int>()
        private val showPledgeFragment = PublishSubject.create<Pair<PledgeData, PledgeReason>>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            this.projectTrackingInput
                    .compose(bindToLifecycle())
                    .subscribe(this.project)

            val project = this.projectTrackingInput
                    .map { it.project() }
            project
                    .filter { it.isBacking }
                    .map { indexOfBackedReward(it) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.backedRewardPosition)

            this.projectTrackingInput
                    .compose<Pair<ProjectTracking, Pair<ScreenLocation, Reward>>>(Transformers.takePairWhen(this.rewardClicked))
                    .map { Pair(pledgeData(it.second.first, it.second.second, it.first), if (it.first.project().isBacking) PledgeReason.UPDATE_REWARD else PledgeReason.PLEDGE) }
                    .compose(bindToLifecycle())
                    .subscribe(this.showPledgeFragment)

            project
                    .map { it.rewards()?.size?: 0 }
                    .compose(bindToLifecycle())
                    .subscribe(this.rewardsCount)
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

        private fun pledgeData(screenLocation: ScreenLocation, reward: Reward, projectTracking: ProjectTracking): PledgeData {
            return PledgeData.builder()
                    .screenLocation(screenLocation)
                    .reward(reward)
                    .projectTracking(projectTracking)
                    .build()
        }

        override fun project(projectTracking: ProjectTracking) {
            this.projectTrackingInput.onNext(projectTracking)
        }

        override fun rewardClicked(screenLocation: ScreenLocation, reward: Reward) {
            this.rewardClicked.onNext(Pair.create(screenLocation, reward))
        }

        @NonNull
        override fun backedRewardPosition(): Observable<Int> = this.backedRewardPosition

        @NonNull
        override fun project(): Observable<ProjectTracking> = this.project

        @NonNull
        override fun rewardsCount(): Observable<Int> = this.rewardsCount

        @NonNull
        override fun showPledgeFragment(): Observable<Pair<PledgeData, PledgeReason>> = this.showPledgeFragment
    }
}
