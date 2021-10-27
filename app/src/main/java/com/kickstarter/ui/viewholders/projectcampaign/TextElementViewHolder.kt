package com.kickstarter.ui.viewholders.projectcampaign

import android.widget.TextView
import com.kickstarter.databinding.ViewElementTextFromHtmlBinding
import com.kickstarter.libs.htmlparser.TextViewElement
import com.kickstarter.ui.viewholders.KSViewHolder

class TextElementViewHolder(
    val binding: ViewElementTextFromHtmlBinding
) : KSViewHolder(binding.root) {
    // TODO: attach ViewModel if necessary
    private val textView: TextView = binding.textView

    fun configure(element: TextViewElement) {
        textView.text = element.attributedText
    }

    override fun bindData(data: Any?) {
        (data as? TextViewElement).apply {
            this?.let { configure(it) }
        }
    }
}
