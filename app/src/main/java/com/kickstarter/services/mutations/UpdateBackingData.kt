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
    val intentClientSecret: String? = null,
    val incremental: Boolean? = null
)

/**
 * Constructs an [UpdateBackingData] object with the provided parameters.
 *
 * This function handles different scenarios for updating a backing:
 * - When updating the payment method with a new payment method using PaymentSheet.
 * - When updating the payment method with a previously existing payment source.
 * - When updating other parameters like location, amount, or rewards.
 *
 * @param backing The current [Backing] object to be updated.
 * @param amount The new pledge amount (optional).
 * @param locationId The new location ID (optional).
 * @param rewardsList The list of new [Reward] objects (optional).
 * @param pMethod The new [StoredCard] to use as the payment method (optional).
 *                If provided, it determines whether to use `intentClientSecret` (for new cards via PaymentSheet)
 *                or `paymentSourceId` (for existing cards).
 * @param incremental Indicates if the update is for an incremental change (optional).
 * @return An [UpdateBackingData] object configured with the provided parameters.
 */
fun getUpdateBackingData(
    backing: Backing,
    amount: String? = null,
    locationId: String? = null,
    rewardsList: List<Reward>? = null,
    pMethod: StoredCard? = null,
    incremental: Boolean? = null
): UpdateBackingData {
    return pMethod?.let { card ->
        // - Updating the payment method, a new one from PaymentSheet or already existing one
        if (card.isFromPaymentSheet()) UpdateBackingData(
            backing,
            amount,
            locationId,
            rewardsList,
            intentClientSecret = card.clientSetupId(),
            incremental = null
        )
        else UpdateBackingData(
            backing,
            amount,
            locationId,
            rewardsList,
            paymentSourceId = card.id(),
            incremental = null
        )
        // - Updating amount, location, rewards or incremental
    } ?: UpdateBackingData(
        backing,
        amount,
        locationId,
        rewardsList,
        incremental = incremental
    )
}
