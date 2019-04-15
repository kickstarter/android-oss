package com.kickstarter.ui.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.kickstarter.models.ShippingRule

class ShippingRulesAdapter(ctx: Context, resourceId: Int, items: ArrayList<ShippingRule>) : KSArrayAdapter<ShippingRule>(ctx, resourceId, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return super.getView(position, convertView, parent)
    }
}