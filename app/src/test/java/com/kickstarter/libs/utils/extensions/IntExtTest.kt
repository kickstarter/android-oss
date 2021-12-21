package com.kickstarter.libs.utils.extensions

import junit.framework.TestCase
import org.junit.Test

class IntExtTest : TestCase() {
    @Test
    fun testIsNonZero() {
        assertTrue(1.isNonZero())
        assertTrue((-1).isNonZero())
        assertFalse(0.isNonZero())
        assertFalse(null.isNonZero())
    }

    @Test
    fun testIsZero() {
        assertFalse(1.isZero())
        assertFalse((-1).isZero())
        assertTrue(0.isZero())
        assertFalse(null.isZero())
    }

    @Test
    fun testIntValueOrZero() {
        assertEquals(5, 5.intValueOrZero())
        assertEquals(0, null.intValueOrZero())
    }

    @Test
    fun testIsNullOrZero() {
        assertFalse(1.isNullOrZero())
        assertFalse((-1).isNullOrZero())
        assertTrue(0.isNullOrZero())
        assertTrue(null.isNullOrZero())
    }
}
