package com.kickstarter.ui.viewholders.projectcampaign

import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.widget.TextView
import com.kickstarter.R
import com.kickstarter.databinding.ViewElementTextFromHtmlBinding
import com.kickstarter.libs.htmlparser.TextComponent
import com.kickstarter.libs.htmlparser.TextViewElement
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.extensions.boldStyle
import com.kickstarter.libs.utils.extensions.bulletStyle
import com.kickstarter.libs.utils.extensions.color
import com.kickstarter.libs.utils.extensions.italicStyle
import com.kickstarter.libs.utils.extensions.linkStyle
import com.kickstarter.libs.utils.extensions.size
import com.kickstarter.ui.viewholders.KSViewHolder

class TextElementViewHolder(
    val binding: ViewElementTextFromHtmlBinding
) : KSViewHolder(binding.root) {
    private val textView: TextView = binding.textView

    fun configure(element: TextViewElement) {
        // - Allow clickable spans
        textView.linksClickable = true
        textView.isClickable = true
        textView.movementMethod = LinkMovementMethod.getInstance()

        val headerSize = context().resources.getDimensionPixelSize(R.dimen.title_3)
        val bodySize = context().resources.getDimensionPixelSize(R.dimen.callout)

        val joinedSpanned = SpannableStringBuilder("")
        element.components.forEach { textItem ->
            val componentText = textItem.text
            val href = textItem.link ?: ""

            val spannable = SpannableString(componentText)
            spannable.color()
            spannable.size(bodySize)
            textItem.styles.forEach { style ->
                when (style) {
                    TextComponent.TextStyleType.BOLD -> spannable.boldStyle()
                    TextComponent.TextStyleType.EMPHASIS -> spannable.italicStyle()
                    TextComponent.TextStyleType.LINK -> spannable.linkStyle { ApplicationUtils.openUrlExternally(context(), href) }
                    TextComponent.TextStyleType.LIST -> spannable.bulletStyle()
                    TextComponent.TextStyleType.HEADER -> {
                        spannable.size(headerSize)
                        spannable.boldStyle()
                    }
                }
            }

            joinedSpanned.append(spannable)
        }

        textView.text = joinedSpanned
    }

    override fun bindData(data: Any?) {
        (data as? TextViewElement).apply {
            this?.let { configure(it) }
        }
    }
}
