package com.kickstarter.libs.utils.extensions

import com.kickstarter.libs.utils.ObjectUtils
import io.reactivex.functions.Function
import rx.functions.Func1

fun <T : Any> T?.isNull(): Boolean {
    return this == null
}

fun <T : Any> T?.isNotNull(): Boolean {
    return this != null
}

fun <T : Any> T?.coalesce(theDefault: T): T {
    if (this.isNotNull()) {
        return this!!
    }
    return theDefault
}

fun <T : Any> coalesceWith(theDefault: T): Func1<T, T> {
    return Func1 { it.coalesce(theDefault) }
}

fun <T : Any> coalesceWithV2(theDefault: T): Function<T, T> {
    return Function { it.coalesce(theDefault) }
}

fun <T : Any> T?.numToString(): String? { // remove for any kotlin code, keep for java
    return when (this) {
        is Long -> this.toString()
        is Float -> this.toString()
        is Int -> this.toString()
        is Double -> this.toString()
        else -> null
    }
}

fun <T : Any> T?.requireNonNull(): T { // remove for any kotlin code, keep for java if it exists on old object utils
    return this.requireNonNull("Value should not be null.")
}

fun <T : Any> T?.requireNonNull(klass: Class<T>): T {
    return this.requireNonNull("$klass required to be non-null.")
}

fun <T : Any> T?.requireNonNull(message: String): T {
    if (this.isNull()) {
        throw NullPointerException(message)
    } else {
        return this!!
    }
}
