package com.kickstarter.models

data class CheckoutPayment(val id: Long, val paymentUrl: String?, val backing: Backing? = null)
