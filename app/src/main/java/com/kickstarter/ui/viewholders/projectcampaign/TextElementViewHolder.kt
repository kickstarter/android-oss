package com.kickstarter.ui.viewholders.projectcampaign

import android.text.method.LinkMovementMethod
import android.widget.TextView
import com.kickstarter.databinding.ViewElementTextFromHtmlBinding
import com.kickstarter.libs.htmlparser.TextViewElement
import com.kickstarter.libs.htmlparser.getStyledComponents
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

        textView.text = element.getStyledComponents(context = context())
    }

    override fun bindData(data: Any?) {
        (data as? TextViewElement).apply {
            this?.let { configure(it) }
        }
    }
}
