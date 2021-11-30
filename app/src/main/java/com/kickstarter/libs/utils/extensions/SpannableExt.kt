@file:JvmName("SpannableExt")
package com.kickstarter.libs.utils.extensions

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.BulletSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View

fun Spannable.size(size: Int) {
    val span = AbsoluteSizeSpan(size)
    this.setSpan(span, 0, this.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
}

fun SpannableString.color() {
    val span = ForegroundColorSpan(Color.BLACK)
    this.setSpan(span, 0, this.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
}

fun SpannableString.boldStyle() {
    val span = StyleSpan(Typeface.BOLD)
    this.setSpan(span, 0, this.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
}

fun SpannableString.italicStyle() {
    val span = StyleSpan(Typeface.ITALIC)
    this.setSpan(span, 0, this.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
}

fun SpannableString.linkStyle(clickCallback: () -> Unit) {
    val span = UnderlineSpan()
    val colorSpan = ForegroundColorSpan(Color.GREEN)
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(view: View) {
            clickCallback()
        }
    }
    this.setSpan(span, 0, this.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    this.setSpan(colorSpan, 0, this.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    this.setSpan(clickableSpan, 0, this.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
}

fun SpannableString.bulletStyle() {
    val span = BulletSpan(30, Color.BLACK)
    this.setSpan(span, 0, this.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
}
