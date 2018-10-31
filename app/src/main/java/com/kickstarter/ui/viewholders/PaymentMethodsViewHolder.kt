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
import java.text.SimpleDateFormat
import java.util.*

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
                    itemView.credit_card_logo_image_view.setImageResource(setCreditCardType(it))
                }

        this.vm.outputs.expirationDate()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    setExpirationDateTextView(it)
                }

        this.vm.outputs.lastFour()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    setLastFourTextView(it)
                }

    }

    override fun bindData(data: Any?) {
        val cards = requireNotNull(data as UserPaymentsQuery.Node)
        this.vm.inputs.addCards(cards)
    }

    private fun setExpirationDateTextView(date: Date) {
        val sdf = SimpleDateFormat("MM/yyyy", Locale.getDefault())
        val formattedDate = sdf.format(date).toString()
        itemView.credit_card_expiration_date_text_view?.text = this.ksString.format(this.creditCardExpirationString, "expiration_date", formattedDate)
    }

    private fun setLastFourTextView(lastFour: String) {
        itemView.credit_card_last_four_text_view?.text = this.ksString.format(this.cardEndingInString, "last_four", lastFour)
    }

    private fun setCreditCardType(cardType: CreditCardType): Int {
        return when (cardType) {
            CreditCardType.AMEX -> R.drawable.amex_md
            CreditCardType.DISCOVER -> R.drawable.discover_md
            CreditCardType.JCB -> R.drawable.jcb_md
            CreditCardType.MASTERCARD -> R.drawable.mastercard_md
            CreditCardType.VISA -> R.drawable.visa_md
            else -> R.drawable.generic_bank_md
        }
    }
}
