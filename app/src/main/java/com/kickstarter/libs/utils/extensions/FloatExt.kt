package com.kickstarter.libs.utils.extensions

fun Float.compareDescending(other: Float) : Int {
    return if (this < other) 1 else if (this == other) 0 else -1
}