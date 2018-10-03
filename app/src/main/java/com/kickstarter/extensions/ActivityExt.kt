package com.kickstarter.extensions

import android.app.Activity
import android.content.Intent
import android.support.design.widget.Snackbar
import android.view.View
import com.kickstarter.R

fun Activity.startActivityWithSlideUpTransition(intent: Intent) {
    this.startActivity(intent)
    this.overridePendingTransition(R.anim.settings_bottom_slide, R.anim.fade_out)
}

fun Activity.startActivityWithSlideLeftTransition(intent: Intent) {
    this.startActivity(intent)
    this.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
}

fun Activity.showErrorSnackbar(anchor: View, stringResId: Int) {
    showErrorSnackbar(anchor, getString(stringResId))
}

fun Activity.showErrorSnackbar(anchor: View, message: String) {
    Snackbar.make(anchor, message, Snackbar.LENGTH_LONG).error(this).show()
}

fun Activity.showSuccessSnackbar(anchor: View, stringResId: Int) {
    showSuccessSnackbar(anchor, getString(stringResId))
}

fun Activity.showSuccessSnackbar(anchor: View, message: String) {
    Snackbar.make(anchor, message, Snackbar.LENGTH_LONG).success(this).show()
}
