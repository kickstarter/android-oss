package com.kickstarter.ui.viewholders

import android.content.Intent
import android.util.Pair
import android.view.View
import androidx.annotation.NonNull
import com.kickstarter.R
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.BackingActivity
import com.kickstarter.ui.activities.CheckoutActivity
import com.kickstarter.viewmodels.HorizontalNoRewardViewHolderViewModel
import kotlinx.android.synthetic.main.item_no_reward.view.*

class HorizontalNoRewardViewHolder(val view: View): KSViewHolder(view) {

    private var viewModel = HorizontalNoRewardViewHolderViewModel.ViewModel(environment())

    private val projectBackButtonString = context().getString(R.string.project_back_button)

    init {

        this.viewModel.outputs.startBackingActivity()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { startBackingActivity(it) }

        this.viewModel.outputs.startCheckoutActivity()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { pr -> startCheckoutActivity(pr.first, pr.second) }

        view.horizontal_no_reward_pledge_button.setOnClickListener {
            this.viewModel.inputs.rewardClicked()
        }
    }

    override fun bindData(data: Any?) {
        val projectAndReward = ObjectUtils.requireNonNull(data as Pair<Project, Reward>)
        val project = ObjectUtils.requireNonNull(projectAndReward.first, Project::class.java)
        val reward = ObjectUtils.requireNonNull(projectAndReward.second, Reward::class.java)

        this.viewModel.inputs.projectAndReward(project, reward)
    }

    private fun startBackingActivity(@NonNull project: Project) {
        val context = context()
        val intent = Intent(context, BackingActivity::class.java)
                .putExtra(IntentKey.PROJECT, project)

        context.startActivity(intent)
        TransitionUtils.transition(context, TransitionUtils.slideInFromRight())
    }

    private fun startCheckoutActivity(@NonNull project: Project, @NonNull reward: Reward) {
        val context = context()
        val intent = Intent(context, CheckoutActivity::class.java)
                .putExtra(IntentKey.PROJECT, project)
                .putExtra(IntentKey.TOOLBAR_TITLE, this.projectBackButtonString)
                .putExtra(IntentKey.URL, project.rewardSelectedUrl(reward))

        context.startActivity(intent)
        TransitionUtils.transition(context, TransitionUtils.slideInFromRight())
    }
}
