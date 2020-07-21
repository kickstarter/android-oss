package com.kickstarter.ui.viewholders

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.util.Pair
import android.view.View
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Reward
import com.kickstarter.ui.adapters.RewardItemsAdapter
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.BackingAddOnViewHolderViewModel
import kotlinx.android.synthetic.main.add_on_items.view.*
import kotlinx.android.synthetic.main.add_on_title.view.*
import kotlinx.android.synthetic.main.item_add_on_pledge.view.*

class BackingAddOnViewHolder(private val view: View) : KSViewHolder(view) {

    private var viewModel = BackingAddOnViewHolderViewModel.ViewModel(environment())
    private val ksString = environment().ksString()

    init {

        val rewardItemAdapter = setUpItemAdapter()


        this.viewModel.outputs
                .description()
                .compose(bindToLifecycle())
                .subscribe {
                    this.view.add_on_description.text = it
                }


        this.viewModel.outputs.rewardItems()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { rewardItemAdapter.rewardsItems(it) }


        this.viewModel.outputs.titleForAddOn()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { this.view.add_on_title_text_view.text = it }


        this.viewModel.outputs.description()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { this.view.add_on_description.text = it }


        this.viewModel.outputs.minimum()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    this.view.add_on_minimum.text = it
                }

        this.viewModel.outputs.conversionIsGone()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { ViewUtils.setGone(this.view.add_on_conversion, it) }


        this.viewModel.outputs.conversion()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { this.view.add_on_conversion.text = "About $it" }


        this.viewModel.outputs.deadlinePillIsGone()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { ViewUtils.setGone(this.view.addon_time_left, it) }

        this.viewModel.outputs.backerLimitPillIsGone()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { ViewUtils.setGone(this.view.addon_backer_limit, it) }

        this.viewModel.outputs.remainingQuantityPillIsGone()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { ViewUtils.setGone(this.view.addon_quantity_remaining, it) }

        this.viewModel.outputs.backerLimit()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { this.view.addon_backer_limit.text = "Limit $it" }

        this.viewModel.outputs.remainingQuantity()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { this.view.addon_quantity_remaining.text = "$it left" }

        this.viewModel.outputs.deadlineCountdownIsGone()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    ViewUtils.setGone(this.view.addon_time_left, it) }

        this.viewModel.outputs.deadlineCountdown()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { this.view.addon_time_left.text = formattedExpirationString(it)  }

        this.viewModel.outputs.shippingAmountIsGone()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { ViewUtils.setGone(this.view.add_on_shipping_amount, it) }

        this.viewModel.outputs.shippingAmount()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    this.view.add_on_shipping_amount.text = this.ksString.format(context().getString(R.string.add_on_shipping_amount_label), "shipping_amount", it)
                }



    }

    private fun formattedExpirationString(@NonNull reward: Reward): String {
        val detail = RewardUtils.deadlineCountdownDetail(reward, context(), this.ksString)
        val value = RewardUtils.deadlineCountdownValue(reward)
        return "$value $detail"
    }

    override fun bindData(data: Any?) {
        if (data is (Pair<*, *>)) {
            if (data.second is Reward) {
                bindAddonsList(data as Pair<ProjectData, Reward>)
            }
        }
    }

    private fun bindAddonsList(projectDataAndAddOn: Pair<ProjectData, Reward>) {
        this.viewModel.inputs.configureWith(projectDataAndAddOn)
    }

    private fun setUpItemAdapter(): RewardItemsAdapter {
        val rewardItemAdapter = RewardItemsAdapter()
        val itemRecyclerView = view.add_on_item_recycler_view
        itemRecyclerView.adapter = rewardItemAdapter
        itemRecyclerView.layoutManager = LinearLayoutManager(context())
        return rewardItemAdapter
    }
}