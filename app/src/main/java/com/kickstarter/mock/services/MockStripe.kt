package com.kickstarter.mock.services

import android.content.Context
import androidx.annotation.NonNull
import com.kickstarter.mock.factories.CardFactory
import com.stripe.android.Stripe
import com.stripe.android.TokenCallback
import com.stripe.android.model.Card
import com.stripe.android.model.Token
import java.util.*

class MockStripe(@NonNull val context: Context, private val withErrors: Boolean) : Stripe(context) {
    override fun createToken(card: Card, callback: TokenCallback) {
        when {
            this.withErrors -> callback.onError(Exception("Stripe error"))
            else -> callback.onSuccess(Token("25", false, Date(), false, CardFactory.card()))
        }
    }
}
