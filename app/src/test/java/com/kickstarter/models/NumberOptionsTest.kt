package com.kickstarter.models

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.NumberOptions
import org.junit.Test
import java.math.RoundingMode

class NumberOptionsTest : KSRobolectricTestCase() {

    @Test
    fun testDefaultInit() {
        val numberOptions = NumberOptions.builder()
            .bucketAbove(3.2f)
            .bucketPrecision(10)
            .currencyCode("USD")
            .currencySymbol("$")
            .precision(12)
            .roundingMode(RoundingMode.HALF_UP)
            .build()

        assertEquals(numberOptions.bucketAbove(), 3.2f)
        assertEquals(numberOptions.bucketPrecision(), 10)
        assertEquals(numberOptions.currencyCode(), "USD")
        assertEquals(numberOptions.currencySymbol(), "$")
        assertEquals(numberOptions.precision(), 12)
        assertEquals(numberOptions.roundingMode(), RoundingMode.HALF_UP)
    }

    @Test
    fun testDefaultToBuilder() {
        val numberOptions = NumberOptions.builder().build().toBuilder().currencySymbol("$").build()
        assertEquals(numberOptions.currencySymbol(), "$")
    }

    @Test
    fun testNumberOptions_equalFalse() {
        val numberOptions = NumberOptions.builder().build()
        val numberOptions2 = NumberOptions.builder().bucketAbove(3.2f).build()
        val numberOptions3 = NumberOptions.builder().currencySymbol("$").currencyCode("USD").build()
        val numberOptions4 = NumberOptions.builder().precision(12).build()

        assertFalse(numberOptions == numberOptions2)
        assertFalse(numberOptions == numberOptions3)
        assertFalse(numberOptions == numberOptions4)

        assertFalse(numberOptions3 == numberOptions2)
        assertFalse(numberOptions3 == numberOptions4)
    }

    @Test
    fun testNumberOptions_equalTrue() {
        val numberOptions1 = NumberOptions.builder().build()
        val numberOptions2 = NumberOptions.builder().build()

        assertEquals(numberOptions1, numberOptions2)
    }
}
