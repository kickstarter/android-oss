package com.kickstarter.mock.factories

import com.kickstarter.models.Checkout

class CheckoutBackingFactory private constructor() {
    companion object {
        fun requiresAction(requiresAction: Boolean): Checkout.Backing {
            return Checkout.Backing.builder()
                    .clientSecret(if (requiresAction) "boop" else null)
                    .requiresAction(requiresAction)
                    .build()
        }
    }
}
