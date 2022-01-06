package com.kickstarter.mock.factories

import com.kickstarter.models.RewardsItem.Companion.builder
import com.kickstarter.models.RewardsItem

object RewardsItemFactory {
    @JvmStatic
    fun rewardsItem(): RewardsItem {
        val itemId = IdFactory.id().toLong()
        return builder()
            .id(IdFactory.id().toLong())
            .item(ItemFactory.item().toBuilder().id(itemId).build())
            .itemId(itemId)
            .quantity(1)
            .rewardId(IdFactory.id().toLong())
            .build()
    }
}