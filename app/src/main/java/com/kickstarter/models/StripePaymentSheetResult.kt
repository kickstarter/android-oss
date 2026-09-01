package com.kickstarter.models

import com.stripe.android.paymentsheet.PaymentSheetResult

sealed class StripePaymentSheetResult {
    object Completed : StripePaymentSheetResult()
    object Canceled : StripePaymentSheetResult()
    data class Failed(val error: Throwable) : StripePaymentSheetResult()
}

fun PaymentSheetResult.toStripePaymentSheetResult() =
    when (this) {
        is PaymentSheetResult.Canceled -> StripePaymentSheetResult.Canceled
        is PaymentSheetResult.Failed -> StripePaymentSheetResult.Failed(error)
        is PaymentSheetResult.Completed -> StripePaymentSheetResult.Completed
    }
