package com.kickstarter.ui.viewholders

import android.util.Pair
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.RewardViewUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Reward
import com.kickstarter.ui.adapters.RewardItemsAdapter
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.AddOnViewHolderViewModel
import kotlinx.android.synthetic.main.add_on_items.view.*
import kotlinx.android.synthetic.main.add_on_title.view.*
import kotlinx.android.synthetic.main.item_add_on.view.*

class AddOnViewHolder(private val view: View) : KSViewHolder(view) {

    private var viewModel = AddOnViewHolderViewModel.ViewModel(environment())
    private val currencyConversionString = context().getString(R.string.About_reward_amount)
    private val ksString = environment().ksString()

    init {
        val rewardItemAdapter = setUpItemAdapter()

        this.viewModel.outputs.conversionIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe(ViewUtils.setGone(this.view.add_on_conversion_text_view))

        this.viewModel.outputs.conversion()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.add_on_conversion_text_view.text = this.ksString.format(this.currencyConversionString,
                        "reward_amount", it) }

        this.viewModel.outputs.descriptionForNoReward()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.add_on_description_text_view.setText(it) }

        this.viewModel.outputs.titleForNoReward()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.add_on_title_no_spannable.setText(it) }

        this.viewModel.outputs.descriptionForReward()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.add_on_description_text_view.text = it }

        this.viewModel.outputs.minimumAmountTitle()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.add_on_minimum_text_view.text = it }

        this.viewModel.outputs.rewardItems()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { rewardItemAdapter.rewardsItems(it) }

        this.viewModel.outputs.rewardItemsAreGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe(ViewUtils.setGone(this.view.items_container))

        this.viewModel.outputs.isAddonTitleGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { shouldHideAddonAmount ->
                    if (shouldHideAddonAmount) {
                        this.view.add_on_title_text_view.visibility = View.GONE
                        this.view.add_on_title_no_spannable.visibility = View.VISIBLE
                    } else {
                        this.view.add_on_title_no_spannable.visibility = View.GONE
                        this.view.add_on_title_text_view.visibility = View.VISIBLE
                    }
                }

        this.viewModel.outputs.titleForReward()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.add_on_title_no_spannable.text = it }

        this.viewModel.outputs.titleForAddOn()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.add_on_title_text_view.text = RewardViewUtils.styleTitleForAddOns(context(), it.first, it.second) }
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
        val itemRecyclerView = view.add_on_item_recycler_view
        itemRecyclerView.adapter = rewardItemAdapter
        itemRecyclerView.layoutManager = LinearLayoutManager(context())
        return rewardItemAdapter
    }

}
