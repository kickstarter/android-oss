package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.RewardsItemViewBinding
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.RewardsItem

class RewardsItemViewHolder(private val binding: RewardsItemViewBinding) : KSViewHolder(binding.root) {
    private val ksString = environment().ksString()
    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val rewardsItem = ObjectUtils.requireNonNull(data as RewardsItem?)
        val title = ksString.format(
            "rewards_info_item_quantity_title", rewardsItem.quantity(),
            "quantity", ObjectUtils.toString(rewardsItem.quantity()),
            "title", rewardsItem.item().name()
        )
        binding.rewardsItemTitle.text = title
    }
}
