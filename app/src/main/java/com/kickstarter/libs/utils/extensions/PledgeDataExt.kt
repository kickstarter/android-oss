@file:JvmName("PledgeDataExt")
package com.kickstarter.libs.utils.extensions

import com.kickstarter.ui.data.PledgeData


/**
 * Total count of selected add-ons (including multiple quantities of a single add-on)
 *
 * @return Integer
 */
fun PledgeData.totalQuantity(): Int {
    var addOnsQuantity = 0
    this.addOns()?.forEach { addOn ->
        addOnsQuantity += addOn.quantity() ?: 0
    }

    return addOnsQuantity
}

/**
 * Total count of unique selected add-ons
 *
 * @return Integer
 */
fun PledgeData.totalCountUnique() = this.addOns()?.size ?: 0

/**
 * The total amount for all selected add-ons, converted to USD
 *
 * @return Double: e.g. 25.00
 */
fun PledgeData.addOnsCost(usdRate: Float): Double {
    val amount = this.addOns()?.map { addOn ->
        addOn.minimum() * (addOn.quantity() ?: 0)
    }?.sum() ?: 0

    return amount.toDouble() * usdRate
}

/**
 * The lowest amount a backer can pledge for the reward (in USD)
 *
 * @return Double: e.g. 25.00
 */
fun PledgeData.rewardCost(usdRate: Float): Double = this.reward().minimum() * usdRate