package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.ItemAddCardBinding

class RewardAddCardViewHolder (val binding : ItemAddCardBinding, val delegate : Delegate) : KSViewHolder(binding.root) {

    init {
        this.binding.addCardButton.setOnClickListener {
            this.delegate.addNewCardButtonClicked()
        }
    }

    override fun bindData(data: Any?) {}

    interface Delegate {
        fun addNewCardButtonClicked()
    }
}
