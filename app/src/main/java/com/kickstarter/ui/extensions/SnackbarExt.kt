package com.kickstarter.ui.extensions

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun snackbar(anchor: View, message: String): Snackbar {
    return Snackbar.make(anchor, message, Snackbar.LENGTH_LONG)
}