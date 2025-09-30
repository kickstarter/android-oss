package com.kickstarter.ui.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.HttpAuthHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebViewDatabase
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewStartUpConfig
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.isDarkModeEnabled
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.extensions.text
import com.kickstarter.viewmodels.OAuthViewModel
import timber.log.Timber
import java.util.concurrent.Executors

/**
 * Will be used for OAuth when default Browser is not Chrome
 * with other browsers (Firefox, Opera, Arc, Duck Duck Go ... etc) even based in Chromium
 * the redirection was not triggered on `LoginToutActivity.onNewIntent`, and the customTabInstance was never killed.
 */
class OAuthWebViewActivity : ComponentActivity() {
    val callback: (String) -> Unit = { inputString ->
        val intent = Intent()
            .putExtra(IntentKey.OAUTH_REDIRECT_URL, inputString)
        this.setResult(Activity.RESULT_OK, intent)
        this.finish()
    }

    private lateinit var webView: WebView

    @OptIn(WebViewCompat.ExperimentalAsyncStartUp::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra(IntentKey.URL) ?: ""

        try {
            // Warm up the provider off the UI thread *before* any WebView is constructed.
            // Ideally should be done on Application launch
            val executor = Executors.newSingleThreadExecutor()
            val config = WebViewStartUpConfig.Builder(executor).build()
            WebViewCompat.startUpWebView(applicationContext, config) { result ->
                Timber.d("**** $result")
            }

            // WebViewCompat.prerenderUrlAsync(webView, url,null, executor) {}
        } catch (e: Exception) {
        }

        webView = WebView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            this.webViewClient = CustomWebViewClient(context = context, callback)
            this.settings.domStorageEnabled = true
            this.settings.allowFileAccess = true
        }

        this.getEnvironment()?.let { env ->
            setContent {
                val darModeEnabled = this.isDarkModeEnabled(env = env)
                KickstarterApp(useDarkTheme = darModeEnabled) {
                    WebViewComposable(url, webView)
                }
            }
        }
    }

    @Composable
    private fun WebViewComposable(url: String, webView: WebView) {
        AndroidView(
            factory = {
                webView
            }, update = {
                it.loadUrl(url)
            }
        )
    }
}

class CustomWebViewClient(private val context: Context, private val callback: (String) -> Unit) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        request?.url?.let {
            if (OAuthViewModel.isAfterRedirectionStep(it)) {
                callback(it.toString())
            }
        } ?: callback("")
        return false
    }

    /**
     * Only used on staging environments for basic http authentication
     */
    override fun onReceivedHttpAuthRequest(
        view: WebView?,
        handler: HttpAuthHandler?,
        host: String?,
        realm: String?
    ) {
        val webDatabase = WebViewDatabase.getInstance(context)

        val alert: AlertDialog.Builder = AlertDialog.Builder(context)

        val container = LinearLayout(context)
        container.orientation = LinearLayout.VERTICAL
        val user = EditText(context)
        user.hint = "Staging credential User:"
        val password = EditText(context)
        password.hint = "Staging credential Password:"

        container.addView(user)
        container.addView(password)

        alert.setView(container)
        alert.setPositiveButton(
            "Send",
            DialogInterface.OnClickListener { dialog, whichButton ->
                if (user.isNotNull() && password.isNotNull()) {
                    webDatabase.setHttpAuthUsernamePassword(
                        host,
                        realm,
                        user.text(),
                        password.text()
                    )
                    handler?.proceed(user.text(), password.text())
                    super.onReceivedHttpAuthRequest(view, handler, host, realm)
                }
            }
        )
        alert.show()
    }
}
