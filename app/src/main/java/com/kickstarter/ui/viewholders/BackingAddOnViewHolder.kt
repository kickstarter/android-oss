package com.kickstarter.ui.viewholders

import android.util.Pair
import android.view.View
import androidx.annotation.NonNull
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.ItemAddOnPledgeBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.adapters.RewardItemsAdapter
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.BackingAddOnViewHolderViewModel

class BackingAddOnViewHolder(private val binding: ItemAddOnPledgeBinding, private val viewListener: ViewListener) : KSViewHolder(binding.root) {

    interface ViewListener {
        fun quantityPerId(quantityPerId: Pair<Int, Long>)
    }

    private var viewModel = BackingAddOnViewHolderViewModel.ViewModel(environment())
    private val ksString = environment().ksString()

    init {

        val rewardItemAdapter = setUpItemAdapter()

        setListenerForDecreaseButton()
        setListenerForIncreaseButton()
        setListenerForAddButton()

        this.viewModel.outputs
                .description()
                .compose(bindToLifecycle())
                .subscribe {
                    binding.addOnDescription.text = it
                }

        this.viewModel.outputs.rewardItemsAreGone()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    ViewUtils.setGone(binding.itemsContainer.addOnItemLayout, it)
                    ViewUtils.setGone(binding.divider, it)
                }

        this.viewModel.outputs.rewardItems()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { rewardItemAdapter.rewardsItems(it) }

        this.viewModel.outputs.titleForAddOn()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { binding.titleContainer.addOnTitleTextView.text = it }


        this.viewModel.outputs.description()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { binding.addOnDescription.text = it }


        this.viewModel.outputs.minimum()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    binding.addOnMinimum.text = it.toString()
                }

        this.viewModel.outputs.conversionIsGone()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { ViewUtils.setGone(binding.addOnConversion, it) }


        this.viewModel.outputs.conversion()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    binding.addOnConversion.text = this.ksString.format(context().getString(R.string.About_reward_amount), "reward_amount", it.toString())
                }


        this.viewModel.outputs.backerLimitPillIsGone()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { ViewUtils.setGone(binding.addonBackerLimit, it) }

        this.viewModel.outputs.remainingQuantityPillIsGone()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { ViewUtils.setGone(binding.addonQuantityRemaining, it) }

        this.viewModel.outputs.backerLimit()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    binding.addonBackerLimit.text = this.ksString.format(context().getString(R.string.limit_limit_per_backer), "limit_per_backer", it)
                }

        this.viewModel.outputs.remainingQuantity()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    binding.addonQuantityRemaining.text =
                            this.ksString.format(context().getString(R.string.rewards_info_time_left), "time", it)
                }

        this.viewModel.outputs.deadlineCountdownIsGone()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    ViewUtils.setGone(binding.addonTimeLeft, it) }

        this.viewModel.outputs.deadlineCountdown()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { binding.addonTimeLeft.text = formattedExpirationString(it)  }

        this.viewModel.outputs.shippingAmountIsGone()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    ViewUtils.setGone(binding.addOnShippingAmount, it)
                }

        this.viewModel.outputs.shippingAmount()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    if  (it.isNotEmpty()) {
                        val rewardAndShippingString = context().getString(R.string.reward_amount_plus_shipping_cost_each)
                        val stringSections = rewardAndShippingString.split("+")
                        val shippingString = "+" + stringSections[1]
                        binding.addOnShippingAmount.text = this.ksString.format(shippingString, "shipping_cost", it)
                    }
                }

        this.viewModel.outputs.addButtonIsGone()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { addButtonIsGone ->
                    if (addButtonIsGone) {
                        showStepper()
                    }
                    else {
                        hideStepper()
                    }
                }

        this.viewModel.outputs.quantityPerId()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { quantityPerId ->
                    quantityPerId?.let { viewListener.quantityPerId(it) }
                    val quantity = quantityPerId.first
                    binding.decreaseQuantityAddOn.isEnabled = (quantity != 0)
                    binding.quantityAddOn.text = quantity.toString()
                }

        this.viewModel.outputs.disableIncreaseButton()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    binding.increaseQuantityAddOn.isEnabled = !it
                }
    }

    private fun hideStepper() {
        binding.initialStateAddOn.visibility = View.VISIBLE
        binding.stepperContainerAddOn.visibility = View.GONE
        binding.initialStateAddOn.isEnabled = true
        binding.increaseQuantityAddOn.isEnabled = false
    }

    private fun showStepper() {
        binding.initialStateAddOn.visibility = View.GONE
        binding.stepperContainerAddOn.visibility = View.VISIBLE
        binding.initialStateAddOn.isEnabled = false
        binding.increaseQuantityAddOn.isEnabled = true
    }

    private fun setListenerForAddButton() {
        binding.initialStateAddOn.setOnClickListener {
            this.viewModel.inputs.addButtonPressed()
        }
    }

    private fun setListenerForIncreaseButton() {
        binding.increaseQuantityAddOn.setOnClickListener {
            this.viewModel.inputs.increaseButtonPressed()
        }
    }

    private fun setListenerForDecreaseButton() {
        binding.decreaseQuantityAddOn.setOnClickListener {
            this.viewModel.inputs.decreaseButtonPressed()
        }
    }

    private fun formattedExpirationString(@NonNull reward: Reward): String {
        val detail = RewardUtils.deadlineCountdownDetail(reward, context(), this.ksString)
        val value = RewardUtils.deadlineCountdownValue(reward)
        return "$value $detail"
    }

    override fun bindData(data: Any?) {
        if (data is (Triple<*, *, *>)) {
            if (data.second is Reward) {
                bindAddonsList(data as Triple<ProjectData, Reward, ShippingRule>)
            }
        }
    }

    private fun bindAddonsList(projectDataAndAddOn: Triple<ProjectData, Reward, ShippingRule>) {
        this.viewModel.inputs.configureWith(projectDataAndAddOn)
    }

    private fun setUpItemAdapter(): RewardItemsAdapter {
        val rewardItemAdapter = RewardItemsAdapter()
        binding.itemsContainer.addOnItemRecyclerView.apply {
           adapter = rewardItemAdapter
           layoutManager = LinearLayoutManager(context())
       }
      
        return rewardItemAdapter
    }
}