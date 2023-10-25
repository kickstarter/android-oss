package com.kickstarter.ui.viewholders

import android.util.Pair
import androidx.core.view.isGone
import com.kickstarter.R
import com.kickstarter.databinding.ItemRewardSelectedCardBinding
import com.kickstarter.libs.rx.transformers.Transformers.observeForUIV2
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.models.Project
import com.kickstarter.models.StoredCard
import com.kickstarter.viewmodels.RewardCardSelectedViewHolderViewModel
import io.reactivex.disposables.CompositeDisposable

class RewardCardSelectedViewHolder(val binding: ItemRewardSelectedCardBinding) : KSViewHolder(binding.root) {

    private val creditCardExpirationString = this.context().getString(R.string.Credit_card_expiration)
    private val lastFourString = this.context().getString(R.string.payment_method_last_four)

    private val viewModel: RewardCardSelectedViewHolderViewModel.ViewModel = RewardCardSelectedViewHolderViewModel.ViewModel()
    private val ksString = requireNotNull(environment().ksString())
    private val disposables = CompositeDisposable()

    init {

        this.viewModel.outputs.expirationDate()
            .compose(observeForUIV2())
            .subscribe { setExpirationDateTextView(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.expirationIsGone()
            .compose(observeForUIV2())
            .subscribe {
                binding.rewardCardDetails.rewardCardExpirationDate.isGone = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.issuerImage()
            .compose(observeForUIV2())
            .subscribe { binding.rewardCardDetails.rewardCardLogo.setImageResource(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.issuer()
            .compose(observeForUIV2())
            .subscribe { binding.rewardCardDetails.rewardCardLogo.contentDescription = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.lastFour()
            .compose(observeForUIV2())
            .subscribe { setLastFourTextView(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.retryCopyIsVisible()
            .compose(observeForUIV2())
            .subscribe { ViewUtils.setGone(binding.retryCardWarning.retryCardWarning, !it) }
            .addToDisposable(disposables)
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

    override fun destroy() {
        disposables.clear()
        viewModel.onCleared()
        super.destroy()
    }
}
