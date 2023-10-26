package com.kickstarter.ui.viewholders

import android.util.Pair
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.kickstarter.R
import com.kickstarter.databinding.ItemRewardUnselectedCardBinding
import com.kickstarter.libs.rx.transformers.Transformers.observeForUIV2
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.models.Project
import com.kickstarter.models.StoredCard
import com.kickstarter.viewmodels.RewardCardUnselectedViewHolderViewModel
import io.reactivex.disposables.CompositeDisposable

class RewardCardUnselectedViewHolder(val binding: ItemRewardUnselectedCardBinding, val delegate: Delegate) : KSViewHolder(binding.root) {

    interface Delegate {
        fun cardSelected(storedCard: StoredCard, position: Int)
    }

    private val viewModel: RewardCardUnselectedViewHolderViewModel.ViewModel = RewardCardUnselectedViewHolderViewModel.ViewModel()
    private val ksString = requireNotNull(environment().ksString())

    private val creditCardExpirationString = this.context().getString(R.string.Credit_card_expiration)
    private val lastFourString = this.context().getString(R.string.payment_method_last_four)
    private val disposables = CompositeDisposable()

    init {

        this.viewModel.outputs.expirationDate()
            .compose(observeForUIV2())
            .subscribe { setExpirationDateText(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.expirationIsGone()
            .compose(observeForUIV2())
            .subscribe {
                this.binding.rewardCardDetailsLayout.rewardCardExpirationDate.isGone = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.isClickable()
            .compose(observeForUIV2())
            .subscribe { this.binding.cardContainer.isClickable = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.issuerImage()
            .compose(observeForUIV2())
            .subscribe { this.binding.rewardCardDetailsLayout.rewardCardLogo.setImageResource(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.issuer()
            .compose(observeForUIV2())
            .subscribe { this.binding.rewardCardDetailsLayout.rewardCardLogo.contentDescription = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.issuerImageAlpha()
            .compose(observeForUIV2())
            .subscribe { this.binding.rewardCardDetailsLayout.rewardCardLogo.alpha = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.lastFour()
            .compose(observeForUIV2())
            .subscribe { setLastFourText(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.lastFourTextColor()
            .compose(observeForUIV2())
            .subscribe { this.binding.rewardCardDetailsLayout.rewardCardLastFour.setTextColor(ContextCompat.getColor(context(), it)) }
            .addToDisposable(disposables)

        this.viewModel.outputs.notAvailableCopyIsVisible()
            .compose(observeForUIV2())
            .subscribe { ViewUtils.setGone(this.binding.cardNotAllowedWarning, !it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.notifyDelegateCardSelected()
            .compose(observeForUIV2())
            .subscribe { this.delegate.cardSelected(it.first, it.second) }
            .addToDisposable(disposables)

        this.viewModel.outputs.retryCopyIsVisible()
            .compose(observeForUIV2())
            .subscribe { ViewUtils.setGone(this.binding.retryCardWarningLayout.retryCardWarning, !it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.selectImageIsVisible()
            .compose(observeForUIV2())
            .subscribe { ViewUtils.setInvisible(this.binding.selectImageView, !it) }
            .addToDisposable(disposables)

        this.binding.cardContainer.setOnClickListener {
            this.viewModel.inputs.cardSelected(adapterPosition)
        }
    }

    override fun bindData(data: Any?) {
        @Suppress("UNCHECKED_CAST")
        val cardAndProject = requireNotNull(data) as Pair<StoredCard, Project>
        this.viewModel.inputs.configureWith(cardAndProject)
    }

    private fun setExpirationDateText(date: String) {
        this.binding.rewardCardDetailsLayout.rewardCardExpirationDate.text = this.ksString.format(
            this.creditCardExpirationString,
            "expiration_date", date
        )
    }

    private fun setLastFourText(lastFour: String) {
        this.binding.rewardCardDetailsLayout.rewardCardLastFour.text = this.ksString.format(
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
