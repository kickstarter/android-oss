package com.kickstarter.ui.extensions

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.kickstarter.R

fun showErrorToast(applicationContext: Context, root: ViewGroup, message: String) {
    val layout: ViewGroup = LayoutInflater.from(applicationContext).inflate(R.layout.toast_error, root, false) as ViewGroup
    val toastTextView: TextView = layout.findViewById(R.id.toast_text_view)
    toastTextView.text = message
    val resources = applicationContext.resources
    val yOffset = resources.getDimensionPixelSize(R.dimen.ks_toolbar_height) + resources.getDimensionPixelSize(R.dimen.grid_1)
    with (Toast(applicationContext)) {
        setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL or Gravity.FILL_HORIZONTAL, 0, yOffset)
        duration = Toast.LENGTH_LONG
        view = layout
        show()
    }
}
