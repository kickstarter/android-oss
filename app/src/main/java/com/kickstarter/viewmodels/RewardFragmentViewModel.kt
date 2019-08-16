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
import com.kickstarter.ui.data.ScreenLocation
import com.kickstarter.ui.fragments.RewardsFragment
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class RewardFragmentViewModel {
    interface Inputs {
        /** Configure with current project.  */
        fun project(project: Project)

        /** Call when a reward is clicked with its current screen location to snapshot.  */
        fun rewardClicked(screenLocation: ScreenLocation, reward: Reward)
    }

    interface Outputs {
        /**  Emits the position of the backed reward. */
        fun backedRewardPosition(): Observable<Int>

        /** Emits the current project. */
        fun project(): Observable<Project>

        /** Emits the count of the current project's rewards. */
        fun rewardsCount(): Observable<Int>

        /** Emits when we should show the [com.kickstarter.ui.fragments.PledgeFragment].  */
        fun showPledgeFragment(): Observable<PledgeData>
    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<RewardsFragment>(environment), Inputs, Outputs {

        private val projectInput = PublishSubject.create<Project>()
        private val rewardClicked = PublishSubject.create<Pair<ScreenLocation, Reward>>()

        private val backedRewardPosition = PublishSubject.create<Int>()
        private val project = BehaviorSubject.create<Project>()
        private val rewardsCount = BehaviorSubject.create<Int>()
        private val showPledgeFragment = PublishSubject.create<PledgeData>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            this.projectInput
                    .compose(bindToLifecycle())
                    .subscribe(this.project)

            this.projectInput
                    .filter { it.isBacking }
                    .map { indexOfBackedReward(it) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.backedRewardPosition)

            this.projectInput
                    .compose<Pair<Project, Pair<ScreenLocation, Reward>>>(Transformers.takePairWhen(this.rewardClicked))
                    .map { PledgeData(it.second.first, it.second.second, it.first) }
                    .compose(bindToLifecycle())
                    .subscribe(this.showPledgeFragment)

            this.projectInput
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

        override fun project(project: Project) {
            this.projectInput.onNext(project)
        }

        override fun rewardClicked(screenLocation: ScreenLocation, reward: Reward) {
            this.rewardClicked.onNext(Pair.create(screenLocation, reward))
        }

        @NonNull
        override fun backedRewardPosition(): Observable<Int> = this.backedRewardPosition

        @NonNull
        override fun project(): Observable<Project> = this.project

        @NonNull
        override fun rewardsCount(): Observable<Int> = this.rewardsCount

        @NonNull
        override fun showPledgeFragment(): Observable<PledgeData> = this.showPledgeFragment
    }
}
