package com.kickstarter.libs.utils.extensions

import android.view.View

fun Boolean.toVisibility(): Int {
    return when (this) {
        true -> View.VISIBLE
        else -> View.GONE
    }
}

/**
 * Returns the input boolean negated.
 */
fun Boolean.negate(): Boolean {
    return !this
}

/**
 * Returns `false` if the boolean is `null` or `false`, and `true` otherwise.
 */
fun Boolean?.isTrue(): Boolean {
    return when (this) {
        null -> false
        true -> true
        false -> false
    }
}

/**
 * Returns `true` if the boolean is `null` or `false`, and `true` otherwise.
 */
fun Boolean?.isFalse(): Boolean {
    return when (this) {
        null -> true
        true -> false
        false -> true
    }
}
