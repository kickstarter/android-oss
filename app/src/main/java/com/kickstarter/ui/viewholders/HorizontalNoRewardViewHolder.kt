package com.kickstarter.ui.viewholders

import android.content.Intent
import android.util.Pair
import android.view.View
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.BackingActivity
import com.kickstarter.ui.data.ScreenLocation
import com.kickstarter.viewmodels.HorizontalNoRewardViewHolderViewModel
import kotlinx.android.synthetic.main.item_no_reward.view.*

class HorizontalNoRewardViewHolder(val view: View, val delegate: Delegate?): KSViewHolder(view) {

    interface Delegate {
        fun rewardClicked(screenLocation: ScreenLocation, reward: Reward)
    }

    private val viewModel = HorizontalNoRewardViewHolderViewModel.ViewModel(environment())

    init {

        this.viewModel.outputs.startBackingActivity()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { startBackingActivity(it) }

        this.viewModel.outputs.showPledgeFragment()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.delegate?.rewardClicked(ViewUtils.getScreenLocation(this.itemView), it.second) }

        this.viewModel.outputs.buttonTint()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.horizontal_no_reward_pledge_button.backgroundTintList = ContextCompat.getColorStateList(context(), it) }

        this.viewModel.outputs.buttonIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setPledgeButtonVisibility(it) }

        this.viewModel.outputs.checkIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(this.view.no_reward_check, it) }

        this.viewModel.outputs.buttonCTA()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.horizontal_no_reward_pledge_button.setText(it) }

        this.view.horizontal_no_reward_pledge_button.setOnClickListener {
            this.viewModel.inputs.rewardClicked()
        }
    }

    override fun bindData(data: Any?) {
        val projectAndReward = ObjectUtils.requireNonNull(data as Pair<Project, Reward>)
        val project = ObjectUtils.requireNonNull(projectAndReward.first, Project::class.java)
        val reward = ObjectUtils.requireNonNull(projectAndReward.second, Reward::class.java)

        this.viewModel.inputs.projectAndReward(project, reward)
    }

    private fun setPledgeButtonVisibility(gone: Boolean) {
        ViewUtils.setGone(this.view.horizontal_no_reward_pledge_button, gone)
        when {
            gone -> ViewUtils.setGone(this.view.no_reward_placeholder_button, true)
            else -> ViewUtils.setInvisible(this.view.no_reward_placeholder_button, true)
        }
    }

    private fun startBackingActivity(@NonNull project: Project) {
        val context = context()
        val intent = Intent(context, BackingActivity::class.java)
                .putExtra(IntentKey.PROJECT, project)

        context.startActivity(intent)
        TransitionUtils.transition(context, TransitionUtils.slideInFromRight())
    }
}
