package com.kickstarter.libs.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency

object MoneyUtils {
    /**
     * Converts a non-negative amount in major units to ISO currency's smallest unit.
     */
    fun toSmallestCurrencyUnit(amount: Double, currencyCode: String): Long {
        require(amount.isFinite() && amount >= 0.0) { "Amount must be finite and non-negative" }
        val fractionDigits = Currency.getInstance(currencyCode).defaultFractionDigits
        require(fractionDigits >= 0) { "Currency must have a defined smallest unit" }

        return BigDecimal.valueOf(amount)
            .movePointRight(fractionDigits)
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
    }
}
