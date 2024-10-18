package com.kickstarter.services.mutations

import com.kickstarter.type.PaymentTypes

data class SavePaymentMethodData(val paymentType: PaymentTypes = PaymentTypes.CREDIT_CARD, val stripeToken: String? = null, val stripeCardId: String? = null, val reusable: Boolean, val intentClientSecret: String? = null)
