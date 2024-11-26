@file:JvmName("PledgeDataExt")
package com.kickstarter.libs.utils.extensions

import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.models.Reward
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext

fun PledgeData.locationId(): Long {
    return if (RewardUtils.isShippable(this.reward())) {
        return this.shippingRule()?.location()?.id() ?: -1
    } else -1
}
/**
 * Shipping cost associated to the selected shipping rule if the
 * selected reward:
 * Amount = RWShipping + AddOnShipping.shipping( xQ)
 *
 */
fun PledgeData.shippingCostIfShipping(): Double {
    val rwShippingCost = if (RewardUtils.isShippable(this.reward())) {
        val matchingLocationIdRule = this.reward().shippingRules()?.find { it.location()?.id() == this.locationId() }
        // - "Earth" shipping rule has location.id == 1
        matchingLocationIdRule?.cost()
            ?: (this.reward().shippingRules()?.find { it.location()?.id() == 1L }?.cost() ?: 0.0)
    } else 0.0

    var addOnsShippingCost = 0.0
    this.addOns()?.map {
        if (RewardUtils.shipsWorldwide(it) || RewardUtils.shipsToRestrictedLocations(it)) {
            addOnsShippingCost += (it.shippingRules()?.firstOrNull()?.cost() ?: 0.0) * (it.quantity() ?: 0)
        } else 0.0
    }

    return rwShippingCost + addOnsShippingCost
}

/**
 * Total checkout Amount = Reward + AddOns( xQ) + bonus + Shipping
 */
fun PledgeData.checkoutTotalAmount(): Double = this.pledgeAmountTotalPlusBonus() + this.shippingCostIfShipping()

/**
 * Total checkout Amount = Reward + AddOns( xQ) + bonus
 */
fun PledgeData.pledgeAmountTotalPlusBonus(): Double = this.pledgeAmountTotal() + this.bonusAmount()

/**
 * Total pledge Amount = Reward + AddOns( xQ)
 */
fun PledgeData.pledgeAmountTotal(): Double {
    // - Avoid project miss configuration where the creator did not configured somehow the late pledge reward correctly
    var latePledge = if (this.reward().latePledgeAmount() == 0.0) this.reward().minimum() else this.reward().latePledgeAmount()
    var crowdfund = this.reward().pledgeAmount()

    if (this.pledgeFlowContext() == PledgeFlowContext.LATE_PLEDGES) {
        this.addOns()?.map {
            // - Avoid project miss configuration where the creator did not configured somehow the late pledge reward correctly
            val amount = if (it.latePledgeAmount() == 0.0) it.minimum() else it.latePledgeAmount()
            latePledge += amount * (it.quantity() ?: 0)
        }
        return latePledge
    } else {
        this.addOns()?.map {
            crowdfund += it.pledgeAmount() * (it.quantity() ?: 0)
        }

        return crowdfund
    }
}

fun PledgeData.rewardsAndAddOnsList(): List<Reward> {
    val list = mutableListOf<Reward>()

    list.add(this.reward())
    list.addAll(this.addOns() ?: emptyList())

    return list
}

fun PledgeData.expandedRewardsAndAddOnsList(): List<Reward> {
    val list = mutableListOf<Reward>()

    list.add(this.reward())
    val mutableAddOnsList = mutableListOf<Reward>()

    this.addOns()?.map {
        if (!it.isAddOn()) mutableAddOnsList.add(it)
        else {
            val q = it.quantity() ?: 1
            for (i in 1..q) {
                mutableAddOnsList.add(it)
            }
        }
    }

    if (mutableAddOnsList.isNotEmpty()) list.addAll(mutableAddOnsList)

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
