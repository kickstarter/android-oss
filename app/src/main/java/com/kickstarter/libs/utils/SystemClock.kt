package com.kickstarter.libs.utils

/**
 * An injectable clock interface with two implementations - SystemClock and FakeClock.
 * The real SystemClock is used in code to avoid hardcoding System.currentTimeMillis().
 * The FakeClock is injected in tests and can be used to advance the time manually using the
 * advanceBy() method. Use this FakeClock instead of calling Thread.sleep() which slows tests down.
 */

fun interface Clock {
    fun now(): Long
}

object SystemClock : Clock {
    override fun now(): Long = System.currentTimeMillis()
}

class FakeClock(start: Long = 0L) : Clock {
    private var current = start
    override fun now(): Long = current
    fun advanceBy(millis: Long) { current += millis }
}
