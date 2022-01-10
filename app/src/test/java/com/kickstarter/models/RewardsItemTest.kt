package com.kickstarter.models

import com.kickstarter.mock.factories.ItemFactory
import com.kickstarter.mock.factories.RewardsItemFactory
import junit.framework.TestCase

class RewardsItemTest : TestCase() {

    fun testEquals_whenSecondNull_returnFalse() {
        val rwItem = RewardsItemFactory.rewardsItem()
        val rwItem1 = RewardsItem.builder()
            .id(9L)
            .itemId(3L)
            .quantity(1)
            .rewardId(3)
            .build()

        assertFalse(rwItem == rwItem1)
    }

    fun testEquals_whenSecondEqual_returnTrue() {
        val rwItem = RewardsItemFactory.rewardsItem()
        val rwItem1 = rwItem

        assertTrue(rwItem == rwItem1)
    }

    fun testEquals_whenSecondNotEqualEqual_itemId() {
        val rwItem = RewardsItemFactory.rewardsItem()
        val rwItem1 = rwItem.toBuilder()
            .itemId(3)
            .build()

        assertFalse(rwItem == rwItem1)

        val rwItem3 = rwItem1.toBuilder()
            .itemId(rwItem.itemId())
            .build()

        assertTrue(rwItem == rwItem3)
    }

    fun testEquals_whenSecondNotEqualEqual_item() {
        val rwItem = RewardsItemFactory.rewardsItem()
        val rwItem1 = rwItem.toBuilder()
            .item(ItemFactory.item().toBuilder().amount(3.0f).build())
            .build()

        assertFalse(rwItem == rwItem1)

        val rwItem3 = rwItem1.toBuilder()
            .item(rwItem.item())
            .build()

        assertTrue(rwItem == rwItem3)
    }

    fun testEquals_whenSecondNotEqualEqual_hasBackers() {
        val rwItem = RewardsItemFactory.rewardsItem()
        val rwItem1 = rwItem.toBuilder()
            .hasBackers(true)
            .build()

        assertFalse(rwItem == rwItem1)

        val rwItem3 = rwItem1.toBuilder()
            .hasBackers(rwItem.hasBackers())
            .build()

        assertTrue(rwItem == rwItem3)
    }

    fun testEquals_whenSecondNotEqualEqual_rewardId() {
        val rwItem = RewardsItemFactory.rewardsItem()
        val rwItem1 = rwItem.toBuilder()
            .rewardId(null)
            .build()

        assertFalse(rwItem == rwItem1)

        val rwItem3 = rwItem1.toBuilder()
            .rewardId(rwItem.rewardId())
            .build()

        assertTrue(rwItem == rwItem3)
    }
}
