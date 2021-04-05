package com.kickstarter.models

import com.kickstarter.mock.factories.LocationFactory
import junit.framework.TestCase

class LocationTest : TestCase() {

    fun testEquals_whenSecondLocationNull_returnFalse() {
        val locA = LocationFactory.empty()
        val locB: Location? = null

        assertFalse(locA == locB)
    }

    fun testEquals_whenLocationEquals_returnTrue() {
        val locA = LocationFactory.germany()
        val locB = LocationFactory.germany()

        assertTrue(locA == locB)
    }

    fun testEquals_whenLocationIdDifferent_returnFalse() {
        val locA = LocationFactory.germany()
        val locB = LocationFactory.germany().toBuilder().id(999).build()

        assertFalse(locA == locB)
    }

    fun testEquals_whenFirstLocationNull_returnFalse() {
        val locA: Location? = null
        val locB = LocationFactory.mexico()

        assertFalse(locA == locB)
    }

    fun testEquals_whenCityIsNull_returnFalse() {
        val locA = LocationFactory.germany().toBuilder().city("Berlin").build()
        val locB = LocationFactory.germany().toBuilder().city(null).build()

        assertFalse(locA == locB)
    }
}
