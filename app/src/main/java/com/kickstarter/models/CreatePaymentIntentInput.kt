package com.kickstarter.models

data class CreatePaymentIntentInput(val project: Project, val amountDollars: String)
