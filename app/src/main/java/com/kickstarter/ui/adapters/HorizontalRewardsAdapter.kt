package com.kickstarter.ui.adapters

import android.util.Pair
import android.view.View
import com.kickstarter.R
import com.kickstarter.models.Project
import com.kickstarter.ui.viewholders.HorizontalRewardViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder
import rx.Observable

class HorizontalRewardsAdapter : KSAdapter() {

    override fun layout(sectionRow: SectionRow): Int {
        return when (sectionRow.section()) {
            0 -> R.layout.item_reward
            else -> R.layout.item_reward
        }
    }


    override fun viewHolder(layout: Int, view: View): KSViewHolder = HorizontalRewardViewHolder(view)

    fun populateRewards(project: Project) {
        sections().clear()

        val rewards = project.rewards()
        addSection(listOf(null))
        if (rewards != null) {
            addSection(Observable.from(rewards)
                    .map { reward -> Pair.create(project, reward) }
                    .toList().toBlocking().single()
            )
            notifyDataSetChanged()
        }
    }
}
