package com.kickstarter.models

/**
 * Data model for complete order mutation request
 */
data class CompleteOrderInput(
    val projectId: String,
    val orderId: String?,
    val stripePaymentMethodId: String?,
    val paymentSourceId: String?,
    val paymentSourceReusable: Boolean?,
    val paymentMethodTypes: List<String>?
)

/**
 * Data model for complete order mutation response
 */
data class CompleteOrderPayload(
    val status: String,
    val clientSecret: String
)
