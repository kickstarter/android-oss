package com.kickstarter.libs.utils.extensions

import android.view.View

fun Boolean.toVisibility(): Int {
    return when (this) {
        true -> View.VISIBLE
        else -> View.GONE
    }
}
