package com.kickstarter.models

import com.kickstarter.mock.factories.ItemFactory
import junit.framework.TestCase

class ItemTest : TestCase() {

    fun testEquals_whenSecondNull_returnFalse() {
        val item = ItemFactory.item()
        val item1 = Item.builder()
            .id(9)
            .amount(0.2f)
            .taxable(true)
            .build()

        assertFalse(item == item1)
    }

    fun testEquals_whenSecondEqual_returnTrue() {
        val item = ItemFactory.item()
        val item1 = item

        assertTrue(item == item1)
    }

    fun testEquals_whenSecondNotEqualEqual_Id() {
        val item = ItemFactory.item()
        val item1 = item.toBuilder()
            .id(3)
            .build()

        assertFalse(item == item1)

        val item3 = item1.toBuilder()
            .id(item.id())
            .build()

        assertTrue(item == item3)
    }

    fun testEquals_whenSecondNotEqualEqual_amount() {
        val item = ItemFactory.item()
        val item1 = item.toBuilder()
            .amount(0.9f)
            .build()

        assertFalse(item == item1)

        val item3 = item1.toBuilder()
            .amount(item.amount())
            .build()

        assertTrue(item == item3)
    }

    fun testEquals_whenSecondNotEqualEqual_description() {
        val item = ItemFactory.item()
        val item1 = item.toBuilder()
            .description("desc")
            .build()

        assertFalse(item == item1)

        val item3 = item1.toBuilder()
            .description(item.description())
            .build()

        assertTrue(item == item3)
    }

    fun testEquals_whenSecondNotEqualEqual_taxable() {
        val item = ItemFactory.item()
        val item1 = item.toBuilder()
            .taxable(true)
            .build()

        assertFalse(item == item1)

        val item3 = item1.toBuilder()
            .taxable(item.taxable())
            .build()

        assertTrue(item == item3)
    }

    fun testEquals_whenSecondNotEqualEqual_projectId() {
        val item = ItemFactory.item()
        val item1 = item.toBuilder()
            .projectId(null)
            .build()

        assertFalse(item == item1)

        val item3 = item1.toBuilder()
            .projectId(item.projectId())
            .build()

        assertTrue(item == item3)
    }

    fun testEquals_whenSecondNotEqualEqual_name() {
        val item = ItemFactory.item()
        val item1 = item.toBuilder()
            .name("name")
            .build()

        assertFalse(item == item1)

        val item3 = item1.toBuilder()
            .name(item.name())
            .build()

        assertTrue(item == item3)
    }
}
