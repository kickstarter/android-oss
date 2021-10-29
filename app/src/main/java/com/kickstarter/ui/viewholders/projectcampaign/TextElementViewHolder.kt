package com.kickstarter.ui.viewholders.projectcampaign

import android.os.Build
import android.text.Html
import android.text.Html.FROM_HTML_MODE_COMPACT
import android.widget.TextView
import com.kickstarter.databinding.ViewElementTextFromHtmlBinding
import com.kickstarter.libs.htmlparser.TextViewElement
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.ui.viewholders.KSViewHolder

class TextElementViewHolder(
    val binding: ViewElementTextFromHtmlBinding
) : KSViewHolder(binding.root) {
    // TODO: attach ViewModel if necessary
    private val textView: TextView = binding.textView

    fun configure(element: TextViewElement) {

        // - TODO: Create a UI Reusable-Component that receives a TextElement
        val text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(element.attributedText, FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(element.attributedText)
        }

        // - TODO: distinct will avoid duplicated, but is a workaround should be fixed on the parser
        val link = element.components.distinct()
            .filter { !it.link.isNullOrEmpty() }

        if (link.isNotEmpty()) {
            textView.setOnClickListener {
                ApplicationUtils.openUrlExternally(context(), link.first().link ?: "")
            }
        }

        textView.text = text
    }

    override fun bindData(data: Any?) {
        (data as? TextViewElement).apply {
            this?.let { configure(it) }
        }
    }
}
