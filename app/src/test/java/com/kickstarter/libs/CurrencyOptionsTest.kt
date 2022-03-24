package com.kickstarter.libs

import junit.framework.TestCase
import org.junit.Test

class CurrencyOptionsTest : TestCase() {

    @Test
    fun testDefaultInit() {
        val currencyOptions = CurrencyOptions.builder()
            .country("JP")
            .currencyCode("USD")
            .currencySymbol("$")
            .value(1234F)
            .build()

        assertEquals(currencyOptions.country(), "JP")
        assertEquals(currencyOptions.currencyCode(), "USD")
        assertEquals(currencyOptions.currencySymbol(), "$")
        assertEquals(currencyOptions.value(), 1234F)
    }

    @Test
    fun testCurrencyOptions_equalTrue() {
        val currencyOptions1 = CurrencyOptions.builder()
            .country("JP")
            .currencyCode("USD")
            .currencySymbol("$")
            .value(1234F)
            .build()

        val currencyOptions2 = CurrencyOptions.builder()
            .country("JP")
            .currencyCode("USD")
            .currencySymbol("$")
            .value(1234F)
            .build()

        assertEquals(currencyOptions1, currencyOptions2)
    }

    @Test
    fun testCurrencyOptions_equalFalse() {
        val currencyOptions1 = CurrencyOptions.builder()
            .country("JP")
            .currencyCode("USD")
            .currencySymbol("$")
            .value(1234F)
            .build()

        val currencyOptions2 = CurrencyOptions.builder()
            .country("JP")
            .currencyCode("USD")
            .currencySymbol("$")
            .value(5678F)
            .build()

        val currencyOptions3 = CurrencyOptions.builder()
            .country("JP")
            .currencyCode("DE")
            .currencySymbol("$")
            .value(5678F)
            .build()

        val currencyOptions4 = CurrencyOptions.builder()
            .country("JP")
            .currencyCode("USD")
            .currencySymbol("€")
            .value(5678F)
            .build()

        assertFalse(currencyOptions1 == currencyOptions2)
        assertFalse(currencyOptions1 == currencyOptions3)
        assertFalse(currencyOptions2 == currencyOptions3)

        assertFalse(currencyOptions3 == currencyOptions4)
        assertFalse(currencyOptions4 == currencyOptions1)
    }
}
