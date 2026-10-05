package com.kickstarter.utils

import android.annotation.SuppressLint
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentsClient
import com.stripe.android.googlepaylauncher.GooglePayAvailabilityClient
import com.stripe.android.googlepaylauncher.GooglePayRepository
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

object GooglePayAvailabilityUtil {
    /* This is a workaround that allows Google Pay to work in `Test` mode, pulled directly from
     * the associated Issue in the Stripe SDK: https://github.com/stripe/stripe-android/issues/13968 */
    fun overrideGooglePayAvailabilityClient() {
        @SuppressLint("RestrictedApi")
        GooglePayRepository.googlePayAvailabilityClientFactory =
            object : GooglePayAvailabilityClient.Factory {
                override fun create(paymentsClient: PaymentsClient) =
                    object : GooglePayAvailabilityClient {
                        override suspend fun isReady(request: IsReadyToPayRequest): Boolean {
                            val relaxed = JSONObject(request.toJson())
                                .apply { remove("existingPaymentMethodRequired") }
                            return paymentsClient
                                .isReadyToPay(IsReadyToPayRequest.fromJson(relaxed.toString()))
                                .await()
                        }
                    }
            }
    }
}
