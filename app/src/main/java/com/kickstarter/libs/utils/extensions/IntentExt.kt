package com.kickstarter.libs.utils.extensions

import android.content.Context
import android.content.Intent
import android.util.Pair
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.*
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.data.ProjectData

fun Intent.getProjectIntent(context: Context): Intent {
    return this.setClass(context, ProjectPageActivity::class.java)
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
    this.setClass(context, CommentsActivity::class.java)
        .putExtra(IntentKey.PROJECT, projectAndData.first)
        .putExtra(IntentKey.PROJECT_DATA, projectAndData.second)

    commentableId?.let { this.putExtra(IntentKey.COMMENT, it) }

    return this
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

fun Intent.getCampaignDetailsActivityIntent(context: Context, projectData: ProjectData): Intent {
    return this.setClass(context, CampaignDetailsActivity::class.java)
        .putExtra(IntentKey.PROJECT_DATA, projectData)
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

    this.setClass(context, UpdateActivity::class.java)
        .putExtra(IntentKey.PROJECT, project)

    updatePostId?.let {
        this.putExtra(IntentKey.UPDATE_POST_ID, it)
    }

    isUpdateComment?.let {
        this.putExtra(IntentKey.IS_UPDATE_COMMENT, it)
    }

    comment?.let {
        this.putExtra(IntentKey.COMMENT, it)
    }

    return this
}

/**
 * Return a Intent ready to launch the video activity
 * @param context
 * @param videoSource
 * @param videoSeekPosition
 */
fun Intent.getVideoActivityIntent(
    context: Context,
    videoSource: String,
    videoSeekPosition: Long
): Intent {
    return this.setClass(context, VideoActivity::class.java)
        .putExtra(IntentKey.VIDEO_URL_SOURCE, videoSource)
        .putExtra(IntentKey.VIDEO_SEEK_POSITION, videoSeekPosition)
}

/**
 * Return a Intent ready to launch the ResetPasswordIntent with extras:
 * @param context
 * @param isResetPasswordFacebook
 * @param email ->  email for reset account
 */
fun Intent.getResetPasswordIntent(
    context: Context,
    isResetPasswordFacebook: Boolean = false,
    email: String? = null
): Intent {
    return this.setClass(context, ResetPasswordActivity::class.java).apply {
        this.putExtra(IntentKey.RESET_PASSWORD_FACEBOOK_LOGIN, isResetPasswordFacebook)
        // ForgetPassword
        email?.let {
            this.putExtra(IntentKey.EMAIL, it)
        }
    }
}

/**
 * Return a Intent ready to launch the LoginActivity with extras:
 * @param context
 * @param isResetPasswordFacebook
 * @param email ->  email for reset account
 */
fun Intent.getLoginActivityIntent(
    context: Context,
    email: String? = null,
    loginReason: LoginReason? = null
): Intent {
    return this.setClass(context, LoginActivity::class.java).apply {
        loginReason?.let {
            this.putExtra(IntentKey.LOGIN_REASON, it)
        }
        email?.let {
            this.putExtra(IntentKey.EMAIL, it)
        }
    }
}
