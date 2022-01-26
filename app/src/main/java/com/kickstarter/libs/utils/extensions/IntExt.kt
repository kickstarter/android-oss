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
