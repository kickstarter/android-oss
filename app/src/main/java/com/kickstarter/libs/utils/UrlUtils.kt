package com.kickstarter.libs.utils

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.kickstarter.R

object UrlUtils {

    fun baseCustomTabsIntent(context: Context): CustomTabsIntent {
        val builder = CustomTabsIntent.Builder()

        builder.setShowTitle(true)
        builder.setToolbarColor(ContextCompat.getColor(context, R.color.primary))

        return builder.build()
    }

    fun buildUrl(baseUrl: String, path: String): String {
        val uriBuilder = Uri.parse(baseUrl).buildUpon()
        uriBuilder.appendEncodedPath(path)

        return uriBuilder.build().toString()
    }

    fun wrapInATag(label: String, url: String): String {
        return "<a href=\"$url\">$label</a>"
    }
}
