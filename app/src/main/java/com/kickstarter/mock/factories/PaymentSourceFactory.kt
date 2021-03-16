package com.kickstarter.mock.factories

import com.kickstarter.models.Backing
import type.CreditCardPaymentType
import java.util.Date

class PaymentSourceFactory private constructor() {
    companion object {
        fun visa(): Backing.PaymentSource {
            return Backing.PaymentSource.builder()
                .id(IdFactory.id().toString())
                .expirationDate(Date())
                .lastFour("4321")
                .paymentType(CreditCardPaymentType.CREDIT_CARD.rawValue())
                .state("ACTIVE")
                .type("VISA")
                .build()
        }

        fun googlePay(): Backing.PaymentSource {
            return Backing.PaymentSource.builder()
                .id(IdFactory.id().toString())
                .paymentType(CreditCardPaymentType.ANDROID_PAY.rawValue())
                .state("ACTIVE")
                .build()
        }

        fun applePay(): Backing.PaymentSource {
            return Backing.PaymentSource.builder()
                .id(IdFactory.id().toString())
                .paymentType(CreditCardPaymentType.APPLE_PAY.rawValue())
                .state("ACTIVE")
                .build()
        }

        fun bankAccount(): Backing.PaymentSource {
            return Backing.PaymentSource.builder()
                .id(IdFactory.id().toString())
                .paymentType(CreditCardPaymentType.BANK_ACCOUNT.rawValue())
                .state("ACTIVE")
                .build()
        }
    }
}
