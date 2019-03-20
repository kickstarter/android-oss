package com.kickstarter.ui.viewholders

import android.util.Pair
import android.view.View
import androidx.annotation.NonNull
import com.kickstarter.R
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.ObjectUtils.requireNonNull
import com.kickstarter.libs.utils.ProjectUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.viewmodels.RewardFragmentViewModel
import kotlinx.android.synthetic.main.item_reward.view.*

class HorizontalRewardViewHolder(private val view: View) : KSViewHolder(view) {

    interface Delegate {
        fun rewardClicked()
    }

    private val ksString = environment().ksString()
    private var viewModel = RewardFragmentViewModel.ViewModel(environment())
    private val pledgeRewardCurrencyOrMoreString = context().getString(R.string.rewards_title_pledge_reward_currency_or_more)
    private val currencyConversionString = context().getString(R.string.About_reward_amount)
    private val remainingRewardsString = context().getString(R.string.Left_count_left_few)

    init {

        this.viewModel.outputs.descriptionTextViewText()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { view.horiztonal_reward_description_text_view.text = it }

        this.viewModel.outputs.minimumTextViewText()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    view.horizontal_reward_minimum_text_view.text = it
                    setMinimumTextView(it)
                }

        this.viewModel.outputs.titleTextViewText()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { view.horizontal_reward_title_text_view.text = it }

        this.viewModel.outputs.conversionTextViewIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe(ViewUtils.setGone(view.horizontal_reward_usd_conversion_text_view))

        this.viewModel.outputs.conversionTextViewText()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setConversionTextView(it) }

        this.viewModel.outputs.limitAndRemainingTextViewIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe(ViewUtils.setGone(view.horizontal_reward_remaining_text_view))

        this.viewModel.outputs.limitAndRemainingTextViewText()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    setRemainingRewardsTextView(it.second)
                }

        this.viewModel.outputs.deadlineCountdownTextViewText()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { view.horizontal_project_ending_text_view.text = it }

        this.viewModel.outputs.getProject()
                .subscribe {
                    view.horizontal_project_ending_text_view.text = formattedDeadlineString(it)
                }
    }


    override fun bindData(data: Any?) {
        val projectAndReward = requireNonNull(data as Pair<Project, Reward>)
        val project = requireNonNull(projectAndReward.first, Project::class.java)
        val reward = requireNonNull(projectAndReward.second, Reward::class.java)

        this.viewModel.inputs.projectAndReward(project, reward)
    }

    private fun formattedDeadlineString(@NonNull project: Project): String {
        val detail =  ProjectUtils.deadlineCountdownDetail(project, context(), this.ksString)
        val value =  ProjectUtils.deadlineCountdownValue(project)
        return "$value $detail"
    }

    private fun setConversionTextView(@NonNull amount: String) {
        view.horizontal_reward_usd_conversion_text_view.text = (this.ksString.format(
                this.currencyConversionString,
                "reward_amount", amount
        ))
    }

    private fun setMinimumTextView(@NonNull minimum: String) {
        view.horizontal_reward_pledge.text = (this.ksString.format(
                this.pledgeRewardCurrencyOrMoreString,
                "reward_currency", minimum
        ))
    }

    private fun setRemainingRewardsTextView(@NonNull remaining: String) {
        view.horizontal_reward_remaining_text_view.text = (this.ksString.format(
                this.remainingRewardsString, "left_count", remaining
        ))
    }

}