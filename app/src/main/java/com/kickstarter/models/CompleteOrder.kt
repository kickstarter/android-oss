package com.kickstarter.models

/**
 * Data model for complete order mutation request
 */
data class CompleteOrderInput(
    val projectId: String,
    val orderId: String? = null,
    val stripePaymentMethodId: String? = null,
    val paymentSourceId: String? = null,
    val paymentSourceReusable: Boolean? = null,
    val paymentMethodTypes: List<String>? = null
)

/**
 * Data model for complete order mutation response
 */
data class CompleteOrderPayload(
    val status: String = "",
    val clientSecret: String = "",
    val trigger3ds: Boolean = false,
    val stripePaymentMethodId: String = ""
)
