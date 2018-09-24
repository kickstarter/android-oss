package com.kickstarter.extensions

import android.content.Context
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.view.ViewGroup
import com.kickstarter.R

fun Snackbar.success(context: Context) {
    val params = this.view.layoutParams as ViewGroup.MarginLayoutParams
    params.setMargins(12, 12, 12, 12)
    this.view.layoutParams = params
    this.view.background = ContextCompat.getDrawable(context, R.drawable.change_email_success_bg)
    ViewCompat.setElevation(this.view, 6f)
}