package com.kickstarter.libs.utils.extensions

import com.stripe.android.model.PaymentMethod
import com.stripe.android.paymentsheet.model.PaymentOption

const val PAYMENT_METHOD_TYPE_GOOGLE_PAY = "google_pay"

fun PaymentOption.isGooglePay(): Boolean =
    this.paymentMethodType == PAYMENT_METHOD_TYPE_GOOGLE_PAY

fun PaymentOption.isLink(): Boolean =
    this.paymentMethodType == PaymentMethod.Type.Link.code
