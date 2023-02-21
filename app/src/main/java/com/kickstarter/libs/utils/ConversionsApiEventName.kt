package com.kickstarter.libs.utils

enum class ConversionsApiEventName(val contextName: String) {
    VIEW_CONTENT("ViewContent"),
    INITIATE_CHECKOUT("InitiateCheckout"),
    ADD_PAYMENT_INFO("AddPaymentInfo"),
    PURCHASE("Purchase")
}
