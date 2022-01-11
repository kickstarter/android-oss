package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.RewardsItemViewBinding
import com.kickstarter.models.RewardsItem
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.ui.viewholders.RewardsItemViewHolder

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
