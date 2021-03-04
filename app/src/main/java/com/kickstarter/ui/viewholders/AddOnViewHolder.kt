package com.kickstarter.ui.viewholders

import android.util.Pair
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.ItemAddOnBinding
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.RewardViewUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Reward
import com.kickstarter.ui.adapters.RewardItemsAdapter
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.AddOnViewHolderViewModel


class AddOnViewHolder(private val binding: ItemAddOnBinding) : KSViewHolder(binding.root) {

    private var viewModel = AddOnViewHolderViewModel.ViewModel(environment())
    private val currencyConversionString = context().getString(R.string.About_reward_amount)
    private val ksString = environment().ksString()

    init {
        val rewardItemAdapter = setUpItemAdapter()

        this.viewModel.outputs.conversionIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe(ViewUtils.setGone(binding.addOnConversionTextView))

        this.viewModel.outputs.conversion()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { binding.addOnConversionTextView.text = this.ksString.format(this.currencyConversionString,
                        "reward_amount", it) }

        this.viewModel.outputs.descriptionForNoReward()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { binding.addOnDescriptionTextView.setText(it) }

        this.viewModel.outputs.titleForNoReward()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { binding.titleContainer.addOnTitleNoSpannable.setText(it) }

        this.viewModel.outputs.descriptionForReward()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { binding.addOnDescriptionTextView.text = it }

        this.viewModel.outputs.minimumAmountTitle()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { binding.addOnMinimumTextView.text = it }

        this.viewModel.outputs.rewardItems()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { rewardItemAdapter.rewardsItems(it) }

        this.viewModel.outputs.rewardItemsAreGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe(ViewUtils.setGone(binding.addOnItemsContainer.addOnItemLayout))

        this.viewModel.outputs.isAddonTitleGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { shouldHideAddonAmount ->
                    if (shouldHideAddonAmount) {
                        binding.titleContainer.addOnTitleTextView.visibility = View.GONE
                        binding.titleContainer.addOnTitleNoSpannable.visibility = View.VISIBLE
                    } else {
                        binding.titleContainer.addOnTitleNoSpannable.visibility = View.GONE
                        binding.titleContainer.addOnTitleTextView.visibility = View.VISIBLE
                    }
                }

        this.viewModel.outputs.titleForReward()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { binding.titleContainer.addOnTitleNoSpannable.text = it }

        this.viewModel.outputs.titleForAddOn()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { binding.titleContainer.addOnTitleTextView.text = RewardViewUtils.styleTitleForAddOns(context(), it.first, it.second) }
    }

    override fun bindData(data: Any?) {
        if (data is (Pair<*, *>)) {
            if (data.second is Reward) {
                bindReward(data as Pair<ProjectData, Reward>)
            }
        }
    }

    private fun bindReward(projectAndReward: Pair<ProjectData, Reward>) {
        this.viewModel.inputs.configureWith(projectAndReward.first, projectAndReward.second)
    }

    private fun setUpItemAdapter(): RewardItemsAdapter {
        val rewardItemAdapter = RewardItemsAdapter()
        val itemRecyclerView = binding.addOnItemsContainer.addOnItemRecyclerView
        itemRecyclerView.adapter = rewardItemAdapter
        itemRecyclerView.layoutManager = LinearLayoutManager(context())
        return rewardItemAdapter
    }

}
