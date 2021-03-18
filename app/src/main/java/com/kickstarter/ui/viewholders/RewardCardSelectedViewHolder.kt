package com.kickstarter.ui.viewholders

import android.util.Pair
import com.kickstarter.R
import com.kickstarter.databinding.ItemRewardSelectedCardBinding
import com.kickstarter.libs.KSString
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Project
import com.kickstarter.models.StoredCard
import com.kickstarter.viewmodels.RewardCardSelectedViewHolderViewModel

class RewardCardSelectedViewHolder(val binding: ItemRewardSelectedCardBinding) : KSViewHolder(binding.root) {

    private val creditCardExpirationString = this.context().getString(R.string.Credit_card_expiration)
    private val lastFourString = this.context().getString(R.string.payment_method_last_four)

    private val viewModel: RewardCardSelectedViewHolderViewModel.ViewModel = RewardCardSelectedViewHolderViewModel.ViewModel(environment())
    private val ksString: KSString = environment().ksString()

    init {

        this.viewModel.outputs.expirationDate()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setExpirationDateTextView(it) }

        this.viewModel.outputs.issuerImage()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { binding.rewardCardDetails.rewardCardLogo.setImageResource(it) }

        this.viewModel.outputs.issuer()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { binding.rewardCardDetails.rewardCardLogo.contentDescription = it }

        this.viewModel.outputs.lastFour()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setLastFourTextView(it) }

        this.viewModel.outputs.retryCopyIsVisible()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { ViewUtils.setGone(binding.retryCardWarning.retryCardWarning, !it) }
    }

    override fun bindData(data: Any?) {
        @Suppress("UNCHECKED_CAST")
        val cardAndProject = requireNotNull(data) as Pair<StoredCard, Project>
        this.viewModel.inputs.configureWith(cardAndProject)
    }

    private fun setExpirationDateTextView(date: String) {
        binding.rewardCardDetails.rewardCardExpirationDate.text = this.ksString.format(
            this.creditCardExpirationString,
            "expiration_date", date
        )
    }

    private fun setLastFourTextView(lastFour: String) {
        binding.rewardCardDetails.rewardCardLastFour.text = this.ksString.format(
            this.lastFourString,
            "last_four",
            lastFour
        )
    }
}
