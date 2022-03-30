package com.kickstarter.models

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.CheckoutDataFactory
import com.kickstarter.ui.data.CheckoutData
import org.junit.Test
import type.CreditCardPaymentType

class CheckoutDataTest : KSRobolectricTestCase() {

    @Test
    fun testDefaultInit() {
        val checkout = CheckoutData.builder()
            .id(1234L)
            .bonusAmount(2.1)
            .paymentType(CreditCardPaymentType.BANK_ACCOUNT)
            .shippingAmount(1.2)
            .amount(3.3)
            .build()

        assertEquals(checkout.id(), 1234L)
        assertEquals(checkout.bonusAmount(), 2.1)
        assertEquals(checkout.shippingAmount(), 1.2)
        assertEquals(checkout.amount(), 3.3)
        assertEquals(checkout.paymentType(), CreditCardPaymentType.BANK_ACCOUNT)
    }

    @Test
    fun testDefaultToBuilderInit() {
        val checkout = CheckoutData.builder().build().toBuilder()
            .id(1234L)
            .bonusAmount(2.1)
            .paymentType(CreditCardPaymentType.BANK_ACCOUNT)
            .shippingAmount(1.2)
            .amount(3.3)
            .build()

        assertEquals(checkout.id(), 1234L)
        assertEquals(checkout.bonusAmount(), 2.1)
        assertEquals(checkout.shippingAmount(), 1.2)
        assertEquals(checkout.amount(), 3.3)
        assertEquals(checkout.paymentType(), CreditCardPaymentType.BANK_ACCOUNT)
    }

    @Test
    fun testCheckoutData_equalFalse() {
        val checkout = CheckoutDataFactory.checkoutData(2.1, 1.1)
        val checkout2 = CheckoutDataFactory.checkoutData(123L, 2.1, 1.1)
        val checkout3 = CheckoutData.builder().id(5678L).build()

        assertFalse(checkout == checkout2)
        assertFalse(checkout == checkout3)
        assertFalse(checkout3 == checkout2)
    }

    @Test
    fun testCheckoutData_equalTrue() {
        val checkout1 = CheckoutData.builder().build()
        val checkout2 = CheckoutData.builder().build()

        assertEquals(checkout1, checkout2)
    }
}
