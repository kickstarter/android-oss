package com.kickstarter.ui.viewholders

import com.kickstarter.R
import com.kickstarter.databinding.ItemPaymentMethodBinding
import com.kickstarter.libs.rx.transformers.Transformers.observeForUIV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.models.StoredCard
import com.kickstarter.viewmodels.PaymentMethodsViewHolderViewModel
import io.reactivex.disposables.CompositeDisposable

class PaymentMethodsViewHolder(private val binding: ItemPaymentMethodBinding, val delegate: Delegate) : KSViewHolder(binding.root) {

    private val ksString = requireNotNull(environment().ksString())
    private val vm: PaymentMethodsViewHolderViewModel.PaymentMethodsViewHolderViewModel = PaymentMethodsViewHolderViewModel.PaymentMethodsViewHolderViewModel()

    private val creditCardExpirationString = this.context().getString(R.string.Credit_card_expiration)
    private val cardEndingInString = this.context().getString(R.string.Card_ending_in_last_four)
    private val disposables = CompositeDisposable()

    interface Delegate {
        fun deleteCardButtonClicked(paymentMethodsViewHolder: PaymentMethodsViewHolder, paymentSourceId: String)
    }

    init {

        this.vm.outputs.issuerImage()
            .compose(observeForUIV2())
            .subscribe { binding.creditCardLogo.setImageResource(it) }
            .addToDisposable(disposables)

        this.vm.outputs.expirationDate()
            .compose(observeForUIV2())
            .subscribe { setExpirationDateTextView(it) }
            .addToDisposable(disposables)

        this.vm.outputs.id()
            .compose(observeForUIV2())
            .subscribe { this.delegate.deleteCardButtonClicked(this, it) }
            .addToDisposable(disposables)

        this.vm.outputs.lastFour()
            .compose(observeForUIV2())
            .subscribe { setLastFourTextView(it) }
            .addToDisposable(disposables)

        this.vm.outputs.issuer()
            .compose(observeForUIV2())
            .subscribe { binding.creditCardLogo.contentDescription = it }
            .addToDisposable(disposables)

        binding.deleteCard.setOnClickListener { this.vm.inputs.deleteIconClicked() }
    }

    override fun bindData(data: Any?) {
        val card = requireNotNull(data as StoredCard)
        this.vm.inputs.card(card)
    }

    private fun setExpirationDateTextView(date: String) {
        binding.creditCardExpirationDate.text = this.ksString.format(
            this.creditCardExpirationString,
            "expiration_date", date
        )
    }

    private fun setLastFourTextView(lastFour: String) {
        binding.creditCardLastFourDigits.text = this.ksString.format(this.cardEndingInString, "last_four", lastFour)
    }

    override fun destroy() {
        disposables.clear()
        super.destroy()
    }
}
