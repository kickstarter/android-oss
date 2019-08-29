package com.kickstarter.mock.factories

import com.kickstarter.models.Backing
import java.util.*

class PaymentSourceFactory private constructor() {
    companion object {
        fun visa(): Backing.PaymentSource {
            return Backing.PaymentSource.builder()
                    .id(IdFactory.id().toString())
                    .expirationDate(Date())
                    .lastFour("4321")
                    .paymentType("CREDIT_CARD")
                    .state("ACTIVE")
                    .type("VISA")
                    .build()
        }

        fun googlePay(): Backing.PaymentSource {
            return Backing.PaymentSource.builder()
                    .id(IdFactory.id().toString())
                    .paymentType("GOOGLE_PAY")
                    .state("ACTIVE")
                    .build()
        }

        fun applePay(): Backing.PaymentSource {
            return Backing.PaymentSource.builder()
                    .id(IdFactory.id().toString())
                    .paymentType("APPLE_PAY")
                    .state("ACTIVE")
                    .build()
        }
    }
}
