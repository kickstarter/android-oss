package com.kickstarter.ui.adapters

import android.content.Context
import android.util.Pair
import android.view.View
import android.widget.Filter
import com.kickstarter.models.Project
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.viewholders.KSArrayViewHolder
import com.kickstarter.ui.viewholders.ShippingRuleViewHolder
import rx.Observable

class ShippingRulesAdapter(ctx: Context, private val resourceId: Int, val items: ArrayList<Pair<ShippingRule, Project>>, private val delegate: Delegate) : KSArrayAdapter<Pair<ShippingRule, Project>>(ctx, resourceId, items) {
    override fun viewHolder(layout: Int, view: View): KSArrayViewHolder {
        return ShippingRuleViewHolder(view, delegate)
    }

    interface Delegate : ShippingRuleViewHolder.Delegate

    private val filter: ShippingRulesFilter = ShippingRulesFilter()

    fun populateShippingRules(rules: List<ShippingRule>, project: Project) {
        this.items.clear()

        this.items.addAll(Observable.from(rules)
                .map { rule -> Pair.create(rule, project) }
                .toList().toBlocking().single())
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): Pair<ShippingRule, Project>? {
        return this.items[position]
    }

    override fun getFilter(): Filter {
        return filter
    }

    inner class ShippingRulesFilter : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filterResults = FilterResults()
            filterResults.values = items
            constraint?.let {
                if(it.isNotEmpty()) {
                    val list = (filterResults.values as ArrayList<Pair<ShippingRule, Project>>)
                            .filter { srAndProject -> srAndProject.first.toString().startsWith(constraint) }
                    filterResults.values = list
                    filterResults.count = list.size
                }
            }

            return filterResults
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
//            items = results.values as ArrayList<Pair<ShippingRule, Project>>
            if (results.count > 0) {
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }

    }
}
