package com.kickstarter.ui.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.lifecycleScope
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.models.chrome.ChromeTabsHelperActivity
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.viewmodels.OAuthViewModel
import com.kickstarter.viewmodels.OAuthViewModelFactory
import kotlinx.coroutines.launch
import timber.log.Timber

class OAuthActivity : AppCompatActivity() {

    private lateinit var helper: ChromeTabsHelperActivity.CustomTabSessionAndClientHelper
    private lateinit var viewModelFactory: OAuthViewModelFactory
    private val viewModel: OAuthViewModel by viewModels {
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpConnectivityStatusCheck(lifecycle)

        Timber.d("OAuthActivity: onCreate Intent: $intent, onCreate data: ${intent.data}")

        this.getEnvironment()?.let { env ->
            viewModelFactory = OAuthViewModelFactory(environment = env)
        }

        viewModel.produceState(intent = intent)

        lifecycleScope.launch {

            viewModel.uiState.collect { state ->
                // - Intent generated with onCreate
                if (state.isAuthorizationStep && state.authorizationUrl.isNotEmpty()) {
                    openChromeTabWithUrl(state.authorizationUrl)
                }

                if (state.isTokenRetrieveStep && state.code.isNotEmpty()) {
                    // TODO WIP PHASE 3, call the endpoint with code & code_challenge, retrieve the token, for now it kills the current activity
                    Timber.d("OAuthActivity Redirect took place: $this")
                    finishWithAnimation()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Timber.d("OAuthActivity: onNewIntent Intent: $intent, data: ${intent?.data}")
        // - Intent generated when the deepLink redirection takes place
        intent?.let { viewModel.produceState(intent = it) }
    }

    private fun openChromeTabWithUrl(url: String) {
        val authorizationUri = Uri.parse(url)

        // BindCustomTabsService, obtain CustomTabsClient and Client, listens to navigation events
        helper = ChromeTabsHelperActivity.CustomTabSessionAndClientHelper(this, authorizationUri) {
            finish()
        }

        // - Fallback in case Chrome is not installed, open WebViewActivity
        val fallback = object : ChromeTabsHelperActivity.CustomTabFallback {
            override fun openUri(activity: Activity, uri: Uri) {
                val intent: Intent = Intent(activity, WebViewActivity::class.java)
                    .putExtra(IntentKey.URL, uri.toString())

                activity.startActivity(intent)
                TransitionUtils.slideInFromRight()
            }
        }

        lifecycleScope.launch {
            // - Once the session is ready and client warmed-up load the url
            helper.isSessionReady().collect { ready ->
                val tabIntent = CustomTabsIntent.Builder(helper.getSession()).build()
                ChromeTabsHelperActivity.openCustomTab(
                    this@OAuthActivity,
                    tabIntent,
                    authorizationUri,
                    fallback
                )
            }
        }
    }
}
