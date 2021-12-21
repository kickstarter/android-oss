package com.kickstarter.libs.utils

object IntegerUtils {
    /**
     * Returns `false` if `value` is `null` or `0`, and `true` otherwise.
     */
    @JvmStatic
    fun isNonZero(value: Int?): Boolean {
        return value != null && value != 0
    }

    /**
     * Returns `true` if `value` is zero, and false otherwise, including when `value` is `null`.
     */
    @JvmStatic
    fun isZero(value: Int?): Boolean {
        return value != null && value == 0
    }

    /**
     * Returns `value` if not null, and `0` otherwise.
     */
    @JvmStatic
    fun intValueOrZero(value: Int?): Int {
        return value ?: 0
    }

    /**
     * Returns `true` if `value` is null or zero, and false otherwise.
     */
    @JvmStatic
    fun isNullOrZero(value: Int?): Boolean {
        return value == null || value == 0
    }

    /**
     * Returns `true` if `value` is zero or higher, and false otherwise.
     */
    fun isZeroOrHigher(value: Int?): Boolean {
        return value != null && value >= 0
    }
}
