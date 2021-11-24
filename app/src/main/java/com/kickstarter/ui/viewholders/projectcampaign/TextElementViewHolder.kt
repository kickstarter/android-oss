package com.kickstarter.ui.viewholders.projectcampaign

import android.text.method.LinkMovementMethod
import android.widget.TextView
import com.kickstarter.databinding.ViewElementTextFromHtmlBinding
import com.kickstarter.libs.htmlparser.TextViewElement
import com.kickstarter.libs.utils.ApplicationUtils
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

        // TODO: use spannable strings to get the result of iterate over each component and apply the correct style
        val joinedText = element.components.joinToString { it.text }

        val link = element.components.filter { !it.link.isNullOrEmpty() }

        if (link.isNotEmpty()) {
            textView.setOnClickListener {
                ApplicationUtils.openUrlExternally(context(), link.first().link ?: "")
            }
        }

        textView.text = joinedText
    }

    override fun bindData(data: Any?) {
        (data as? TextViewElement).apply {
            this?.let { configure(it) }
        }
    }
}
