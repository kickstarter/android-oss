package com.kickstarter.models

data class CreatePaymentIntentInput(val project: Project, val amount: String, val checkoutId: String, val backing: Backing)
