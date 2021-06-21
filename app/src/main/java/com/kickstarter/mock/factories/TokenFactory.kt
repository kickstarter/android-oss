package com.kickstarter.mock.factories
import com.stripe.android.model.Token
import org.json.JSONObject

class TokenFactory private constructor() {
    companion object {

        private val RAW_TOKEN_NO_ID = JSONObject(
            """
            {
                "object": "token",
                "card": {
                    "id": "card_189fi32eZvKYlo2CHK8NPRME",
                    "object": "card",
                    "address_city": null,
                    "address_country": null,
                    "address_line1": null,
                    "address_line1_check": null,
                    "address_line2": null,
                    "address_state": null,
                    "address_zip": null,
                    "address_zip_check": null,
                    "brand": "Visa",
                    "country": "US",
                    "cvc_check": null,
                    "dynamic_last4": null,
                    "exp_month": 8,
                    "exp_year": 2017,
                    "funding": "credit",
                    "last4": "4242",
                    "metadata": {},
                    "name": null,
                    "tokenization_method": null
                },
                "client_ip": null,
                "created": 1462905355,
                "livemode": false,
                "type": "card",
                "used": false,
                "id": "btok_9xJAbronBnS9bH"
            }
            """.trimIndent()
        )

        /**
         * Returns a Token
         * @return Token
         * see:  https://github.com/stripe/stripe-android/blob/ad426ca33105e7bc5f17c027f8146354d13f75ae/payments-core/src/test/java/com/stripe/android/model/TokenTest.kt
         */
        fun token(): Token {
            return requireNotNull(Token.fromJson(RAW_TOKEN_NO_ID))
        }
    }
}
