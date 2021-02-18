@file:JvmName("CheckoutDataExt")
package com.kickstarter.libs.utils.extensions

import com.kickstarter.ui.data.CheckoutData

/**
 * Returns the total amount for a pledge.
 * @return Total pledge amount includes shipping, bonus support, and add-ons if applicable
 *
 * Checkout.amount = bonus support + add-ons + reward
 * Checkout.shippingAmount = shipping reward + shipping add-ons
 */
fun CheckoutData.totalAmount(): Double {
    return this.amount() + this.shippingAmount()
}