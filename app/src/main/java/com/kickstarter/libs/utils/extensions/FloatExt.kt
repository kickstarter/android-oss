package com.kickstarter.libs.utils.extensions

fun Float.compareDescending(other: Float): Int {
    return other.compareTo(this)
}
