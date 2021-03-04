package com.kickstarter.ui.adapters

import android.util.Pair
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.EmptyViewBinding
import com.kickstarter.databinding.ExpandableHeaderItemBinding
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.viewholders.EmptyViewHolder
import com.kickstarter.ui.viewholders.ExpandableHeaderViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder

class ExpandableHeaderAdapter: KSAdapter() {
    init {
        insertSection(SECTION_REWARD_SUMMARY, emptyList<Pair<Project, Reward>>())
    }

    override fun layout(sectionRow: SectionRow):Int {
        return when (sectionRow.section()) {
            SECTION_REWARD_SUMMARY -> R.layout.expandable_header_item
            else -> 0
        }
    }

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return when(layout) {
            R.layout.expandable_header_item -> ExpandableHeaderViewHolder(ExpandableHeaderItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
            else -> EmptyViewHolder(EmptyViewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
        }
    }

    fun populateData(rewards: List<Pair<Project, Reward>>) {
        if (rewards != null) {
            setSection(SECTION_REWARD_SUMMARY, rewards)
            notifyDataSetChanged()
        }
    }

    companion object {
        private const val SECTION_REWARD_SUMMARY = 0
    }
}