package com.kickstarter.libs.utils.extensions

import android.os.Bundle

fun Bundle?.maybeGetBundle(key: String): Bundle? {
    return this?.getBundle(key)
}
