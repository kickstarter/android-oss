package com.kickstarter.ui.viewholders.projectcampaign

import android.text.method.LinkMovementMethod
import android.widget.TextView
import com.kickstarter.R
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

        val headerSize = context().resources.getDimensionPixelSize(R.dimen.title_3)
        val bodySize = context().resources.getDimensionPixelSize(R.dimen.callout)

        textView.text = element.getStyledComponents(bodySize, headerSize, context())
    }

    override fun bindData(data: Any?) {
        (data as? TextViewElement).apply {
            this?.let { configure(it) }
        }
    }
}
