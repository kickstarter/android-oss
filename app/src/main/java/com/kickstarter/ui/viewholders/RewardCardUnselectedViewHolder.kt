package com.kickstarter.ui.viewholders

import android.util.Pair
import androidx.core.content.ContextCompat
import com.kickstarter.R
import com.kickstarter.databinding.ItemRewardUnselectedCardBinding
import com.kickstarter.libs.KSString
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Project
import com.kickstarter.models.StoredCard
import com.kickstarter.viewmodels.RewardCardUnselectedViewHolderViewModel
import kotlinx.android.synthetic.main.retry_card_warning.view.*
import kotlinx.android.synthetic.main.reward_card_details.view.*

class RewardCardUnselectedViewHolder(val binding: ItemRewardUnselectedCardBinding, val delegate: Delegate) : KSViewHolder(binding.root) {

    interface Delegate {
        fun cardSelected(storedCard: StoredCard, position: Int)
    }

    private val viewModel: RewardCardUnselectedViewHolderViewModel.ViewModel = RewardCardUnselectedViewHolderViewModel.ViewModel(environment())
    private val ksString: KSString = environment().ksString()

    private val creditCardExpirationString = this.context().getString(R.string.Credit_card_expiration)
    private val lastFourString = this.context().getString(R.string.payment_method_last_four)

    init {

        this.viewModel.outputs.expirationDate()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setExpirationDateText(it) }

        this.viewModel.outputs.isClickable()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.binding.cardContainer.isClickable = it }

        this.viewModel.outputs.issuerImage()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.binding.root.reward_card_logo.setImageResource(it) }

        this.viewModel.outputs.issuer()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.binding.root.reward_card_logo.contentDescription = it }

        this.viewModel.outputs.issuerImageAlpha()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.binding.root.reward_card_logo.alpha = it }

        this.viewModel.outputs.lastFour()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setLastFourText(it) }

        this.viewModel.outputs.lastFourTextColor()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.binding.root.reward_card_last_four.setTextColor(ContextCompat.getColor(context(), it)) }

        this.viewModel.outputs.notAvailableCopyIsVisible()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { ViewUtils.setGone(this.binding.cardNotAllowedWarning, !it) }

        this.viewModel.outputs.notifyDelegateCardSelected()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.delegate.cardSelected(it.first, it.second) }

        this.viewModel.outputs.retryCopyIsVisible()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { ViewUtils.setGone(this.binding.root.retry_card_warning, !it) }

        this.viewModel.outputs.selectImageIsVisible()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { ViewUtils.setInvisible(this.binding.selectImageView, !it) }

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
        this.binding.root.reward_card_expiration_date.text = this.ksString.format(
            this.creditCardExpirationString,
            "expiration_date", date
        )
    }

    private fun setLastFourText(lastFour: String) {
        this.binding.root.reward_card_last_four.text = this.ksString.format(
            this.lastFourString,
            "last_four",
            lastFour
        )
    }
}
