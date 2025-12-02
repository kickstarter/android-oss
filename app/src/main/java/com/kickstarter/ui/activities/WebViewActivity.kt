package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import com.kickstarter.databinding.WebViewLayoutBinding
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.models.User
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.ui.views.KSWebView
import com.kickstarter.utils.WindowInsetsUtil
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class WebViewActivity : ComponentActivity() {
    private lateinit var binding: WebViewLayoutBinding
    private lateinit var environment: Environment
    private lateinit var currentUser: Observable<KsOptional<User>>
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.getEnvironment()?.let { env ->
            environment = env
        }
        currentUser = requireNotNull(environment.currentUserV2()).observable()

        binding = WebViewLayoutBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root
        )
        setContentView(binding.root)

        val toolbarTitle = intent.getStringExtra(IntentKey.TOOLBAR_TITLE)
        toolbarTitle?.let { binding.webViewToolbar.webViewToolbar.setTitle(it) }

        binding.webView.setDelegate(object : KSWebView.Delegate {
            override fun externalLinkActivated(url: String) {
            }

            override fun pageIntercepted(url: String) {
                if (url.contains("authenticate")) {
                    finish()
                }
            }

            override fun onReceivedError(url: String) {
            }
        })

        onBackPressedDispatcher.addCallback {
            if (binding.webView.canGoBack()) {
                binding.webView.goBack()
            } else {
                finishWithAnimation()
            }
        }
        observeLoginState()
    }


    // Check if the user is logged in.
    // If no, start the LoginToutActivity. Wait for its result within the same backstack.
    // Once LoginToutActivity is finished successfully WebViewActivity can proceed to load the url to the webview.
    private fun observeLoginState() {
        currentUser
            .subscribe {
                when (it.getValue()) {
                    null -> startLoginToutActivity()
                    else -> loadUrl()
                }
            }.addToDisposable(disposables)
    }

    private fun loadUrl() {
        val url = intent.getStringExtra(IntentKey.URL)
        url?.let { binding.webView.loadUrl(it) }
    }

    private fun startLoginToutActivity() {
        val intent = Intent(this, LoginToutActivity::class.java)
            .putExtra(IntentKey.LOGIN_REASON, LoginReason.STAR_PROJECT)
        startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
    }
}
