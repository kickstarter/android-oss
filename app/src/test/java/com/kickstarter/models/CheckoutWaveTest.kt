package com.kickstarter.models

import com.kickstarter.mock.factories.CheckoutWaveFactory
import junit.framework.TestCase

class CheckoutWaveTest : TestCase() {

    fun testEquals_whenSecondCheckoutWaveDifferentId_returnFalse() {
        val checkoutWaveA = CheckoutWaveFactory.checkoutWaveActive()
        val checkoutWaveB = CheckoutWaveFactory.checkoutWaveActive().toBuilder().id(2).build()

        assertFalse(checkoutWaveA == checkoutWaveB)
    }

    fun testEquals_whenSecondCheckoutWaveDifferentActiveState_returnFalse() {
        val checkoutWaveA = CheckoutWaveFactory.checkoutWaveActive()
        val checkoutWaveB = CheckoutWaveFactory.checkoutWaveActive().toBuilder().active(false).build()

        assertFalse(checkoutWaveA == checkoutWaveB)
    }

    fun testEquals_whenCheckoutWaveEquals_returnTrue() {
        val checkoutWaveA = CheckoutWaveFactory.checkoutWaveActive()
        val checkoutWaveB = CheckoutWaveFactory.checkoutWaveActive()

        assertTrue(checkoutWaveA == checkoutWaveB)
    }
}
