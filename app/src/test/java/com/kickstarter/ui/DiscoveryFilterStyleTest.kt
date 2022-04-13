package com.kickstarter.ui

import junit.framework.TestCase
import org.junit.Test

class DiscoveryFilterStyleTest : TestCase() {

    @Test
    fun testDefaultInit() {
        val discoveryFilterStyle =
            DiscoveryFilterStyle.builder()
                .light(true)
                .primary(false)
                .visible(true)
                .build()

        assertEquals(discoveryFilterStyle.light(), true)
        assertEquals(discoveryFilterStyle.primary(), false)
        assertEquals(discoveryFilterStyle.visible(), true)
        assertEquals(discoveryFilterStyle.selected(), false)
        assertEquals(discoveryFilterStyle.showLiveProjectsCount(), false)
    }

    @Test
    fun testEquals_whenFieldsDontMatch_returnFalse() {
        val discoveryFilterStyle1 =
            DiscoveryFilterStyle.builder()
                .light(true)
                .primary(false)
                .visible(true)
                .build()

        val discoveryFilterStyle2 =
            discoveryFilterStyle1.toBuilder()
                .light(false)
                .showLiveProjectsCount(true)
                .build()

        val discoveryFilterStyle3 =
            discoveryFilterStyle2.toBuilder()
                .showLiveProjectsCount(false)
                .primary(true)
                .build()

        assertFalse(discoveryFilterStyle1 == discoveryFilterStyle2)
        assertFalse(discoveryFilterStyle1 == discoveryFilterStyle3)
        assertFalse(discoveryFilterStyle2 == discoveryFilterStyle3)
    }

    @Test
    fun testEquals_whenFieldsMatch_returnTrue() {
        val discoveryFilterStyle1 =
            DiscoveryFilterStyle.builder()
                .light(true)
                .primary(false)
                .visible(true)
                .build()

        val discoveryFilterStyle2 = discoveryFilterStyle1

        assertTrue(discoveryFilterStyle1 == discoveryFilterStyle2)
    }

    @Test
    fun testToBuilder() {
        val discoveryFilterStyle =
            DiscoveryFilterStyle.builder()
                .showLiveProjectsCount(true)
                .build()
                .toBuilder()
                .visible(true)
                .copy(true)
                .build()

        assertEquals(discoveryFilterStyle.light(), true)
        assertEquals(discoveryFilterStyle.primary(), false)
        assertEquals(discoveryFilterStyle.visible(), true)
        assertEquals(discoveryFilterStyle.selected(), false)
        assertEquals(discoveryFilterStyle.showLiveProjectsCount(), true)
    }
}
