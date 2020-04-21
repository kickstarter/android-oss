package com.kickstarter.ui.viewholders

import android.util.Pair
import android.view.View
import com.kickstarter.R
import com.kickstarter.libs.KSString
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.models.Project
import com.kickstarter.models.StoredCard
import com.kickstarter.viewmodels.RewardCardSelectedViewHolderViewModel
import kotlinx.android.synthetic.main.reward_card_details.view.*

class RewardCardSelectedViewHolder(val view : View) : KSViewHolder(view) {

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
                .subscribe { this.view.reward_card_logo.setImageResource(it) }

        this.viewModel.outputs.issuer()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.reward_card_logo.contentDescription = it }

        this.viewModel.outputs.lastFour()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setLastFourTextView(it) }
    }

    override fun bindData(data: Any?) {
        @Suppress("UNCHECKED_CAST")
        val cardAndProject = requireNotNull(data) as Pair<StoredCard, Project>
        this.viewModel.inputs.configureWith(cardAndProject)
    }

    private fun setExpirationDateTextView(date: String) {
        this.view.reward_card_expiration_date.text = this.ksString.format(this.creditCardExpirationString,
                "expiration_date", date)
    }

    private fun setLastFourTextView(lastFour: String) {
        this.view.reward_card_last_four.text = this.ksString.format(this.lastFourString,
                "last_four",
                lastFour)
    }

}
