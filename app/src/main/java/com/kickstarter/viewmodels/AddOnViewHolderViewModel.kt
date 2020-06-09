package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.models.Reward
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.viewholders.RewardViewHolder

interface AddOnViewHolderViewModel {
    interface Inputs {
        /** Configure with the current [ProjectData] and [Reward]. */
        fun configureWith(projectData: ProjectData, reward: Reward)

        /** Call when the user clicks on a reward. */
        fun rewardClicked(position: Int)
    }

    interface Outputs {
    }

    class ViewModel(@NonNull environment: Environment) : ActivityViewModel<RewardViewHolder>(environment), Inputs, Outputs {

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            // TODO in https://kickstarter.atlassian.net/browse/NT-1290
        }

        override fun configureWith(projectData: ProjectData, reward: Reward) {
            // TODO in https://kickstarter.atlassian.net/browse/NT-1290
        }

        override fun rewardClicked(position: Int) {
            // TODO in https://kickstarter.atlassian.net/browse/NT-1290
        }
    }
}
