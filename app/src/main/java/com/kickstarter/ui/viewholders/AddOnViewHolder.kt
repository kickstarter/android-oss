package com.kickstarter.ui.viewholders

import android.util.Pair
import android.view.View
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.size.Precision
import com.kickstarter.R
import com.kickstarter.databinding.ItemAddOnBinding
import com.kickstarter.libs.rx.transformers.Transformers.observeForUIV2
import com.kickstarter.libs.utils.RewardViewUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.models.Reward
import com.kickstarter.ui.adapters.RewardItemsAdapter
import com.kickstarter.ui.compose.designsystem.KSSecretRewardBadge
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.AddOnViewHolderViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class AddOnViewHolder(private val binding: ItemAddOnBinding) : KSViewHolder(binding.root) {

    private var viewModel = AddOnViewHolderViewModel.ViewModel(environment())
    private val currencyConversionString = context().getString(R.string.About_reward_amount)
    private val ksString = requireNotNull(environment().ksString())
    private val disposables = CompositeDisposable()

    init {
        val rewardItemAdapter = setUpItemAdapter()

        this.viewModel.outputs.imageForReward()
            .compose(observeForUIV2())
            .subscribe {
                binding.addOnImageView.visibility = View.VISIBLE
                binding.addOnImageView.load(it.full()) {
                    precision(Precision.EXACT)
                    placeholder(R.color.soft_grey_disable)
                }
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.conversionIsGone()
            .compose(observeForUIV2())
            .subscribe {
                ViewUtils.setGone(binding.addOnConversionTextView, it)
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.conversion()
            .compose(observeForUIV2())
            .subscribe {
                binding.addOnConversionTextView.text = this.ksString.format(
                    this.currencyConversionString,
                    "reward_amount", it
                )
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.descriptionForNoReward()
            .compose(observeForUIV2())
            .subscribe { binding.addOnDescriptionTextView.setText(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.titleForNoReward()
            .compose(observeForUIV2())
            .subscribe { binding.titleContainer.addOnTitleNoSpannable.setText(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.descriptionForReward()
            .compose(observeForUIV2())
            .subscribe { binding.addOnDescriptionTextView.text = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.minimumAmountTitle()
            .compose(observeForUIV2())
            .subscribe { binding.addOnMinimumTextView.text = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.rewardItems()
            .compose(observeForUIV2())
            .subscribe { rewardItemAdapter.rewardsItems(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.rewardItemsAreGone()
            .compose(observeForUIV2())
            .subscribe {
                ViewUtils.setGone(binding.addOnItemsContainer.addOnItemLayout, it)
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.isAddonTitleGone()
            .compose(observeForUIV2())
            .subscribe { shouldHideAddonAmount ->
                if (shouldHideAddonAmount) {
                    binding.titleContainer.addOnTitleTextView.visibility = View.GONE
                    binding.titleContainer.addOnTitleNoSpannable.visibility = View.VISIBLE
                } else {
                    binding.titleContainer.addOnTitleNoSpannable.visibility = View.GONE
                    binding.titleContainer.addOnTitleTextView.visibility = View.VISIBLE
                }
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.titleForReward()
            .compose(observeForUIV2())
            .subscribe { binding.titleContainer.addOnTitleNoSpannable.text = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.titleForAddOn()
            .compose(observeForUIV2())
            .subscribe { binding.titleContainer.addOnTitleTextView.text = RewardViewUtils.styleTitleForAddOns(context(), it.first, it.second) }
            .addToDisposable(disposables)

        this.viewModel.outputs.localPickUpIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.rewardItemLocalPickupContainer.localPickupGroup.isGone = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.localPickUpName()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.rewardItemLocalPickupContainer.localPickupLocation.text = it
            }
            .addToDisposable(disposables)
    }

    override fun bindData(data: Any?) {
        if (data is (Pair<*, *>)) {
            if (data.second is Reward) {
                bindReward(data as Pair<ProjectData, Reward>)
            }
        }
    }

    override fun destroy() {
        viewModel.clear()
        disposables.clear()
        super.destroy()
    }

    private fun bindReward(projectAndReward: Pair<ProjectData, Reward>) {
        val project = projectAndReward.first
        val reward = projectAndReward.second

        viewModel.inputs.configureWith(project, reward)

        val hasImage = reward.image()?.full()?.isNotEmpty() == true

        val badgeOverImage = binding.secretBadgeComposeOverImage
        val badgeAboveCard = binding.secretBadgeComposeAboveCard

        if (reward.isSecretReward() == true) {
            if (hasImage) {
                badgeOverImage.visibility = View.VISIBLE
                badgeAboveCard.visibility = View.GONE

                badgeOverImage.setContent {
                    KSTheme {
                        KSSecretRewardBadge()
                    }
                }
            } else {
                badgeOverImage.visibility = View.GONE
                badgeAboveCard.visibility = View.VISIBLE

                badgeAboveCard.setContent {
                    KSTheme {
                        KSSecretRewardBadge()
                    }
                }
            }
        } else {
            badgeOverImage.visibility = View.GONE
            badgeAboveCard.visibility = View.GONE
        }
    }

    private fun setUpItemAdapter(): RewardItemsAdapter {
        val rewardItemAdapter = RewardItemsAdapter()
        val itemRecyclerView = binding.addOnItemsContainer.addOnItemRecyclerView
        itemRecyclerView.adapter = rewardItemAdapter
        itemRecyclerView.layoutManager = LinearLayoutManager(context())
        return rewardItemAdapter
    }
}
