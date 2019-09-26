package com.kickstarter.libs.utils

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.kickstarter.R

object UrlUtils {

    private const val KEY_REF = "ref"

    fun appendPath(baseUrl: String, path: String): String {
        val uriBuilder = Uri.parse(baseUrl).buildUpon()
        uriBuilder.appendEncodedPath(path)

        return uriBuilder.build().toString()
    }

    fun appendQueryParameter(baseUrl: String, key: String, value: String): String {
        val uriBuilder = Uri.parse(baseUrl).buildUpon()
        uriBuilder.appendQueryParameter(key, value)

        return uriBuilder.build().toString()
    }

    fun appendRefTag(baseUrl: String, tag: String): String {
        return appendQueryParameter(baseUrl, KEY_REF, tag)
    }

    fun baseCustomTabsIntent(context: Context): CustomTabsIntent {
        val builder = CustomTabsIntent.Builder()

        builder.setShowTitle(true)
        builder.setToolbarColor(ContextCompat.getColor(context, R.color.primary))

        return builder.build()
    }

    fun refTag(url: String): String? {
        return Uri.parse(url).getQueryParameter(KEY_REF)
    }
}
