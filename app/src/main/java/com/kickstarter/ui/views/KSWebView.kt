package com.kickstarter.ui.views

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView.setWebContentsDebuggingEnabled
import android.widget.FrameLayout
import com.kickstarter.KSApplication
import com.kickstarter.R
import com.kickstarter.libs.WebViewJavascriptInterface
import com.kickstarter.services.KSWebViewClient
import kotlinx.android.synthetic.main.web_view.view.*
import javax.inject.Inject


class KSWebView : FrameLayout, KSWebViewClient.Delegate {

    @Inject
    lateinit var client: KSWebViewClient

    var url: String? = ""

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.web_view, this, true)

        if (!isInEditMode) {

            (context.applicationContext as KSApplication).component().inject(this)
            internal_web_view.webViewClient = this.client
            internal_web_view.webChromeClient = WebChromeClient()
            internal_web_view.settings.javaScriptEnabled = true
            internal_web_view.settings.allowFileAccess = false
            this.client.setDelegate(this)

            setWebContentsDebuggingEnabled(true)

            internal_web_view.addJavascriptInterface(WebViewJavascriptInterface(this.client), "WebViewJavascriptInterface")

            retry.setOnClickListener {
                internal_web_view.goBack()
                error.visibility = View.GONE
            }
        }


    }

    override fun onSaveInstanceState(): Parcelable? {
        return super.onSaveInstanceState()
    }

    override fun error(webViewClient: KSWebViewClient, url: String?) {
        this.url = internal_web_view.url
        progress.visibility = View.VISIBLE
        error.visibility = View.VISIBLE
        internal_web_view.stopLoading()
        internal_web_view.loadUrl("about:blank")
    }

    override fun webViewOnPageStarted(webViewClient: KSWebViewClient, url: String?) {
        progress.visibility = View.VISIBLE
    }

    override fun webViewOnPageFinished(webViewClient: KSWebViewClient, url: String?) {
        progress.visibility = View.GONE
    }

    override fun webViewPageIntercepted(webViewClient: KSWebViewClient, url: String) {
    }

    override fun webViewExternalLinkActivated(webViewClient: KSWebViewClient, url: String) {
    }


    fun loadUrl(url:String) {
        internal_web_view.loadUrl(url)
    }

    fun client(): KSWebViewClient {
        return this.client
    }

    fun evaluateJavascript(javascript: String, resultCallback: ValueCallback<String>) {
        internal_web_view.evaluateJavascript(javascript, resultCallback)
    }

    fun canGoBack(): Boolean {
        return internal_web_view.canGoBack()
    }

    fun goBack() {
        internal_web_view.goBack()
    }
}
