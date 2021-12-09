package com.kickstarter.ui.viewholders.projectcampaign

import android.annotation.SuppressLint
import androidx.fragment.app.FragmentActivity
import com.kickstarter.databinding.ViewElementExternalSourceFromHtmlBinding
import com.kickstarter.libs.htmlparser.ExternalSourceViewElement
import com.kickstarter.ui.adapters.projectcampaign.ViewElementAdapter
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.ui.views.WebChromeVideoFullScreenClient

class ExternalViewViewHolder(
    val binding: ViewElementExternalSourceFromHtmlBinding,
    private val fullScreenDelegate: ViewElementAdapter.FullScreenDelegate,
    val requireActivity: FragmentActivity
) : KSViewHolder(binding.root) {
    private val webView = binding.externalSourceWebView

    fun configure(element: ExternalSourceViewElement) {
        setupWebView()
        val htmlContent = "<body style=\"margin: 0; padding: 0\">${element.htmlContent}</body>"
        webView.loadData(htmlContent, "text/html", "utf-8")
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.javaScriptEnabled = true
        webView.webChromeClient = WebChromeVideoFullScreenClient(
            requireActivity,
            fullScreenDelegate, absoluteAdapterPosition
        )
    }

    override fun bindData(data: Any?) {
        (data as? ExternalSourceViewElement).apply {
            this?.let { configure(it) }
        }
    }
}
