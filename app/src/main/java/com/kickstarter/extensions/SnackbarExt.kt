package com.kickstarter.extensions

import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import com.kickstarter.R

fun Snackbar.confirmation(): Snackbar {
    style(true, Gravity.CENTER, R.color.white, R.drawable.bg_snackbar_confirmation)
    return this
}

fun Snackbar.error(): Snackbar {
    style(true, Gravity.CENTER, R.color.ksr_soft_black, R.drawable.bg_snackbar_error)
    return this
}

fun Snackbar.headsUp(): Snackbar {
    style(true, Gravity.CENTER, R.color.ksr_teal_500, R.drawable.bg_snackbar_heads_up)
    return this
}

fun Snackbar.networkError(): Snackbar {
    style(true, Gravity.CENTER, R.color.ksr_teal_500, R.drawable.bg_snackbar_heads_up)
    return this
}

fun Snackbar.adjustMargins() {
    val params = this.view.layoutParams as ViewGroup.MarginLayoutParams
    val grid1 = context.resources.getDimensionPixelSize(R.dimen.grid_1)
    val grid2 = context.resources.getDimensionPixelSize(R.dimen.grid_2)
    params.setMargins(grid1, 0, grid1, grid2)
    this.view.layoutParams = params
}

private fun Snackbar.getTextView(): TextView {
    return view.findViewById(android.support.design.R.id.snackbar_text) as TextView
}

private fun Snackbar.setBackground(backgroundId: Int) {
    this.view.background = ContextCompat.getDrawable(context, backgroundId)
}

fun Snackbar.setTextGravity(gravity: Int): Snackbar {
    getTextView().gravity = gravity
    return this
}

fun Snackbar.setTextColor(colorId: Int): Snackbar {
    getTextView().setTextColor(ContextCompat.getColor(context, colorId))
    return this
}

fun Snackbar.style(adjustMargins: Boolean, gravity: Int, textColorId: Int, backgroundId: Int) {
    when {
        adjustMargins -> adjustMargins()
    }
    setTextGravity(gravity)
    setTextColor(textColorId)
    setBackground(backgroundId)
}
