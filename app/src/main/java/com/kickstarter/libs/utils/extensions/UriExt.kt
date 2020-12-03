@file:JvmName("UriExt")
package com.kickstarter.libs.utils.extensions

import android.net.Uri

fun Uri.host(): String {
    return this.host?: ""
}

fun Uri.lastPathSegment(): String {
    return this.lastPathSegment?: ""
}

fun Uri.path(): String {
    return this.path?: ""
}

fun Uri.query(): String {
    return this.query ?: ""
}

/**
 * Get token from Uri query params
 * From "at={TOKEN}&ref=ksr_email_user_email_verification" to "{TOKEN}"
 */
fun Uri.getTokenFromQueryParams(): String {
    return this.query
            ?.replace("at=", "")
            ?.replace("&ref=ksr_email_user_email_verification", "")
            ?: ""
}