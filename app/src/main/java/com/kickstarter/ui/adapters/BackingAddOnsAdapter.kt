package com.kickstarter.ui.adapters

import android.view.View
import com.kickstarter.R
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.viewholders.BackingAddOnViewHolder
import com.kickstarter.ui.viewholders.EmptyViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder

class BackingAddOnsAdapter : KSAdapter() {

    init {
        insertSection(SECTION_BACKING_ADD_ONS_CARD, emptyList<Reward>())
    }

    override fun layout(sectionRow: SectionRow): Int = when (sectionRow.section()){
        SECTION_BACKING_ADD_ONS_CARD -> R.layout.item_add_on_pledge
        else -> 0
    }

    override fun viewHolder(layout: Int, view: View): KSViewHolder {
        return when(layout) {
            R.layout.item_add_on_pledge -> BackingAddOnViewHolder(view)
            else -> EmptyViewHolder(view)
        }
    }

    fun populateDataForAddOns(rewards: List<Triple<ProjectData, Reward, ShippingRule>>) {
        if (rewards.isNotEmpty()) {
            setSection(SECTION_BACKING_ADD_ONS_CARD, rewards)
            notifyDataSetChanged()
        }
    }

    companion object {
        private const val SECTION_BACKING_ADD_ONS_CARD = 0
    }
}