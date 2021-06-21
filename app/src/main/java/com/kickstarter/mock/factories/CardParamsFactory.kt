package com.kickstarter.mock.factories

import com.stripe.android.model.CardParams
import java.util.Calendar

class CardParamsFactory {

    companion object {
        @JvmOverloads
        fun params(
            number: String? = "4242424242424242",
            expMonth: Int? = 1,
            expYear: Int? = 2025,
            cvc: String? = "555"
        ): CardParams {
            return CardParams(
                number = number ?: "",
                expMonth = expMonth ?: 1,
                expYear = expYear ?: 2025,
                cvc = cvc
            )
        }

        fun unionPay() = params("620", 1, futureYear(), "555")

        fun visa() = params("424", 1, futureYear(), "555")

        fun unknown() = params("000", 1, futureYear(), "555")

        private fun futureYear(): Int {
            return Calendar.getInstance().get(Calendar.YEAR) + 2
        }
    }
}
