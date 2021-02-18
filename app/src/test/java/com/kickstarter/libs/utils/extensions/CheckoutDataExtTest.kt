package com.kickstarter.libs.utils.extensions

import com.kickstarter.ui.data.CheckoutData
import junit.framework.TestCase
import type.CreditCardPaymentType

class CheckoutDataExtTest: TestCase() {

    fun testTotalAmount_WithoutShipping() {
        val coData = CheckoutData.builder()
                .paymentType(CreditCardPaymentType.CREDIT_CARD)
                .shippingAmount(0.0)
                .amount(200.0)
                .build()

        assertEquals(coData.totalAmount(), 200.0)
    }

    fun testTotalAmount_WithShipping() {
        val coData = CheckoutData.builder()
                .paymentType(CreditCardPaymentType.CREDIT_CARD)
                .shippingAmount(30.0)
                .amount(200.0)
                .build()

        assertEquals(coData.totalAmount(), 230.0)
    }
}