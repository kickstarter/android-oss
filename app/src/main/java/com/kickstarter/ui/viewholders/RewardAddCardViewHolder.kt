package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.ItemAddCardBinding
import com.kickstarter.libs.utils.extensions.setGone

class RewardAddCardViewHolder(val binding: ItemAddCardBinding, val delegate: Delegate) : KSViewHolder(binding.root) {

    init {
        this.binding.addCardButton.setOnClickListener {
            this.binding.newPaymentPlusIcon.setGone(true)
            this.binding.newPaymentProgress.setGone(false)
            this.delegate.addNewCardButtonClicked()
        }
    }

    override fun bindData(data: Any?) {}

    interface Delegate {
        fun addNewCardButtonClicked()
    }
}
