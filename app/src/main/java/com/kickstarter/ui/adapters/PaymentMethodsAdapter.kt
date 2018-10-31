package com.kickstarter.ui.adapters

import UserPaymentsQuery
import android.view.View
import com.kickstarter.R
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.ui.viewholders.PaymentMethodsViewHolder

class PaymentMethodsAdapter(private val delegate: PaymentMethodsViewHolder.Delegate): KSAdapter() {

    interface Delegate: PaymentMethodsViewHolder.Delegate

    override fun layout(sectionRow: SectionRow): Int = R.layout.list_item_payment_methods

    override fun viewHolder(layout: Int, view: View): KSViewHolder = PaymentMethodsViewHolder(view, delegate)

    fun populateCards(cards: MutableList<UserPaymentsQuery.Node>) {
        addSection(cards)
        notifyDataSetChanged()
    }
}
