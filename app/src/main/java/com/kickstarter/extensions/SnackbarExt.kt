package com.kickstarter.extensions

import android.content.Context
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.view.ViewGroup
import com.kickstarter.R

fun Snackbar.adjustMargins() {
    val params = this.view.layoutParams as ViewGroup.MarginLayoutParams
    val grid1 = context.resources.getDimensionPixelSize(R.dimen.grid_1)
    val grid2 = context.resources.getDimensionPixelSize(R.dimen.grid_2)
    params.setMargins(grid1, 0, grid1, grid2)
    this.view.layoutParams = params
}

fun Snackbar.error(context: Context) {
    adjustMargins()
    this.view.background = ContextCompat.getDrawable(context, R.drawable.bg_snackbar_error)
}

fun Snackbar.success(context: Context) {
    adjustMargins()
    this.view.background = ContextCompat.getDrawable(context, R.drawable.bg_snackbar_success)
}
