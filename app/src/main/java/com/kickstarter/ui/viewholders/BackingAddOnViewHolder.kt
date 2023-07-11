package com.kickstarter.ui.viewholders

import android.util.Pair
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.ItemAddOnPledgeBinding
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.adapters.RewardItemsAdapter
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.BackingAddOnViewHolderViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class BackingAddOnViewHolder(private val binding: ItemAddOnPledgeBinding, private val viewListener: ViewListener) : KSViewHolder(binding.root) {

    interface ViewListener {
        fun quantityPerId(quantityPerId: Pair<Int, Long>)
    }

    private lateinit var viewModel: BackingAddOnViewHolderViewModel.BackingAddOnViewHolderViewModel
    private val disposables = CompositeDisposable()
    private val ksString = requireNotNull(environment().ksString())

    init {
        this.context().getEnvironment()?.let { env ->
            viewModel = BackingAddOnViewHolderViewModel.BackingAddOnViewHolderViewModel(env)
        }

        val rewardItemAdapter = RewardItemsAdapter()
        binding.addOnCard.setUpItemsAdapter(rewardItemAdapter, LinearLayoutManager(context()))

        this.viewModel.outputs.rewardItemsAreGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.addOnCard.setAddOnItemLayoutVisibility(!it)
                binding.addOnCard.setDividerVisibility(!it)
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.rewardItems()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { rewardItemAdapter.rewardsItems(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.titleForAddOn()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.addOnCard.setAddOnTitleText(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.description()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.addOnCard.setAddOnDescription(it)
                binding.addOnCard.setAddonDescriptionVisibility(!it.isNullOrEmpty())
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.minimum()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.addOnCard.setAddOnMinimumText(it.toString())
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.conversionIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.addOnCard.setAddonConversionVisibility(!it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.conversion()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.addOnCard.setAddonConversionText(
                    this.ksString.format(
                        context().getString(R.string.About_reward_amount),
                        "reward_amount",
                        it.toString()
                    )
                )
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.backerLimitPillIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.addOnCard.setBackerLimitPillVisibility(!it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.remainingQuantityPillIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.addOnCard.setAddonQuantityRemainingPillVisibility(!it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.backerLimit()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.addOnCard.setBackerLimitText(
                    this.ksString.format(
                        context().getString(R.string.limit_limit_per_backer),
                        "limit_per_backer",
                        it
                    )
                )
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.remainingQuantity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.addOnCard.setAddonQuantityRemainingText(
                    this.ksString.format(
                        context().getString(R.string.rewards_info_time_left),
                        "time",
                        it
                    )
                )
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.deadlineCountdownIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.addOnCard.setTimeLeftVisibility(!it)
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.deadlineCountdown()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.addOnCard.setTimeLeftText(formattedExpirationString(it)) }
            .addToDisposable(disposables)

        this.viewModel.outputs.shippingAmountIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.addOnCard.setShippingAmountVisibility(!it)
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.shippingAmount()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.isNotEmpty()) {
                    val rewardAndShippingString =
                        context().getString(R.string.reward_amount_plus_shipping_cost_each)
                    val stringSections = rewardAndShippingString.split("+")
                    val shippingString = "+" + stringSections[1]
                    binding.addOnCard.setShippingAmountText(
                        this.ksString.format(
                            shippingString,
                            "shipping_cost",
                            it
                        )
                    )
                }
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.maxQuantity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.addOnCard.setStepperMax(it)
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.quantityPerId()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { quantityPerId ->
                quantityPerId?.let { viewListener.quantityPerId(it) }
                val quantity = quantityPerId.first
                binding.addOnCard.setStepperInitialValue(quantity)
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.localPickUpIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                this.binding.addOnCard.setLocalPickUpIsGone(it)
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.localPickUpName()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                this.binding.addOnCard.setLocalPickUpName(it)
            }
            .addToDisposable(disposables)

        binding.addOnCard.outputs.stepperQuantity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { viewModel.inputs.currentQuantity(it) }
            .addToDisposable(disposables)
    }

    private fun formattedExpirationString(reward: Reward): String {
        val detail = RewardUtils.deadlineCountdownDetail(reward, context(), this.ksString)
        val value = RewardUtils.deadlineCountdownValue(reward)
        return "$value $detail"
    }

    @Suppress("UNCHECKED_CAST")
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

    override fun destroy() {
        this.viewModel.clearDisposables()
        disposables.clear()
        super.destroy()
    }
}
