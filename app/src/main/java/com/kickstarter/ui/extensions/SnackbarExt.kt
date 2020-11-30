package com.kickstarter.ui.extensions

import android.view.View
import androidx.annotation.ColorInt
import com.google.android.material.snackbar.Snackbar

fun snackbar(anchor: View, message: String): Snackbar {
    return Snackbar.make(anchor, message, Snackbar.LENGTH_LONG)
}

/**
 * Create snackbar with given anchor view, message and color
 * @param anchor
 * @param message
 * @param backGroundColor
 * @param textColor
 *
 * @return snackbar
 */
fun showSnackbarWithColor(anchor: View, message: String, @ColorInt backGroundColor: Int, @ColorInt textColor: Int) =
        snackbar(anchor, message).apply {
            setBackgroundTint(backGroundColor)
            setTextColor(textColor)
        }.show()