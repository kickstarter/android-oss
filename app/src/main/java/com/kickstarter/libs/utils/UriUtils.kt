package com.kickstarter.libs.utils

import android.net.Uri

fun host(uri: Uri): String {
    return uri.host?: ""
}

fun lastPathSegment(uri: Uri): String {
    return uri.lastPathSegment?: ""
}

fun path(uri: Uri): String {
    return uri.path?: ""
}
