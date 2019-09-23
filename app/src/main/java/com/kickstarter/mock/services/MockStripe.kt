package com.kickstarter.mock.services

import android.content.Context
import androidx.annotation.NonNull
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.mock.factories.CardFactory
import com.stripe.android.ApiResultCallback
import com.stripe.android.Stripe
import com.stripe.android.model.Card
import com.stripe.android.model.Token
import java.util.*

class MockStripe(@NonNull val context: Context, private val withErrors: Boolean) : Stripe(context, Secrets.StripePublishableKey.STAGING) {
    override fun createToken(card: Card, callback: ApiResultCallback<Token>) {
        when {
            this.withErrors -> callback.onError(Exception("Stripe error"))
            else -> callback.onSuccess(Token("25", false, Date(), false, CardFactory.card()))
        }
    }
}
