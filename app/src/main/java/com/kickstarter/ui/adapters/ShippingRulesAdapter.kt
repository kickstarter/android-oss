package com.kickstarter.ui.adapters

import android.content.Context
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kickstarter.models.Project
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.viewholders.ShippingRuleViewHolder
import rx.Observable

class ShippingRulesAdapter(ctx: Context, private val resourceId: Int, items: ArrayList<ShippingRule>) : KSArrayAdapter<ShippingRule>(ctx, resourceId, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(ctx).inflate(resourceId, parent, false)
        val holder = ShippingRuleViewHolder(view)
        populateData(holder, position)
        return view
    }

    fun populateShippingRules(rules: List<ShippingRule>?, project: Project) {
        this.clear()

        if (rules != null) {
            addSection(Observable.from(rules)
                    .map { rule -> Pair.create(rule, project) }
                    .toList().toBlocking().single())
        }
    }
}