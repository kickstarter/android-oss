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
import com.kickstarter.ui.data.ScreenLocation
import com.kickstarter.viewmodels.HorizontalRewardViewHolderViewModel
import kotlinx.android.synthetic.main.item_reward.view.*

class HorizontalRewardViewHolder(private val view: View, val delegate: Delegate) : KSViewHolder(view) {

    interface Delegate {
        fun rewardClicked(screenLocation: ScreenLocation, reward: Reward)
    }

    private val ksString = environment().ksString()
    private lateinit var reward: Reward
    private var viewModel = HorizontalRewardViewHolderViewModel.ViewModel(environment())

    private val currencyConversionString = context().getString(R.string.About_reward_amount)
    private val pledgeRewardCurrencyOrMoreString = context().getString(R.string.rewards_title_pledge_reward_currency_or_more)
    private val remainingRewardsString = context().getString(R.string.Left_count_left_few)

    init {

        this.viewModel.outputs.conversionTextViewIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe(ViewUtils.setGone(view.horizontal_reward_usd_conversion_text_view))

        this.viewModel.outputs.conversionText()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setConversionTextView(it) }

        this.viewModel.outputs.descriptionText()
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

        this.viewModel.outputs.limitAndRemainingText()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setRemainingRewardsTextView(it.second) }

        this.viewModel.outputs.minimumText()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setMinimumText(it) }

        this.viewModel.outputs.reward()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { view.horizontal_reward_ending_text_view.text = formattedDeadlineString(it) }

        this.viewModel.outputs.rewardEndDateSectionIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(view.horizontal_reward_ending_text_view, it) }

        this.viewModel.outputs.titleText()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { view.horizontal_reward_title_text_view.text = it }

        this.viewModel.outputs.titleTextViewIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(view.horizontal_reward_title_text_view, it) }

        this.viewModel.outputs.showPledgeFragment()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.delegate.rewardClicked(getScreenLocationOfReward(), this.reward) }

        this.viewModel.outputs.startBackingActivity()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.startBackingActivity(it) }

        view.horizontal_reward_pledge_button.setOnClickListener {
            this.viewModel.inputs.rewardClicked()
        }
    }

    override fun bindData(data: Any?) {
        val projectAndReward = requireNonNull(data as Pair<Project, Reward>)
        val project = requireNonNull(projectAndReward.first, Project::class.java)
        this.reward = requireNonNull(projectAndReward.second, Reward::class.java)

        this.viewModel.inputs.projectAndReward(project, this.reward)
    }

    private fun formattedDeadlineString(@NonNull reward: Reward): String {
        val detail = RewardUtils.deadlineCountdownDetail(reward, context(), this.ksString)
        val value = RewardUtils.deadlineCountdownValue(reward)
        return "$value $detail"
    }

    private fun setConversionTextView(@NonNull amount: String) {
        this.view.horizontal_reward_usd_conversion_text_view.text = (this.ksString.format(
                this.currencyConversionString,
                "reward_amount", amount
        ))
    }

    private fun setMinimumText(@NonNull minimum: String) {
        this.view.horizontal_reward_minimum_text_view.text = minimum
        this.view.horizontal_reward_pledge_button.text = (this.ksString.format(
                this.pledgeRewardCurrencyOrMoreString,
                "reward_currency", minimum
        ))
    }

    private fun setRemainingRewardsTextView(@NonNull remaining: String) {
        this.view.horizontal_reward_remaining_text_view.text = (this.ksString.format(
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

    private fun getScreenLocationOfReward(): ScreenLocation {
        val rewardLocation = IntArray(2)
        this.itemView.getLocationInWindow(rewardLocation)
        val x = rewardLocation[0].toFloat()
        val y = rewardLocation[1].toFloat()
        val height = this.itemView.height
        val width = this.itemView.width
        return ScreenLocation(x, y, height.toFloat(), width.toFloat())
    }
}
