package com.kickstarter.mock.factories

import com.kickstarter.ui.data.CheckoutData
import type.CreditCardPaymentType

object CheckoutDataFactory {
    @JvmStatic
    fun checkoutData(shippingAmount: Double, totalAmount: Double): CheckoutData {
        return CheckoutData.builder()
            .amount(totalAmount)
            .paymentType(CreditCardPaymentType.CREDIT_CARD)
            .shippingAmount(shippingAmount)
            .build()
    }

    @JvmStatic
    fun checkoutData(id: Long, shippingAmount: Double, totalAmount: Double): CheckoutData {
        return checkoutData(shippingAmount, totalAmount)
            .toBuilder()
            .id(id)
            .build()
    }
}
