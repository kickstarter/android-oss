package com.kickstarter.ui.extensions

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.material.snackbar.Snackbar

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
