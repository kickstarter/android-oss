package com.kickstarter.libs.utils.extensions

import junit.framework.TestCase
import org.junit.Test

class BooleanExtTest : TestCase() {
    @Test
    fun testIsTrue() {
        assertTrue(true.isTrue())
        assertFalse(false.isTrue())
        assertFalse(null.isTrue())
    }

    @Test
    fun testIsIntTrue() {
        assertTrue(1.isNonZero())
        assertFalse(0.isNonZero())
        assertFalse(0.isNonZero())
    }

    @Test
    fun testIsFalse() {
        assertFalse(true.isFalse())
        assertTrue(false.isFalse())
        assertTrue(null.isFalse())
    }

    @Test
    fun testNegate() {
        assertEquals(true.negate(), false)
        assertEquals(false.negate(), true)
    }
}
