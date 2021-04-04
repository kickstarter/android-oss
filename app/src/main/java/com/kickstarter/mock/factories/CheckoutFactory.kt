package com.kickstarter.mock.factories

import com.kickstarter.models.Checkout

class CheckoutFactory private constructor() {
    companion object {
        fun requiresAction(requiresAction: Boolean): Checkout {
            return Checkout.builder()
                .id(IdFactory.id().toLong())
                .backing(CheckoutBackingFactory.requiresAction(requiresAction))
                .build()
        }
    }
}
