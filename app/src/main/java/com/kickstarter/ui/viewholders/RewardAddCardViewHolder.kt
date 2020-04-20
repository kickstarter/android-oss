package com.kickstarter.ui.viewholders

import android.view.View
import kotlinx.android.synthetic.main.item_add_card.view.*

class RewardAddCardViewHolder (val view : View, val delegate : Delegate) : KSViewHolder(view) {

    init {
        this.view.add_card_button.setOnClickListener {
            this.delegate.addNewCardButtonClicked()
        }
    }

    override fun bindData(data: Any?) {}

    interface Delegate {
        fun addNewCardButtonClicked()
    }
}
