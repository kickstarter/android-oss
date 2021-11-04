package com.kickstarter.ui.viewholders.projectcampaign

import android.annotation.SuppressLint
import com.kickstarter.databinding.ViewElementExternalSourceFromHtmlBinding
import com.kickstarter.libs.htmlparser.ExternalSourceViewElement
import com.kickstarter.ui.viewholders.KSViewHolder

class ExternalViewViewHolder(
    val binding: ViewElementExternalSourceFromHtmlBinding
) : KSViewHolder(binding.root) {
    private val webView = binding.externalSourceWebView

    fun configure(element: ExternalSourceViewElement) {
        setupWebView()
        webView.loadData(element.htmlContent, "text/html", "utf-8")
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.javaScriptEnabled = true
    }

    override fun bindData(data: Any?) {
        (data as? ExternalSourceViewElement).apply {
            this?.let { configure(it) }
        }
    }
}
