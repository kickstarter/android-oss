package com.kickstarter.ui.extensions

import android.text.Editable
import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.kickstarter.R
import com.kickstarter.libs.utils.ViewUtils
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

fun TextView.urlSpanWithoutUnderlines() {
    val spannable: Spannable = SpannableString(this.text)
    spannable.getSpans(0, spannable.length, URLSpan::class.java).forEach { span ->
        val start = spannable.getSpanStart(span)
        val end = spannable.getSpanEnd(span)
        spannable.removeSpan(span)
        val newSpan = object : URLSpan(span.url) {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }
        spannable.setSpan(newSpan, start, end, 0)
    }
    text = spannable
}

fun TextView.parseAndSpanHtmlTag(text: String, clickableSpan: ClickableSpan) {
    val spannableBuilder = SpannableStringBuilder(ViewUtils.html(text))
    // https://stackoverflow.com/a/19989677
    val urlSpans = spannableBuilder.getSpans(0, text.length, URLSpan::class.java)
    for (urlSpan in urlSpans) {
        val spanStart = spannableBuilder.getSpanStart(urlSpan)
        val spanEnd = spannableBuilder.getSpanEnd(urlSpan)
        val spanFlags = spannableBuilder.getSpanFlags(urlSpan)
        spannableBuilder.setSpan(clickableSpan, spanStart, spanEnd, spanFlags)
        spannableBuilder.removeSpan(urlSpan)
    }

    this.text = spannableBuilder
    this.movementMethod = LinkMovementMethod.getInstance()
}

fun TextView.getUpdatedText() {
    this.addTextChangedListener(object : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            // TODO Auto-generated method stub
        }

        override fun beforeTextChanged(
            s: CharSequence,
            start: Int,
            count: Int,
            after: Int
        ) {
            // TODO Auto-generated method stub
        }

        override fun afterTextChanged(s: Editable) {
            // TODO Auto-generated method stub
        }
    })
}

fun TextView.setClickableHtml(callback: (String) -> Unit = {}) {
    val spannableBuilder = SpannableStringBuilder(ViewUtils.html(text.toString()))
    // https://stackoverflow.com/a/19989677
    val urlSpans = spannableBuilder.getSpans(0, text.length, URLSpan::class.java)
    for (urlSpan in urlSpans) {
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                callback(urlSpan.url)
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color = ContextCompat.getColor(context, R.color.accent)
            }
        }
        val spanStart = spannableBuilder.getSpanStart(urlSpan)
        val spanEnd = spannableBuilder.getSpanEnd(urlSpan)
        val spanFlags = spannableBuilder.getSpanFlags(urlSpan)
        spannableBuilder.setSpan(clickableSpan, spanStart, spanEnd, spanFlags)
        spannableBuilder.removeSpan(urlSpan)
    }

    text = spannableBuilder
    movementMethod = LinkMovementMethod.getInstance()
}
