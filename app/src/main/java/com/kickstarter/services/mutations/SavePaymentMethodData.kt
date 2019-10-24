package com.kickstarter.services.mutations

import type.PaymentTypes

data class SavePaymentMethodData(val paymentType: PaymentTypes = PaymentTypes.CREDIT_CARD, val stripeToken: String, val stripeCardId: String, val reusable: Boolean)