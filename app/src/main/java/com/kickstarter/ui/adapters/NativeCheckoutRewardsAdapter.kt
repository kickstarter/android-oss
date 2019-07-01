package com.kickstarter.ui.adapters

import android.util.Pair
import android.view.View
import com.kickstarter.R
import com.kickstarter.models.Project
import com.kickstarter.ui.viewholders.EmptyViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.ui.viewholders.NativeCheckoutRewardViewHolder
import rx.Observable

class NativeCheckoutRewardsAdapter(private val delegate: Delegate) : KSAdapter() {

    interface Delegate: NativeCheckoutRewardViewHolder.Delegate

    override fun layout(sectionRow: SectionRow): Int {
        return R.layout.item_reward
    }

    override fun viewHolder(layout: Int, view: View): KSViewHolder {
        return when(layout) {
            R.layout.item_reward -> NativeCheckoutRewardViewHolder(view, this.delegate)
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
