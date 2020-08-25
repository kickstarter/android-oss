package com.kickstarter.ui.viewholders

import android.content.Intent
import android.util.Pair
import android.view.View
import androidx.annotation.NonNull
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding.view.RxView
import com.kickstarter.R
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.*
import com.kickstarter.libs.utils.ObjectUtils.requireNonNull
import com.kickstarter.libs.utils.TransitionUtils.slideInFromRight
import com.kickstarter.libs.utils.TransitionUtils.transition
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.BackingActivity
import com.kickstarter.ui.adapters.RewardItemsAdapter
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.RewardViewHolderViewModel
import kotlinx.android.synthetic.main.item_reward.view.*

class RewardViewHolder(private val view: View, val delegate: Delegate?, private val inset: Boolean = false) : KSViewHolder(view) {

    interface Delegate {
        fun rewardClicked(reward: Reward)
    }

    private val ksString = environment().ksString()
    private var viewModel = RewardViewHolderViewModel.ViewModel(environment())

    private val currencyConversionString = context().getString(R.string.About_reward_amount)
    private val remainingRewardsString = context().getString(R.string.Left_count_left_few)

    init {

        val rewardItemAdapter = setUpRewardItemsAdapter()

        this.viewModel.outputs.conversionIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe(ViewUtils.setGone(this.view.reward_conversion_text_view))

        this.viewModel.outputs.conversion()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setConversionTextView(it) }

        this.viewModel.outputs.descriptionForNoReward()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.reward_description_text_view.setText(it) }

        this.viewModel.outputs.descriptionForReward()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.reward_description_text_view.text = it }

        this.viewModel.outputs.descriptionIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(this.view.reward_description_container, it) }

        this.viewModel.outputs.buttonIsEnabled()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.reward_pledge_button.isEnabled = it }

        this.viewModel.outputs.remainingIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe(ViewUtils.setGone(this.view.reward_remaining_text_view))

        this.viewModel.outputs.limitContainerIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(this.view.reward_limit_container, it) }

        this.viewModel.outputs.remaining()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setRemainingRewardsTextView(it) }

        this.viewModel.outputs.buttonCTA()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.reward_pledge_button.setText(it) }

        this.viewModel.outputs.shippingSummary()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setShippingSummaryText(it) }

        this.viewModel.outputs.shippingSummaryIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(this.view.reward_shipping_summary, it) }

        this.viewModel.outputs.minimumAmountTitle()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.reward_minimum_text_view.text = it }

        this.viewModel.outputs.reward()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.reward_ending_text_view.text = formattedExpirationString(it) }

        this.viewModel.outputs.endDateSectionIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(this.view.reward_ending_text_view, it) }

        this.viewModel.outputs.rewardItems()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { rewardItemAdapter.rewardsItems(it) }

        this.viewModel.outputs.rewardItemsAreGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe(ViewUtils.setGone(this.view.rewards_item_section))

        this.viewModel.outputs.titleForNoReward()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.reward_title_text_view.setText(it) }

        this.viewModel.outputs.titleForReward()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.reward_title_text_view.text = it }

        this.viewModel.outputs.titleIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(this.view.reward_title_text_view, it) }

        this.viewModel.outputs.showFragment()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.delegate?.rewardClicked(it.second) }

        this.viewModel.outputs.buttonIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setPledgeButtonVisibility(it) }

        this.viewModel.outputs.backersCount()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setBackersCountTextView(it) }

        this.viewModel.outputs.backersCountIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(this.view.reward_backers_count, it) }

        this.viewModel.outputs.estimatedDelivery()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.reward_estimated_delivery.text = it }

        this.viewModel.outputs.estimatedDeliveryIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(this.view.reward_estimated_delivery_section, it) }

        this.viewModel.outputs.isMinimumPledgeAmountGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    ViewUtils.setGone(this.view.reward_conversion_text_view, it)
                    ViewUtils.setGone(this.view.reward_minimum_text_view, it)
                }

        RxView.clicks(this.view.reward_pledge_button)
                .compose(bindToLifecycle())
                .subscribe { this.viewModel.inputs.rewardClicked(this.adapterPosition) }

        this.viewModel.outputs.hasAddOnsAvailable()
                .filter { ObjectUtils.isNotNull(it) }
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { hasAddOns ->
                    if (hasAddOns) this.view.reward_add_ons_available.visibility = View.VISIBLE
                    else ViewUtils.setGone(this.view.reward_add_ons_available, true)
                }

        this.viewModel.outputs.selectedRewardTagIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { isGone ->
                    if (!isGone) this.view.reward_selected_reward_tag.visibility = View.VISIBLE
                    else ViewUtils.setGone(this.view.reward_selected_reward_tag, true)
                }

    }

    override fun bindData(data: Any?) {
        @Suppress("UNCHECKED_CAST")
        val projectAndReward = requireNonNull(data as Pair<ProjectData, Reward>)
        val projectTracking = requireNonNull(projectAndReward.first, ProjectData::class.java)
        val reward = requireNonNull(projectAndReward.second, Reward::class.java)

        this.viewModel.inputs.configureWith(projectTracking, reward)
    }

    private fun formattedExpirationString(@NonNull reward: Reward): String {
        val detail = RewardUtils.deadlineCountdownDetail(reward, context(), this.ksString)
        val value = RewardUtils.deadlineCountdownValue(reward)
        return "$value $detail"
    }

    private fun setBackersCountTextView(count: Int) {
        val backersCountText = this.ksString.format("rewards_info_backer_count_backers", count,
                "backer_count", NumberUtils.format(count))
        this.view.reward_backers_count.text = backersCountText
    }

    private fun setConversionTextView(@NonNull amount: String) {
        this.view.reward_conversion_text_view.text = this.ksString.format(this.currencyConversionString,
                "reward_amount", amount)
    }

    private fun setPledgeButtonVisibility(gone: Boolean) {
        if (BooleanUtils.isTrue(this.inset)) {
            ViewUtils.setGone(this.view.reward_button_container, true)
            ViewUtils.setGone(this.view.reward_button_placeholder, true)
        } else {
            ViewUtils.setGone(this.view.reward_button_container, gone)
            when {
                gone -> ViewUtils.setGone(this.view.reward_button_placeholder, true)
                else -> ViewUtils.setInvisible(this.view.reward_button_placeholder, true)
            }
        }
    }

    private fun setRemainingRewardsTextView(@NonNull remaining: String) {
        this.view.reward_remaining_text_view.text = this.ksString.format(this.remainingRewardsString,
                "left_count", remaining)
    }

    private fun setShippingSummaryText(stringResAndLocationName: Pair<Int, String?>) {
        this.view.reward_shipping_summary.text = RewardViewUtils.shippingSummary(context(), this.ksString, stringResAndLocationName)
    }

    private fun setUpRewardItemsAdapter(): RewardItemsAdapter {
        val rewardItemAdapter = RewardItemsAdapter()
        val itemRecyclerView = view.rewards_item_recycler_view
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
