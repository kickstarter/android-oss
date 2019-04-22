package com.kickstarter.ui.adapters

import android.content.Context
import android.util.Pair
import android.view.View
import android.widget.Filter
<<<<<<< HEAD
import com.kickstarter.libs.utils.ObjectUtils
=======
import android.widget.Filterable
>>>>>>> 2e29d67dbeca56ddf470eb9b5f133738a96d79df
import com.kickstarter.models.Project
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.viewholders.KSArrayViewHolder
import com.kickstarter.ui.viewholders.ShippingRuleViewHolder
import rx.Observable

class ShippingRulesAdapter(ctx: Context, private val resourceId: Int, var items: ArrayList<Pair<ShippingRule, Project>> = arrayListOf(), private val delegate: Delegate) : KSArrayAdapter<Pair<ShippingRule, Project>>(ctx, resourceId, items), Filterable {

    interface Delegate : ShippingRuleViewHolder.Delegate

<<<<<<< HEAD
    private val filter: ShippingRulesFilter = ShippingRulesFilter()

    fun populateShippingRules(rules: List<ShippingRule>, project: Project) {
        this.items.clear()

        if (ObjectUtils.isNotNull(rules) || rules.isNotEmpty()) {
            this.items.addAll(Observable.from(rules)
                    .map { rule -> Pair.create(rule, project) }
                    .toList().toBlocking().single())
        }
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): Pair<ShippingRule, Project>? {
        return this.items[position]
    }

    override fun getFilter(): Filter {
        return filter
    }

    inner class ShippingRulesFilter : Filter() {
=======
    private val ruleFilter = object : Filter() {
>>>>>>> 2e29d67dbeca56ddf470eb9b5f133738a96d79df

        //TODO - The filter is working now but only producing one result working on fixing the list.
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filterResults = FilterResults()
            filterResults.values = items
            constraint?.let {
                if (it.isNotEmpty()) {
                    val list = (filterResults.values as ArrayList<Pair<ShippingRule, Project>>)
                            .filter { srAndProject -> srAndProject.first.toString().startsWith(constraint) }
                    filterResults.values = list
                    filterResults.count = list.size
                }
            }
            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results != null && results.count > 0) {
                items = results.values as ArrayList<Pair<ShippingRule, Project>>
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
        this.items.clear()

        this.items.addAll(Observable.from(rules)
                .map { rule -> Pair.create(rule, project) }
                .toList().toBlocking().single())
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): Pair<ShippingRule, Project>? {
        return this.items[position]
    }

    override fun getCount(): Int = this.items.size

    override fun getFilter(): Filter {
        return ruleFilter
    }
}
