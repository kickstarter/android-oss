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

    interface Delegate : ShippingRuleViewHolder.Delegate

    private var filteredItems: ArrayList<Pair<ShippingRule, Project>> = items
    private val ruleFilter = object : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            results.values = items
            constraint?.let {
                if (it.isNotEmpty()) {
                    val list = (results.values as ArrayList<Pair<ShippingRule, Project>>)
                            .filter { srAndProject -> srAndProject.first.toString().startsWith(constraint, ignoreCase = true) }
                    results.values = list
                    results.count = list.size
                }
            }

            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results != null && results.count > 0) {
                filteredItems = results.values as ArrayList<Pair<ShippingRule, Project>>
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }
    }

    override fun viewHolder(layout: Int, view: View): KSArrayViewHolder {
        return ShippingRuleViewHolder(view, delegate)
    }

    fun populateShippingRules(rules: List<ShippingRule>, project: Project) {
        this.filteredItems.clear()

        this.filteredItems.addAll(Observable.from(rules)
                .map { rule -> Pair.create(rule, project) }
                .toList().toBlocking().single())
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): Pair<ShippingRule, Project>? {
        return this.filteredItems[position]
    }

    override fun getCount(): Int = this.filteredItems.size

    override fun getFilter(): Filter {
        return ruleFilter
    }
}
