package com.kickstarter.libs.utils

import com.kickstarter.libs.models.Country
import com.kickstarter.libs.utils.MoneyUtils.toSmallestCurrencyUnit
import org.junit.Assert
import org.junit.Assert.assertThrows
import org.junit.Test

class MoneyUtilsTest {
    @Test
    fun testToSmallestCurrencyUnit_forAllSupportedCurrencies() {
        val currencies = Country.entries.map { it.currencyCode }.distinct()
        val expectedAmounts = mapOf(
            "AUD" to 4237L,
            "CAD" to 4237L,
            "CHF" to 4237L,
            "DKK" to 4237L,
            "EUR" to 4237L,
            "GBP" to 4237L,
            "HKD" to 4237L,
            "JPY" to 42L,
            "MXN" to 4237L,
            "NOK" to 4237L,
            "NZD" to 4237L,
            "PLN" to 4237L,
            "SEK" to 4237L,
            "SGD" to 4237L,
            "USD" to 4237L
        )
        Assert.assertEquals(expectedAmounts.keys, currencies.toSet())
        currencies.forEach { currencyCode ->
            Assert.assertEquals(
                currencyCode,
                expectedAmounts.getValue(currencyCode),
                toSmallestCurrencyUnit(42.37, currencyCode)
            )
        }
        Assert.assertEquals(4200L, toSmallestCurrencyUnit(4200.0, "JPY"))
    }

    @Test
    fun testToSmallestCurrencyUnit_roundsHalfUp() {
        Assert.assertEquals(4237L, toSmallestCurrencyUnit(42.374, "USD"))
        Assert.assertEquals(4238L, toSmallestCurrencyUnit(42.375, "USD"))
        Assert.assertEquals(42L, toSmallestCurrencyUnit(42.4, "JPY"))
        Assert.assertEquals(43L, toSmallestCurrencyUnit(42.5, "JPY"))
        Assert.assertEquals(0L, toSmallestCurrencyUnit(0.0, "USD"))
    }

    @Test
    fun testToSmallestCurrencyUnit_rejectsInvalidInputs() {
        listOf(-1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach {
            assertThrows(IllegalArgumentException::class.java) {
                toSmallestCurrencyUnit(it, "USD")
            }
        }
        assertThrows(IllegalArgumentException::class.java) {
            toSmallestCurrencyUnit(1.0, "INVALID")
        }
        assertThrows(IllegalArgumentException::class.java) {
            toSmallestCurrencyUnit(1.0, "XXX")
        }
        assertThrows(ArithmeticException::class.java) {
            toSmallestCurrencyUnit(1e17, "USD")
        }
    }
}
