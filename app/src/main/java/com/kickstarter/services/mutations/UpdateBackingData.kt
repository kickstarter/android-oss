package com.kickstarter.services.mutations

import com.kickstarter.models.Backing
import com.kickstarter.models.Reward
import com.kickstarter.models.StoredCard
import com.kickstarter.models.extensions.isFromPaymentSheet

data class UpdateBackingData(
    val backing: Backing,
    val amount: String? = null,
    val locationId: String? = null,
    val rewardsIds: List<Reward>? = null,
    val paymentSourceId: String? = null,
    val intentClientSecret: String? = null
)

/**
 * Obtain the data model input that will be send to UpdateBacking mutation
 * - When updating payment method with a new payment method using payment sheet
 * - When updating payment method with a previously existing payment source
 * - Updating any other parameter like location, amount or rewards
 */
fun getUpdateBackingData(
    backing: Backing,
    amount: String? = null,
    locationId: String? = null,
    rewardsList: List<Reward>? = null,
    pMethod: StoredCard? = null
): UpdateBackingData {
    return pMethod?.let { card ->
        // - Updating the payment method, a new one from PaymentSheet or already existing one
        if (card.isFromPaymentSheet()) UpdateBackingData(
            backing,
            amount,
            locationId,
            rewardsList,
            intentClientSecret = card.clientSetupId()
        )
        else UpdateBackingData(
            backing,
            amount,
            locationId,
            rewardsList,
            paymentSourceId = card.id()
        )
        // - Updating amount, location or rewards
    } ?: UpdateBackingData(
        backing,
        amount,
        locationId,
        rewardsList
    )
}
