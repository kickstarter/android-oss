package com.kickstarter.mock.factories

import com.stripe.android.model.Card
import com.stripe.android.model.Token
import java.util.Date

class TokenFactory private constructor() {
    companion object {

        fun token(card: Card): Token {
            return Token("id", false, Date(), false, card)
        }
    }
}
