package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.EmptyViewBinding
import com.kickstarter.databinding.ItemAddOnPledgeBinding
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.viewholders.BackingAddOnViewHolder
import com.kickstarter.ui.viewholders.EmptyViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder

class BackingAddOnsAdapter(private val viewListener: BackingAddOnViewHolder.ViewListener) : KSAdapter() {

    init {
        insertSection(SECTION_NO_ADD_ONS_AVAILABLE, emptyList<Boolean>())
        insertSection(SECTION_BACKING_ADD_ONS_CARD, emptyList<Reward>())
    }

    override fun layout(sectionRow: SectionRow): Int = when (sectionRow.section()){
        SECTION_BACKING_ADD_ONS_CARD -> R.layout.item_add_on_pledge
        SECTION_NO_ADD_ONS_AVAILABLE -> R.layout.item_empty_add_on
        else -> 0
    }

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return when(layout) {
            R.layout.item_empty_add_on -> EmptyViewHolder(EmptyViewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
            R.layout.item_add_on_pledge -> BackingAddOnViewHolder(ItemAddOnPledgeBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false), viewListener)
            else -> EmptyViewHolder(EmptyViewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
        }
    }

    fun populateDataForAddOns(rewards: List<Triple<ProjectData, Reward, ShippingRule>>) {
        setSection(SECTION_BACKING_ADD_ONS_CARD, rewards)
        notifyDataSetChanged()
    }

    fun showEmptyState(isEmptyState: Boolean) {
        if (isEmptyState) {
            setSection(SECTION_NO_ADD_ONS_AVAILABLE, listOf(true))
        } else {
            setSection(SECTION_NO_ADD_ONS_AVAILABLE, emptyList<Boolean>())
        }
        notifyDataSetChanged()
    }

    companion object {
        private const val SECTION_NO_ADD_ONS_AVAILABLE = 0
        private const val SECTION_BACKING_ADD_ONS_CARD = 1
    }
}