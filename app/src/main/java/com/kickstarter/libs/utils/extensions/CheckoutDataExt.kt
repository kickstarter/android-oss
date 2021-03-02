@file:JvmName("CheckoutDataExt")
package com.kickstarter.libs.utils.extensions

import com.kickstarter.ui.data.CheckoutData
import kotlin.math.round

/**
 * Returns the total amount for a pledge.
 * @return Total pledge amount includes shipping, bonus support, and add-ons if applicable
 *
 * Checkout.amount = bonus support + add-ons + reward
 * Checkout.shippingAmount = shipping reward + shipping add-ons
 *
 * @return Double
 */
fun CheckoutData.totalAmount(): Double {
    return this.amount() + this.shippingAmount()
}

/**
 * Returns the total amount for a pledge in USD.
 * @return Total pledge amount includes shipping, bonus support, and add-ons if applicable
 *
 * Checkout.amount = bonus support + add-ons + reward
 * Checkout.shippingAmount = shipping reward + shipping add-ons
 *
 * @return Double
 */
fun CheckoutData.totalAmount(usdRate: Float) = round(this.totalAmount() * usdRate)

/**
 * Returns the bonus amount added to the pledge
 *
 * @return Double
 */
fun CheckoutData.bonus() = this.bonusAmount()?.let { it } ?: 0.0

/**
 * Returns the bonus amount added to the pledge in USD
 *
 * @return Double
 */
fun CheckoutData.bonus(usdRate: Float) = round(this.bonus() * usdRate)

/**
 * Returns the shipping amount in USD
 *
 * @return Double
 */
fun CheckoutData.shippingAmount(usdRate: Float) = round(this.shippingAmount() * usdRate)