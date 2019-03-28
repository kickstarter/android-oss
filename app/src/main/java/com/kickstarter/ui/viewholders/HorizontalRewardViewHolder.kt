package com.kickstarter.ui.viewholders

import android.content.Intent
import android.util.Pair
import android.view.View
import androidx.annotation.NonNull
import com.kickstarter.R
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.ObjectUtils.requireNonNull
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.TransitionUtils.slideInFromRight
import com.kickstarter.libs.utils.TransitionUtils.transition
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.BackingActivity
import com.kickstarter.ui.activities.CheckoutActivity
import com.kickstarter.viewmodels.RewardFragmentViewModel
import kotlinx.android.synthetic.main.item_reward.view.*

class HorizontalRewardViewHolder(private val view: View) : KSViewHolder(view) {

    interface Delegate {
        fun rewardClicked()
    }

    private val ksString = environment().ksString()
    private var viewModel = RewardFragmentViewModel.ViewModel(environment())

    private val currencyConversionString = context().getString(R.string.About_reward_amount)
    private val pledgeRewardCurrencyOrMoreString = context().getString(R.string.rewards_title_pledge_reward_currency_or_more)
    private val projectBackButtonString =  context().getString(R.string.project_back_button)
    private val remainingRewardsString = context().getString(R.string.Left_count_left_few)

    init {

        this.viewModel.outputs.conversionTextViewIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe(ViewUtils.setGone(view.horizontal_reward_usd_conversion_text_view))

        this.viewModel.outputs.conversionTextViewText()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setConversionTextView(it) }

        this.viewModel.outputs.currentProject()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {

                }

        this.viewModel.outputs.descriptionTextViewText()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { view.horizontal_reward_description_text_view.text = it }

        this.viewModel.outputs.isClickable()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { view.horizontal_reward_pledge_button.isClickable = it }

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

        this.viewModel.outputs.minimumTextViewText()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    view.horizontal_reward_minimum_text_view.text = it
                    setMinimumTextView(it)
                }

        this.viewModel.outputs.reward()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { view.horizontal_project_ending_text_view.text = formattedDeadlineString(it) }

        this.viewModel.outputs.rewardEndDateSectionIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    ViewUtils.setGone(view.horizontal_project_ending_text_view, it) }

        this.viewModel.outputs.titleTextViewText()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { view.horizontal_reward_title_text_view.text = it }

        this.viewModel.outputs.startBackingActivity()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.startBackingActivity(it) }

        this.viewModel.outputs.startCheckoutActivity()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { pr -> startCheckoutActivity(pr.first, pr.second) }

        view.horizontal_reward_pledge_button.setOnClickListener {
            this.viewModel.inputs.rewardClicked()
        }

    }

    override fun bindData(data: Any?) {
        val projectAndReward = requireNonNull(data as Pair<Project, Reward>)
        val project = requireNonNull(projectAndReward.first, Project::class.java)
        val reward = requireNonNull(projectAndReward.second, Reward::class.java)

        this.viewModel.inputs.projectAndReward(project, reward)
    }

    private fun formattedDeadlineString(@NonNull reward: Reward): String {
        val detail = RewardUtils.deadlineCountdownDetail(reward, context(), this.ksString)
        val value = RewardUtils.deadlineCountdownValue(reward)
        return "$value $detail"
    }

    private fun setConversionTextView(@NonNull amount: String) {
        view.horizontal_reward_usd_conversion_text_view.text = (this.ksString.format(
                this.currencyConversionString,
                "reward_amount", amount
        ))
    }

    private fun setMinimumTextView(@NonNull minimum: String) {
        view.horizontal_reward_pledge_button.text = (this.ksString.format(
                this.pledgeRewardCurrencyOrMoreString,
                "reward_currency", minimum
        ))
    }

    private fun setRemainingRewardsTextView(@NonNull remaining: String) {
        view.horizontal_reward_remaining_text_view.text = (this.ksString.format(
                this.remainingRewardsString, "left_count", remaining
        ))
    }

    private fun startBackingActivity(@NonNull project: Project) {
        val context = context()
        val intent = Intent(context, BackingActivity::class.java)
                .putExtra(IntentKey.PROJECT, project)

        context.startActivity(intent)
        transition(context, slideInFromRight())
    }

    private fun startCheckoutActivity(@NonNull project: Project, @NonNull reward: Reward) {
        val context = context()
        val intent = Intent(context, CheckoutActivity::class.java)
                .putExtra(IntentKey.PROJECT, project)
                .putExtra(IntentKey.TOOLBAR_TITLE, this.projectBackButtonString)
                .putExtra(IntentKey.URL, project.rewardSelectedUrl(reward))

        context.startActivity(intent)
        transition(context, slideInFromRight())
    }
}
