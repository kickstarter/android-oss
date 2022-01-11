package com.kickstarter.models

import junit.framework.TestCase
import org.junit.Test

class SingleLocationTest : TestCase() {

    @Test
    fun testEquals_whenLocalizedName() {
        val sl1 = SingleLocation.builder().id(2).localizedName("name").build()
        val sl2 = SingleLocation.builder().id(2).localizedName("other name").build()
        assertFalse(sl1 == sl2)

        val sl3 = sl2.toBuilder().localizedName(sl1.localizedName()).build()
        assertTrue(sl1 == sl3)
    }

    @Test
    fun testEquals_whenId() {
        val sl1 = SingleLocation.builder().id(2).localizedName("name").build()
        val sl2 = SingleLocation.builder().id(9).localizedName("name").build()
        assertFalse(sl1 == sl2)

        val sl3 = sl2.toBuilder().id(sl1.id()).build()
        assertTrue(sl1 == sl3)
    }

    @Test
    fun testEquals() {
        val sl1 = SingleLocation.builder().id(2).localizedName("name").build()
        val sl2 = sl1

        assertTrue(sl1 == sl2)
    }
}
