package com.kickstarter.extensions

import android.content.Context
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.view.ViewGroup
import com.kickstarter.R

fun Snackbar.success(context: Context) {
    val params = this.view.layoutParams as ViewGroup.MarginLayoutParams
    val grid1 = context.resources.getDimensionPixelSize(R.dimen.grid_1)
    val grid2 = context.resources.getDimensionPixelSize(R.dimen.grid_2)
    params.setMargins(grid1, 0, grid1, grid2)
    this.view.layoutParams = params
    this.view.background = ContextCompat.getDrawable(context, R.drawable.change_email_success_bg)
}