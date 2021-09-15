package com.kickstarter.ui.viewholders

import android.annotation.SuppressLint
import android.util.Pair
import android.view.View
import androidx.annotation.NonNull
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding.view.RxView
import com.kickstarter.R
import com.kickstarter.databinding.ItemRewardBinding
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.BooleanUtils
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ObjectUtils.requireNonNull
import com.kickstarter.libs.utils.RewardItemDecorator
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.RewardViewUtils
import com.kickstarter.libs.utils.extensions.setGone
import com.kickstarter.models.Reward
import com.kickstarter.ui.adapters.RewardItemsAdapter
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.RewardViewHolderViewModel

class RewardViewHolder(private val binding: ItemRewardBinding, val delegate: Delegate?, private val inset: Boolean = false) : KSViewHolder(binding.root) {

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
            .subscribe(this.binding.rewardConversionTextView.setGone())

        this.viewModel.outputs.conversion()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setConversionTextView(it) }

        this.viewModel.outputs.descriptionForNoReward()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.binding.rewardDescriptionTextView.setText(it) }

        this.viewModel.outputs.descriptionForReward()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.binding.rewardDescriptionTextView.text = it }

        this.viewModel.outputs.descriptionIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.binding.rewardDescriptionContainer.setGone(it) }

        this.viewModel.outputs.buttonIsEnabled()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.binding.rewardPledgeButton.isEnabled = it }

        this.viewModel.outputs.remainingIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe(this.binding.rewardRemainingTextView.setGone())

        this.viewModel.outputs.limitContainerIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                this.binding.rewardLimitContainer.setGone(it)
            }

        this.viewModel.outputs.remaining()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setRemainingRewardsTextView(it) }

        this.viewModel.outputs.buttonCTA()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.binding.rewardPledgeButton.setText(it) }

        this.viewModel.outputs.shippingSummary()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setShippingSummaryText(it) }

        this.viewModel.outputs.shippingSummaryIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.binding.rewardShippingSummary.setGone(it) }

        this.viewModel.outputs.minimumAmountTitle()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.binding.rewardMinimumTextView.text = it }

        this.viewModel.outputs.reward()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.binding.rewardEndingTextView.text = formattedExpirationString(it) }

        this.viewModel.outputs.endDateSectionIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.binding.rewardEndingTextView.setGone(it) }

        this.viewModel.outputs.rewardItems()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { rewardItemAdapter.rewardsItems(it) }

        this.viewModel.outputs.rewardItemsAreGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe(this.binding.rewardsItemSection.setGone())

        this.viewModel.outputs.titleForNoReward()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.binding.rewardTitleTextView.setText(it) }

        this.viewModel.outputs.titleForReward()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.binding.rewardTitleTextView.text = it }

        this.viewModel.outputs.titleIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.binding.rewardTitleTextView.setGone(it) }

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
            .subscribe { this.binding.rewardBackersCount.setGone(it) }

        this.viewModel.outputs.estimatedDelivery()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.binding.rewardEstimatedDelivery.text = it }

        this.viewModel.outputs.estimatedDeliveryIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.binding.rewardEstimatedDeliverySection.setGone(it) }

        this.viewModel.outputs.isMinimumPledgeAmountGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                this.binding.rewardConversionTextView.setGone(it)
                this.binding.rewardMinimumTextView.setGone(it)
            }

        RxView.clicks(this.binding.rewardPledgeButton)
            .compose(bindToLifecycle())
            .subscribe { this.viewModel.inputs.rewardClicked(this.bindingAdapterPosition) }

        this.viewModel.outputs.hasAddOnsAvailable()
            .filter { ObjectUtils.isNotNull(it) }
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                this.binding.rewardAddOnsAvailable.setGone(!it)
            }

        this.viewModel.outputs.selectedRewardTagIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { isGone ->
                if (!isGone) this.binding.rewardSelectedRewardTag.visibility = View.VISIBLE
                else this.binding.rewardSelectedRewardTag.setGone(true)
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
        val backersCountText = this.ksString.format(
            "rewards_info_backer_count_backers", count,
            "backer_count", NumberUtils.format(count)
        )
        this.binding.rewardBackersCount.text = backersCountText
    }

    private fun setConversionTextView(@NonNull amount: String) {
        this.binding.rewardConversionTextView.text = this.ksString.format(
            this.currencyConversionString,
            "reward_amount", amount
        )
    }

    private fun setPledgeButtonVisibility(gone: Boolean) {
        if (BooleanUtils.isTrue(this.inset)) {
            this.binding.rewardButtonContainer.setGone(true)
            this.binding.rewardButtonPlaceholder.setGone(true)
        } else {
            this.binding.rewardButtonContainer.setGone(gone)
            when {
                gone -> this.binding.rewardButtonPlaceholder.setGone(true)
                else -> this.binding.rewardButtonPlaceholder.setGone(true)
            }
        }
    }

    private fun setRemainingRewardsTextView(@NonNull remaining: String) {
        this.binding.rewardRemainingTextView.text = this.ksString.format(
            this.remainingRewardsString,
            "left_count", remaining
        )
    }

    private fun setShippingSummaryText(stringResAndLocationName: Pair<Int, String?>) {
        this.binding.rewardShippingSummary.text = RewardViewUtils.shippingSummary(context(), this.ksString, stringResAndLocationName)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setUpRewardItemsAdapter(): RewardItemsAdapter {
        val rewardItemAdapter = RewardItemsAdapter()
        val itemRecyclerView = binding.rewardsItemRecyclerView
        itemRecyclerView.adapter = rewardItemAdapter
        itemRecyclerView.layoutManager = LinearLayoutManager(context())
        this.context().getDrawable(R.drawable.divider_grey_300_horizontal)?.let {
            itemRecyclerView.addItemDecoration(RewardItemDecorator(it))
        }
        return rewardItemAdapter
    }
}
