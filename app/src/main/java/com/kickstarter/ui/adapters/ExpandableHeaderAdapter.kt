package com.kickstarter.ui.adapters

import android.util.Pair
import android.view.View
import com.kickstarter.R
import com.kickstarter.ui.viewholders.EmptyViewHolder
import com.kickstarter.ui.viewholders.ExpandableHeaderViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder

class ExpandableHeaderAdapter: KSAdapter() {
    init {
        insertSection(SECTION_REWARD_SUMMARY, emptyList<Pair<String, String>>())
    }

    override fun layout(sectionRow: SectionRow): Int =  R.layout.fragment_pledge_section_header_item

    override fun viewHolder(layout: Int, view: View): KSViewHolder {
        return when(layout) {
            R.layout.fragment_pledge_section_header_item -> ExpandableHeaderViewHolder(view)
            else -> EmptyViewHolder(view)
        }
    }

    fun populateData(rewards: Pair<String, String>) {
        if (rewards != null) {
            setSection(SECTION_REWARD_SUMMARY, listOf(rewards))
            notifyDataSetChanged()
        }
    }

    companion object {
        private const val SECTION_REWARD_SUMMARY = 0
    }
}