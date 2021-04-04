package com.kickstarter.mock.factories

import com.stripe.android.model.Card

class CardFactory {

    companion object {
        @JvmOverloads
        fun card(number: String? = "4242424242424242", expMonth: Int? = 1, expYear: Int? = 2025, cvc: String? = "555"): Card {
            return Card.Builder(number, expMonth, expYear, cvc)
                .id("3")
                .build()
        }
    }
}
