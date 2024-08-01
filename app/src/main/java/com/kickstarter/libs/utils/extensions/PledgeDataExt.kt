@file:JvmName("PledgeDataExt")
package com.kickstarter.libs.utils.extensions

import com.kickstarter.models.Reward
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext

fun PledgeData.pledgeAmountTotal(): Double {
    var crowdFund = this.reward().pledgeAmount()
    var latePledge = this.reward().latePledgeAmount()

    if (this.pledgeFlowContext() != PledgeFlowContext.LATE_PLEDGES) {
        this.addOns()?.map {
            crowdFund += it.latePledgeAmount() * (it.quantity() ?: 0)
        }
        return crowdFund
    } else {
        this.addOns()?.map {
            latePledge += it.pledgeAmount() * (it.quantity() ?: 0)
        }

        return latePledge
    }
}

fun PledgeData.rewardsAndAddOnsList(): List<Reward> {
    val list = mutableListOf<Reward>()

    list.add(this.reward())
    list.addAll(this.addOns() ?: emptyList())

    return list
}

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
