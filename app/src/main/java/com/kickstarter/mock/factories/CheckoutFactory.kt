package com.kickstarter.mock.factories

import com.kickstarter.models.Checkout
import type.CheckoutState

class CheckoutFactory private constructor() {
    companion object {
        fun successful(): Checkout {
            return Checkout.builder()
                    .state(CheckoutState.SUCCESSFUL.rawValue())
                    .backing(CheckoutBackingFactory.requiresAction(false))
                    .build()
        }

        fun requiresSCA(): Checkout {
            return Checkout.builder()
                    .state(CheckoutState.SUCCESSFUL.rawValue())
                    .backing(CheckoutBackingFactory.requiresAction(true))
                    .build()
        }
    }
}