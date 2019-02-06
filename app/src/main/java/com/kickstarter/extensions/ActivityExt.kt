package com.kickstarter.extensions

import android.app.Activity
import android.content.Intent
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.kickstarter.R

fun Activity.startActivityWithSlideUpTransition(intent: Intent) {
    this.startActivity(intent)
    this.overridePendingTransition(R.anim.settings_slide_in_from_bottom, R.anim.settings_slide_out_from_top)
}

fun Activity.startActivityWithSlideLeftTransition(intent: Intent) {
    this.startActivity(intent)
    this.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
}

fun Activity.showSnackbar(anchor: View, stringResId: Int) {
    showSnackbar(anchor, getString(stringResId))
}

fun showSnackbar(anchor: View, message: String) {
    snackbar(anchor, message).show()
}

fun snackbar(anchor: View, message: String): Snackbar {
    return Snackbar.make(anchor, message, Snackbar.LENGTH_LONG)
}
