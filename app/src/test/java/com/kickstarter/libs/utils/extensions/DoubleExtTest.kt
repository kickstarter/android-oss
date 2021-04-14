package com.kickstarter.libs.utils.extensions

import junit.framework.TestCase

class DoubleExtTest : TestCase() {

    fun testMultiply2RoundDecimal() {

        val val1 = 2.0212
        val val2 = 6.0443

        val expected = 12.22

        assertEquals(val1.multiplyRound2Decimal(val2), expected)
    }

    fun testRoundDecimal() {

        val val1 = 2.0212
        val expected = 2.02

        assertEquals(val1.round(), expected)
    }
}
