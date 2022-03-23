package com.kickstarter.models

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.LocationFactory
import org.junit.Test

class ShippingRuleTest : KSRobolectricTestCase() {

    @Test
    fun testDefaultInit() {
        val location = LocationFactory.unitedStates()
        val shippingRule = ShippingRule.builder()
            .id(1L)
            .cost(30.0)
            .location(location)
            .build()

        assertEquals(shippingRule.id(), 1L)
        assertEquals(shippingRule.cost(), 30.0)
        assertEquals(shippingRule.location(), location)
    }

    @Test
    fun testDefaultToBuilder() {
        val location = LocationFactory.unitedStates()
        val shippingRule = ShippingRule.builder().build().toBuilder().location(location).build()
        assertEquals(shippingRule.location(), location)
    }

    @Test
    fun testShippingRule_equalFalse() {
        val shippingRule = ShippingRule.builder().build()
        val shippingRule2 = ShippingRule.builder().id(1L).build()
        val shippingRule3 = ShippingRule.builder().cost(30.0).build()
        val shippingRule4 = ShippingRule.builder().location(LocationFactory.unitedStates()).build()

        assertFalse(shippingRule == shippingRule2)
        assertFalse(shippingRule == shippingRule3)
        assertFalse(shippingRule == shippingRule4)

        assertFalse(shippingRule3 == shippingRule2)
        assertFalse(shippingRule3 == shippingRule4)
    }

    @Test
    fun testShippingRule_equalTrue() {
        val shippingRule1 = ShippingRule.builder().build()
        val shippingRule2 = ShippingRule.builder().build()

        assertEquals(shippingRule1, shippingRule2)
    }

    @Test
    fun testShippingRule_toString() {
        val location = LocationFactory.unitedStates()
        val shippingRule = ShippingRule.builder()
            .id(1L)
            .cost(30.0)
            .location(location)
            .build()

        assertEquals(shippingRule.toString(), location.displayableName())
    }
}
