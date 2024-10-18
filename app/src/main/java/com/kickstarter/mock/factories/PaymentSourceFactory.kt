package com.kickstarter.mock.factories

import com.kickstarter.models.PaymentSource
import com.kickstarter.type.CreditCardPaymentType
import java.util.Date

class PaymentSourceFactory private constructor() {
    companion object {
        fun visa(): PaymentSource {
            return PaymentSource.builder()
                .id(IdFactory.id().toString())
                .expirationDate(Date())
                .lastFour("4321")
                .paymentType(CreditCardPaymentType.CREDIT_CARD.rawValue)
                .state("ACTIVE")
                .type("VISA")
                .build()
        }

        fun googlePay(): PaymentSource {
            return PaymentSource.builder()
                .id(IdFactory.id().toString())
                .paymentType(CreditCardPaymentType.ANDROID_PAY.rawValue)
                .state("ACTIVE")
                .build()
        }

        fun applePay(): PaymentSource {
            return PaymentSource.builder()
                .id(IdFactory.id().toString())
                .paymentType(CreditCardPaymentType.APPLE_PAY.rawValue)
                .state("ACTIVE")
                .build()
        }

        fun bankAccount(): PaymentSource {
            return PaymentSource.builder()
                .id(IdFactory.id().toString())
                .paymentType(CreditCardPaymentType.BANK_ACCOUNT.rawValue)
                .state("ACTIVE")
                .build()
        }
    }
}
