package com.kickstarter.libs.utils.extensions

import junit.framework.TestCase
import org.junit.Test

class FloatExtKtTest : TestCase() {
    @Test
    fun testCompareDescending() {
        assertEquals(-1, 1.0f.compareDescending(0f))
        assertEquals(1, 2f.compareDescending(3f))
        assertEquals(0, 2f.compareDescending(2f))
    }
}