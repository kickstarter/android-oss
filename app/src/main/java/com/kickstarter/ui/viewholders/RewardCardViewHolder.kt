package com.kickstarter.ui.viewholders

import android.util.Pair
import android.view.View
import com.kickstarter.R
import com.kickstarter.libs.KSString
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Project
import com.kickstarter.models.StoredCard
import com.kickstarter.viewmodels.RewardCardViewHolderViewModel
import kotlinx.android.synthetic.main.item_reward_credit_card.view.*
import kotlinx.android.synthetic.main.reward_card_details.view.*

class RewardCardViewHolder(val view : View, val delegate : Delegate) : KSViewHolder(view) {

    interface Delegate {
        fun selectCardButtonClicked(position: Int)
    }

    private val viewModel: RewardCardViewHolderViewModel.ViewModel = RewardCardViewHolderViewModel.ViewModel(environment())
    private val ksString: KSString = environment().ksString()

    private val cardNotAllowedString = this.context().getString(R.string.You_cant_use_this_credit_card_to_back_a_project_from_project_country)
    private val creditCardExpirationString = this.context().getString(R.string.Credit_card_expiration)
    private val endingInString = this.context().getString(R.string.Ending_in_last_four)

    init {

        this.viewModel.outputs.expirationDate()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setExpirationDateText(it) }

        this.viewModel.outputs.issuerImage()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.reward_card_logo.setImageResource(it) }

        this.viewModel.outputs.lastFour()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setLastFourText(it) }

        this.viewModel.outputs.buttonCTA()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.select_button.setText(it) }

        this.viewModel.outputs.buttonEnabled()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.select_button.isEnabled = it }

        this.viewModel.outputs.notAvailableCopyIsVisible()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(this.view.card_not_allowed_warning, !it) }

        this.viewModel.outputs.projectCountry()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setCardNotAllowedWarningText(it) }

        this.view.select_button.setOnClickListener {
            this.delegate.selectCardButtonClicked(adapterPosition)
        }
    }

    override fun bindData(data: Any?) {
        @Suppress("UNCHECKED_CAST")
        val cardAndProject = requireNotNull(data) as Pair<StoredCard, Project>
        this.viewModel.inputs.configureWith(cardAndProject)
    }

    private fun setCardNotAllowedWarningText(country: String) {
        this.view.card_not_allowed_warning.text = this.ksString.format(this.cardNotAllowedString,
                "project_country", country)
    }

    private fun setExpirationDateText(date: String) {
        this.view.reward_card_expiration_date.text = this.ksString.format(this.creditCardExpirationString,
                "expiration_date", date)
    }

    private fun setLastFourText(lastFour: String) {
        this.view.reward_card_last_four.text = this.ksString.format(this.endingInString, "last_four", lastFour)
    }

}
