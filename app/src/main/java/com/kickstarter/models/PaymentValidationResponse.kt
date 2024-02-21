package com.kickstarter.models

data class PaymentValidationResponse(val isValid: Boolean, val messages: List<String>)
