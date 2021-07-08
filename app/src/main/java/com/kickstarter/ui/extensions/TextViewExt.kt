package com.kickstarter.ui.extensions

import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import org.jsoup.Jsoup
import java.util.Locale

fun TextView.makeLinks(vararg links: Pair<String, View.OnClickListener>, @ColorRes linkColor: Int = -1, isUnderlineText: Boolean = true) {
    val spannableString = SpannableString(this.text)
    var startIndexOfLink = -1
    for (link in links) {
        val clickableSpan = object : ClickableSpan() {
            override fun updateDrawState(textPaint: TextPaint) {
                textPaint.isUnderlineText = isUnderlineText
                if (linkColor != -1)
                    textPaint.color = ContextCompat.getColor(context, linkColor)
            }

            override fun onClick(view: View) {
                Selection.setSelection((view as TextView).text as Spannable, 0)
                view.invalidate()
                link.second.onClick(view)
            }
        }

        startIndexOfLink = this.text.toString().toLowerCase(Locale.getDefault()).indexOf(link.first.toLowerCase(Locale.getDefault()), startIndexOfLink + 1)

        if (startIndexOfLink == -1) continue // todo if you want to verify your texts contains links text

        spannableString.setSpan(
            clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    this.setText(spannableString, TextView.BufferType.SPANNABLE)
    this.movementMethod =
        LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
    this.linksClickable = true
}

/**
Parse HTML into a Document. As no base URI is specified, absolute URL detection relies on the HTML including a
{@code <base href>} tag.

@see #Jsoup.parse(String)
 */
fun TextView.parseHtmlTag() {
    this.text = Jsoup.parse(this.text.toString()).text()
}

fun TextView.stripUnderlines() {
    val s: Spannable = SpannableString(this.text)
    var spans = s.getSpans(0, s.length, URLSpan::class.java)
    for (span in spans) {
        val start = s.getSpanStart(span)
        val end = s.getSpanEnd(span)
        s.removeSpan(span)
        val newSpan = object : URLSpan(span.url) {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }
        s.setSpan(newSpan, start, end, 0)
    }
    text = s
}
