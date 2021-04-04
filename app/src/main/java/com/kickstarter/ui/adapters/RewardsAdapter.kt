package com.kickstarter.ui.adapters

import android.util.Pair
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.EmptyViewBinding
import com.kickstarter.databinding.ItemRewardBinding
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.viewholders.EmptyViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.ui.viewholders.RewardViewHolder
import rx.Observable

class RewardsAdapter(private val delegate: Delegate) : KSAdapter() {

    interface Delegate : RewardViewHolder.Delegate

    override fun layout(sectionRow: SectionRow): Int {
        return R.layout.item_reward
    }

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return when (layout) {
            R.layout.item_reward -> RewardViewHolder(ItemRewardBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false), this.delegate)
            else -> EmptyViewHolder(EmptyViewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
        }
    }

    fun populateRewards(projectData: ProjectData) {
        sections().clear()

        val rewards = projectData.project().rewards()

        if (rewards != null) {
            addSection(
                Observable.from(rewards)
                    .map { reward -> Pair.create(projectData, reward) }
                    .toList().toBlocking().single()
            )
            notifyDataSetChanged()
        }
    }
}
