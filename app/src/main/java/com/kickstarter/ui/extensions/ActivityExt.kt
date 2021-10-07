package com.kickstarter.ui.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.kickstarter.R
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.models.Project
import com.kickstarter.ui.data.ProjectData
import android.util.Pair
import com.kickstarter.libs.utils.extensions.getRootCommentsActivityIntent
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

fun Activity.startRootCommentsActivity(projectAndData: Pair<Project, ProjectData>) {
    startActivity(
        Intent().getRootCommentsActivityIntent(this, projectAndData)
    )

    this.let {
        TransitionUtils.transition(it, TransitionUtils.slideInFromRight())
    }
}
