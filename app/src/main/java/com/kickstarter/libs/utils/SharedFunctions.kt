package com.kickstarter.libs.utils

import com.google.android.gms.common.util.Base64Utils
import com.kickstarter.models.Relay
import com.kickstarter.models.Reward
import com.kickstarter.ui.data.CheckoutData
import type.CreditCardPaymentType
import java.nio.charset.Charset
import kotlin.math.absoluteValue

/**
 * Total count of selected add-ons (including multiple quantities of a single add-on)
 *
 * @return Integer
 */
fun totalQuantity(addOns: List<Reward>?): Int {
    var addOnsQuantity = 0
    addOns?.forEach { addOn ->
        addOnsQuantity += addOn.quantity() ?: 0
    }

    return addOnsQuantity
}

/**
 * Total count of unique selected add-ons
 *
 * @return Integer
 */
fun totalCountUnique(addOns: List<Reward>?) = addOns?.size ?: 0

/**
 * The total amount for all selected add-ons, converted to USD
 *
 * @return Double: e.g. 25.00
 */
fun addOnsCost(usdRate: Float, addOns: List<Reward>?): Double {
    val amount = addOns?.map { addOn ->
        addOn.minimum() * (addOn.quantity() ?: 0)
    }?.sum() ?: 0

    return amount.toDouble() * usdRate
}

/**
 * The lowest amount a backer can pledge for the reward (in USD)
 *
 * @return Double: e.g. 25.00
 */
fun rewardCost(usdRate: Float, reward: Reward): Double = reward.minimum() * usdRate

fun checkoutProperties(
    amount: Double,
    checkoutId: Long?,
    bonus: Double,
    shippingAmount: Double,
    paymentType: CreditCardPaymentType = CreditCardPaymentType.CREDIT_CARD
): CheckoutData {

    return CheckoutData.builder()
        .amount(amount)
        .id(checkoutId)
        .paymentType(paymentType)
        .bonusAmount(bonus)
        .shippingAmount(shippingAmount)
        .build()
}


fun <T : Relay> encodeRelayId(relay: T): String {
    val classSimpleName = relay.javaClass.simpleName.replaceFirst("AutoParcel_", "")
    val id = relay.id()
    return Base64Utils.encodeUrlSafe(("$classSimpleName-$id").toByteArray(Charset.defaultCharset()))
}

fun decodeRelayId(encodedRelayId: String?): Long? {
    return try {
        String(Base64Utils.decode(encodedRelayId), Charset.defaultCharset())
            .replaceBeforeLast("-", "", "")
            .toLong()
            .absoluteValue
    } catch (e: Exception) {
        null
    }
}