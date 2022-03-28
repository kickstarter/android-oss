package com.kickstarter.models

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.CheckoutBackingFactory
import com.kickstarter.mock.factories.CheckoutFactory
import org.junit.Test

class CheckoutTest : KSRobolectricTestCase() {

    @Test
    fun testDefaultInit() {
        val backing = Checkout.Backing.builder()
            .requiresAction(true)
            .build()

        val checkout = Checkout.builder()
            .id(1234L)
            .backing(backing)
            .build()

        assertEquals(checkout.id(), 1234L)
        assertEquals(checkout.backing(), backing)
        assertEquals(backing.requiresAction(), true)
        assertEquals(backing.clientSecret(), null)
    }

    @Test
    fun testDefaultWebInit() {

        val backing = Checkout.Backing.builder().build().toBuilder().requiresAction(false).build()
        assertFalse(backing.requiresAction())

        val checkout = Checkout.builder().build().toBuilder().backing(backing).build()
        assertEquals(checkout.backing(), backing)
    }

    @Test
    fun testCheckout_equalFalse() {
        val checkout = CheckoutFactory.requiresAction(false)
        val checkout2 = Checkout.builder().backing(CheckoutBackingFactory.requiresAction(true)).build()
        val checkout3 = Checkout.builder().id(5678L).build()

        assertFalse(checkout == checkout2)
        assertFalse(checkout == checkout3)
        assertFalse(checkout3 == checkout2)
    }

    @Test
    fun testCheckout_equalTrue() {
        val checkout1 = Checkout.builder().build()
        val checkout2 = Checkout.builder().build()

        assertEquals(checkout1, checkout2)
    }
}
