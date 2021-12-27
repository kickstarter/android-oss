package com.kickstarter.libs.utils.extensions

import junit.framework.TestCase
import org.junit.Test

class DoubleExtTest : TestCase() {
    @Test
    fun testRoundDecimal() {

        val val1 = 2.0212
        val expected = 2.02

        assertEquals(val1.round(), expected)
    }
}
