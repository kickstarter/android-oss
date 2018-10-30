package com.kickstarter.ui.viewholders

import UserPaymentsQuery
import android.support.annotation.NonNull
import android.view.View
import butterknife.ButterKnife
import com.kickstarter.R
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.viewmodels.PaymentMethodsViewHolderViewModel
import kotlinx.android.synthetic.main.list_item_payment_methods.view.*
import type.CreditCardType

class PaymentMethodsViewHolder(@NonNull view: View, @NonNull delegate: Delegate) : KSViewHolder(view) {

    private val ksString = environment().ksString()
    private val vm: PaymentMethodsViewHolderViewModel.ViewModel = PaymentMethodsViewHolderViewModel.ViewModel(environment())

    private val creditCardExpirationString = this.context().getString(R.string.Credit_card_expiration)
    private val cardEndingInString = this.context().getString(R.string.Card_ending_in_last_four)


    interface Delegate {

    }

    init {

        ButterKnife.bind(this, view)

        //TODO: Write method to map image to the card type
        this.vm.outputs.type()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    itemView.credit_card_logo_image_view.setImageResource(setCreditCardType(it.rawValue()))
                }

        this.vm.outputs.expirationDate()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    itemView.credit_card_expiration_date_text_view.text = it.toString()
//                    setExpirationDateTextView(it)
                }

        //TODO: Write object to hold id
        this.vm.outputs.id()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {

                }

        this.vm.outputs.lastFour()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    itemView.credit_card_last_four_text_view?.text = it
//                    setLastFourTextView(it)
                }

    }

    override fun bindData(data: Any?) {
        val cards = requireNotNull(data as UserPaymentsQuery.Node)
        this.vm.inputs.addCards(cards)
    }

//    private fun setLastFourTextView(lastFour: String) {
//        this.lastFourNumbers?.text = this.ksString.format(this.cardEndingInString, lastFour)
//    }
//
//    private fun setExpirationDateTextView(expirationDate: String) {
//        this.expirationDate?.text = this.ksString.format(this.creditCardExpirationString, expirationDate)
//    }

    private fun setCreditCardType(cardType: String) :Int {
        return when (cardType) {
            CreditCardType.AMEX.rawValue() -> R.drawable.amex_md
            CreditCardType.DISCOVER.rawValue() -> R.drawable.discover_md
            CreditCardType.JCB.rawValue() -> R.drawable.jcb_md
            CreditCardType.MASTERCARD.rawValue() -> R.drawable.mastercard_md
            CreditCardType.VISA.rawValue() -> R.drawable.visa_sm
            else -> R.drawable.generic_bank_md
        }
    }
}