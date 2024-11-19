package com.kickstarter.ui.adapters

import android.content.Context
import android.util.Pair
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import androidx.viewbinding.ViewBinding
import com.kickstarter.databinding.ItemShippingRuleBinding
import com.kickstarter.models.Project
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.viewholders.KSArrayViewHolder
import com.kickstarter.ui.viewholders.ShippingRuleViewHolder

class ShippingRulesAdapter(ctx: Context, private val resourceId: Int, val items: ArrayList<Pair<ShippingRule, Project>>, private val delegate: Delegate) : KSArrayAdapter<Pair<ShippingRule, Project>>(ctx, resourceId, items) {

    interface Delegate : ShippingRuleViewHolder.Delegate

    private var filteredItems: ArrayList<Pair<ShippingRule, Project>> = items
    private val ruleFilter = object : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            results.values = items
            constraint?.let {
                if (it.isNotEmpty()) {
                    val list = (results.values as ArrayList<Pair<ShippingRule, Project>>)
                        .filter { shippingRule -> shippingRule.first.toString().startsWith(constraint, ignoreCase = true) }
                    results.values = list
                    results.count = list.size
                }
            }

            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results != null) {
                filteredItems = results.values as ArrayList<Pair<ShippingRule, Project>>
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }
    }

    override fun viewHolder(viewBinding: ViewBinding): KSArrayViewHolder? {
        return (viewBinding as? ItemShippingRuleBinding)?.let { ShippingRuleViewHolder(it, delegate) }
    }

    override fun getViewBinding(layout: Int, viewGroup: ViewGroup): ViewBinding {
        return ItemShippingRuleBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
    }

    override fun getItem(position: Int): Pair<ShippingRule, Project>? {
        return this.filteredItems[position]
    }

    override fun getCount(): Int = this.filteredItems.size

    override fun getFilter(): Filter {
        return ruleFilter
    }

    fun populateShippingRules(rules: List<ShippingRule>, project: Project) {
        this.filteredItems.clear()

        this.filteredItems.addAll(
            rules.map {
                    rule ->
                Pair.create(rule, project)
            }
        )
        notifyDataSetChanged()
    }
}
