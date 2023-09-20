package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.RewardsItemViewBinding
import com.kickstarter.models.RewardsItem

class RewardsItemViewHolder(private val binding: RewardsItemViewBinding) : KSViewHolder(binding.root) {
    private val ksString = requireNotNull(environment().ksString())
    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val rewardsItem = requireNotNull(data as RewardsItem?)
        val title = ksString.format(
            "rewards_info_item_quantity_title", rewardsItem.quantity(),
            "quantity", rewardsItem.quantity().toString(),
            "title", rewardsItem.item()?.name()
        )
        binding.rewardsItemTitle.text = title
    }
}
