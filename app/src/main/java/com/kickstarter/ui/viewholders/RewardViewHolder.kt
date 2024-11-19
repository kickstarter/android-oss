package com.kickstarter.ui.viewholders

import android.annotation.SuppressLint
import android.util.Pair
import android.view.View
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.ItemRewardBinding
import com.kickstarter.libs.rx.transformers.Transformers.observeForUIV2
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.RewardItemDecorator
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.RewardViewUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.models.Reward
import com.kickstarter.ui.adapters.RewardItemsAdapter
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.RewardViewHolderViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class RewardViewHolder(private val binding: ItemRewardBinding, val delegate: Delegate?, private val inset: Boolean = false) : KSViewHolder(binding.root) {

    interface Delegate {
        fun rewardClicked(reward: Reward)
    }

    private val ksString = requireNotNull(environment().ksString())
    private var viewModel = RewardViewHolderViewModel.ViewModel(environment())

    private val currencyConversionString = context().getString(R.string.About_reward_amount)
    private val remainingRewardsString = context().getString(R.string.Left_count_left_few)
    private val disposables = CompositeDisposable()

    init {

        val rewardItemAdapter = setUpRewardItemsAdapter()

        this.viewModel.outputs.conversionIsGone()
            .compose(observeForUIV2())
            .subscribe { this.binding.rewardConversionTextView.isGone = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.conversion()
            .compose(observeForUIV2())
            .subscribe { setConversionTextView(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.descriptionForNoReward()
            .compose(observeForUIV2())
            .subscribe { this.binding.rewardDescriptionTextView.setText(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.descriptionForReward()
            .compose(observeForUIV2())
            .subscribe { this.binding.rewardDescriptionTextView.text = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.descriptionIsGone()
            .compose(observeForUIV2())
            .subscribe { this.binding.rewardDescriptionContainer.isGone = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.buttonIsEnabled()
            .compose(observeForUIV2())
            .subscribe { this.binding.rewardPledgeButton.isEnabled = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.remainingIsGone()
            .compose(observeForUIV2())
            .subscribe { this.binding.rewardRemainingTextView.isGone = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.limitContainerIsGone()
            .compose(observeForUIV2())
            .subscribe {
                ViewUtils.setGone(this.binding.rewardLimitContainer, it)
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.remaining()
            .compose(observeForUIV2())
            .subscribe { setRemainingRewardsTextView(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.buttonCTA()
            .compose(observeForUIV2())
            .subscribe { this.binding.rewardPledgeButton.setText(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.shippingSummary()
            .compose(observeForUIV2())
            .subscribe { setShippingSummaryText(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.shippingSummaryIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                this.binding.rewardShippingSummary.isGone = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.minimumAmountTitle()
            .compose(observeForUIV2())
            .subscribe { this.binding.rewardMinimumTextView.text = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.reward()
            .compose(observeForUIV2())
            .subscribe { this.binding.rewardEndingTextView.text = formattedExpirationString(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.endDateSectionIsGone()
            .compose(observeForUIV2())
            .subscribe { this.binding.rewardEndingTextView.isGone = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.rewardItems()
            .compose(observeForUIV2())
            .subscribe { rewardItemAdapter.rewardsItems(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.rewardItemsAreGone()
            .compose(observeForUIV2())
            .subscribe { this.binding.rewardsItemSection.isGone = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.titleForNoReward()
            .compose(observeForUIV2())
            .subscribe { this.binding.rewardTitleTextView.setText(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.titleForReward()
            .compose(observeForUIV2())
            .subscribe { this.binding.rewardTitleTextView.text = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.titleIsGone()
            .compose(observeForUIV2())
            .subscribe { this.binding.rewardTitleTextView.isGone = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.showFragment()
            .compose(observeForUIV2())
            .subscribe { this.delegate?.rewardClicked(it.second) }
            .addToDisposable(disposables)

        this.viewModel.outputs.buttonIsGone()
            .compose(observeForUIV2())
            .subscribe { setPledgeButtonVisibility(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.backersCount()
            .compose(observeForUIV2())
            .subscribe { setBackersCountTextView(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.backersCountIsGone()
            .compose(observeForUIV2())
            .subscribe { this.binding.rewardBackersCount.isGone = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.estimatedDelivery()
            .compose(observeForUIV2())
            .subscribe { this.binding.rewardEstimatedDelivery.text = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.estimatedDeliveryIsGone()
            .compose(observeForUIV2())
            .subscribe { this.binding.rewardEstimatedDeliverySection.isGone = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.isMinimumPledgeAmountGone()
            .compose(observeForUIV2())
            .subscribe {
                this.binding.rewardConversionTextView.isGone = it
                this.binding.rewardMinimumTextView.isGone = it
            }
            .addToDisposable(disposables)

        this.binding.rewardPledgeButton.setOnClickListener {
            this.viewModel.inputs.rewardClicked(this.adapterPosition)
        }

        this.viewModel.outputs.hasAddOnsAvailable()
            .filter { it.isNotNull() }
            .compose(observeForUIV2())
            .subscribe {
                this.binding.rewardAddOnsAvailable.isGone = !it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.selectedRewardTagIsGone()
            .compose(observeForUIV2())
            .subscribe { isGone ->
                if (!isGone) this.binding.rewardSelectedRewardTag.visibility = View.VISIBLE
                else this.binding.rewardSelectedRewardTag.isGone = true
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.localPickUpIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                this.binding.localPickupContainer.localPickupGroup.isGone = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.localPickUpName()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                this.binding.localPickupContainer.localPickupLocation.text = it
            }
            .addToDisposable(disposables)
    }

    override fun bindData(data: Any?) {
        @Suppress("UNCHECKED_CAST")
        val projectAndReward = requireNotNull(data as Pair<ProjectData, Reward>)
        val projectTracking = requireNotNull(projectAndReward.first) { ProjectData::class.java.toString() + "   required to be non-null." }
        val reward = requireNotNull(projectAndReward.second) { Reward::class.java.toString() + "   required to be non-null." }

        this.viewModel.inputs.configureWith(projectTracking, reward)
    }

    private fun formattedExpirationString(reward: Reward): String {
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

    private fun setConversionTextView(amount: String) {
        this.binding.rewardConversionTextView.text = this.ksString.format(
            this.currencyConversionString,
            "reward_amount", amount
        )
    }

    private fun setPledgeButtonVisibility(gone: Boolean) {
        if (this.inset.isTrue()) {
            ViewUtils.setGone(this.binding.rewardButtonContainer, true)
            ViewUtils.setGone(this.binding.rewardButtonPlaceholder, true)
        } else {
            ViewUtils.setGone(this.binding.rewardButtonContainer, gone)
            when {
                gone -> ViewUtils.setGone(this.binding.rewardButtonPlaceholder, true)
                else -> ViewUtils.setInvisible(this.binding.rewardButtonPlaceholder, true)
            }
        }
    }

    private fun setRemainingRewardsTextView(remaining: Int) {
        if (remaining > 0) {
            this.binding.rewardRemainingTextView.isGone = false
            this.binding.rewardRemainingTextView.text = this.ksString.format(
                this.remainingRewardsString,
                "left_count", NumberUtils.format(remaining)
            )
        } else {
            this.binding.rewardRemainingTextView.isGone = true
        }
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

    override fun destroy() {
        viewModel.onCleared()
        super.destroy()
    }
}
