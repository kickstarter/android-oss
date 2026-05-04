package com.kickstarter.libs.utils.extensions

/**
 * Returns `false` if `this` is `null` or `0`, and `true` otherwise.
 */
fun Int?.isNonZero(): Boolean {
    return this != null && this != 0
}
/**
 * Returns `true` if `this` is zero, and false otherwise, including when `this` is `null`.
 */
fun Int?.isZero(): Boolean {
    return this != null && this == 0
}

/**
 * Returns `this` if not null, and `0` otherwise.
 */

fun Int?.intValueOrZero(): Int {
    return this ?: 0
}

/**
 * Returns `true` if `this` is null or zero, and false otherwise.
 */

fun Int?.isNullOrZero(): Boolean {
    return this == null || this == 0
}

/**
 * Returns `true` if `this` is zero or higher, and false otherwise.
 */
fun Int?.isZeroOrHigher(): Boolean {
    return this != null && this >= 0
}

/**
 * Returns `true` if `this` is 1 or Higer
 * False if `this` is null or this is 0 or minor
 */
fun Int?.toBoolean(): Boolean {
    return this != null && this >= 1
}

/**
 * Formats an integer as a compact string: 999 → "999", 1200 → "1.2K", 1000000 → "1M".
 */
fun Int.toCompactFormat(): String {
    return when {
        this >= 1_000_000 -> {
            val value = this / 1_000_000.0
            if (value % 1.0 == 0.0) "${value.toInt()}M" else String.format("%.1fM", value)
        }
        this >= 1_000 -> {
            val value = this / 1_000.0
            if (value % 1.0 == 0.0) "${value.toInt()}K" else String.format("%.1fK", value)
        }
        else -> this.toString()
    }
}
