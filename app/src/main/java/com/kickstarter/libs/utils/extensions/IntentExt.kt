package com.kickstarter.libs.utils.extensions

import android.content.Context
import android.content.Intent
import android.util.Pair
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.CommentsActivity
import com.kickstarter.ui.activities.ProjectActivity
import com.kickstarter.ui.activities.ProjectPageActivity
import com.kickstarter.ui.data.ProjectData

fun Intent.getProjectIntent(context: Context, isFfEnabled: Boolean): Intent {
    return this.setClass(context, if (isFfEnabled) ProjectPageActivity::class.java else ProjectActivity::class.java)
}

/**
 * Return a Intent ready to launch the CommentsActivity with extras:
 * @param context
 * @param projectAndData
 * @param comment -> to open the comments activity to a specific thread
 */
fun Intent.getRootCommentsActivityIntent(
    context: Context,
    projectAndData: Pair<Project, ProjectData>,
    commentableId: String? = null
): Intent {
    val theIntent = this.setClass(context, CommentsActivity::class.java)
        .putExtra(IntentKey.PROJECT, projectAndData.first)
        .putExtra(IntentKey.PROJECT_DATA, projectAndData.second)

    commentableId?.let { theIntent.putExtra(IntentKey.COMMENT, it) }

    return theIntent
}
