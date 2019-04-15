package com.kickstarter.ui.viewholders

import android.view.View
import com.kickstarter.viewmodels.ShippingRuleViewHolderViewModel
import com.trello.rxlifecycle.ActivityEvent
import rx.Observable

class ShippingRuleViewHolder(private val view: View) : KSArrayViewHolder(view) {

    val viewModel = ShippingRuleViewHolderViewModel.ViewModel(environment())

    init {

    }
    override fun bindData(any: Any?) {

    }
}