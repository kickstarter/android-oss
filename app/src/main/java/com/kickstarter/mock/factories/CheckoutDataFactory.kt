package com.kickstarter.mock.factories

import com.kickstarter.ui.data.CheckoutData
import type.CreditCardPaymentType

class CheckoutDataFactory private constructor() {
    companion object {
        fun checkoutData(shippingAmount: Double, totalAmount: Double): CheckoutData {
            return CheckoutData.builder()
                .amount(totalAmount)
                .paymentType(CreditCardPaymentType.CREDIT_CARD)
                .shippingAmount(shippingAmount)
                .build()
        }

        fun checkoutData(id: Long, shippingAmount: Double, totalAmount: Double): CheckoutData {
            return checkoutData(shippingAmount, totalAmount)
                .toBuilder()
                .id(id)
                .build()
        }
    }
}
