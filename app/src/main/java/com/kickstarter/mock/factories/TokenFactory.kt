package com.kickstarter.mock.factories
import com.stripe.android.model.Card
import com.stripe.android.model.Token
import org.json.JSONObject

class TokenFactory private constructor() {
    companion object {

        /**
         * Returns a Token
         * @param card to generate the token for
         * see:  https://github.com/stripe/stripe-android/blob/ad426ca33105e7bc5f17c027f8146354d13f75ae/payments-core/src/test/java/com/stripe/android/model/TokenTest.kt
         */
        fun token(card: Card): Token {
            val tokenString = JSONObject(
                """
            {
                "object": "token",
                "card": {
                    "id": "${card.id}",
                    "object": "card",
                    "address_city": null,
                    "address_country": null,
                    "address_line1": null,
                    "address_line1_check": null,
                    "address_line2": null,
                    "address_state": null,
                    "address_zip": null,
                    "address_zip_check": null,
                    "brand": "${card.brand}",
                    "country": "${card.country}",
                    "cvc_check": null,
                    "dynamic_last4": null,
                    "exp_month": ${card.expMonth},
                    "exp_year": ${card.expYear},
                    "funding": "${card.funding}",
                    "last4": "${card.last4}",
                    "metadata": {},
                    "name": null,
                    "tokenization_method": null
                },
                "client_ip": null,
                "created": 1462905355,
                "livemode": false,
                "type": "card",
                "used": false
            }
                """.trimIndent()
            )
            return requireNotNull(Token.fromJson(tokenString))
        }
    }
}
