package com.kickstarter.ui.viewholders

import android.util.Pair
import android.view.View
import com.kickstarter.libs.utils.ObjectUtils.requireNonNull
import com.kickstarter.models.Reward
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.AddOnViewHolderViewModel

class AddOnViewHolder(private val view: View, val delegate: ViewListener?) : KSViewHolder(view) {

    interface ViewListener {
        fun rewardClicked(reward: Reward)
    }

    private var viewModel = AddOnViewHolderViewModel.ViewModel(environment())

    init {

    }

    override fun bindData(data: Any?) {
        @Suppress("UNCHECKED_CAST")
        val projectAndReward = requireNonNull(data as Pair<ProjectData, Reward>)
        val projectTracking = requireNonNull(projectAndReward.first, ProjectData::class.java)
        val reward = requireNonNull(projectAndReward.second, Reward::class.java)

        this.viewModel.inputs.configureWith(projectTracking, reward)
    }

}
