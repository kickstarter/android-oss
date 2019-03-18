package com.kickstarter.ui.viewholders

import android.view.View
import androidx.annotation.NonNull
import com.kickstarter.R
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.ObjectUtils.requireNonNull
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.viewmodels.RewardFragmentViewModel
import kotlinx.android.synthetic.main.item_reward.view.*
import timber.log.Timber

class HorizontalRewardViewHolder(private val view: View) : KSViewHolder(view) {

    interface Delegate {
        fun rewardClicked()
    }

    private val ksString = environment().ksString()
    private var viewModel = RewardFragmentViewModel.ViewModel(environment())
    private val pledgeRewardCurrencyOrMoreString = context().getString(R.string.rewards_title_pledge_reward_currency_or_more)

    init {

        this.viewModel.outputs.descriptionTextViewText()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { view.description.text = it }

        this.viewModel.outputs.minimumTextViewText()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setMinimumTextView(it) }

        this.viewModel.outputs.titleTextViewText()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { view.title.text = it }
    }


    override fun bindData(data: Any?) {
        val reward = requireNonNull(data as Reward)
        val project: Project = requireNonNull(data as Project)

        this.viewModel.inputs.projectAndReward(project, reward)
    }

    private fun setMinimumTextView(@NonNull minimum: String) {
        view.price.text = (this.ksString.format(
                this.pledgeRewardCurrencyOrMoreString,
                "reward_currency", minimum
        ))
    }

}