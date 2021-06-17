package com.kickstarter.mock.factories

import com.stripe.android.model.CardParams

class CardParamsFactory {

    companion object {
        @JvmOverloads
        fun params(number: String? = "4242424242424242", expMonth: Int? = 1, expYear: Int? = 2025, cvc: String? = "555"): CardParams {
            return CardParams(number = number ?: "", expMonth = expMonth ?: 1, expYear = expYear ?: 2025, cvc = cvc)
        }
    }
}
