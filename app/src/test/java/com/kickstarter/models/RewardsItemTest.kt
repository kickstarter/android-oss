package com.kickstarter.models

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

    fun testEquals_whenSecondNotEqualEqual() {
        val rwItem = RewardsItemFactory.rewardsItem()
        val rwItem1 = rwItem.toBuilder()
            .itemId(3)
            .build()

        assertTrue(rwItem == rwItem1)

        val rwItem3 = rwItem1.toBuilder()
            .itemId(rwItem.itemId())
            .build()

        assertTrue(rwItem == rwItem3)
    }
}
