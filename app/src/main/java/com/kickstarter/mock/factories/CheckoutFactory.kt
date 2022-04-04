package com.kickstarter.mock.factories

import com.kickstarter.models.Checkout

object CheckoutFactory {
    @JvmStatic
    fun requiresAction(requiresAction: Boolean): Checkout {
        return Checkout.builder()
            .id(IdFactory.id().toLong())
            .backing(CheckoutBackingFactory.requiresAction(requiresAction))
            .build()
    }
}
