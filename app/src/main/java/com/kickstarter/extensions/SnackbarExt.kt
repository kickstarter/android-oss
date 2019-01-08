package com.kickstarter.extensions

import android.view.Gravity
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.kickstarter.R

fun Snackbar.confirmation(): Snackbar {
    style(Gravity.CENTER, R.color.white, R.color.ksr_cobalt_500)
    return this
}

fun Snackbar.error(): Snackbar {
    style( Gravity.CENTER, R.color.ksr_soft_black, R.color.ksr_apricot_500)
    return this
}

fun Snackbar.headsUp(): Snackbar {
    style(Gravity.CENTER, R.color.ksr_teal_500, R.color.ksr_soft_black)
    return this
}

fun Snackbar.networkError(): Snackbar {
    style(Gravity.CENTER, R.color.ksr_soft_black, R.color.ksr_teal_500)
    return this
}

private fun Snackbar.getTextView(): TextView {
    return view.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
}

private fun Snackbar.setBackgroundColor(backgroundColor: Int) {
    this.view.setBackgroundColor(ContextCompat.getColor(context, backgroundColor))
}

fun Snackbar.setTextGravity(gravity: Int): Snackbar {
    getTextView().gravity = gravity
    return this
}

fun Snackbar.setTextColor(colorId: Int): Snackbar {
    getTextView().setTextColor(ContextCompat.getColor(context, colorId))
    return this
}

fun Snackbar.style(gravity: Int, textColorId: Int, backgroundColorId: Int) {
    setTextGravity(gravity)
    setTextColor(textColorId)
    setBackgroundColor(backgroundColorId)
}
