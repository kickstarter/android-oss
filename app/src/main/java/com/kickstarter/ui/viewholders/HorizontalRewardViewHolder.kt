package com.kickstarter.ui.viewholders

import android.content.Intent
import android.util.Pair
import android.view.View
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.ObjectUtils.requireNonNull
import com.kickstarter.libs.utils.RewardItemDecorator
import com.kickstarter.libs.utils.RewardViewUtils
import com.kickstarter.libs.utils.TransitionUtils.slideInFromRight
import com.kickstarter.libs.utils.TransitionUtils.transition
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.BackingActivity
import com.kickstarter.ui.adapters.RewardItemsAdapter
import com.kickstarter.ui.data.ScreenLocation
import com.kickstarter.viewmodels.HorizontalRewardViewHolderViewModel
import kotlinx.android.synthetic.main.item_reward.view.*


class HorizontalRewardViewHolder(private val view: View, val delegate: Delegate?) : KSViewHolder(view) {

    interface Delegate {
        fun rewardClicked(screenLocation: ScreenLocation, reward: Reward)
    }

    private val ksString = environment().ksString()
    private var viewModel = HorizontalRewardViewHolderViewModel.ViewModel(environment())

    private val currencyConversionString = context().getString(R.string.About_reward_amount)
    private val pledgeRewardCurrencyOrMoreString = context().getString(R.string.rewards_title_pledge_reward_currency_or_more)
    private val remainingRewardsString = context().getString(R.string.Left_count_left_few)

    init {

        val rewardItemAdapter = setUpRewardItemsAdapter()

        this.viewModel.outputs.conversionIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe(ViewUtils.setGone(this.view.horizontal_reward_usd_conversion_text_view))

        this.viewModel.outputs.conversion()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setConversionTextView(it) }

        this.viewModel.outputs.description()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    this.view.horizontal_reward_description_text_view.text = it
                            ?: this.context().getText(R.string.Pledge_any_amount_to_help_bring_this_project_to_life)
                }

        this.viewModel.outputs.isClickable()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { configureRewardButton(it) }

        this.viewModel.outputs.remainingIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe(ViewUtils.setGone(this.view.horizontal_reward_remaining_text_view))

        this.viewModel.outputs.limitContainerIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(this.view.reward_limit_container, it) }

        this.viewModel.outputs.remaining()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setRemainingRewardsTextView(it) }

        this.viewModel.outputs.alternatePledgeButtonText()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.horizontal_reward_pledge_button.setText(it) }

        this.viewModel.outputs.minimumAmount()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setMinimumButtonText(it) }

        this.viewModel.outputs.minimumAmountTitle()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.horizontal_reward_minimum_text_view.text = it }

        this.viewModel.outputs.reward()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.horizontal_reward_ending_text_view.text = formattedExpirationString(it) }

        this.viewModel.outputs.endDateSectionIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(this.view.horizontal_reward_ending_text_view, it) }

        this.viewModel.outputs.rewardItems()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { rewardItemAdapter.rewardsItems(it) }

        this.viewModel.outputs.rewardItemsAreGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe(ViewUtils.setGone(this.view.horizontal_rewards_item_section))

        this.viewModel.outputs.title()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    this.view.horizontal_reward_title_text_view.text = it
                            ?: this.context().getString(R.string.Make_a_pledge_without_a_reward)
                }

        this.viewModel.outputs.titleIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(this.view.horizontal_reward_title_text_view, it) }

        this.viewModel.outputs.showPledgeFragment()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.delegate?.rewardClicked(ViewUtils.getScreenLocation(this.itemView), it.second) }

        this.viewModel.outputs.startBackingActivity()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.startBackingActivity(it) }

        this.viewModel.outputs.buttonTint()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.horizontal_reward_pledge_button.backgroundTintList = ContextCompat.getColorStateList(context(), it) }

        this.viewModel.outputs.buttonIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setPledgeButtonVisibility(it) }

        this.viewModel.outputs.checkIsInvisible()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setInvisible(this.view.reward_check, it) }

        this.viewModel.outputs.checkTintColor()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.reward_check.imageTintList = ContextCompat.getColorStateList(context(), it) }

        this.viewModel.outputs.checkBackgroundDrawable()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.reward_check.setBackgroundResource(it) }

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

    private fun configureRewardButton(isRewardAvailable: Boolean) {
        this.view.horizontal_reward_pledge_button.isClickable = isRewardAvailable
        this.view.horizontal_reward_pledge_button.isEnabled = isRewardAvailable
    }

    private fun formattedExpirationString(@NonNull reward: Reward): String {
        val detail = RewardViewUtils.deadlineCountdownDetail(reward, context(), this.ksString)
        val value = RewardViewUtils.deadlineCountdownValue(reward)
        return "$value $detail"
    }

    private fun setConversionTextView(@NonNull amount: String) {
        this.view.horizontal_reward_usd_conversion_text_view.text = this.ksString.format(this.currencyConversionString,
                "reward_amount", amount)
    }

    private fun setMinimumButtonText(@NonNull minimum: String) {
        this.view.horizontal_reward_pledge_button.text = this.ksString.format(this.pledgeRewardCurrencyOrMoreString,
                "reward_currency", minimum)
    }

    private fun setPledgeButtonVisibility(gone: Boolean) {
        ViewUtils.setGone(this.view.reward_button_container, gone)
        when {
            gone -> ViewUtils.setGone(this.view.reward_button_placeholder, true)
            else -> ViewUtils.setInvisible(this.view.reward_button_placeholder, true)
        }
    }

    private fun setRemainingRewardsTextView(@NonNull remaining: String) {
        this.view.horizontal_reward_remaining_text_view.text = this.ksString.format(this.remainingRewardsString,
                "left_count", remaining)
    }

    private fun setUpRewardItemsAdapter(): RewardItemsAdapter {
        val rewardItemAdapter = RewardItemsAdapter()
        val itemRecyclerView = view.horizontal_rewards_item_recycler_view
        itemRecyclerView.adapter = rewardItemAdapter
        itemRecyclerView.layoutManager = LinearLayoutManager(context())
        this.context().getDrawable(R.drawable.divider_grey_300_horizontal)?.let {
            itemRecyclerView.addItemDecoration(RewardItemDecorator(it))
        }
        return rewardItemAdapter
    }

    private fun startBackingActivity(@NonNull project: Project) {
        val context = context()
        val intent = Intent(context, BackingActivity::class.java)
                .putExtra(IntentKey.PROJECT, project)

        context.startActivity(intent)
        transition(context, slideInFromRight())
    }

}
