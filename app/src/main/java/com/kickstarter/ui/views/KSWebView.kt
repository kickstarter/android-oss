package com.kickstarter.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView.setWebContentsDebuggingEnabled
import android.widget.FrameLayout
import com.kickstarter.KSApplication
import com.kickstarter.databinding.WebViewBinding
import com.kickstarter.libs.Build
import com.kickstarter.libs.WebViewJavascriptInterface
import com.kickstarter.services.KSWebViewClient
import com.kickstarter.services.RequestHandler
import javax.inject.Inject

private const val LOGTAG = "KSWebView"
class KSWebView@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), KSWebViewClient.Delegate {

    private var binding = WebViewBinding.inflate(LayoutInflater.from(context), this, true)
    @Inject
    lateinit var client: KSWebViewClient

    @Inject
    lateinit var build: Build

    private var delegate: Delegate? = null

    interface Delegate {
        fun externalLinkActivated(url: String)
        fun pageIntercepted(url: String)
        fun onReceivedError(url: String)
    }

    init {
        initWebView(context)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(context: Context) {

        if (!isInEditMode) {
            (context.applicationContext as KSApplication).component().inject(this)
            binding.internalWebView.webViewClient = this.client
            binding.internalWebView.webChromeClient = WebChromeClient()
            binding.internalWebView.settings.javaScriptEnabled = true
            binding.internalWebView.settings.allowFileAccess = false
            this.client.setDelegate(this)

            if (Build.isInternal() || build.isDebug) {
                setWebContentsDebuggingEnabled(true)
            }

            binding.internalWebView.addJavascriptInterface(WebViewJavascriptInterface(this.client), "WebViewJavascriptInterface")

            binding.webViewError.root.setOnClickListener {
                binding.internalWebView.goBack()
                binding.webViewError.root.visibility = View.GONE
            }
        }
    }

    override fun externalLinkActivated(url: String) {
        this.delegate?.externalLinkActivated(url)
    }

    override fun onPageFinished(url: String?) {
        setVisibilityIfNecessary(binding.webViewProgress.root, View.GONE)
    }

    override fun onPageStarted(url: String?) {
        binding.webViewProgress.root.visibility = View.VISIBLE
    }

    override fun pageIntercepted(url: String) {
        this.delegate?.pageIntercepted(url)
    }

    override fun onReceivedError(url: String) {
        this.delegate?.onReceivedError(url)
        setVisibilityIfNecessary(binding.webViewProgress.root, View.VISIBLE)
        binding.webViewError.root.visibility = View.VISIBLE
        binding.internalWebView.stopLoading()
        binding.internalWebView.loadUrl("about:blank")
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        this.client.setDelegate(null)
    }

    fun canGoBack(): Boolean {
        return binding.internalWebView.canGoBack()
    }

    fun evaluateJavascript(javascript: String?, resultCallback: ValueCallback<String>?) {
        javascript?.let { binding.internalWebView.evaluateJavascript(it, resultCallback) }
    }

    fun goBack() {
        binding.internalWebView.goBack()
    }

    fun loadUrl(url: String?) {
        url?.let {
            binding.internalWebView.loadUrl(it)
        }
    }

    fun registerRequestHandlers(requestHandlers: List<RequestHandler>) {
        this.client.registerRequestHandlers(requestHandlers)
    }

    fun setDelegate(delegate: Delegate?) {
        this.delegate = delegate
    }

    private fun setVisibilityIfNecessary(view: View, visibility: Int) {
        if (view.visibility != visibility) {
            view.visibility = visibility
        }
    }
}
