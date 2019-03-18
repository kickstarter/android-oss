package com.kickstarter.ui.viewholders

import android.view.View
import com.kickstarter.R
import com.kickstarter.libs.KSString
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.models.StoredCard
import com.kickstarter.viewmodels.RewardCardViewHolderViewModel
import kotlinx.android.synthetic.main.item_reward_credit_card.view.*

class RewardCardViewHolder(val view : View, val delegate : Delegate) : KSViewHolder(view) {

    interface Delegate {
        fun selectCardButtonClicked(viewHolder: ProjectContextViewHolder)
    }

    private val viewModel: RewardCardViewHolderViewModel.ViewModel = RewardCardViewHolderViewModel.ViewModel(environment())
    private val ksString: KSString = environment().ksString()

    private val creditCardExpirationString = this.context().getString(R.string.Credit_card_expiration)
    private val cardEndingInString = this.context().getString(R.string.Card_ending_in_last_four)

    init {

        this.viewModel.outputs.issuerImage()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { view.reward_card_logo.setImageResource(it) }

        this.viewModel.outputs.expirationDate()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { setExpirationDateTextView(it) }

        this.viewModel.outputs.lastFour()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { setLastFourTextView(it) }
    }

    override fun bindData(data: Any?) {
        val card = requireNotNull(data as StoredCard)
        this.viewModel.inputs.configureWith(card)
    }

    private fun setExpirationDateTextView(date: String) {
        view.reward_card_expiration_date.text = this.ksString.format(this.creditCardExpirationString,
                "expiration_date", date)
    }

    private fun setLastFourTextView(lastFour: String) {
        view.reward_card_last_four.text = this.ksString.format(this.cardEndingInString, "last_four", lastFour)
    }

}
