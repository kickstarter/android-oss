package com.kickstarter.ui.adapters

import android.util.Pair
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.EmptyViewBinding
import com.kickstarter.databinding.ItemAddOnBinding
import com.kickstarter.models.Reward
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.viewholders.AddOnViewHolder
import com.kickstarter.ui.viewholders.EmptyViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder

class RewardAndAddOnsAdapter : KSAdapter() {

    init {
        insertSection(SECTION_REWARD_CARD, emptyList<Reward>())
        insertSection(SECTION_ADD_ONS_CARD, emptyList<Reward>())
    }

    override fun layout(sectionRow: SectionRow): Int = when (sectionRow.section()){
        SECTION_REWARD_CARD,
        SECTION_ADD_ONS_CARD -> R.layout.item_add_on
        else -> 0
    }

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return when(layout) {
            R.layout.item_add_on -> AddOnViewHolder(ItemAddOnBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
            else -> EmptyViewHolder(EmptyViewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
        }
    }

    fun populateDataForAddOns(rewards: List<Pair<ProjectData,Reward>>) {
        setSection(SECTION_ADD_ONS_CARD, rewards)
        notifyDataSetChanged()
    }

    fun populateDataForReward(reward: Pair<ProjectData, Reward>) {
        setSection(SECTION_REWARD_CARD, listOf(reward))
        notifyDataSetChanged()
    }

    companion object {
        private const val SECTION_REWARD_CARD = 0
        private const val SECTION_ADD_ONS_CARD = 1
    }
}
