package com.kickstarter.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView.setWebContentsDebuggingEnabled
import android.widget.FrameLayout
import com.kickstarter.KSApplication
import com.kickstarter.R
import com.kickstarter.libs.Build
import com.kickstarter.libs.WebViewJavascriptInterface
import com.kickstarter.libs.perimeterx.PerimeterXClientType
import com.kickstarter.services.KSWebViewClient
import com.kickstarter.services.RequestHandler
import kotlinx.android.synthetic.main.web_view.view.*
import timber.log.Timber
import javax.inject.Inject

private const val LOGTAG = "KSWebView"
class KSWebView : FrameLayout, KSWebViewClient.Delegate {

    @Inject
    lateinit var client: KSWebViewClient

    @Inject
    lateinit var build: Build

    @Inject
    lateinit var perimeterX: PerimeterXClientType

    private var delegate: Delegate? = null

    interface Delegate {
        fun externalLinkActivated(url: String)
        fun pageIntercepted(url: String)
        fun onReceivedError(url: String)
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.web_view, this, true)

        if (!isInEditMode) {
            (context.applicationContext as KSApplication).component().inject(this)
            internal_web_view.webViewClient = this.client
            internal_web_view.webChromeClient = WebChromeClient()
            internal_web_view.settings.javaScriptEnabled = true
            internal_web_view.settings.allowFileAccess = false
            this.client.setDelegate(this)

            if (Build.isInternal() || build.isDebug) {
                setWebContentsDebuggingEnabled(true)
            }

            internal_web_view.addJavascriptInterface(WebViewJavascriptInterface(this.client), "WebViewJavascriptInterface")

            web_view_error.setOnClickListener {
                internal_web_view.goBack()
                web_view_error.visibility = View.GONE
            }
        }

    }


    /**
     * Added `_pxmvid` cookie to each loaded url, with 1 hour expiration time
     * @see {@link https://docs.perimeterx.com/pxconsole/docs/android-sdk-integration-guide#section-web-view-integration}
     */
    private fun setPerimeterXCookie(url: String?) {
        val cookieManager: CookieManager = CookieManager.getInstance()
        cookieManager.removeSessionCookies { }

        val cookie = this.perimeterX.getCookieForWebView()
        cookieManager.setCookie(url, cookie)
        if (build.isDebug) Timber.d("$LOGTAG: for url:$url cookie:${cookieManager.getCookie(url)} ")
    }

    override fun externalLinkActivated(url: String) {
        this.delegate?.externalLinkActivated(url)
    }

    override fun onPageFinished(url: String?) {
        setVisibilityIfNecessary(web_view_progress, View.GONE)
    }

    override fun onPageStarted(url: String?) {
        web_view_progress.visibility = View.VISIBLE
    }

    override fun pageIntercepted(url: String) {
        this.delegate?.pageIntercepted(url)
    }

    override fun onReceivedError(url: String) {
        this.delegate?.onReceivedError(url)
        setVisibilityIfNecessary(web_view_progress, View.VISIBLE)
        web_view_error.visibility = View.VISIBLE
        internal_web_view.stopLoading()
        internal_web_view.loadUrl("about:blank")
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        this.client.setDelegate(null)
    }

    fun canGoBack(): Boolean {
        return internal_web_view.canGoBack()
    }

    fun evaluateJavascript(javascript: String?, resultCallback: ValueCallback<String>?) {
        internal_web_view.evaluateJavascript(javascript, resultCallback)
    }

    fun goBack() {
        internal_web_view.goBack()
    }

    fun loadUrl(url: String?) {
        url?.let {
            internal_web_view.loadUrl(it)
            setPerimeterXCookie(it)
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
