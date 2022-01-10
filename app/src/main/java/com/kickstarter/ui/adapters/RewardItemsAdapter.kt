package com.kickstarter.ui.adapters

import com.kickstarter.ui.adapters.KSAdapter
import com.kickstarter.models.RewardsItem
import com.kickstarter.R
import androidx.annotation.LayoutRes
import android.view.ViewGroup
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.ui.viewholders.RewardsItemViewHolder
import android.view.LayoutInflater
import com.kickstarter.databinding.RewardsItemViewBinding

class RewardItemsAdapter : KSAdapter() {
    fun rewardsItems(rewardsItems: List<RewardsItem?>) {
        setSection(0, rewardsItems)
        notifyDataSetChanged()
    }

    override fun layout(sectionRow: SectionRow): Int {
        return R.layout.rewards_item_view
    }

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return RewardsItemViewHolder(
            RewardsItemViewBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )
        )
    }

    init {
        addSection(emptyList<Any>())
    }
}