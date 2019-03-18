package com.kickstarter.ui.adapters

import android.view.View
import com.kickstarter.R
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.viewholders.HorizontalRewardViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder

const val SECTION_REWARDS = 0

class HorizontalRewardsAdapter (val delegate: HorizontalRewardViewHolder.Delegate) : KSAdapter() {

    init {
        addSection(emptyList<Any>())
    }

    interface Delegate : HorizontalRewardViewHolder.Delegate

    override fun layout(sectionRow: SectionRow): Int = R.layout.item_reward

    override fun viewHolder(layout: Int, view: View): KSViewHolder = HorizontalRewardViewHolder(view)

    fun populateRewards(project: Project,rewards: List<Reward>) {
        setSection(SECTION_REWARDS, rewards)
    }
}