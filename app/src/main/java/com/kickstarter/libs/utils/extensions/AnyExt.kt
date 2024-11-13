package com.kickstarter.libs.utils.extensions

import io.reactivex.functions.Function

fun <T : Any> T?.isNull(): Boolean {
    return this == null
}

fun <T : Any> T?.isNotNull(): Boolean {
    return this != null
}

fun <T : Any> T?.coalesce(theDefault: T): T {
    if (this.isNotNull() && this != "") {
        return this!!
    }
    return theDefault
}

fun <T : Any> coalesceWithV2(theDefault: T): Function<T, T> {
    return Function { it.coalesce(theDefault) }
}

fun <T : Any> T?.numToString(): String? {
    return when (this) {
        is Long -> this.toString()
        is Float -> this.toString()
        is Int -> this.toString()
        is Double -> this.toString()
        else -> null
    }
}

/**
 * Cast a `null`able value into a non-`null` value, and throw a `NullPointerException` if the value is `null`. Provide
 * a message for a better description of why you require this value to be non-`null`.
 */
fun <T : Any> T?.requireNonNull(klass: Class<T>): T {
    return this.requireNonNull("$klass required to be non-null.")
}

/**
 * Cast a `null`able value into a non-`null` value, and throw a `NullPointerException` if the value is `null`. Provide
 * a message for a better description of why you require this value to be non-`null`.
 */
private fun <T : Any> T?.requireNonNull(message: String): T {
    return requireNotNull(this) { message }
}
