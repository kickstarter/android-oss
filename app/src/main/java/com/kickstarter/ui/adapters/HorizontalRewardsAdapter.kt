package com.kickstarter.ui.adapters

import android.util.Pair
import android.view.View
import com.kickstarter.R
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.models.Project
import com.kickstarter.ui.viewholders.EmptyViewHolder
import com.kickstarter.ui.viewholders.HorizontalNoRewardViewHolder
import com.kickstarter.ui.viewholders.HorizontalRewardViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder
import rx.Observable

class HorizontalRewardsAdapter : KSAdapter() {

    override fun layout(sectionRow: SectionRow): Int {
        return when (sectionRow.section()) {
            0 -> R.layout.item_no_reward
            else -> R.layout.item_reward
        }
    }

    override fun viewHolder(layout: Int, view: View): KSViewHolder {
        return when(layout) {
            R.layout.item_no_reward -> HorizontalNoRewardViewHolder(view)
            R.layout.item_reward -> HorizontalRewardViewHolder(view)
            else -> EmptyViewHolder(view)
        }
    }

    fun populateRewards(project: Project) {
        sections().clear()

        val rewards = project.rewards()

        if (rewards != null) {

            addSection(Observable.from(rewards)
                    .filter { RewardUtils.isNoReward(it) }
                    .map { reward -> Pair.create(project, reward) }
                    .toList().toBlocking().single()
            )

            addSection(Observable.from(rewards)
                    .filter { RewardUtils.isReward(it) }
                    .map { reward -> Pair.create(project, reward) }
                    .toList().toBlocking().single()
            )
            notifyDataSetChanged()
        }
    }
}
