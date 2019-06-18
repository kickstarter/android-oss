package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.R
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.libs.utils.BackingUtils
import com.kickstarter.libs.utils.BooleanUtils
import com.kickstarter.libs.utils.ProjectUtils
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.viewholders.KSViewHolder
import rx.Observable
import rx.subjects.PublishSubject

interface HorizontalNoRewardViewHolderViewModel {
    interface Inputs {
        /** Call with a reward and project when data is bound to the view.  */
        fun projectAndReward(project: Project, reward: Reward)

        /** Call when the user clicks on a reward. */
        fun rewardClicked()
    }

    interface Outputs {
        /** Emits the string resource ID of the pledge button.   */
        fun buttonCTA(): Observable<Int>

        /** Emits a boolean determining if the pledge button should be shown. */
        fun buttonIsGone(): Observable<Boolean>

        /** Emits the color resource ID to tint the pledge button. */
        fun buttonTint(): Observable<Int>

        /** Emits `true` if the backed check should be hidden, `false` otherwise.  */
        fun checkIsGone(): Observable<Boolean>

        /** Show [com.kickstarter.ui.fragments.PledgeFragment].  */
        fun showPledgeFragment(): Observable<Pair<Project, Reward>>

        /** Start the [com.kickstarter.ui.activities.BackingActivity] with the project.  */
        fun startBackingActivity(): Observable<Project>
    }

    class ViewModel(@NonNull environment: Environment) : ActivityViewModel<KSViewHolder>(environment), Inputs, Outputs {
        private val projectAndReward = PublishSubject.create<Pair<Project, Reward>>()
        private val rewardClicked = PublishSubject.create<Void>()

        private val buttonIsGone: Observable<Boolean>
        private val buttonTint: Observable<Int>
        private val checkIsGone: Observable<Boolean>
        private val showPledgeFragment: Observable<Pair<Project, Reward>>
        private val startBackingActivity: Observable<Project>
        private val buttonCTA: Observable<Int>

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            this.startBackingActivity = this.projectAndReward
                    .compose<Pair<Project, Reward>>(takeWhen<Pair<Project, Reward>, Void>(this.rewardClicked))
                    .filter { pr -> ProjectUtils.isCompleted(pr.first) && BackingUtils.isBacked(pr.first, pr.second) }
                    .map { pr -> pr.first }

            this.showPledgeFragment = this.projectAndReward
                    .filter { pr -> isSelectable(pr.first, pr.second) && pr.first.isLive }
                    .compose(takeWhen<Pair<Project, Reward>, Void>(this.rewardClicked))

            this.buttonIsGone = this.projectAndReward
                    .map { BackingUtils.isBacked(it.first, it.second) || it.first.isLive }
                    .map { BooleanUtils.negate(it) }
                    .distinctUntilChanged()

            this.buttonTint = this.projectAndReward
                    .map { RewardUtils.pledgeButtonColor(it.first, it.second) }
                    .compose(bindToLifecycle())
                    .distinctUntilChanged()

            this.checkIsGone = this.projectAndReward
                    .map { !it.first.isLive && BackingUtils.isBacked(it.first, it.second) }
                    .map { BooleanUtils.negate(it) }
                    .distinctUntilChanged()

            this.buttonCTA = this.projectAndReward
                    .map { !it.first.isLive && BackingUtils.isBacked(it.first, it.second) }
                    .map {
                        when {
                            it -> R.string.View_your_pledge
                            else -> R.string.Pledge_without_a_reward
                        }
                    }
                    .distinctUntilChanged()
        }

        override fun projectAndReward(@NonNull project: Project, @NonNull reward: Reward) {
            this.projectAndReward.onNext(Pair.create(project, reward))
        }

        override fun rewardClicked() {
            return this.rewardClicked.onNext(null)
        }

        @NonNull
        override fun buttonCTA(): Observable<Int> = this.buttonCTA

        @NonNull
        override fun buttonIsGone(): Observable<Boolean> = this.buttonIsGone

        @NonNull
        override fun buttonTint(): Observable<Int> = this.buttonTint

        @NonNull
        override fun checkIsGone(): Observable<Boolean> = this.checkIsGone

        @NonNull
        override fun showPledgeFragment(): Observable<Pair<Project, Reward>> = this.showPledgeFragment

        @NonNull
        override fun startBackingActivity(): Observable<Project> = this.startBackingActivity

        private fun isSelectable(@NonNull project: Project, @NonNull reward: Reward): Boolean {
            if (BackingUtils.isBacked(project, reward) || project.isLive) {
                return true
            }

            return false
        }
    }
}
