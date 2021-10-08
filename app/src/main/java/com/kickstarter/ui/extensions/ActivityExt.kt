package com.kickstarter.ui.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Pair
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.kickstarter.R
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.extensions.getCreatorBioWebViewActivityIntent
import com.kickstarter.libs.utils.extensions.getCreatorDashboardActivityIntent
import com.kickstarter.libs.utils.extensions.getProjectUpdatesActivityIntent
import com.kickstarter.libs.utils.extensions.getRootCommentsActivityIntent
import com.kickstarter.libs.utils.extensions.getUpdatesActivityIntent
import com.kickstarter.models.Project
import com.kickstarter.ui.data.ProjectData
import timber.log.Timber

fun Activity.hideKeyboard() {
    val inputManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (inputManager.isAcceptingText) {
        inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        currentFocus?.clearFocus()
    }
}

fun Activity.showSnackbar(anchor: View, stringResId: Int) {
    showSnackbar(anchor, getString(stringResId))
}

fun showSnackbar(anchor: View, message: String) {
    snackbar(anchor, message).show()
}

fun Activity.showSuccessSnackBar(anchor: View, message: String) {
    val backgroundColor = this.resources.getColor(R.color.kds_create_300, this.theme)
    val textColor = this.resources.getColor(R.color.kds_support_700, this.theme)
    showSnackbarWithColor(anchor, message, backgroundColor, textColor)
}

fun Activity.showErrorSnackBar(anchor: View, message: String) {
    val backgroundColor = this.resources.getColor(R.color.kds_alert, this.theme)
    val textColor = this.resources.getColor(R.color.kds_white, this.theme)
    showSnackbarWithColor(anchor, message, backgroundColor, textColor)
}

fun Activity.showRatingDialogWidget() {
    val manager = ReviewManagerFactory.create(this)
    val requestReviewTask = manager.requestReviewFlow()

    requestReviewTask.addOnCompleteListener { request ->
        if (request.isSuccessful) {
            Timber.v("${this.localClassName } : showRatingDialogWidget request: ${request.isSuccessful} ")
            // Request succeeded and a ReviewInfo instance was received
            val reviewInfo: ReviewInfo = request.result

            // Start the review flow UI
            val flow = manager.launchReviewFlow(this, reviewInfo)

            flow.addOnSuccessListener {
                Timber.v("${this.localClassName } : showRatingDialogWidget launchReviewFlow: Success")
            }

            flow.addOnFailureListener {
                Timber.v("${this.localClassName } : showRatingDialogWidget launchReviewFlow: Failure")
            }

            flow.addOnCompleteListener {
                Timber.v("${this.localClassName } : showRatingDialogWidget launchReviewFlow: Complete")
            }
        } else {
            Timber.v("${this.localClassName } : showRatingDialogWidget request: ${request.isSuccessful} ")
        }
    }
}

/**
 * This function starts the RootCommentActivity with Transition animation included
 * @param projectAndData
 * @param commentableId -> specific for deeplinking to a concrete thread
 */
fun Activity.startRootCommentsActivity(projectAndData: Pair<Project, ProjectData>, commentableId: String? = null) {
    startActivity(
        Intent().getRootCommentsActivityIntent(this, projectAndData, commentableId)
    )

    this.let {
        TransitionUtils.transition(it, TransitionUtils.slideInFromRight())
    }
}

fun Activity.startCreatorDashboardActivity(project: Project) {
    startActivity(Intent().getCreatorDashboardActivityIntent(this, project))
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
}

fun Activity.startCreatorBioWebViewActivity(project: Project) {
    startActivity(Intent().getCreatorBioWebViewActivityIntent(this, project))
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
}

/**
 * This function starts the General Updates activity with Transition animation included
 * the Transition occurs slide_in_right -> fade_out_slide_out_left
 * @param project
 * @param updatePostId -> for deeplink to an specific post update
 * @param isUpdateComment -> for deeplink navigation into a comment for a concrete post
 * @param comment -> to open the comments activity to a specific thread
 */
fun Activity.startUpdatesActivity(
    project: Project,
    updatePostId: String? = null,
    isUpdateComment: Boolean? = null,
    comment: String? = null
) {
    startActivity(Intent().getUpdatesActivityIntent(this, project, updatePostId, isUpdateComment, comment))
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
}

/**
 * This function starts the Project Updates activity with Transition animation included
 * the Transition occurs slide_in_right -> fade_out_slide_out_left
 * @param projectAndData
 */
fun Activity.startProjectUpdatesActivity(projectAndData: Pair<Project, ProjectData>) {
    startActivity(Intent().getProjectUpdatesActivityIntent(this, projectAndData))
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
}
