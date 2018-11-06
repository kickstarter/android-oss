package com.kickstarter.ui.viewholders

import UserPaymentsQuery
import android.support.annotation.NonNull
import android.view.View
import com.kickstarter.R
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.viewmodels.PaymentMethodsViewHolderViewModel
import kotlinx.android.synthetic.main.item_payment_method.view.*

class PaymentMethodsViewHolder(@NonNull view: View, @NonNull val delegate: Delegate) : KSViewHolder(view) {

    private val ksString = environment().ksString()
    private val vm: PaymentMethodsViewHolderViewModel.ViewModel = PaymentMethodsViewHolderViewModel.ViewModel(environment())

    private val creditCardExpirationString = this.context().getString(R.string.Credit_card_expiration)
    private val cardEndingInString = this.context().getString(R.string.Card_ending_in_last_four)

    interface Delegate {
        fun deleteCardButtonClicked(paymentMethodsViewHolder: PaymentMethodsViewHolder, paymentSourceId: String)
    }

    init {

        this.vm.outputs.type()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    itemView.credit_card_logo.setImageResource(it)
                }

        this.vm.outputs.expirationDate()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    setExpirationDateTextView(it)
                }

        this.vm.outputs.id()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    this.delegate.deleteCardButtonClicked(this, it)
                }

        this.vm.outputs.lastFour()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    setLastFourTextView(it)
                }

        itemView.delete_card.setOnClickListener {
            this.vm.inputs.deleteCardClick()
        }

    }

    override fun bindData(data: Any?) {
        val cards = requireNotNull(data as UserPaymentsQuery.Node)
        this.vm.inputs.card(cards)
    }

    private fun setExpirationDateTextView(date: String) {
        itemView.credit_card_expiration_date.text = this.ksString.format(this.creditCardExpirationString,
                "expiration_date", date)
    }

    private fun setLastFourTextView(lastFour: String) {
        itemView.credit_card_last_four_digits.text = this.ksString.format(this.cardEndingInString, "last_four", lastFour)
    }

}
