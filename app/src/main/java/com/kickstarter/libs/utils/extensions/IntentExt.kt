package com.kickstarter.libs.utils.extensions

import android.content.Context
import android.content.Intent
import android.util.Pair
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.CommentsActivity
import com.kickstarter.ui.activities.CreatorBioActivity
import com.kickstarter.ui.activities.CreatorDashboardActivity
import com.kickstarter.ui.activities.ProjectActivity
import com.kickstarter.ui.activities.ProjectPageActivity
import com.kickstarter.ui.activities.ProjectUpdatesActivity
import com.kickstarter.ui.activities.UpdateActivity
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

/**
 * Return a Intent ready to launch the Creator Dashboard with extras:
 * @param context
 * @param projectAndData
 */
fun Intent.getCreatorDashboardActivityIntent(context: Context, project: Project): Intent {
    return this.setClass(context, CreatorDashboardActivity::class.java)
        .putExtra(IntentKey.PROJECT, project)
}

/**
 * Return a Intent ready to launch the creator Bio activity
 * @param context
 * @param project
 */
fun Intent.getCreatorBioWebViewActivityIntent(context: Context, project: Project): Intent {
    return this.setClass(context, CreatorBioActivity::class.java)
        .putExtra(IntentKey.PROJECT, project)
        .putExtra(IntentKey.URL, project.creatorBioUrl())
}

/**
 * Return a Intent ready to launch the ProjectUpdates activity
 * @param context
 * @param projectAndData
 * @param comment -> to open the comments activity to a specific thread
 */
fun Intent.getProjectUpdatesActivityIntent(context: Context, projectAndData: Pair<Project, ProjectData>): Intent {
    return this.setClass(context, ProjectUpdatesActivity::class.java)
        .putExtra(IntentKey.PROJECT, projectAndData.first)
        .putExtra(IntentKey.PROJECT_DATA, projectAndData.second)
}

/**
 * Return a Intent ready to launch the UpdatesActivity with extras:
 * @param context
 * @param project
 * @param updatePostId -> for deeplink to an specific post update
 * @param isUpdateComment -> for deeplink navigation into a comment for a concrete post
 * @param comment -> to open the comments activity to a specific thread
 */
fun Intent.getUpdatesActivityIntent(
    context: Context,
    project: Project,
    updatePostId: String? = null,
    isUpdateComment: Boolean? = null,
    comment: String? = null
): Intent {

    val theIntent = this.setClass(context, UpdateActivity::class.java)
        .putExtra(IntentKey.PROJECT, project)

    updatePostId?.let {
        theIntent.putExtra(IntentKey.UPDATE_POST_ID, it)
    }

    isUpdateComment?.let {
        theIntent.putExtra(IntentKey.IS_UPDATE_COMMENT, it)
    }

    comment?.let {
        theIntent.putExtra(IntentKey.COMMENT, it)
    }

    return theIntent
}
