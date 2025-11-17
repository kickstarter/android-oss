package com.kickstarter.ui.viewholders.projectcampaign

import android.annotation.SuppressLint
import androidx.fragment.app.FragmentActivity
import com.kickstarter.databinding.ViewElementExternalSourceFromHtmlBinding
import com.kickstarter.libs.htmlparser.ExternalSourceViewElement
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.extensions.getEnvironment
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
        // If `null`, `loadDataWithBaseURL` will default to "about:blank". Our own URL may be preferable.
        val baseUrl = requireActivity.getEnvironment()?.webEndpoint() ?: Secrets.WebEndpoint.PRODUCTION
        webView.loadDataWithBaseURL(baseUrl, htmlContent, "text/html", "utf-8", null)
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
