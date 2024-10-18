package com.kickstarter.libs.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Parcelable
import com.kickstarter.R
import com.kickstarter.ui.activities.DiscoveryActivity

object ApplicationUtils {
    fun openUrlExternally(context: Context, url: String) {
        val uri = Uri.parse(url)
        val targetIntents = targetIntents(context, uri)

        if (targetIntents.isNotEmpty()) {
            /* We need to remove the first intent so it's not duplicated when we add the
      EXTRA_INITIAL_INTENTS intents. */
            val chooserIntent = Intent.createChooser(
                targetIntents.toMutableList().removeAt(0),
                context.getString(R.string.View_project)
            )
            chooserIntent.putExtra(
                Intent.EXTRA_INITIAL_INTENTS,
                targetIntents.toTypedArray<Parcelable>()
            )
            context.startActivity(chooserIntent)
        }
    }

    /**
     *
     * Starts the main activity at the top of a task stack, clearing all previous activities.
     *
     * `ACTION_MAIN` does not expect to receive any data in the intent, it should be the same intent as if a user had
     * just launched the app.
     */
    fun startNewDiscoveryActivity(context: Context) {
        val intent = Intent(context, DiscoveryActivity::class.java)
            .setAction(Intent.ACTION_MAIN)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        context.startActivity(intent)
    }

    /**
     * Clears all activities from the task stack except discovery.
     */
    fun resumeDiscoveryActivity(context: Context) {
        val intent = Intent(context, DiscoveryActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        context.startActivity(intent)
    }

    private fun targetIntents(context: Context, uri: Uri): List<Intent> {
        val fakeUri = Uri.parse("http://www.kickstarter.com")
        val browserIntent = Intent(Intent.ACTION_VIEW, fakeUri)

        return context.packageManager.queryIntentActivities(browserIntent, 0)
            .filter { resolveInfo: ResolveInfo -> !resolveInfo.activityInfo.packageName.contains("com.kickstarter") }
            .map { resolveInfo: ResolveInfo ->
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage(resolveInfo.activityInfo.packageName)
                intent.setData(uri)
                intent
            }
            .toList()
    }
}
