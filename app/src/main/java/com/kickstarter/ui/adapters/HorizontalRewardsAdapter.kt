package com.kickstarter.ui.adapters

import android.util.Pair
import android.view.View
import com.kickstarter.R
import com.kickstarter.models.Project
import com.kickstarter.ui.viewholders.EmptyViewHolder
import com.kickstarter.ui.viewholders.HorizontalRewardViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder
import rx.Observable

class HorizontalRewardsAdapter(private val delegate: Delegate) : KSAdapter() {

    interface Delegate: HorizontalRewardViewHolder.Delegate

    override fun layout(sectionRow: SectionRow): Int {
        return R.layout.item_reward
    }

    override fun viewHolder(layout: Int, view: View): KSViewHolder {
        return when(layout) {
            R.layout.item_reward -> HorizontalRewardViewHolder(view, this.delegate)
            else -> EmptyViewHolder(view)
        }
    }

    fun populateRewards(project: Project) {
        sections().clear()

        val rewards = project.rewards()

        if (rewards != null) {
            addSection(Observable.from(rewards)
                    .map { reward -> Pair.create(project, reward) }
                    .toList().toBlocking().single()
            )
            notifyDataSetChanged()
        }
    }
}
