package com.kickstarter.ui.adapters

import UserPaymentsQuery
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import com.kickstarter.R
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.ui.viewholders.PaymentMethodsViewHolder

const val SECTION_CARDS = 0

class PaymentMethodsAdapter(private val delegate: PaymentMethodsViewHolder.Delegate, diffCallback: DiffUtil.ItemCallback<Any>): KSListAdapter(diffCallback) {

    init {
        addSection(emptyList<Any>())
    }

    interface Delegate: PaymentMethodsViewHolder.Delegate

    override fun layout(sectionRow: SectionRow): Int = R.layout.item_payment_method

    override fun viewHolder(layout: Int, view: View): KSViewHolder = PaymentMethodsViewHolder(view, delegate)

    fun populateCards(cards: MutableList<UserPaymentsQuery.Node>) {
        setSection(SECTION_CARDS, cards)
        submitList(items())
    }
}
