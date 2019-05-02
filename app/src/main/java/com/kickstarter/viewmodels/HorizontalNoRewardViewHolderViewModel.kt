package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.BackingUtils
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
        /** Start the [com.kickstarter.ui.activities.BackingActivity] with the project.  */
        fun startBackingActivity(): Observable<Project>

        /** Start [com.kickstarter.ui.activities.CheckoutActivity] with the project's reward selected.  */
        fun startCheckoutActivity(): Observable<Pair<Project, Reward>>
    }

    class ViewModel(@NonNull environment: Environment) : ActivityViewModel<KSViewHolder>(environment), Inputs, Outputs {
        private val projectAndReward = PublishSubject.create<Pair<Project, Reward>>()
        private val rewardClicked = PublishSubject.create<Void>()

        private val startBackingActivity: Observable<Project>
        private val startCheckoutActivity: Observable<Pair<Project, Reward>>

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            this.startBackingActivity = this.projectAndReward
                    .compose<Pair<Project, Reward>>(Transformers.takeWhen<Pair<Project, Reward>, Void>(this.rewardClicked))
                    .filter { pr -> ProjectUtils.isCompleted(pr.first) && BackingUtils.isBacked(pr.first, pr.second) }
                    .map { pr -> pr.first }

            this.startCheckoutActivity = this.projectAndReward
                    .filter { pr -> isSelectable(pr.first, pr.second) && pr.first.isLive }
                    .compose(Transformers.takeWhen<Pair<Project, Reward>, Void>(this.rewardClicked))

        }

        override fun rewardClicked() {
            return this.rewardClicked.onNext(null)
        }

        override fun startBackingActivity(): Observable<Project> {
            return this.startBackingActivity
        }

        @NonNull
        override fun startCheckoutActivity(): Observable<Pair<Project, Reward>> {
            return this.startCheckoutActivity
        }

        override fun projectAndReward(@NonNull project: Project, @NonNull reward: Reward) {
            this.projectAndReward.onNext(Pair.create(project, reward))
        }

        private fun isSelectable(@NonNull project: Project, @NonNull reward: Reward): Boolean {
            if (BackingUtils.isBacked(project, reward)) {
                return true
            }

            if (!project.isLive) {
                return false
            }

            return !RewardUtils.isLimitReached(reward)
        }
    }
}
